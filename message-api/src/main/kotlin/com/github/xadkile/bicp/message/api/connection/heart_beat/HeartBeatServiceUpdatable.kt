package com.github.xadkile.bicp.message.api.connection.heart_beat

import org.zeromq.ZMQ

/**
 * A mutable heart beat service that can be updated midway
 */
interface HeartBeatServiceUpdatable : HeartBeatService {
    fun updateSocket(newSocket: ZMQ.Socket)
    fun subscribe(updater:HeartBeatServiceUpdater)
}

interface HeartBeatServiceUpdater {
    fun update(hbservice:HeartBeatServiceUpdatable)
}
