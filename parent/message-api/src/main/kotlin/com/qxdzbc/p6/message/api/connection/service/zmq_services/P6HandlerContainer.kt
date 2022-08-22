package com.qxdzbc.p6.message.api.connection.service.zmq_services

import com.qxdzbc.p6.common.CanCheckEmpty
import com.qxdzbc.p6.message.api.connection.service.zmq_services.msg.P6Event

interface P6HandlerContainer<T> : CanCheckEmpty {
    fun addHandler(msgType: P6Event, handler: P6Handler<T>)

    /**
     * return the removed handlers
     */
    fun removeHandlerForMsgType(msgType: P6Event): List<P6Handler<T>>

    /**
     * return the removed handler
     */
    fun removeHandler(msgType: P6Event, id: String): P6Handler<T>?

    /**
     * return the removed handler
     */
    fun removeHandler(id: String): P6Handler<T>?
    fun getHandler(id: String): P6Handler<T>?
    fun getHandlerByMsgType(msgType: P6Event): List<P6Handler<T>>
}
