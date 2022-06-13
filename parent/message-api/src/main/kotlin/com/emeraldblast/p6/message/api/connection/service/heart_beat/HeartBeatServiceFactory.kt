package com.emeraldblast.p6.message.api.connection.service.heart_beat

import com.emeraldblast.p6.message.api.connection.kernel_context.KernelContext
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory

@AssistedFactory
interface HeartBeatServiceFactory {
    fun create(
        kernelContext: KernelContext,
        liveCount: Int,
        @Assisted("pollTimeOut")pollTimeOut: Long,
        @Assisted("startTimeOut")startTimeOut: Long,
    ): LiveCountHeartBeatServiceCoroutine
}
