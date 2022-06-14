package com.emeraldblast.p6.message.api.connection.service.iopub.handler

import java.util.*

/**
 * A handler whose id is a random uuid
 */
abstract class UUIDMsgHandler : MsgHandler {
    override val id: String = UUID.randomUUID().toString()
}
