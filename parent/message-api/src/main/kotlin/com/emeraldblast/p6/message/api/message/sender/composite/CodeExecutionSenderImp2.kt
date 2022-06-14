package com.emeraldblast.p6.message.api.message.sender.composite

import com.github.michaelbull.result.*
import com.emeraldblast.p6.common.exception.error.CommonErrors
import com.emeraldblast.p6.common.exception.error.ErrorReport
import com.emeraldblast.p6.message.api.connection.kernel_context.KernelContextReadOnly
import com.emeraldblast.p6.message.api.connection.kernel_context.errors.KernelErrors
import com.emeraldblast.p6.message.api.connection.service.iopub.IOPubListenerService
import com.emeraldblast.p6.message.api.connection.service.iopub.errors.IOPubServiceErrors
import com.emeraldblast.p6.message.api.message.protocol.MsgStatus
import com.emeraldblast.p6.message.api.message.protocol.data_interface_definition.IOPub
import com.emeraldblast.p6.message.api.message.protocol.data_interface_definition.Shell
import com.emeraldblast.p6.message.api.message.sender.MsgSender
import com.emeraldblast.p6.message.api.message.sender.exception.SenderErrors
import com.emeraldblast.p6.message.api.message.sender.shell.ExecuteReply
import com.emeraldblast.p6.message.api.message.sender.shell.ExecuteRequest
import com.emeraldblast.p6.message.utils.Utils.cancelIfPossible
import kotlinx.coroutines.*

/**
 * Send an piece of code to be executed in the kernel. Return the result of the computation itself.
 * 1. I need kernelContext to ensure that if the kernel is shutdown while the code is executing, I can return the correct error
 * 2. I need the IO pub service so that I can get the result
 */
class CodeExecutionSenderImp2 internal constructor(
    val kernelContext: KernelContextReadOnly,
) : CodeExecutionSender {

    /**
     * This works like this:
     * - add handlers to ioPub listener service to catch incoming messages
     * - send input message
     * - wait for listener service to catch returned result
     * - remove handlers from listener service when done
     * what is the reason for using temporary handler.
     */
    override suspend fun send(
        message: ExecuteRequest
    ): Result<ExecuteResult?, ErrorReport> {

        if (kernelContext.isKernelNotRunning()) {
            val report = ErrorReport(
                header = KernelErrors.KernelDown.header,
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


        if (ioPubListenerService.isNotRunning()) {
            return Err(
                ErrorReport(
                    header = IOPubServiceErrors.IOPubServiceNotRunning.header,
                    data = IOPubServiceErrors.IOPubServiceNotRunning.Data("occur at ${this.javaClass.canonicalName}.send"),
                )
            )
        }
        val executeSender: MsgSender<ExecuteRequest, Result<ExecuteReply, ErrorReport>> =
            senderProviderRs.unwrap().executeRequestSender()

        var rt: Result<ExecuteResult?, ErrorReport> ? = null
        // x: setup defered job
        val defExecuteResult: CompletableDeferred<ExecuteResult> = CompletableDeferred()
        val defError: CompletableDeferred<ErrorReport> = CompletableDeferred()
        val defIdleStatus: CompletableDeferred<IOPub.Status.ExecutionState> = CompletableDeferred()
        val defBusyStatus: CompletableDeferred<IOPub.Status.ExecutionState> = CompletableDeferred()

        ioPubListenerService.executeResultHandler.addJob(message.header, defExecuteResult)
        ioPubListenerService.executeErrorHandler.addJob(message.header, defError)
        ioPubListenerService.idleExecutionStatusHandler.addJob(message.header, defIdleStatus)
        ioPubListenerService.busyExecutionStatusHandler.addJob(message.header, defBusyStatus)

        // x: sending the computing request
        val sendRs: Result<ExecuteReply, ErrorReport> = executeSender.send(message)
        if (sendRs is Ok) {
            val content: Shell.Execute.Reply.Content = sendRs.value.content
            val msgStatus: MsgStatus = content.status
            when (msgStatus) {
                MsgStatus.OK -> {
                    val o = coroutineScope {
                        launch(Dispatchers.IO) {
                            val executeResult = defExecuteResult.await()
                            rt = Ok(executeResult)
                            defError.cancelIfPossible()
                            defIdleStatus.cancelIfPossible()
                            defBusyStatus.cancelIfPossible()
                        }
                        launch(Dispatchers.IO) {
                            val executeResult = defError.await()
                            rt = Err(executeResult)
                            defExecuteResult.cancelIfPossible()
                            defIdleStatus.cancelIfPossible()
                            defBusyStatus.cancelIfPossible()
                        }
                        launch(Dispatchers.IO) {
                            val idleStatus = defIdleStatus.await()
                            if(rt==null){
                                rt = Ok(null)
                            }
                            defExecuteResult.cancelIfPossible()
                            defBusyStatus.cancelIfPossible()
                            defError.cancelIfPossible()
                        }
                        launch(Dispatchers.IO) {
                            val busyStatus = defBusyStatus.await()
                            // do nothing
                        }
                    }
                    o.join()
                }
                MsgStatus.ERROR -> {
                    val report = ErrorReport(
                        header = SenderErrors.CodeError.header,
                        data = SenderErrors.CodeError.Data(content),
                    )
                    rt = Err(report)
                }
                MsgStatus.ABORTED -> {
                    val report = ErrorReport(
                        header = SenderErrors.CodeError.header,
                        data = SenderErrors.CodeError.Data(content),
                    )
                    rt = Err(report)
                }
                else -> {
                    val report = ErrorReport(
                        header = CommonErrors.Unknown.header,
                        data = CommonErrors.Unknown.Data("Unknown error when executing code", null)
                    )
                    rt = Err(report)
                }
            }
        } else {
            rt = Err(sendRs.unwrapError())
        }
        ioPubListenerService.executeResultHandler.removeJob(message.header)
        ioPubListenerService.executeErrorHandler.removeJob(message.header)
        return rt!!
    }
}
