package com.github.xadkile.bicp.message.api.msg.sender.composite

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.unwrapError
import com.github.xadkile.bicp.message.api.msg.listener.IOPubListener
import com.github.xadkile.bicp.message.api.msg.listener.IOPubListenerC
import com.github.xadkile.bicp.message.api.msg.listener.MsgHandler
import com.github.xadkile.bicp.message.api.msg.listener.MsgHandlers
import com.github.xadkile.bicp.message.api.msg.protocol.message.JPMessage
import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgStatus
import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgType
import com.github.xadkile.bicp.message.api.msg.protocol.message.data_interface_definition.IOPub
import com.github.xadkile.bicp.message.api.msg.sender.MsgSender
import com.github.xadkile.bicp.message.api.msg.sender.exception.UnableToSendMsgException
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteRequest
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteSender
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


typealias ExecuteResult = JPMessage<IOPub.ExecuteResult.MetaData, IOPub.ExecuteResult.Content>

class CodeExecutionSender(
    val executeSender: ExecuteSender,
    val ioPubListener: IOPubListener,
) : MsgSender<ExecuteRequest, ExecuteResult> {
    override fun send(message: ExecuteRequest): ExecuteResult {
        return zzzz(message)
    }

    fun zzzz(message: ExecuteRequest) :ExecuteResult{
        // rmd: start listener
        var rt:ExecuteResult? = null
        ioPubListener.start()
        ioPubListener.addHandler(
            MsgHandlers.withUUID(IOPub.ExecuteResult.msgType) {
                val receivedMsg:ExecuteResult = it.toModel()
                if(receivedMsg.header == message.header){
                    rt = receivedMsg
                }
            }
        )
        // rmd: send the message
        // if sending ok, wait for result on IOPub
        // if sending fail => return error
        // now the problem is how the hell do I return this function from within a handler????
        // I must wait: so a while(true) is needed => this make this function blocking
        val r = ioPubListener.use {
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
            if(z is Err){
                // return the err
            }
        }
        while(rt==null){
            // wait for the listener
        }
        return rt!!

    }
}
