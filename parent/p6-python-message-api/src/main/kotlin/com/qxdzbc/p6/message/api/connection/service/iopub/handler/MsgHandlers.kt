package com.qxdzbc.p6.message.api.connection.service.iopub.handler

import com.qxdzbc.p6.message.api.message.protocol.JPRawMessage
import com.qxdzbc.p6.message.api.message.protocol.MsgType

/**
 * Factory for creating msg handlers
 */
object MsgHandlers {
    /**
     * create a [MsgHandler] with a random uuid
     */
    fun withUUID(
        msgType: MsgType,
        handlerFunction: (msg: JPRawMessage) -> Unit = {},
    ): UUIDMsgHandler {
        return object : UUIDMsgHandler() {
            private val mt = msgType
            override fun handle(msg: JPRawMessage) {
                handlerFunction(msg)
            }
            override val msgType: MsgType = mt
        }
    }
    fun <RESULT,KEY>deferredJobHandler(
        msgType: MsgType,
        handlerFunction: (msg: JPRawMessage) -> Unit = {},
    ): DeferredJobHandler<RESULT,KEY> {
        return object : AbsDeferredJobHandler<RESULT,KEY>() {
            override val msgType: MsgType = msgType
            override fun handle(msg: JPRawMessage) {
                handlerFunction(msg)
            }
        }
    }
}
