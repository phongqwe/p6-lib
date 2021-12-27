package com.github.xadkile.p6.message.api.msg.sender.exception

import com.github.xadkile.p6.exception.error.ErrorHeader
import com.github.xadkile.p6.message.api.msg.protocol.JPMessage
import com.github.xadkile.p6.message.api.msg.protocol.MsgContent
import com.github.xadkile.p6.message.api.msg.sender.composite.CodeExecutionSender
import org.zeromq.ZMsg

object SenderErrors {
    object InvalidSendState : ErrorHeader("UnknownSendState".hashCode(),"invalid send state"){
        class Data(val currentState: CodeExecutionSender.SendingState)
    }

    object CodeError: ErrorHeader("SenderErrors.CodeExecution".hashCode(),"error in code"){
        class Data (val messageContent: MsgContent)
    }

    object UnableToQueueZMsg : ErrorHeader("UnableToQueueZMsg".hashCode(),"unable to queue ZMsg"){
        class Data(val message: ZMsg)
    }

    object UnableToSendMsg : ErrorHeader("UnableToSendMsg".hashCode(),"unable to send message"){
        class Data(val message: JPMessage<*, *>)
    }
}
