package com.emeraldblast.p6.message.api.connection.service.iopub.handler

import com.emeraldblast.p6.message.api.message.protocol.JPRawMessage
import com.emeraldblast.p6.message.api.message.protocol.MsgType

/**
 * For use with [IOPubListenerService].
 */
interface MsgHandler {
    /**
     * callback function.
     */
    fun handle(msg: JPRawMessage)

    /**
     * unique id
     */
    val id: String

    /**
     * the type of msg that this handler is supposed to handle
     */
    val msgType: MsgType
}

