package com.github.xadkile.p6.message.api.connection.service.zmq_services

import com.github.xadkile.p6.common.CanCheckEmpty
import com.github.xadkile.p6.message.api.connection.service.zmq_services.msg.P6Event

interface P6MsgHandlerContainer : CanCheckEmpty {
    fun addHandler(msgType: P6Event, handler: P6MessageHandler)

    /**
     * return the removed handlers
     */
    fun removeHandlerForMsgType(msgType: P6Event): List<P6MessageHandler>

    /**
     * return the removed handler
     */
    fun removeHandler(msgType: P6Event, id: String): P6MessageHandler?

    /**
     * return the removed handler
     */
    fun removeHandler(id: String): P6MessageHandler?
    fun getHandler(id: String): P6MessageHandler?
    fun getHandlerByMsgType(msgType: P6Event): List<P6MessageHandler>
}
