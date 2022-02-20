package com.github.xadkile.p6.message.api.connection.service.zmq_services

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.xadkile.p6.common.exception.error.ErrorReport
import kotlinx.coroutines.*
import org.zeromq.ZMQ
import java.net.ServerSocket

abstract class AbstractZMQService(
    private val coroutineScope: CoroutineScope,
    private val coroutineDispatcher: CoroutineDispatcher,
    private val handlerContainer:P6MsgHandlerContainer = P6MsgHandlerContainerMutableImp()
) : ZMQListenerService, P6MsgHandlerContainer by handlerContainer{

    var job: Job? = null

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

    override suspend fun stop(): Result<Unit, ErrorReport> {
        this.job?.cancelAndJoin()
        return Ok(Unit)
    }

    override fun isRunning(): Boolean {
        return this.job?.isActive ?: false
    }

//    override fun getHandlerByMsgType(msgType: P6MsgType): List<P6MessageHandler> {
//        return this.handlerContainer.getHandlerByMsgType(msgType)
//    }
//
//    override fun addHandler(msgType: P6MsgType, handler: P6MessageHandler) {
//        this.handlerContainer.addHandler(msgType,handler)
//    }
//
//    override fun removeHandler(id: String): P6MessageHandler? {
//        return this.handlerContainer.removeHandler(id)
//    }
//
//    override fun getHandler(id: String): P6MessageHandler? {
//        return this.handlerContainer.getHandler(id)
//    }
//
//    override fun removeHandlerForMsgType(msgType: P6MsgType): List<P6MessageHandler> {
//        return this.handlerContainer.removeHandlerForMsgType(msgType)
//    }
//
//    override fun removeHandler(msgType: P6MsgType, id: String): P6MessageHandler? {
//        return this.handlerContainer.removeHandler(msgType,id)
//    }
}
