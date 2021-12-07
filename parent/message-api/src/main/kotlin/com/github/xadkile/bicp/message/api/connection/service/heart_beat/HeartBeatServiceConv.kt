package com.github.xadkile.bicp.message.api.connection.service.heart_beat

/**
 * Host "dangerous" method that overlook certain safety measures but is CONVenient to use, but should be use with care
 */
interface HeartBeatServiceConv : HeartBeatService {
    /**
     * return true if this service is running and the hba is alive
     */
    fun convCheck():Boolean{
        val c1 = this.isServiceRunning()
        val c2 = this.isHBAlive()
        return c1 && c2
    }
}

