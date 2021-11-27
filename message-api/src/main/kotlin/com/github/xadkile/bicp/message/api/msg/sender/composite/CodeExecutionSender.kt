package com.github.xadkile.bicp.message.api.msg.sender.composite

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.unwrap
import com.github.michaelbull.result.unwrapError
import com.github.xadkile.bicp.message.api.connection.ipython_context.KernelContextReadOnlyConv
import com.github.xadkile.bicp.message.api.connection.ipython_context.KernelIsDownException
import com.github.xadkile.bicp.message.api.msg.listener.IOPubListener
import com.github.xadkile.bicp.message.api.msg.listener.MsgHandlers
import com.github.xadkile.bicp.message.api.msg.protocol.message.JPMessage
import com.github.xadkile.bicp.message.api.msg.protocol.message.JPRawMessage
import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgStatus
import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgType
import com.github.xadkile.bicp.message.api.msg.protocol.message.data_interface_definition.IOPub
import com.github.xadkile.bicp.message.api.msg.sender.MsgSender
import com.github.xadkile.bicp.message.api.msg.sender.exception.UnableToSendMsgException
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteRequest
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteSender
import com.github.xadkile.bicp.message.api.other.Sleeper
import kotlinx.coroutines.*
import org.zeromq.ZMQ
import org.zeromq.ZMsg


typealias ExecuteResult = JPMessage<IOPub.ExecuteResult.MetaData, IOPub.ExecuteResult.Content>

class CodeExecutionSender(
    val kernelContext: KernelContextReadOnlyConv,
) : MsgSender<ExecuteRequest, Result<ExecuteResult, Exception>> {

    override suspend fun send(
        message: ExecuteRequest,
        dispatcher: CoroutineDispatcher,
    ): Result<ExecuteResult, Exception> {

        if(kernelContext.isNotRunning()){
            return Err(KernelIsDownException())
        }

        var rt: Result<ExecuteResult, Exception>? = null
        val executeSender = ExecuteSender(kernelContext)
        val ioPubListener = IOPubListener(
            kernelContext = kernelContext.conv(),
        )

        ioPubListener.addHandler(
            MsgHandlers.withUUID(IOPub.ExecuteResult.msgType) {
                val receivedMsg: ExecuteResult = it.toModel()
                if (receivedMsg.parentHeader == message.header) {
                    rt = Ok(receivedMsg)
                    ioPubListener.stop()
                }
            }
        )
        coroutineScope {
            // rmd: start the iopub listener, run it on a separated job.
            launch {
                ioPubListener.start(this, dispatcher)
            }
            launch(dispatcher) {
                // rmd: wait until ioPubListener to go online
                Sleeper.waitUntil { ioPubListener.isRunning() }
                val sendStatus = executeSender.send(message, Dispatchers.Default)
                val z = when (sendStatus) {
                    is Ok -> {
                        val sendOk: Boolean = sendStatus.value.content.status == MsgStatus.ok
                        if (sendOk) {
                            Ok(MsgStatus.ok)
                        } else {
                            Err(UnableToSendMsgException(message))
                        }
                    }
                    else -> {
                        Err(sendStatus.unwrapError())
                    }
                }
                if (z is Err) {
                    rt = z
                    ioPubListener.stop()
                }
            }
        }
        Sleeper.waitUntil { rt != null }

        return rt!!
    }
}
