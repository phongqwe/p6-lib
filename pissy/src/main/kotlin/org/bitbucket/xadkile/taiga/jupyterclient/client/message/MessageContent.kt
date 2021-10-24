package org.bitbucket.xadkile.taiga.jupyterclient.client.message

interface MessageContent {
    /**
     * msg_type is defined in jupyter document
     * https://jupyter-client.readthedocs.io/en/latest/messaging.html
     */
    fun getMsgType():String

    /**
     * message content structure is define in jupyter document
     * https://jupyter-client.readthedocs.io/en/latest/messaging.html
     */
    fun getContent():Content
}
