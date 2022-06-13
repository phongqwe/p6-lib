package com.emeraldblast.p6.message.api.connection.kernel_context.context_object

import com.emeraldblast.p6.message.api.message.protocol.KernelConnectionFileContent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory

@AssistedFactory
interface ChannelProviderFactory {
    fun create(connectFile: KernelConnectionFileContent): ChannelProviderImp
}
