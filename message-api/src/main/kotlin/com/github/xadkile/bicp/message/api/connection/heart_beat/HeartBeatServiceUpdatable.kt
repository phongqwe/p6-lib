package com.github.xadkile.bicp.message.api.connection.heart_beat

import com.github.xadkile.bicp.message.api.connection.kernel_context.SocketProvider

/**
 * A mutable heart beat service that can be updated midway
 */
interface HeartBeatServiceUpdatable : HeartBeatService {
    fun updateSocket(socketProvider: SocketProvider)
}

