package com.github.xadkile.p6.message.api.connection.service.heart_beat

import com.github.xadkile.p6.message.api.connection.service.Service

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

    fun isServiceUpAndHBLive():Boolean{
        val c1 = this.isServiceRunning()
        val c2 = this.isHBAlive()
        return c1 && c2
    }

    override fun isRunning(): Boolean {
        return isServiceRunning()
    }
}
