package com.github.xadkile.p6.message.api.connection.kernel_context

class KernelTimeOut(
    val processInitTimeOut:Long=5000,
    val processStopTimeout:Long=5000,
    val connectionFileWriteTimeout:Long=5000,
    val serviceInitTimeOut:Long=5000,
    val messageTimeOut:Long = 5000
) {
}
