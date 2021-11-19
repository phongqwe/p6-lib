package com.github.xadkile.bicp.message.api.connection.ipython_context

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.xadkile.bicp.message.api.connection.heart_beat.HeartBeatService
import com.github.xadkile.bicp.message.api.connection.heart_beat.HeartBeatServiceConv
import com.github.xadkile.bicp.message.api.connection.heart_beat.HeartBeatServiceConvImp
import com.github.xadkile.bicp.message.api.protocol.KernelConnectionFileContent
import com.github.xadkile.bicp.message.api.protocol.other.MsgIdGenerator
import org.zeromq.ZContext
import java.io.InputStream
import java.io.OutputStream

class IPythonContextConvImp(private val context: IPythonContext) : IPythonContextConv {
    override fun original(): IPythonContext {
        return this.context
    }



}
