package org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.message.data_interface_definition

import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.message.MsgContent
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.message.MsgType
import javax.inject.Qualifier

object Control {
    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Address

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Channel

    object ShutdownRequest {
        val msgType = MsgType.Control_shutdown_request
        class Content private constructor(val restart: Boolean) : MsgContent
    }
}
