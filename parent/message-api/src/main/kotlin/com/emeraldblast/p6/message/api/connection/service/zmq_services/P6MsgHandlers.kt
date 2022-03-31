package com.emeraldblast.p6.message.api.connection.service.zmq_services

import com.emeraldblast.p6.message.api.connection.service.zmq_services.msg.P6Message
import com.emeraldblast.p6.message.api.connection.service.zmq_services.msg.P6Response
import java.util.*

object P6MsgHandlers {
    fun makeHandler(handleMsg: (msg: P6Message) -> Unit = {}): P6MessageHandler {
        return object : P6MessageHandler {
            override val id: String = UUID.randomUUID().toString()
            override fun handleMessage(msg: P6Message) {
                handleMsg(msg)
            }
        }
    }
    fun makeP6ResHandler(handleMsg: (msg: P6Response) -> Unit = {}): P6ResponseHandler {
        return object : P6ResponseHandler {
            override val id: String = UUID.randomUUID().toString()
            override fun handleMessage(msg: P6Response) {
                handleMsg(msg)
            }
        }
    }
}
