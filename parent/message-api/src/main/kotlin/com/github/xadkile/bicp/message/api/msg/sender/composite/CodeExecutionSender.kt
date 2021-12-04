package com.github.xadkile.bicp.message.api.msg.sender.composite

import com.github.michaelbull.result.*
import com.github.xadkile.bicp.message.api.connection.kernel_context.KernelContextReadOnlyConv
import com.github.xadkile.bicp.message.api.connection.kernel_context.exception.KernelIsDownException
import com.github.xadkile.bicp.message.api.connection.service.iopub.IOPubListenerServiceReadOnly
import com.github.xadkile.bicp.message.api.exception.UnknownException
import com.github.xadkile.bicp.message.api.msg.listener.exception.ExecutionErrException
import com.github.xadkile.bicp.message.api.msg.listener.MsgHandler
import com.github.xadkile.bicp.message.api.msg.listener.exception.IOPubListenerNotRunningException
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
 */
class CodeExecutionSender(
    val kernelContext: KernelContextReadOnlyConv,
    val executeSender: MsgSender<ExecuteRequest, Result<ExecuteReply, Exception>>,
    val ioPubListenerService: IOPubListenerServiceReadOnly,
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

        if (kernelContext.isNotRunning()) {
            return Err(KernelIsDownException.occurAt(this))
        }

        if (ioPubListenerService.isRunning().not()) {
            return Err(IOPubListenerNotRunningException.occurAt(this))
        }

        var rt: Result<ExecuteResult, Exception>? = null
        var state = SendingState.Start
        val handlers: List<MsgHandler> = listOf(
            // ph: config listener - catch execute_result message
            IOPub.ExecuteResult.handler { msg, listener ->
                val receivedMsg: ExecuteResult = msg.toModel()
                if (receivedMsg.parentHeader == message.header) {
                    rt = Ok(receivedMsg)
                    state = state.transit(rt, kernelContext)
                }
            },
            // ph: execution err handler
            IOPub.ExecuteError.handler { msg, listener ->
                val receivedMsg: JPMessage<IOPub.ExecuteError.MetaData, IOPub.ExecuteError.Content> = msg.toModel()
                if (receivedMsg.parentHeader == message.header) {
                    rt = Err(ExecutionErrException(receivedMsg.content))
                    state = state.transit(rt, kernelContext)
                }
            }
        )
        ioPubListenerService.addHandlers(handlers)

        // ph: sending the computing request
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
