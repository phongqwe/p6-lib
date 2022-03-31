package com.emeraldblast.p6.message.api.connection.service.zmq_services

import com.emeraldblast.p6.message.api.connection.service.zmq_services.msg.P6Event

/**
 * A mutable implementation of [P6HandlerContainer]
 * TODO add test
 */
class P6HandlerContainerMutableImp <T>: P6HandlerContainer<T>{
    private var listenerMap: Map<P6Event, Map<String, P6Handler<T>>> = emptyMap()

    override fun addHandler(msgType: P6Event, handler: P6Handler<T>) {
        var z: Map<String, P6Handler<T>> = this.listenerMap[msgType] ?: emptyMap()
        z = z + (handler.id to handler)
        this.listenerMap = this.listenerMap + (msgType to z)
    }

    override fun removeHandler(id: String): P6Handler<T>? {
        val targetMsgType = mutableListOf<P6Event>()
        var rt: P6Handler<T>? = null
        for ((key, handlerMap) in this.listenerMap) {
            if (handlerMap.containsKey(id)) {
                targetMsgType.add(key)
            }
        }
        for (msgType in targetMsgType) {
            val m = this.listenerMap[msgType] as Map<String, P6Handler<T>>
            rt = m[id]
            val newMap = m - id
            this.listenerMap = this.listenerMap + (msgType to newMap)
        }
        return rt
    }

    override fun getHandler(id: String): P6Handler<T>? {
        for ((_,handlerMap) in this.listenerMap){
            if(handlerMap.containsKey(id)){
                return handlerMap[id]
            }
        }
        return null
    }

    override fun getHandlerByMsgType(msgType: P6Event): List<P6Handler<T>> {
        return this.listenerMap[msgType]?.values?.toList() ?: emptyList()
    }

    override fun isEmpty(): Boolean {
        if(this.listenerMap.isEmpty()){
            return true
        }else{
            for((_,m) in this.listenerMap){
                if(m.isNotEmpty()){
                    return false
                }
            }
            return true
        }
    }

    override fun removeHandlerForMsgType(msgType: P6Event): List<P6Handler<T>> {
        val rt = this.listenerMap[msgType] ?: emptyMap()
        this.listenerMap = this.listenerMap - msgType
        return rt.values.toList()
    }

    override fun removeHandler(msgType: P6Event, id: String): P6Handler<T>? {
        val m = this.listenerMap[msgType]
        if(m != null){
            val rt = m[id]
            val newMap = m - id
            if(newMap.isNotEmpty()){
                this.listenerMap = this.listenerMap + (msgType to newMap)
            }
            return rt
        }else{
            return null
        }
    }
}
