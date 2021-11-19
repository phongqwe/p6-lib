package com.github.xadkile.bicp.message.api.sender

import com.github.michaelbull.result.*
import com.github.xadkile.bicp.message.api.connection.heart_beat.HeartBeatService
import com.github.xadkile.bicp.message.api.connection.heart_beat.HeartBeatServiceConv
import com.github.xadkile.bicp.message.api.connection.ipython_context.IPythonContext
import org.zeromq.ZFrame
import org.zeromq.ZMQ
import org.zeromq.ZMsg

/**
 *  Sending requests to zmq.
 *  Receiving responses to zmq
 */
class ZMQMsgSender {
    class UnableToQueueException : Exception()
    companion object {
        fun send(
            message: List<ByteArray>,
            socket: ZMQ.Socket,
            ipContext: IPythonContext,
            interval: Long = SenderConstant.defaultPollingDuration
        ): Result<ZMsg,Exception> {
            val hb = ipContext.getHeartBeatService()
                .map { it.conv() }
            if(hb is Ok){
                val hbs = hb.unwrap()
                // reminder: if hb service is not running, then
                // there is no way to ensure that zmq is running
                // => don't send any message if hb service is dead
                if (hbs.convCheck()) {
                    val poller: ZMQ.Poller = ipContext.zContext().createPoller(1)
                    val payload: List<ZFrame> = message.map { ZFrame(it) }
                    val zmsg = ZMsg().also { it.addAll(payload) }
                    poller.register(socket)
                    val queueOk: Boolean = zmsg.send(socket)
                    if (queueOk) {
                        var recvMsg: ZMsg? = null
                        while (hbs.convCheck() && recvMsg == null) {
                            val i: Int = poller.poll(interval)
                            if (i == 1) {
                                recvMsg = ZMsg.recvMsg(socket)
                                break
                            }
                        }
                        if(recvMsg!=null){
                            return Ok(recvMsg)
                        }else{
                            return Err(Exception())
                        }
                    } else {
                        return Err(UnableToQueueException())
                    }
                } else {
                    return Err(HeartBeatService.NotRunningException())
                }
            }else{
                return Err(hb.unwrapError())
            }
        }

//        fun send(
//            socket: ZMQ.Socket,
//            ipContext: IPythonContext,
//            interval: Long = SenderConstant.defaultPollingDuration,
//            vararg message: ByteArray,
//        ): Result<ZMsg,Exception> {
//            return send(message.asList(), socket, ipContext, interval)
//        }

//        fun send(
//            socket: ZMQ.Socket,
//            ipContext: IPythonContext,
//            interval: Long = SenderConstant.defaultPollingDuration,
//            vararg message: String,
//        ): Result<ZMsg,Exception> {
//            return this.send(message.map { it.toByteArray(Charsets.UTF_8) }, socket, ipContext, interval)
//        }
    }
}
