package com.github.xadkile.bicp.message.api.msg.sender.composite

import com.github.michaelbull.result.*
import com.github.xadkile.bicp.message.api.connection.kernel_context.KernelContextReadOnlyConv
import com.github.xadkile.bicp.message.api.connection.kernel_context.exception.KernelIsDownException
import com.github.xadkile.bicp.message.api.exception.UnknownException
import com.github.xadkile.bicp.message.api.connection.service.iopub.IOPubListenerServiceReadOnly
import com.github.xadkile.bicp.message.api.connection.service.iopub.exception.ExecutionErrException
import com.github.xadkile.bicp.message.api.connection.service.iopub.MsgHandler
import com.github.xadkile.bicp.message.api.connection.service.iopub.exception.IOPubListenerNotRunningException
import com.github.xadkile.bicp.message.api.msg.protocol.JPMessage
import com.github.xadkile.bicp.message.api.msg.protocol.MsgStatus
import com.github.xadkile.bicp.message.api.msg.protocol.data_interface_definition.IOPub
import com.github.xadkile.bicp.message.api.msg.protocol.data_interface_definition.handler
import com.github.xadkile.bicp.message.api.msg.sender.MsgSender
import com.github.xadkile.bicp.message.api.msg.sender.exception.UnableToSendMsgException
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteReply
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteRequest
import kotlinx.coroutines.*

typealias ExecuteResult = JPMessage<IOPub.ExecuteResult.MetaData, IOPub.ExecuteResult.Content>


/**
 * Send an piece of code to be executed in the kernel. Return the result of the computation itself.
 * 1. I need kernelContext to ensure that if the kernel is shutdown while the code is executing, I can return the correct error
 * 2. I need the IO pub service so that I can get the result
 */
class CodeExecutionSender internal constructor(
    val kernelContext: KernelContextReadOnlyConv,
//    val executeSender: MsgSender<ExecuteRequest, Result<ExecuteReply, Exception>>,
//    val ioPubListenerService: IOPubListenerServiceReadOnly,
) : MsgSender<ExecuteRequest, Result<ExecuteResult, Exception>> {


    /**
     * This works like this:
     * - add handlers to ioPub listener service catch incoming message
     * - send message
     * - wait for listener service to return result
     * - remove handlers from listener service
     */
    override suspend fun send(
        message: ExecuteRequest,
        dispatcher: CoroutineDispatcher,
    ): Result<ExecuteResult, Exception> {

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
        val executeSender: MsgSender<ExecuteRequest, Result<ExecuteReply, Exception>> = hasSenderProvider.unwrap().executeRequestSender()

        if (ioPubListenerService.isNotRunning()) {
            return Err(IOPubListenerNotRunningException.occurAt(this))
        }

        var rt: Result<ExecuteResult, Exception>? = null
        var state = SendingState.Start
        val handlers: List<MsgHandler> = listOf(
            // x: config listener - catch execute_result message
            IOPub.ExecuteResult.handler { msg ->
                val receivedMsg: ExecuteResult = msg.toModel()
                if (receivedMsg.parentHeader == message.header) {
                    rt = Ok(receivedMsg)
                    state = state.transit(rt, kernelContext)
                }
            },
            // x: execution err handler
            IOPub.ExecuteError.handler { msg ->
                val receivedMsg: JPMessage<IOPub.ExecuteError.MetaData, IOPub.ExecuteError.Content> = msg.toModel()
                if (receivedMsg.parentHeader == message.header) {
                    rt = Err(ExecutionErrException(receivedMsg.content))
                    state = state.transit(rt, kernelContext)
                }
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
                    rt = Err(UnableToSendMsgException(message))
                    state = state.transit(rt, kernelContext)
                }
            } else {
                rt = Err(sendStatus.unwrapError())
                state = state.transit(rt, kernelContext)
            }
        }

        // ph: this ensure that this sender will wait until state reach terminal states: Done
        while (state != SendingState.Done) {
            state = state.transit(rt, kernelContext)
        }

        // x: remove temp handlers from the listener to prevent bug
        ioPubListenerService.removeHandlers(handlers)

        if (state == SendingState.Done) {
            if (rt != null) {
                return rt!!
            } else {
                return Err(KernelIsDownException("Kernel is killed before result is returned"))
            }
        } else {
            return Err(UnknownException.occurAt(this))
        }
    }

    /**
     * state of the sending action
     */
    private enum class SendingState {
        Start {
            override fun transit(
                hasResult: Boolean,
                kernelIsRunning: Boolean,
            ): SendingState {
                return Working.transit(hasResult, kernelIsRunning)
            }
        },
        Working {
            override fun transit(
                hasResult: Boolean,
                kernelIsRunning: Boolean,
            ): SendingState {

                if (hasResult) {
                    return Done
                }
                if (kernelIsRunning == false) {
                    return Done
                }
                return this
            }
        },
        Done {
            override fun transit(
                hasResult: Boolean,
                kernelIsRunning: Boolean,
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
        ): SendingState

        /**
         * interpret state from objects, then transit
         */
        fun transit(
            rt: Result<*, *>?,
            kernelContext: KernelContextReadOnlyConv,
        ): SendingState {

            return this.transit(
                hasResult = rt != null,
                kernelIsRunning = kernelContext.getConvHeartBeatService().get()?.isHBAlive() ?: false,
            )
        }
    }
}
