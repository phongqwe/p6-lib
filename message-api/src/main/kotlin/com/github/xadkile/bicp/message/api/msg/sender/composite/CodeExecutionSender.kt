package com.github.xadkile.bicp.message.api.msg.sender.composite

import com.github.michaelbull.result.*
import com.github.xadkile.bicp.message.api.connection.kernel_context.KernelContextReadOnly
import com.github.xadkile.bicp.message.api.connection.kernel_context.KernelContextReadOnlyConv
import com.github.xadkile.bicp.message.api.connection.kernel_context.KernelIsDownException
import com.github.xadkile.bicp.message.api.msg.listener.MsgHandlers
import com.github.xadkile.bicp.message.api.msg.listener.MsgListener
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
 */
class CodeExecutionSender(
    val kernelContext: KernelContextReadOnlyConv,
    val executeSender: MsgSender<ExecuteRequest, Result<ExecuteReply, Exception>>,
    val ioPubListener: MsgListener,
) : MsgSender<ExecuteRequest, Result<ExecuteResult, Exception>> {

    override suspend fun send(
        message: ExecuteRequest,
        dispatcher: CoroutineDispatcher,
    ): Result<ExecuteResult, Exception> {

        if (kernelContext.isNotRunning()) {
            return Err(KernelIsDownException.occurAt(this))
        }

        var rt: Result<ExecuteResult, Exception>? = null
        // p: config listener
        ioPubListener.also { listener ->
            listener.addHandler(
                MsgHandlers.withUUID(IOPub.ExecuteResult.msgType,
                    handlerFunction = { m, l ->
                        val receivedMsg: ExecuteResult = m.toModel()
                        if (receivedMsg.parentHeader == message.header) {
                            rt = Ok(receivedMsg)
                            l.stop()
                        }
                    }
                )
            )
        }
        coroutineScope {
            // rmd: start the iopub listener, it is on a separated coroutine.
            val startRs = ioPubListener.start(this, dispatcher)
            if (startRs is Ok) {
                Sleeper.waitUntil { ioPubListener.isRunning() }
                launch(dispatcher) {
                    // rmd: wait until ioPubListener to go online
                    val sendStatus = executeSender.send(message, dispatcher)
                    val sendRes: Result<MsgStatus, Exception> = if (sendStatus is Ok) {
                        val msgOk: Boolean = sendStatus.get()!!.content.status == MsgStatus.ok
                        if (msgOk) {
                            Ok(MsgStatus.ok)
                        } else {
                            Err(UnableToSendMsgException(message))
                        }
                    } else {
                        Err(sendStatus.unwrapError())
                    }
                    if (sendRes is Err) {
                        rt = sendRes
                        ioPubListener.stop()
                    }
                }
            } else {
                rt = Err(startRs.unwrapError())
                ioPubListener.stop()
            }
        }
        Sleeper.waitUntil {
            rt != null
        }
        ioPubListener.stop()
        return rt!!

    }

    override fun getKernelContext(): KernelContextReadOnly {
        return this.kernelContext
    }
}
