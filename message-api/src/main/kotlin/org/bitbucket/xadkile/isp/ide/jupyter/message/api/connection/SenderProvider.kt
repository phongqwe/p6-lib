package org.bitbucket.xadkile.isp.ide.jupyter.message.api.connection

import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.message.data_definition.Shell
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.sender.MsgSender
import org.bitbucket.xadkile.isp.ide.jupyter.message.imp.shell.ExecuteRequestSender
import org.bitbucket.xadkile.isp.ide.jupyter.message.imp.shell.ResponseZ
import java.util.*

/**
 * provide instances of sender
 */
interface SenderProvider {
    fun getExecuteRequestSender(): MsgSender<Shell.ExecuteRequest.Out.Content,Optional<ResponseZ>>
}
