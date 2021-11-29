package com.github.xadkile.bicp.message.api.msg.listener

import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgType

/**
 * A container of [MsgHandler]
 */
interface MsgHandlerContainer {
    fun addHandler(handler: MsgHandler)
    fun getHandlers(msgType: MsgType): List<MsgHandler>

    fun containHandler(id: String): Boolean
    fun containHandler(handler: MsgHandler): Boolean

    fun removeHandler(handlerId: String)
    fun removeHandler(handler: MsgHandler)

    fun allHandlers():List<MsgHandler>

    fun isEmpty():Boolean
    fun isNotEmpty():Boolean
}
