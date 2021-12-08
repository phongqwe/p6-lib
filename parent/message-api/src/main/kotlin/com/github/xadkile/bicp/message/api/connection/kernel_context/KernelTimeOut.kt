package com.github.xadkile.bicp.message.api.connection.kernel_context

class KernelTimeOut(
    val processInitTimeOut:Long=5000,
    val processStopTimeout:Long=5000,
    val connectionFileWriteTimeout:Long=5000,
    val serviceInitTimeOut:Long=5000,
) {
}
