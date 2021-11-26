package com.github.xadkile.bicp.message.api.msg.sender.composite

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.unwrap
import com.github.michaelbull.result.unwrapError
import com.github.xadkile.bicp.message.api.connection.ipython_context.KernelContextReadOnlyConv
import com.github.xadkile.bicp.message.api.msg.listener.IOPubListener
import com.github.xadkile.bicp.message.api.msg.listener.MsgHandlers
import com.github.xadkile.bicp.message.api.msg.protocol.message.JPMessage
import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgStatus
import com.github.xadkile.bicp.message.api.msg.protocol.message.data_interface_definition.IOPub
import com.github.xadkile.bicp.message.api.msg.sender.exception.UnableToSendMsgException
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteRequest
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteSender
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


typealias ExecuteResult = JPMessage<IOPub.ExecuteResult.MetaData, IOPub.ExecuteResult.Content>

class CodeExecutionSender(
    val kernelContext: KernelContextReadOnlyConv,
)
//    : MsgSender<ExecuteRequest, ExecuteResult>
{
    suspend fun send(message: ExecuteRequest): ExecuteResult {

        var rt: ExecuteResult? = null

        val executeSender = ExecuteSender(kernelContext)

        val ioPubListener = IOPubListener(
             kernelContext = kernelContext.conv()
        )

        ioPubListener.addHandler(
            MsgHandlers.withUUID(IOPub.ExecuteResult.msgType) {
                val receivedMsg: ExecuteResult = it.toModel()
                if (receivedMsg.parentHeader == message.header) {
                    rt = receivedMsg
                    ioPubListener.stopII()
                    ioPubListener.stop()
                }
            }
        )

        ioPubListener.start()

        coroutineScope {
//            launch {
//                ioPubListener.startBare()
//            }
            launch {
                val sendStatus = executeSender.send(message)
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
                    // return the err
                }
            }
        }

        while (rt == null) {
            println("wait rt")
            // wait for the listener
        }
        return rt!!

    }
}
