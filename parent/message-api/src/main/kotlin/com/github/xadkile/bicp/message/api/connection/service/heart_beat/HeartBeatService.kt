package com.github.xadkile.bicp.message.api.connection.service.heart_beat

import com.github.xadkile.bicp.message.api.connection.service.Service

/**
 * A perpetual background service that check the heart beat channel periodically.
 */
interface HeartBeatService: Service {

    /**
     * return the latest liveness status of the heart beat channel
     * true = alive heart beat channel
     * false = dead heart beat channel
     */
    fun isHBAlive():Boolean

    /**
     * return true if this service is running, false otherwise
     */
    fun isServiceRunning():Boolean

    /**
     * convert this to a more convenient but more dangerous interface. Use with care and thought
     */
    fun conv(): HeartBeatServiceConv

    override fun isRunning(): Boolean {
        return isServiceRunning()
    }
}
