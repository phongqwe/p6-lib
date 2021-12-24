package com.github.xadkile.p6.message.api.msg.sender.composite

import com.github.michaelbull.result.*
import com.github.xadkile.p6.exception.ExceptionInfo
import com.github.xadkile.p6.exception.UnknownException
import com.github.xadkile.p6.message.api.connection.kernel_context.KernelContextReadOnlyConv
import com.github.xadkile.p6.message.api.connection.kernel_context.exception.KernelIsDownException
import com.github.xadkile.p6.message.api.connection.service.iopub.IOPubListenerServiceReadOnly
import com.github.xadkile.p6.message.api.connection.service.iopub.MsgHandler
import com.github.xadkile.p6.message.api.connection.service.iopub.MsgHandlers
import com.github.xadkile.p6.message.api.connection.service.iopub.exception.ExecutionErrException
import com.github.xadkile.p6.message.api.connection.service.iopub.exception.IOPubListenerNotRunningException
import com.github.xadkile.p6.message.api.msg.protocol.JPMessage
import com.github.xadkile.p6.message.api.msg.protocol.MsgStatus
import com.github.xadkile.p6.message.api.msg.protocol.MsgType
import com.github.xadkile.p6.message.api.msg.protocol.data_interface_definition.IOPub
import com.github.xadkile.p6.message.api.msg.protocol.data_interface_definition.handler
import com.github.xadkile.p6.message.api.msg.sender.MsgSender
import com.github.xadkile.p6.message.api.msg.sender.exception.UnableToSendMsgException
import com.github.xadkile.p6.message.api.msg.sender.shell.ExecuteReply
import com.github.xadkile.p6.message.api.msg.sender.shell.ExecuteRequest
import com.github.xadkile.p6.message.api.other.Sleeper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

typealias ExecuteResult = JPMessage<IOPub.ExecuteResult.MetaData, IOPub.ExecuteResult.Content>


/**
 * Send an piece of code to be executed in the kernel. Return the result of the computation itself.
 * 1. I need kernelContext to ensure that if the kernel is shutdown while the code is executing, I can return the correct error
 * 2. I need the IO pub service so that I can get the result
 */
class CodeExecutionSender internal constructor(
    val kernelContext: KernelContextReadOnlyConv,
) : MsgSender<ExecuteRequest, Result<ExecuteResult?, Exception>> {


    /**
     * This works like this:
     * - add handlers to ioPub listener service to catch incoming messages
     * - send input message
     * - wait for listener service to catch returned result
     * - remove handlers from listener service when done
     */
    override suspend fun send(
        message: ExecuteRequest,
        dispatcher: CoroutineDispatcher,
    ): Result<ExecuteResult?, Exception> {

        if (kernelContext.isKernelNotRunning()) {
            return Err(KernelIsDownException.occurAt(this))
        }

        val hasIoPubService = kernelContext.getIOPubListenerService()
        val hasSenderProvider = kernelContext.getSenderProvider()

        if (hasIoPubService is Err) {
            return Err(hasIoPubService.unwrapError())
        }

        if (hasSenderProvider is Err) {
            return Err(hasSenderProvider.unwrapError())
        }

        val ioPubListenerService: IOPubListenerServiceReadOnly = hasIoPubService.unwrap()
        val executeSender: MsgSender<ExecuteRequest, Result<ExecuteReply, Exception>> =
            hasSenderProvider.unwrap().executeRequestSender()

        if (ioPubListenerService.isNotRunning()) {
            return Err(IOPubListenerNotRunningException.occurAt(this))
        }

        var rt: Result<ExecuteResult, Exception>? = null
        var executionState = IOPub.Status.ExecutionState.undefined
        var state = SendingState.Start

        // x: create handlers
        val handlers: List<MsgHandler> = listOf(
            IOPub.Status.handler { msg ->
                // x: code pieces that do not return a value or only do side effects will not trigger execution_result
                // x: so I must rely on execution state to terminate this call
                val receivedMsg: JPMessage<IOPub.Status.MetaData, IOPub.Status.Content> = msg.toModel()
                executionState = receivedMsg.content.executionState
                state = state.transit(rt, kernelContext, executionState)
            },
            // x: catch execute_result message
            IOPub.ExecuteResult.handler { msg ->
                val receivedMsg: ExecuteResult = msg.toModel()
                if (receivedMsg.parentHeader == message.header) {
                    rt = Ok(receivedMsg)
                    state = state.transit(rt, kernelContext, executionState)
                }
            },
            // x: handler for execution err
            IOPub.ExecuteError.handler { msg ->
                val receivedMsg: JPMessage<IOPub.ExecuteError.MetaData, IOPub.ExecuteError.Content> = msg.toModel()
                if (receivedMsg.parentHeader == message.header) {
                    rt = Err(
                        ExecutionErrException(ExceptionInfo(
                            loc = this,
                            data = receivedMsg.content))
                    )
                    state = state.transit(rt, kernelContext, executionState)
                }
            },
            //TODO something to do with message that return display data
            MsgHandlers.withUUID(MsgType.IOPub_display_data) { msg ->
                println(msg)
            }
        )
        // x: add temp handlers to catch the result
        ioPubListenerService.addHandlers(handlers)

        // x: sending the computing request
        withContext(dispatcher) {
            val sendStatus = executeSender.send(message, dispatcher)
            if (sendStatus is Ok) {
                val msgIsOk: Boolean = sendStatus.get()!!.content.status == MsgStatus.ok
                if (msgIsOk.not()) {
                    rt = Err(UnableToSendMsgException(ExceptionInfo(
                        loc = this@CodeExecutionSender,
                        data = message
                    )))
                    state = state.transit(rt, kernelContext, executionState)
                }
            } else {
                rt = Err(sendStatus.unwrapError())
                state = state.transit(rt, kernelContext, executionState)
            }
        }

        // x: this ensure that this sender will wait until state reach terminal states: Done
        Sleeper.delayUntil(50) {
            state = state.transit(rt, kernelContext, executionState)
            when (state) {
                SendingState.HasResult, SendingState.DoneButNoResult, SendingState.KernelDieMidway -> true
                else -> false
            }
        }

        // x: remove temp handlers from the listener to prevent bug
        ioPubListenerService.removeHandlers(handlers)
        val rt2 = when (state) {
            SendingState.HasResult -> rt!!
            SendingState.DoneButNoResult -> Ok(null)
            SendingState.KernelDieMidway -> Err(KernelIsDownException(ExceptionInfo(
                msg = "Kernel is killed before result is returned",
                loc = this,
                data = Unit
            )))
            else -> Err(UnknownException.occurAt(this))
        }
        return rt2

//        if (state == SendingState.HasResult) {
//            if (rt != null) {
//                return rt!!
//            } else {
//                if(executionState == IOPub.Status.ExecutionState.idle){
//                    //
//                    return Err(KernelIsDownException(ExceptionInfo(
//                        msg = "Ace",
//                        loc = this,
//                        data = Unit
//                    )))
//                }else{
//                    return Err(KernelIsDownException(ExceptionInfo(
//                        msg = "Kernel is killed before result is returned",
//                        loc = this,
//                        data = Unit
//                    )))
//                }
//            }
//        } else {
//            return Err(UnknownException.occurAt(this))
//        }
    }

    /**
     * state of the sending action
     */
    private enum class SendingState {
        Start {
            override fun transit(
                hasResult: Boolean,
                kernelIsRunning: Boolean,
                executionState: IOPub.Status.ExecutionState,
            ): SendingState {
                return Working.transit(hasResult, kernelIsRunning, executionState)
            }
        },
        KernelDieMidway {
            override fun transit(
                hasResult: Boolean,
                kernelIsRunning: Boolean,
                executionState: IOPub.Status.ExecutionState,
            ): SendingState {
                return this
            }
        },
        Working {
            override fun transit(
                hasResult: Boolean,
                kernelIsRunning: Boolean,
                executionState: IOPub.Status.ExecutionState,
            ): SendingState {

                if (hasResult) {
                    return HasResult
                }
                if (executionState == IOPub.Status.ExecutionState.idle) {
                    return DoneButNoResult
                }
                if (kernelIsRunning.not()) {
                    return KernelDieMidway
                }

                return this
            }
        },
        DoneButNoResult {
            override fun transit(
                hasResult: Boolean,
                kernelIsRunning: Boolean,
                executionState: IOPub.Status.ExecutionState,
            ): SendingState {
                return this
            }
        },
        HasResult {
            override fun transit(
                hasResult: Boolean,
                kernelIsRunning: Boolean,
                executionState: IOPub.Status.ExecutionState,
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
            executionState: IOPub.Status.ExecutionState,
        ): SendingState

        /**
         * interpret state from objects, then transit
         */
        fun transit(
            rt: Result<*, *>?,
            kernelContext: KernelContextReadOnlyConv,
            executionState: IOPub.Status.ExecutionState,
        ): SendingState {

            return this.transit(
                hasResult = rt != null,
                kernelIsRunning = kernelContext.getConvHeartBeatService().get()?.isHBAlive() ?: false,
                executionState = executionState
            )
        }
    }
}
