package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.data_definition

import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgContent
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgType

object Control {
    object ShutdownRequest {
        val msgType = MsgType.Control_shutdown_request
        class Content private constructor(val restart: Boolean) : MsgContent
    }
}
