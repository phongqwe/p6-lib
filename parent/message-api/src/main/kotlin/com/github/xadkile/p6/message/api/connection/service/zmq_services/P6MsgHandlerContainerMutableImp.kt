package com.github.xadkile.p6.message.api.connection.service.zmq_services

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
//import com.github.xadkile.p6.common.exception.lib.error.ErrorReport
import com.github.xadkile.p6.message.api.connection.service.zmq_services.msg.P6MsgType
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.zeromq.ZMQ
import java.net.ServerSocket

/**
 * A mutable implementation of [P6MsgHandlerContainer]
 * TODO add test
 */
class P6MsgHandlerContainerMutableImp : P6MsgHandlerContainer{
    private var listenerMap: Map<P6MsgType, Map<String, P6MessageHandler>> = emptyMap()

    override fun addHandler(msgType: P6MsgType, handler: P6MessageHandler) {
        var z: Map<String, P6MessageHandler> = this.listenerMap[msgType] ?: emptyMap()
        z = z + (handler.id to handler)
        this.listenerMap = this.listenerMap + (msgType to z)
    }

    override fun removeHandler(id: String): P6MessageHandler? {
        val targetMsgType = mutableListOf<P6MsgType>()
        var rt: P6MessageHandler? = null
        for ((key, handlerMap) in this.listenerMap) {
            if (handlerMap.containsKey(id)) {
                targetMsgType.add(key)
            }
        }
        for (msgType in targetMsgType) {
            val m = this.listenerMap[msgType] as Map<String, P6MessageHandler>
            rt = m[id]
            val newMap = m - id
            this.listenerMap = this.listenerMap + (msgType to newMap)
        }
        return rt
    }

    override fun getHandler(id: String): P6MessageHandler? {
        for ((_,handlerMap) in this.listenerMap){
            if(handlerMap.containsKey(id)){
                return handlerMap[id]
            }
        }
        return null
    }

    override fun getHandlerByMsgType(msgType: P6MsgType): List<P6MessageHandler> {
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

    override fun removeHandlerForMsgType(msgType: P6MsgType): List<P6MessageHandler> {
        val rt = this.listenerMap[msgType] ?: emptyMap()
        this.listenerMap = this.listenerMap - msgType
        return rt.values.toList()
    }

    override fun removeHandler(msgType: P6MsgType, id: String): P6MessageHandler? {
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
