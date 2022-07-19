package com.emeraldblast.p6.message.di

import com.emeraldblast.p6.message.api.connection.kernel_services.KernelServiceManager
import com.emeraldblast.p6.message.api.connection.kernel_services.KernelServiceManagerImp
import com.emeraldblast.p6.message.api.connection.service.iopub.MsgHandlerContainerImp
import com.emeraldblast.p6.message.api.connection.service.iopub.MsgHandlerContainer
import dagger.Binds

@dagger.Module
interface ServiceModule {
    @Binds
    @MsgApiScope
    fun KernelServiceManager(i: KernelServiceManagerImp): KernelServiceManager

    @Binds
    @MsgApiScope
    fun HandlerContainer(i: MsgHandlerContainerImp): MsgHandlerContainer
}
