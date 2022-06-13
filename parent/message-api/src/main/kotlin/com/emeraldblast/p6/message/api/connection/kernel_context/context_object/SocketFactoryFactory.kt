package com.emeraldblast.p6.message.api.connection.kernel_context.context_object

import dagger.assisted.AssistedFactory
import org.zeromq.ZContext

@AssistedFactory
interface SocketFactoryFactory{
    fun create(
        channelProvider: ChannelProvider,
        zContext: ZContext
    ): SocketFactoryImp
}
