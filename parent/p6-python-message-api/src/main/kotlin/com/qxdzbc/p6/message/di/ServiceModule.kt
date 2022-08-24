package com.qxdzbc.p6.message.di

import com.qxdzbc.p6.message.api.connection.kernel_services.KernelServiceManager
import com.qxdzbc.p6.message.api.connection.kernel_services.KernelServiceManagerImp
import com.qxdzbc.p6.message.api.connection.service.iopub.MsgHandlerContainerImp
import com.qxdzbc.p6.message.api.connection.service.iopub.MsgHandlerContainer
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
