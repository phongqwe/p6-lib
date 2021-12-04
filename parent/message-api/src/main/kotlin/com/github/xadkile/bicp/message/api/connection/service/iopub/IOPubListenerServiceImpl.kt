package com.github.xadkile.bicp.message.api.connection.service.iopub

import com.github.xadkile.bicp.message.api.msg.listener.MsgHandler
import com.github.xadkile.bicp.message.api.msg.listener.MsgListener
import com.github.xadkile.bicp.message.api.msg.protocol.MsgType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IOPubListenerServiceImpl(
    private val ioPubListener: MsgListener,
    private val cScope: CoroutineScope,
    private val cDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : IOPubListenerService {

    override fun addHandler(handler: MsgHandler) {
        this.ioPubListener.addHandler(handler)
    }

    override fun getHandlers(msgType: MsgType): List<MsgHandler> {
        return this.ioPubListener.getHandlers(msgType)
    }

    override fun containHandler(id: String): Boolean {
        return this.ioPubListener.containHandler(id)
    }

    override fun containHandler(handler: MsgHandler): Boolean {
        return this.ioPubListener.containHandler(handler)
    }

    override fun removeHandler(handlerId: String) {
        return this.ioPubListener.removeHandler(handlerId)
    }

    override fun removeHandler(handler: MsgHandler) {
        this.ioPubListener.removeHandler(handler)
    }

    override fun allHandlers(): List<MsgHandler> {
        return this.ioPubListener.allHandlers()
    }

    override fun isEmpty(): Boolean {
        return this.ioPubListener.isEmpty()
    }

    override fun isNotEmpty(): Boolean {
        return this.ioPubListener.isNotEmpty()
    }

    override fun start() {
        this.ioPubListener.start(
            externalScope = this.cScope,
            dispatcher = this.cDispatcher
        )
    }

    override fun stop() {
        ioPubListener.stop()
    }

    override fun isRunning(): Boolean {
        return this.ioPubListener.isRunning()
    }
}
