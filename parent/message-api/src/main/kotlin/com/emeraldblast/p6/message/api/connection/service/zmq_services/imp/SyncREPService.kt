package com.emeraldblast.p6.message.api.connection.service.zmq_services.imp

import com.emeraldblast.p6.message.api.connection.kernel_context.KernelContextReadOnly
import com.emeraldblast.p6.message.api.connection.kernel_context.KernelCoroutineScope
import com.emeraldblast.p6.message.api.connection.service.zmq_services.AbstractZMQService
import com.emeraldblast.p6.message.api.connection.service.zmq_services.ZMQListenerService
import com.emeraldblast.p6.message.api.connection.service.zmq_services.msg.P6Response
import com.emeraldblast.p6.message.api.connection.service.zmq_services.msg.toModel
import com.emeraldblast.p6.message.di.RepServiceLogger
import com.emeraldblast.p6.message.di.ServiceCoroutineDispatcher
import com.emeraldblast.p6.proto.P6MsgProtos
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
import org.zeromq.SocketType
import org.zeromq.ZMQ
import org.zeromq.ZMsg

/**
 * A rep service that handles incoming messages in a synchronized function, so it's thread-safe
 */
class SyncREPService @AssistedInject constructor(
    @Assisted private val kernelContext: KernelContextReadOnly,
    @KernelCoroutineScope
     coroutineScope: CoroutineScope,
    @ServiceCoroutineDispatcher
    coroutineDispatcher: CoroutineDispatcher,
    @RepServiceLogger
    val repServiceLogger:Logger? = null
) : AbstractZMQService<P6Response>(coroutineScope, coroutineDispatcher), ZMQListenerService<P6Response> {

    override val socketType: SocketType = SocketType.REP

    companion object {
        val marker = MarkerFactory.getMarker(SyncREPService::class.java.canonicalName).also {
            it.add(ZMQListenerService.marker)
        }
    }

    private val logger: Logger = LoggerFactory.getLogger(this::class.java.canonicalName)


    override fun makeSocket(): ZMQ.Socket {
        val zcontext = kernelContext.zContext()
        val socket = zcontext.createSocket(SocketType.REP)
        socket.bind("tcp://*:${this.zmqPort}")
        return socket
    }

    @Synchronized
    override fun receiveMessage(socket: ZMQ.Socket) {
        try {
            val msg: ZMsg? = ZMsg.recvMsg(socket)
            if (msg != null) {
                repServiceLogger?.info(marker,"receive msg ok")
                val dataBytes = msg.fold(byteArrayOf()){ acc, zframe->
                    acc + zframe.data
                }
                repServiceLogger?.info(marker,"bytes received: $dataBytes")
                val p6MsgProto = P6MsgProtos.P6ResponseProto.newBuilder()
                        .mergeFrom(dataBytes)
                        .build()
                logger.debug(marker,"proto obj: $p6MsgProto")
                repServiceLogger?.debug(marker,"proto obj: $p6MsgProto")

                val p6Msg: P6Response = p6MsgProto.toModel()
                logger.debug(marker, "parsed p6 res: $p6Msg")
                repServiceLogger?.debug(marker, "parsed p6 res: $p6Msg")
                val handlers = this.getHandlerByMsgType(p6Msg.header.eventType)
                for (handler in handlers) {
                    handler.handleMessage(p6Msg)
                }
                // x: send a reply when all handlers finish running
                socket.send("ok")
                logger.info(marker,"all handlers for event \"${p6Msg.header.eventType}\" complete running")
                repServiceLogger?.info(marker,"all handlers for event \"${p6Msg.header.eventType}\" complete running")
            }else{
                logger.warn(marker,"Received null message")
                repServiceLogger?.warn(marker,"Received null message")
                socket.send("fail")
            }
        } catch (e: Exception) {
            // receiver service must not crash
            logger.error(marker,"Other exception when receiving message",e)
            repServiceLogger?.error(marker,"Other exception when receiving message",e)
            socket.send("fail")
        }
    }
}
