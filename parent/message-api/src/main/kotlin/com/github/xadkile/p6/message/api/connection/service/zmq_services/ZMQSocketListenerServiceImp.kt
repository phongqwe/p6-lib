package com.github.xadkile.p6.message.api.connection.service.zmq_services

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.xadkile.p6.exception.lib.error.ErrorReport
import kotlinx.coroutines.*
import org.zeromq.*
import java.net.ServerSocket

abstract class ZMQSocketListenerServiceImp(
    private val coroutineScope: CoroutineScope,
    private val coroutineDispatcher: CoroutineDispatcher,
) : ZMQSocketListenerService {

    var job: Job? = null
    protected var listenerMap: Map<String, MessageHandler> = emptyMap()

    override val zmqPort: Int = run {
        val s = ServerSocket(0)
        val port = s.localPort
        s.close()
        port
    }

    protected abstract fun makeSocket(): ZMQ.Socket
    protected abstract fun receiveMessage(socket:ZMQ.Socket)

    override suspend fun start(): Result<Unit, ErrorReport> {
        if (this.isRunning()) return Ok(Unit)
        val socket = makeSocket()
        this.job = coroutineScope.launch(coroutineDispatcher) {
            socket.use { sk ->
                while (isActive) {
                    receiveMessage(sk)
                }
            }
        }
        return Ok(Unit)
    }

    override fun addListener(listener: MessageHandler) {
        this.listenerMap = this.listenerMap + (listener.id to listener)
    }

    override fun removeListener(id: String): Boolean {
        this.listenerMap = this.listenerMap - id
        return true
    }

    override fun getListener(id: String): MessageHandler? {
        return this.listenerMap[id]
    }

    override suspend fun stop(): Result<Unit, ErrorReport> {
        this.job?.cancelAndJoin()
        return Ok(Unit)
    }

    override fun isRunning(): Boolean {
        return this.job?.isActive ?: false
    }
}
