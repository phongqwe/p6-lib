package com.github.xadkile.p6.message.api.connection.service.zmq_services

import com.github.xadkile.p6.message.api.connection.service.zmq_services.msg.P6Message
import java.util.*

object P6MsgHandlers {
    fun makeHandler(handleMsg: (msg: P6Message) -> Unit = {}, handleError: (P6Message) -> Unit = {}): P6MessageHandler {
        return object : P6MessageHandler {
            override val id: String = UUID.randomUUID().toString()
            override fun handleMessage(msg: P6Message) {
                handleMsg(msg)
            }

            override fun handleError(msg: P6Message) {
                handleError(msg)
            }
        }
    }
}
