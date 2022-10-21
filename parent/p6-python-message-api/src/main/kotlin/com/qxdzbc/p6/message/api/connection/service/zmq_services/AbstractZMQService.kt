package com.qxdzbc.p6.message.api.connection.service.zmq_services

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.qxdzbc.common.error.ErrorReport
import kotlinx.coroutines.*
import org.zeromq.ZMQ
import java.net.ServerSocket

abstract class AbstractZMQService<T>(
    protected val coroutineScope: CoroutineScope,
    protected val coroutineDispatcher: CoroutineDispatcher,
    protected val handlerContainer:P6HandlerContainer<T> = P6HandlerContainerMutableImp()
) : ZMQListenerService<T>, P6HandlerContainer<T> by handlerContainer{

    var job: Job? = null

    override val zmqPort: Int = run {
        val s = ServerSocket(0)
        val port = s.localPort
        s.close()
        port
    }

    protected abstract fun makeSocket(): ZMQ.Socket

    /**
     * implement this method to provide code to receive messages
     */
    protected abstract fun receiveMessage(socket: ZMQ.Socket)
    private var iSocket:ZMQ.Socket? = null
    override suspend fun start(): Result<Unit, ErrorReport> {
        if (this.isRunning()) return Ok(Unit)
        this.job = coroutineScope.launch(coroutineDispatcher) {
            val socket: ZMQ.Socket = makeSocket()
            iSocket = socket
            socket.use { sk: ZMQ.Socket ->
                while (isActive) {
                    receiveMessage(sk)
                }
            }
        }
        return Ok(Unit)
    }

    override suspend fun stopJoin(): Result<Unit, ErrorReport> {
        iSocket?.close()
        this.job?.cancelAndJoin()
        return Ok(Unit)
    }

    override fun stop(): Result<Unit, ErrorReport> {
        iSocket?.close()
        this.job?.cancel()
        return Ok(Unit)
    }

    override fun isRunning(): Boolean {
        return this.job?.isActive ?: false
    }
}
