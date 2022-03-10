package com.github.xadkile.p6.message.api.connection.service.zmq_services

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.xadkile.p6.common.exception.error.ErrorReport
import kotlinx.coroutines.*
import org.zeromq.ZMQ
import java.net.ServerSocket

abstract class AbstractZMQService(
    protected val coroutineScope: CoroutineScope,
    protected val coroutineDispatcher: CoroutineDispatcher,
    protected val handlerContainer:P6MsgHandlerContainer = P6MsgHandlerContainerMutableImp()
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
        this.job = coroutineScope.launch(coroutineDispatcher) {
            val socket: ZMQ.Socket = makeSocket()
            socket.use { sk: ZMQ.Socket ->
                while (isActive) {
                    receiveMessage(sk)
                }
            }
        }
        return Ok(Unit)
    }

    override suspend fun stopJoin(): Result<Unit, ErrorReport> {
        this.job?.cancelAndJoin()
        return Ok(Unit)
    }

    override fun stop(): Result<Unit, ErrorReport> {
        this.job?.cancel()
        return Ok(Unit)
    }

    override fun isRunning(): Boolean {
        return this.job?.isActive ?: false
    }
}
