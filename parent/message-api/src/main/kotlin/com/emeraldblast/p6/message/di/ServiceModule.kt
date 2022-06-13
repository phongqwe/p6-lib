package com.emeraldblast.p6.message.di

import com.emeraldblast.p6.message.api.connection.service.iopub.MsgHandlerContainerImp
import com.emeraldblast.p6.message.api.connection.service.iopub.MsgHandlerContainer
import dagger.Binds

@dagger.Module
interface ServiceModule {
//    @Binds
//    @MsgApiScope
//    fun HeartBeatService(i: LiveCountHeartBeatServiceCoroutine):HeartBeatService
//
    @Binds
    fun HandlerContainer(i: MsgHandlerContainerImp): MsgHandlerContainer
//
    companion object{
//        @ServiceInitTimeOut
//        fun ServiceInitTimeOut(kernelConfig:KernelConfig):Long{
//            return kernelConfig.timeOut.serviceInitTimeOut
//        }
    }
}
