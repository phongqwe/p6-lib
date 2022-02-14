package com.github.xadkile.p6.message.api.connection.service.iopub

import com.github.xadkile.p6.message.api.msg.protocol.MsgType

class HandlerContainerImp: MsgHandlerContainer {

    private val map:MutableMap<MsgType,List<MsgHandler>> = mutableMapOf()

    override fun addHandler(handler: MsgHandler) {
        val msgType = handler.msgType
        val newList = this.map.getOrDefault(msgType, emptyList()) + handler
        this.map[msgType] = newList
    }

    override fun getHandlers(msgType: MsgType): List<MsgHandler> {
        return this.map.getOrDefault(msgType, emptyList())
    }

    override fun containHandler(id: String):Boolean {
        return this.map.values.any { handlerList-> handlerList.any { it.id == id } }
    }

    override fun containHandler(handler: MsgHandler):Boolean {
        return this.map.values.any{handlerList -> handlerList.contains(handler)}
    }

    // TODO this method is not thread-safe, this is shared state. I must take measure to ensure that this is thread safe...
    override fun removeHandler(handlerId: String) {
        var key: MsgType?=null
        for((k,v) in this.map){
            if(v.any { it.id == handlerId }){
                key = k
                break
            }
        }
        if(key!=null){
            val newList = this.map[key]?.filter { it.id!=handlerId }!!
            if(newList.isEmpty()){
                this.map.remove(key)
            }else{
                this.map[key] = newList
            }
        }
    }

    // TODO this method is not thread-safe, this is shared state. I must take measure to ensure that this is thread safe...
    override fun removeHandler( handler: MsgHandler) {
        val msgType = handler.msgType
        val newList = this.getHandlers(msgType).filter { it != handler || it.id != handler.id }
        if(newList.isEmpty()){
            this.map.remove(msgType)
        }else{
            this.map[msgType] = newList
        }
    }

    override fun allHandlers(): List<MsgHandler> {
        return this.map.values.flatten()
    }

    override fun isEmpty(): Boolean {
        return this.map.isEmpty()
    }

    override fun isNotEmpty(): Boolean {
        return this.map.isNotEmpty()
    }
}
