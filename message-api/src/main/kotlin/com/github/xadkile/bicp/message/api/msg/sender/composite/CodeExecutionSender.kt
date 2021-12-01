package com.github.xadkile.bicp.message.api.msg.sender.composite

import com.github.michaelbull.result.*
import com.github.xadkile.bicp.message.api.connection.kernel_context.KernelContextReadOnlyConv
import com.github.xadkile.bicp.message.api.connection.kernel_context.KernelIsDownException
import com.github.xadkile.bicp.message.api.exception.UnknownException
import com.github.xadkile.bicp.message.api.msg.listener.IOPubListener
import com.github.xadkile.bicp.message.api.msg.protocol.JPMessage
import com.github.xadkile.bicp.message.api.msg.protocol.MsgStatus
import com.github.xadkile.bicp.message.api.msg.protocol.data_interface_definition.IOPub
import com.github.xadkile.bicp.message.api.msg.protocol.data_interface_definition.handler
import com.github.xadkile.bicp.message.api.msg.sender.MsgSender
import com.github.xadkile.bicp.message.api.msg.sender.exception.UnableToSendMsgException
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteReply
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteRequest
import com.github.xadkile.bicp.message.api.other.Sleeper
import kotlinx.coroutines.*


typealias ExecuteResult = JPMessage<IOPub.ExecuteResult.MetaData, IOPub.ExecuteResult.Content>

/**
 * Send an piece of code to be executed in the kernel. Return the result of the computation itself.
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

        // p: config listener - catch execute_result message
        ioPubListener.addHandler(
            IOPub.ExecuteResult.handler { msg, listener ->
                val receivedMsg: ExecuteResult = msg.toModel()
                if (receivedMsg.parentHeader == message.header) {
                    rt = Ok(receivedMsg)
                    listener.stop()
                    state = state.transit(rt, kernelContext, ioPubListener)
                }
            }
        )


        // ph: config listener - catch status messages, "busy", "idle"
        ioPubListener.addHandler(
            IOPub.Status.handler { msg, listener ->
                val jpMsg: JPMessage<IOPub.Status.MetaData, IOPub.Status.Content> = msg.toModel()
                if (jpMsg.parentHeader == message.header) {
                    if (jpMsg.content.executionState == IOPub.Status.ExecutionState.idle) {
                        println("Reach idle state-> stop")
                        // TODO consider keeping or not keeping this marker handler. Does it solve any problem?
                        listener.stop()
                    } else if (jpMsg.content.executionState == IOPub.Status.ExecutionState.busy) {
                        println("Reach busy -> start computing")
                        // TODO consider keeping or not keeping this marker handler. Does it solve any problem?
                    }
                }
            }
        )

        ioPubListener.addHandler(
            IOPub.Error.handler { msg, listener ->
                println(msg)
            }
        )


        // ph: sending the computing request
        coroutineScope {
            // ph: start the listener on a separated coroutine.
            val startRs: Result<Unit, Exception> = ioPubListener.start(this, dispatcher)

            // ph: only send the message if the ioPubListener was started successfully
            if (startRs is Ok) {
                // rmd: wait until ioPubListener to go online
                Sleeper.waitUntil { ioPubListener.isRunning() }
                launch(dispatcher) {
                    val sendStatus = executeSender.send(message, dispatcher)
                    if (sendStatus is Ok) {
                        val msgIsOk: Boolean = sendStatus.get()!!.content.status == MsgStatus.ok
                        if (msgIsOk.not()) {
                            rt = Err(UnableToSendMsgException(message))
                            ioPubListener.stop()
                            state = state.transit(rt, kernelContext, ioPubListener)
                        }
                    } else {
                        rt = Err(sendStatus.unwrapError())
                        ioPubListener.stop()
                        state = state.transit(rt, kernelContext, ioPubListener)
                    }
                }
            } else {
                rt = Err(startRs.unwrapError())
                ioPubListener.stop()
                state = state.transit(rt, kernelContext, ioPubListener)
            }
        }

        // ph: this ensure that this sender will wait until state reach terminal states: Done, KernelDown,ListenerDown
        while (state != SendingState.Done) {
            state = state.transit(rt, kernelContext, ioPubListener)
        }
        ioPubListener.stop()
        state = state.transit(rt, kernelContext, ioPubListener)

        if (state == SendingState.Done) {
            return rt!!
        } else {
            return Err(UnknownException.occurAt(this))
        }
    }

    /**
     * state of the sending action
     */
    internal enum class SendingState {
        Start {
            override fun transit(
                hasResult: Boolean,
                kernelIsRunning: Boolean,
                listenerIsRunning: Boolean,
            ): SendingState {
                return Working.transit(hasResult, kernelIsRunning, listenerIsRunning)
            }
        },
        Working {
            override fun transit(
                hasResult: Boolean,
                kernelIsRunning: Boolean,
                listenerIsRunning: Boolean,
            ): SendingState {

                if (hasResult) {
                    return Done
                }
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
                hasResult: Boolean,
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
            hasResult: Boolean,
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
                hasResult = rt != null,
                kernelIsRunning = kernelContext.getConvHeartBeatService().get()?.isHBAlive() ?: false,
                listenerIsRunning = ioPubListener.isRunning())
        }
    }
}
