package com.github.xadkile.bicp.message.api.msg.sender.composite

import com.github.michaelbull.result.*
import com.github.xadkile.bicp.message.api.connection.kernel_context.KernelContextReadOnlyConv
import com.github.xadkile.bicp.message.api.connection.kernel_context.KernelIsDownException
import com.github.xadkile.bicp.message.api.exception.UnknownException
import com.github.xadkile.bicp.message.api.msg.listener.IOPubListener
import com.github.xadkile.bicp.message.api.msg.listener.MsgHandlers
import com.github.xadkile.bicp.message.api.msg.protocol.message.JPMessage
import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgStatus
import com.github.xadkile.bicp.message.api.msg.protocol.message.data_interface_definition.IOPub
import com.github.xadkile.bicp.message.api.msg.sender.MsgSender
import com.github.xadkile.bicp.message.api.msg.sender.exception.UnableToSendMsgException
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteReply
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteRequest
import com.github.xadkile.bicp.message.api.other.Sleeper
import kotlinx.coroutines.*


typealias ExecuteResult = JPMessage<IOPub.ExecuteResult.MetaData, IOPub.ExecuteResult.Content>

/**
 * If the kernel die midway, this would wait forever
 * TODO This is essentially a state machine, think of a way to structure it better. The current structure is messy.
 * TODO make it clearer what exception is returned and when they are returned
 * The state machine revolves around:
 *  - the status of the kernel: idle, busy
 *  - the status of heart beat channel: on or off
 *  - the status of return value: null or not null
 *  - the status of the listener: running or not
 *  anything else?
 */
class CodeExecutionSender(
    val kernelContext: KernelContextReadOnlyConv,
    val executeSender: MsgSender<ExecuteRequest, Result<ExecuteReply, Exception>>,
    val ioPubListener: IOPubListener,
) : MsgSender<ExecuteRequest, Result<ExecuteResult, Exception>> {

    override suspend fun send(
        message: ExecuteRequest,
        dispatcher: CoroutineDispatcher,
    ): Result<ExecuteResult, Exception> {

        if (kernelContext.isNotRunning()) {
            return Err(KernelIsDownException.occurAt(this))
        }

        var rt: Result<ExecuteResult, Exception>? = null
        var state = SendingState.Start

        // p: config listener
        ioPubListener.addHandler(
            MsgHandlers.withUUID(IOPub.ExecuteResult.msgType) { m, l ->
                val receivedMsg: ExecuteResult = m.toModel()
                if (receivedMsg.parentHeader == message.header) {
                    state = state.sideEffect({
                        rt = Ok(receivedMsg)
                        l.stop()
                    }, rt, kernelContext, ioPubListener)
                }
            }
        )

        ioPubListener.addHandler(
            // ph: catch "idle" and "busy" status message
            MsgHandlers.withUUID(IOPub.Status.msgType) { m, l ->
                val msg: JPMessage<IOPub.Status.MetaData, IOPub.Status.Content> = m.toModel()
                if (msg.parentHeader == message.header) {
                    if (msg.content.executionState == IOPub.Status.ExecutionState.idle) {
                        println("Reach idle state-> stop")
                        // TODO consider keeping or not keeping this marker handler. Does it solve any problem?
                        l.stop()
                    } else if (msg.content.executionState == IOPub.Status.ExecutionState.busy) {
                        println("Reach busy -> start computing")
                        // TODO consider keeping or not keeping this marker handler. Does it solve any problem?
                    }
                }
            }
        )

        // ph: sending the computing request
        coroutineScope {
            // rmd: start the iopub listener on a separated coroutine.
            val startRs = ioPubListener.start(this, dispatcher)

            // ph: only send the message if the ioPubListener was started successfully
            if (startRs is Ok) {
                // rmd: wait until ioPubListener to go online
                Sleeper.waitUntil { ioPubListener.isRunning() }
                launch(dispatcher) {
                    val sendStatus = executeSender.send(message, dispatcher)
                    if (sendStatus is Ok) {
                        val msgIsOk: Boolean = sendStatus.get()!!.content.status == MsgStatus.ok
                        if (msgIsOk.not()) {
                            state = state.sideEffect({
                                rt = Err(UnableToSendMsgException(message))
                                ioPubListener.stop()
                            }, rt, kernelContext, ioPubListener)
                        }
                    } else {
                        state = state.sideEffect({
                            rt = Err(sendStatus.unwrapError())
                            ioPubListener.stop()
                        }, rt, kernelContext, ioPubListener)
                    }
                }
            } else {
                state = state.sideEffect({
                    rt = Err(startRs.unwrapError())
                    ioPubListener.stop()
                }, rt, kernelContext, ioPubListener)
            }
        }

        // ph: this ensure that this sender will wait until state reach terminal states: Done, KernelDown,ListenerDown
        while (true) {
            when(state){
                SendingState.Done,/*SendingState.KernelDown,SendingState.ListenerDown*/ -> break
                else -> {}
            }
            state = state.transit(rt, kernelContext, ioPubListener)
        }
        state = state.sideEffect({ ioPubListener.stop() }, rt, kernelContext, ioPubListener)

//        when(state){
//            SendingState.Done -> return rt!!
//            SendingState.KernelDown -> return  Err(KernelIsDownException.occurAt(this))
//            SendingState.ListenerDown -> return Err(UnknownException.occurAt(this))
//            else -> return Err(UnknownException.occurAt(this))
//        }
        if (state == SendingState.Done) {
            return rt!!
        } else {
            if (kernelContext.isNotRunning()) {
                return Err(KernelIsDownException.occurAt(this))
            } else {
                return Err(UnknownException.occurAt(this))
            }
        }
    }

    /**
     * state of the sending action
     */
    internal enum class SendingState {
        Start {
            override fun transit(
                rtHasResult: Boolean,
                kernelIsRunning: Boolean,
                listenerIsRunning: Boolean,
            ): SendingState {
                return Working.transit(rtHasResult, kernelIsRunning, listenerIsRunning)
            }
        },
//        KernelDown {
//            override fun transit(
//                rtHasResult: Boolean,
//                kernelIsRunning: Boolean,
//                listenerIsRunning: Boolean,
//            ): SendingState {
//                return this
//            }
//        },
//        ListenerDown {
//            override fun transit(
//                rtHasResult: Boolean,
//                kernelIsRunning: Boolean,
//                listenerIsRunning: Boolean,
//            ): SendingState {
//                return this
//            }
//        },
        Working {
            override fun transit(
                rtHasResult: Boolean,
                kernelIsRunning: Boolean,
                listenerIsRunning: Boolean,
            ): SendingState {

                if (rtHasResult) {
                    return Done
                }/*else{
                    if (kernelIsRunning == false) {
                        return KernelDown
                    }
                    if (listenerIsRunning == false) {
                        return ListenerDown
                    }
                }*/
                if (kernelIsRunning == false) {
                    return Done
                }
                if (listenerIsRunning == false) {
                    return Done
                }
                return this
            }
        },
        Done {
            override fun transit(
                rtHasResult: Boolean,
                kernelIsRunning: Boolean,
                listenerIsRunning: Boolean,
            ): SendingState {
                return this
            }
        }

        ;

        /**
         * transit state with direct value
         */
        abstract fun transit(
            rtHasResult: Boolean,
            kernelIsRunning: Boolean,
            listenerIsRunning: Boolean,
        ): SendingState

        /**
         * interpret state from objects, then transit
         */
        fun transit(
            rt: Result<*, *>?,
            kernelContext: KernelContextReadOnlyConv,
            ioPubListener: IOPubListener,
        ): SendingState {

            return this.transit(
                rtHasResult = rt != null,
                kernelIsRunning = kernelContext.getConvHeartBeatService().get()?.isHBAlive() ?: false,
                listenerIsRunning = ioPubListener.isRunning())
        }

        /**
         * run a side effect function, then transit state
         */
        suspend fun sideEffect(
            sideEffectFunc: suspend () -> Unit, rt: Result<*, *>?,
            kernelContext: KernelContextReadOnlyConv,
            ioPubListener: IOPubListener,
        ): SendingState {

            sideEffectFunc()
            return this.transit(rt, kernelContext, ioPubListener)
        }
    }
}
