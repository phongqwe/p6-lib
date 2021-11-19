package com.github.xadkile.bicp.message.api.connection.heart_beat

import com.github.michaelbull.result.Result
import java.lang.RuntimeException

/**
 * A perpetual background service that check the heart beat channel every some time period.
 */
interface HeartBeatService {
    /**
     * start the service
     */
    fun start():Boolean

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
     * perform proactive hb liveness check
     */
    fun checkHB(): Result<Unit, Exception>

    /**
     * stop the service
     */
    fun stop():Boolean

    /**
     * convert this to a more convenient but more dangerous interface. Use with care and thought
     */
    fun conv():HeartBeatServiceConv

    class NotRunningException(override val message:String=""):RuntimeException(message)
}
