package com.github.xadkile.bicp.message.api.msg.listener

import com.github.xadkile.bicp.message.api.protocol.message.MsgType

interface MsgHandlerContainer:Map<MsgType,List<MsgHandler>> {
    fun addHandler(msgType: MsgType, handler: MsgHandler)
    fun getHandler(msgType: MsgType) : List<MsgHandler>
    fun removeHandler(handlerId:String)
    fun removeHandler(msgType: MsgType, handlerId: String)
    fun removeHandler(msgType: MsgType, handler:MsgHandler)
}
