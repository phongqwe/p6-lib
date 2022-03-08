package com.github.xadkile.p6.message.api.connection.service.iopub

import java.util.*

/**
 * A handler whose id is a random uuid
 */
abstract class UUIDMsgHandler : MsgHandler {
    private val _id:String by lazy {
        UUID.randomUUID().toString()
    }
    override val id: String = _id;

}
