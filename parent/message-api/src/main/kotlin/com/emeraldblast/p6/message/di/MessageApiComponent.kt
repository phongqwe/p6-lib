package com.emeraldblast.p6.message.di

import com.emeraldblast.p6.message.api.connection.kernel_context.KernelConfig
import com.emeraldblast.p6.message.api.connection.kernel_context.KernelContext
import com.emeraldblast.p6.message.api.connection.kernel_context.KernelCoroutineScope
import com.emeraldblast.p6.message.api.connection.kernel_services.KernelServiceManager
import com.emeraldblast.p6.message.api.connection.service.heart_beat.HeartBeatServiceFactory
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import org.slf4j.Logger
import org.zeromq.ZContext


@Component(
    modules = [
        KernelContextModule::class,
        ServiceModule::class,
        SenderModule::class,
        IOPubHandlerModule::class
    ]
)
@MsgApiScope
interface MessageApiComponent {

    @MsgApiScope
    fun kernelServiceManager(): KernelServiceManager

    @MsgApiScope
    fun zContext(): ZContext

    @MsgApiScope
    fun kernelContext(): KernelContext

//    @MsgApiScope
//    fun heartBeatServiceFactory():HeartBeatServiceFactory

    @Component.Builder
    interface Builder {
        fun build(): MessageApiComponent

        @BindsInstance
        fun kernelConfig(config: KernelConfig?=null): Builder

        @BindsInstance
        fun kernelCoroutineScope(@KernelCoroutineScope scope: CoroutineScope): Builder

        @BindsInstance
        fun networkServiceCoroutineDispatcher(@ServiceCoroutineDispatcher dispatcher: CoroutineDispatcher): Builder

        @BindsInstance
        fun serviceLogger(@RepServiceLogger logger: Logger? = null): Builder

        @BindsInstance
        fun msgApiCommonLogger(@MsgApiCommonLogger logger: Logger? = null): Builder
    }
}
