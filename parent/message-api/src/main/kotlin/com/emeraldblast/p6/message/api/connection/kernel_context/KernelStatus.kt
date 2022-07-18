package com.emeraldblast.p6.message.api.connection.kernel_context

data class KernelStatus(
    val isProcessLive: Boolean,
    val isConnectionFileWritten: Boolean,
    val connectionFileIsRead: Boolean,
    val isSessonOk: Boolean,
    val isChannelProviderOk: Boolean,
    val isMsgEncodeOk: Boolean,
    val isSenderProviderOk: Boolean,
) {
    fun isOk(): Boolean {
        return listOf(
            isProcessLive, isConnectionFileWritten, connectionFileIsRead,
            isSessonOk, isChannelProviderOk, isMsgEncodeOk, isSenderProviderOk
        ).all { it }
    }
}
