package com.github.xadkile.bicp.message.api.connection

import com.github.xadkile.bicp.message.api.protocol.message.data_interface_definition.Shell
import com.github.xadkile.bicp.message.api.sender.MsgSender
import com.github.xadkile.bicp.message.api.sender.shell.ResponseZ
import java.util.*

/**
 * provide instances of sender
 */
interface SenderProvider {
    fun getExecuteRequestSender(): MsgSender<Shell.ExecuteRequest.Out.Content,Optional<ResponseZ>>
}
