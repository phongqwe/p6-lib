package com.github.xadkile.bicp.message.api.connection

import com.github.xadkile.bicp.message.api.protocol.message.data_interface_definition.Shell
import com.github.xadkile.bicp.message.api.sender.MsgSender
import com.github.xadkile.bicp.message.api.sender.shell.ExecuteRequestResponseMessage
import java.util.*

/**
 * provide instances of sender
 */
interface SenderProvider {
    fun getExecuteRequestSender(): MsgSender<
            Shell.ExecuteRequest.Input.MetaData,
            Shell.ExecuteRequest.Input.Content,
            Optional<ExecuteRequestResponseMessage>>
}

//class SenderProviderImp : SenderProvider{
//    override fun getExecuteRequestSender(): MsgSender<
//            Shell.ExecuteRequest.Input.MetaData,
//            Shell.ExecuteRequest.Input.Content,
//            Optional<ExecuteRequestResponseMessage>> {
//        TODO()
//    }
//}
