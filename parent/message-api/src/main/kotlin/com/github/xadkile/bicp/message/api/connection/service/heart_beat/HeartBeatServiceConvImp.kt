package com.github.xadkile.bicp.message.api.connection.service.heart_beat

import com.github.michaelbull.result.Result

class HeartBeatServiceConvImp(private val service: HeartBeatService): HeartBeatServiceConv {
    override suspend fun start(): Result<Unit,Exception> {
        return this.service.start()
    }

    override fun isHBAlive(): Boolean {
        return this.service.isHBAlive()
    }

    override fun isServiceRunning(): Boolean {
        return this.service.isServiceRunning()
    }

    override suspend fun stop(): Result<Unit,Exception> {
        return this.service.stop()
    }

    override fun conv(): HeartBeatServiceConv {
        return this.service.conv()
    }
}
