package com.github.xadkile.bicp.message.api.connection.service.iopub

import java.util.*

/**
 * A handler whose id is a random uuid
 */
abstract class UUIDMsgHandler : MsgHandler {
    private val _id:String by lazy {
        UUID.randomUUID().toString()
    }
    override fun id(): String {
        return _id
    }
}
