package com.emeraldblast.p6.message.api.connection.service.iopub

import com.emeraldblast.p6.message.api.connection.service.iopub.handler.MsgHandler
import com.emeraldblast.p6.message.api.message.protocol.MsgType

/**
 * A container of [MsgHandler]. Handlers are grouped by [MsgType]
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
