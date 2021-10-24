package org.bitbucket.xadkile.taiga.jupyterclient.client.message.message

import org.bitbucket.xadkile.taiga.jupyterclient.client.message.Content
import org.bitbucket.xadkile.taiga.jupyterclient.client.message.MessageContent


class ShutdownRequestContent private constructor(val restart: Boolean): MessageContent, Content, Content.Facade {
    private constructor():this(false)
    companion object{
        val Restart = ShutdownRequestContent(true)
        val NoRestart = ShutdownRequestContent(false)
    }

    override fun getMsgType(): String {
        return "shutdown_request"
    }

    override fun getContent(): Content {
        return this
    }

    override fun toFacade(): Content.Facade {
        return this
    }
}
