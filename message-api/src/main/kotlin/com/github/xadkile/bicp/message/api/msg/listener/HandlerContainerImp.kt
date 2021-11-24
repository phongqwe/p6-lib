package com.github.xadkile.bicp.message.api.msg.listener

import com.github.xadkile.bicp.message.api.protocol.message.MsgType

class HandlerContainerImp: MsgHandlerContainer {

    private val map:MutableMap<MsgType,List<MsgHandler>> = mutableMapOf()

    override fun addHandler(handler: MsgHandler) {
        val msgType = handler.msgType()
        val newList = this.map.getOrDefault(msgType, emptyList()) + handler
        this.map[msgType] = newList
    }

    override fun getHandlers(msgType: MsgType): List<MsgHandler> {
        return this.getOrDefault(msgType, emptyList())
    }

    override fun containHandler(id: String):Boolean {
        return this.values.any { handlerList-> handlerList.any { it.id() == id } }
    }

    override fun containHandler(handler: MsgHandler):Boolean {
        return this.values.any{handlerList -> handlerList.contains(handler)}
    }

    override fun removeHandler(handlerId: String) {
        var key:MsgType?=null
        for((k,v) in this.map){
            if(v.any { it.id() == handlerId }){
                key = k
                break
            }
        }
        if(key!=null){
            val newList = this.map[key]?.filter { it.id()!=handlerId }!!
            if(newList.isEmpty()){
                this.map.remove(key)
            }else{
                this.map[key] = newList
            }
        }
    }

    override fun removeHandler(msgType: MsgType, handlerId: String) {
        val newList = this.getHandlers(msgType).filter { it.id()!=handlerId }
        if(newList.isEmpty()){
            this.map.remove(msgType)
        }else{
            this.map[msgType] = newList
        }
    }

    override fun removeHandler( handler: MsgHandler) {
        val msgType = handler.msgType()
        val newList = this.getHandlers(msgType).filter { it != handler }
        if(newList.isEmpty()){
            this.map.remove(msgType)
        }else{
            this.map[msgType] = newList
        }
    }

    override val entries: Set<Map.Entry<MsgType, List<MsgHandler>>>
        get() = this.map.entries
    override val keys: Set<MsgType>
        get() = this.map.keys
    override val size: Int
        get() = this.map.size
    override val values: Collection<List<MsgHandler>>
        get() = this.map.values

    override fun containsKey(key: MsgType): Boolean {
        return this.map.containsKey(key)
    }

    override fun containsValue(value: List<MsgHandler>): Boolean {
        return this.map.containsValue(value)
    }

    override fun get(key: MsgType): List<MsgHandler>? {
        return this.map.get(key)
    }

    override fun isEmpty(): Boolean {
        return this.map.isEmpty()
    }
}
