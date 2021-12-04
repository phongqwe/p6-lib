package com.github.xadkile.bicp.message.api.msg.listener

import com.github.xadkile.bicp.message.api.msg.protocol.MsgType

/**
 * A container of [MsgHandler]
 */
interface MsgHandlerContainer {
    fun addHandler(handler: MsgHandler)
    fun addHandlers(handlers:List<MsgHandler>){
        for (handler in handlers){
            this.addHandler(handler)
        }
    }

    fun getHandlers(msgType: MsgType): List<MsgHandler>

    fun containHandler(id: String): Boolean
    fun containHandler(handler: MsgHandler): Boolean

    fun removeHandler(handlerId: String)
    fun removeHandler(handler: MsgHandler)
    fun removeHandlers(handlers: List<MsgHandler>){
        for(handler in handlers){
            this.removeHandler(handler)
        }
    }


    fun allHandlers():List<MsgHandler>

    fun isEmpty():Boolean
    fun isNotEmpty():Boolean
}
