package com.github.xadkile.p6.message.api.message.sender.composite

import com.github.michaelbull.result.*
import com.github.xadkile.p6.common.exception.error.CommonErrors
import com.github.xadkile.p6.common.exception.error.ErrorReport
import com.github.xadkile.p6.message.api.connection.kernel_context.KernelContextReadOnly
import com.github.xadkile.p6.message.api.connection.kernel_context.errors.KernelErrors
import com.github.xadkile.p6.message.api.connection.service.iopub.IOPubListenerService
import com.github.xadkile.p6.message.api.connection.service.iopub.MsgHandler
import com.github.xadkile.p6.message.api.connection.service.iopub.MsgHandlers
import com.github.xadkile.p6.message.api.connection.service.iopub.errors.IOPubServiceErrors
import com.github.xadkile.p6.message.api.message.protocol.JPMessage
import com.github.xadkile.p6.message.api.message.protocol.MsgStatus
import com.github.xadkile.p6.message.api.message.protocol.MsgType
import com.github.xadkile.p6.message.api.message.protocol.data_interface_definition.IOPub
import com.github.xadkile.p6.message.api.message.protocol.data_interface_definition.Shell
import com.github.xadkile.p6.message.api.message.sender.MsgSender
import com.github.xadkile.p6.message.api.message.sender.exception.SenderErrors
import com.github.xadkile.p6.message.api.message.sender.shell.ExecuteReply
import com.github.xadkile.p6.message.api.message.sender.shell.ExecuteRequest
import com.github.xadkile.p6.message.api.other.Sleeper

typealias ExecuteResult = JPMessage<IOPub.ExecuteResult.MetaData, IOPub.ExecuteResult.Content>


/**
 * Send an piece of code to be executed in the kernel. Return the result of the computation itself.
 * 1. I need kernelContext to ensure that if the kernel is shutdown while the code is executing, I can return the correct error
 * 2. I need the IO pub service so that I can get the result
 */
class CodeExecutionSender internal constructor(
    val kernelContext: KernelContextReadOnly,
) : MsgSender<ExecuteRequest, Result<ExecuteResult?, ErrorReport>> {

    /**
     * This works like this:
     * - add handlers to ioPub listener service to catch incoming messages
     * - send input message
     * - wait for listener service to catch returned result
     * - remove handlers from listener service when done
     */
    override suspend fun send(
        message: ExecuteRequest
    ): Result<ExecuteResult?, ErrorReport> {

        if (kernelContext.isKernelNotRunning()) {
            val report = ErrorReport(
                header = KernelErrors.KernelDown,
                data = KernelErrors.KernelDown.Data(""),
                loc = "${this.javaClass.canonicalName}.send"
            )
            return Err(report)
        }

        val ioPubServiceRs = kernelContext.getIOPubListenerService()
        val senderProviderRs = kernelContext.getSenderProvider()

        if (ioPubServiceRs is Err) {
            return Err(ioPubServiceRs.error)
        }

        if (senderProviderRs is Err) {
            return Err(senderProviderRs.error)
        }

        val ioPubListenerService: IOPubListenerService = ioPubServiceRs.unwrap()
        val executeSender: MsgSender<ExecuteRequest, Result<ExecuteReply, ErrorReport>> =
            senderProviderRs.unwrap().executeRequestSender()

        if (ioPubListenerService.isNotRunning()) {
            return Err(ErrorReport(
                header = IOPubServiceErrors.IOPubServiceNotRunning,
                data = IOPubServiceErrors.IOPubServiceNotRunning.Data("occur at ${this.javaClass.canonicalName}.send"),
                loc = "${this.javaClass.canonicalName}.send"
            ))
        }

        var rt: Result<ExecuteResult?, ErrorReport>? = null
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
                    val report = ErrorReport(
                        header = SenderErrors.CodeError,
                        data = SenderErrors.CodeError.Data(receivedMsg.content),
                    )
                    rt = Err(report)
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
        val sendRs: Result<ExecuteReply, ErrorReport> = executeSender.send(message)
        if (sendRs is Ok) {
            val content: Shell.Execute.Reply.Content = sendRs.value.content
            val msgStatus: MsgStatus = content.status
            when (msgStatus) {
                MsgStatus.OK -> {
                    //dont do anything, wait for result from the listener
                }
                MsgStatus.ERROR -> {
                    val report = ErrorReport(
                        header = SenderErrors.CodeError,
                        data = SenderErrors.CodeError.Data(content),
                    )
                    rt = Err(report)
                }
                MsgStatus.ABORTED -> {
                    val report = ErrorReport(
                        header = SenderErrors.CodeError,
                        data = SenderErrors.CodeError.Data(content),
                    )
                    rt = Err(report)
                }
                else -> {
                    val report = ErrorReport(
                        header = CommonErrors.Unknown,
                        data = CommonErrors.Unknown.Data("Unknown error when executing code", null)
                    )
                    rt = Err(report)
                }
            }
            state = state.transit(rt, kernelContext, executionState)
        } else {
            rt = Err(sendRs.unwrapError())
            state = state.transit(rt, kernelContext, executionState)
        }

        if (rt != null) {
            return rt as Result<ExecuteResult?, ErrorReport>
        }

        // x: this ensure that this sender will wait until state reach terminal states
        Sleeper.delayUntil(10) {
            state = state.transit(rt, kernelContext, executionState)
            val waitRt = when (state) {
                SendingState.HasResult, SendingState.DoneButNoResult, SendingState.KernelDieMidway -> true
                else -> false
            }
            waitRt
        }

        ioPubListenerService.removeHandlers(handlers)

        val rt2: Result<ExecuteResult?, ErrorReport> = when (state) {
            SendingState.HasResult -> rt!!
            SendingState.DoneButNoResult -> Ok(null)
            SendingState.KernelDieMidway -> {
                val report = ErrorReport(
                    header = KernelErrors.KernelDown,
                    data = KernelErrors.KernelDown.Data("Kernel is killed before result is returned"),
                    loc = "${this.javaClass.canonicalName}.send"
                )
                Err(report)
            }
            else -> Err(
                ErrorReport(
                    header = SenderErrors.InvalidSendState,
                    data = SenderErrors.InvalidSendState.Data(state)
                )
            )
        }
        return rt2
    }

    /**
     * state of the sending action
     */
    enum class SendingState {
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
            kernelContext: KernelContextReadOnly,
            executionState: IOPub.Status.ExecutionState,
        ): SendingState {

            return this.transit(
                hasResult = rt != null,
                kernelIsRunning = kernelContext.isKernelRunning(),
                executionState = executionState
            )
        }
    }
}
