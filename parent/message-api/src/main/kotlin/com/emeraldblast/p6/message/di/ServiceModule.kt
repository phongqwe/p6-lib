package com.emeraldblast.p6.message.di

import com.emeraldblast.p6.message.api.connection.kernel_context.KernelConfig
import com.emeraldblast.p6.message.api.connection.service.heart_beat.HeartBeatService
import com.emeraldblast.p6.message.api.connection.service.heart_beat.LiveCountHeartBeatServiceCoroutine
import com.emeraldblast.p6.message.api.connection.service.iopub.IOPubListenerService
import com.emeraldblast.p6.message.api.connection.service.iopub.IOPubListenerServiceImpl
import dagger.Binds

@dagger.Module
interface ServiceModule {
//    @Binds
//    @MsgApiScope
//    fun HeartBeatService(i: LiveCountHeartBeatServiceCoroutine):HeartBeatService
//
//    @Binds
//    @MsgApiScope
//    fun IOPubListenerService(i: IOPubListenerServiceImpl): IOPubListenerService

    companion object{
//        @ServiceInitTimeOut
//        fun ServiceInitTimeOut(kernelConfig:KernelConfig):Long{
//            return kernelConfig.timeOut.serviceInitTimeOut
//        }
    }
}
