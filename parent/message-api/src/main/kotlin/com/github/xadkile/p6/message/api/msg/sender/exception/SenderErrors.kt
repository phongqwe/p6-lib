package com.github.xadkile.p6.message.api.msg.sender.exception

import com.github.xadkile.p6.exception.lib.error.ErrorHeader
import com.github.xadkile.p6.message.api.msg.protocol.JPMessage
import com.github.xadkile.p6.message.api.msg.protocol.MsgContent
import com.github.xadkile.p6.message.api.msg.sender.composite.CodeExecutionSender
import org.zeromq.ZMsg

object SenderErrors {

    private const val prefix= "Sender error "
    object InvalidSendState : ErrorHeader("${prefix}1","invalid send state"){
        class Data(val currentState: CodeExecutionSender.SendingState)
    }

    /**
     * Indicate there are errors in code sent to kernel to be executed
     */
    object CodeError: ErrorHeader("${prefix}2","error in code"){
        class Data (val messageContent: MsgContent)
    }

    object UnableToQueueZMsg : ErrorHeader("${prefix}3","unable to queue ZMsg"){
        class Data(val message: ZMsg)
    }

    object UnableToSendMsg : ErrorHeader("${prefix}4","unable to send message"){
        class Data(val message: JPMessage<*, *>)
    }
}
