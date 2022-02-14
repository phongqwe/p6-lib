package com.github.xadkile.p6.message.api.connection.service.zmq_services

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.xadkile.p6.exception.lib.error.ErrorReport
import kotlinx.coroutines.*
import org.zeromq.*
import java.net.ServerSocket

abstract class AbstractZMQSocketService(
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

    /**
     * implement this method to provde code to receive messages
     */
    protected abstract fun receiveMessage(socket:ZMQ.Socket)

    override suspend fun start(): Result<Unit, ErrorReport> {
        if (this.isRunning()) return Ok(Unit)
        val socket:ZMQ.Socket = makeSocket()
        this.job = coroutineScope.launch(coroutineDispatcher) {
            socket.use { sk:ZMQ.Socket ->
                while (isActive) {
                    receiveMessage(sk)
                }
            }
        }
        return Ok(Unit)
    }

    override fun addHandler(handler: MessageHandler) {
        this.listenerMap = this.listenerMap + (handler.id to handler)
    }

    override fun removeHandler(id: String): Boolean {
        this.listenerMap = this.listenerMap - id
        return true
    }

    override fun getHandler(id: String): MessageHandler? {
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
