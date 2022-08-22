package com.qxdzbc.p6.message.api.connection.kernel_context

/**
 * A summary of the current kernel
 * @param isConnectionFileWritten indicates whether the connection file is written into the disk or not
 * @param connectionFileIsRead indicates whether the connection file read into the kernel context object or not
 */
data class KernelStatus(
    val isProcessUnderManagement:Boolean,
    val isProcessLive: Boolean,
    val isConnectionFileWritten: Boolean,
    val connectionFileIsRead: Boolean,
    val isSessonOk: Boolean,
    val isChannelProviderOk: Boolean,
    val isMsgEncodeOk: Boolean,
    val isSenderProviderOk: Boolean,
) {
    fun isRunning(): Boolean {
        val isProcessLive = run {
            if (this.isProcessUnderManagement) {
                this.isProcessLive
            } else {
                true
            }
        }
        return listOf(
            isProcessLive, isConnectionFileWritten, connectionFileIsRead,
            isSessonOk, isChannelProviderOk, isMsgEncodeOk, isSenderProviderOk
        ).all { it }
    }
}
