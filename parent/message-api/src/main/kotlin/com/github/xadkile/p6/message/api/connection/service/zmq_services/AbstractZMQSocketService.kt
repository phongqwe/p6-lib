package com.github.xadkile.p6.message.api.connection.service.zmq_services

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.xadkile.p6.exception.lib.error.ErrorReport
import com.github.xadkile.p6.message.api.connection.service.zmq_services.msg.P6MsgType
import kotlinx.coroutines.*
import org.zeromq.ZMQ
import java.net.ServerSocket

abstract class AbstractZMQSocketService(
    private val coroutineScope: CoroutineScope,
    private val coroutineDispatcher: CoroutineDispatcher,
) : ZMQSocketListenerService {

    var job: Job? = null
    protected var listenerMap: Map<P6MsgType, Map<String, P6MessageHandler>> = emptyMap()

    override val zmqPort: Int = run {
        val s = ServerSocket(0)
        val port = s.localPort
        s.close()
        port
    }

    protected abstract fun makeSocket(): ZMQ.Socket

    /**
     * implement this method to provde code to receive messages
     */
    protected abstract fun receiveMessage(socket: ZMQ.Socket)

    override suspend fun start(): Result<Unit, ErrorReport> {
        if (this.isRunning()) return Ok(Unit)
        val socket: ZMQ.Socket = makeSocket()
        this.job = coroutineScope.launch(coroutineDispatcher) {
            socket.use { sk: ZMQ.Socket ->
                while (isActive) {
                    receiveMessage(sk)
                }
            }
        }
        return Ok(Unit)
    }

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

    override suspend fun stop(): Result<Unit, ErrorReport> {
        this.job?.cancelAndJoin()
        return Ok(Unit)
    }

    override fun isRunning(): Boolean {
        return this.job?.isActive ?: false
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
