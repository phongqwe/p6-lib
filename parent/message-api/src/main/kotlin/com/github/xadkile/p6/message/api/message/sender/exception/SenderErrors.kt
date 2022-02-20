package com.github.xadkile.p6.message.api.message.sender.exception

import com.github.xadkile.p6.common.exception.lib.error.ErrorType
import com.github.xadkile.p6.message.api.message.protocol.JPMessage
import com.github.xadkile.p6.message.api.message.protocol.MsgContent
import com.github.xadkile.p6.message.api.message.sender.composite.CodeExecutionSender
import org.zeromq.ZMsg

object SenderErrors {

    private const val prefix= "Sender error "
    object InvalidSendState : ErrorType("${prefix}1","invalid send state"){
        class Data(val currentState: CodeExecutionSender.SendingState)
    }

    /**
     * Indicate there are errors in code sent to kernel to be executed
     */
    object CodeError: ErrorType("${prefix}2","error in code"){
        class Data (val messageContent: MsgContent)
    }

    object UnableToQueueZMsg : ErrorType("${prefix}3","unable to queue ZMsg"){
        class Data(val message: ZMsg)
    }

    object UnableToSendMsg : ErrorType("${prefix}4","unable to send message"){
        class Data(val message: JPMessage<*, *>)
    }
}
