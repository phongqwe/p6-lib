package com.emeraldblast.p6.message.api.message.sender.exception

import com.emeraldblast.p6.common.exception.error.ErrorHeader
import com.emeraldblast.p6.message.api.message.protocol.JPMessage
import com.emeraldblast.p6.message.api.message.protocol.MsgContent
import org.zeromq.ZMsg

object SenderErrors {

    private const val prefix= "Sender error "
    object InvalidSendState {
        val header=ErrorHeader("${prefix}1","invalid send state")
        class Data(val currentState: CodeExecutionSenderImp.SendingState)
    }

    /**
     * Indicate there are errors in code sent to kernel to be executed
     */
    object CodeError{
        val header=ErrorHeader("${prefix}2","error in code")
        class Data (val messageContent: MsgContent)
    }

    object UnableToQueueZMsg {
        val header=ErrorHeader("${prefix}3","unable to queue ZMsg")
        class Data(val message: ZMsg)
    }

    object UnableToSendMsg {
        val header=ErrorHeader("${prefix}4","unable to send message")
        class Data(val message: JPMessage<*, *>)
    }
}
