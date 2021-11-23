package com.github.xadkile.bicp.message.api.msg.listener

import com.github.xadkile.bicp.message.api.protocol.message.MsgType

class HandlerContainerImp: MsgHandlerContainer {
    val map:MutableMap<MsgType,List<MsgHandler>> = mutableMapOf()
    override fun addHandler(msgType: MsgType, handler: MsgHandler) {
        val newList = this.map.getOrDefault(msgType, emptyList()) + handler
        this.map[msgType] = newList
    }

    override fun getHandler(msgType: MsgType): List<MsgHandler> {
        return this.getOrDefault(msgType, emptyList())
    }

    override fun removeHandler(handlerId: String) {
        TODO("Not yet implemented")
    }

    override fun removeHandler(msgType: MsgType, handlerId: String) {
        val newList = this.getHandler(msgType).filter { it.id()!=handlerId }
        this.map[msgType] = newList
    }

    override fun removeHandler(msgType: MsgType, handler: MsgHandler) {
        TODO("Not yet implemented")
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
