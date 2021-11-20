package com.github.xadkile.bicp.message.api.connection.heart_beat

import com.github.michaelbull.result.Result

class HeartBeatServiceConvImp(private val service: HeartBeatService): HeartBeatServiceConv {
    override fun start(): Boolean {
        return this.service.start()
    }

    override fun isHBAlive(): Boolean {
        return this.service.isHBAlive()
    }

    override fun isServiceRunning(): Boolean {
        return this.service.isServiceRunning()
    }

//    override fun checkHB(): Result<Unit, Exception> {
//        return this.service.checkHB()
//    }

    override fun stop(): Boolean {
        return this.service.stop()
    }

    override fun conv(): HeartBeatServiceConv {
        return this.service.conv()
    }
}
