package com.emeraldblast.p6.message.api.connection.service.iopub

import com.emeraldblast.p6.message.api.message.protocol.JPRawMessage
import com.emeraldblast.p6.message.api.message.protocol.MsgType

/**
 * For use with [IOPubListenerService].
 */
interface MsgHandler {
    /**
     * callback function.
     * A handler may need to interact with other suspend function, so this function is a suspend function.
     * This function may be long running, and I want to run it inside a coroutine ??????
     */
    fun handle(msg: JPRawMessage)

    /**
     * unique id
     */
    val id: String
    val msgType: MsgType
}

