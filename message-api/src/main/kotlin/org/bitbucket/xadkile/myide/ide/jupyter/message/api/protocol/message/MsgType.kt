package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message
/**
 * msg_type is defined in jupyter document
 * https://jupyter-client.readthedocs.io/en/latest/messaging.html
 */
interface MsgType {
    enum class Control : MsgType{
        shutdown_request, shutdown_reply
    }
}
