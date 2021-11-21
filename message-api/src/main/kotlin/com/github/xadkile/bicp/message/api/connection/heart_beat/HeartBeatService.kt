package com.github.xadkile.bicp.message.api.connection.heart_beat

/**
 * A perpetual background service that check the heart beat channel periodically.
 */
interface HeartBeatService {
    /**
     * start this service.
     * Calling start() on an already started service should do no harm.
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
     * stop this service.
     * Calling stop() on an already stopped service should do no harm.
     */
    fun stop():Boolean

    /**
     * convert this to a more convenient but more dangerous interface. Use with care and thought
     */
    fun conv():HeartBeatServiceConv

    class NotRunningException(override val message:String=""):RuntimeException(message)
    class ZMQIsDeadException(override val message:String=""):Exception(message)
}

