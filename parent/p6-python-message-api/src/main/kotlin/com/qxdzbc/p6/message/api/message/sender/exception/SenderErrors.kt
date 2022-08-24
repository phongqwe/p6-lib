package com.qxdzbc.p6.message.api.message.sender.exception

import com.qxdzbc.common.error.ErrorHeader
import com.qxdzbc.p6.message.api.message.protocol.JPMessage
import com.qxdzbc.p6.message.api.message.protocol.MsgContent
import com.qxdzbc.p6.message.api.message.protocol.MsgStatus
import com.qxdzbc.p6.message.api.message.protocol.data_interface_definition.IOPub
import com.qxdzbc.p6.message.api.message.protocol.data_interface_definition.Shell
import com.qxdzbc.p6.message.api.message.sender.composite.SendingState
import org.zeromq.ZMsg

object SenderErrors {

    private const val prefix= "Sender error "
    object InvalidSendState {
        val header= ErrorHeader("${prefix}1","invalid send state")
        class Data(val currentState: SendingState)
    }

    /**
     * Indicate there are errors in code sent to kernel to be executed
     */
    object CodeError{
        val header= ErrorHeader("${prefix}2","error in code")
        class Data (val messageContent: Shell.Execute.Reply.Content)
    }

    object UnableToQueueZMsg {
        val header= ErrorHeader("${prefix}3","unable to queue ZMsg")
        class Data(val message: ZMsg)
    }

    object UnableToSendMsg {
        val header= ErrorHeader("${prefix}4","unable to send message")
        class Data(val message: JPMessage<*, *>)
    }

    object IOPubExecuteError{
        val header= ErrorHeader("${prefix}5","IOPub execute error")
        class Data (val messageContent: IOPub.ExecuteError.Content)
    }
}
