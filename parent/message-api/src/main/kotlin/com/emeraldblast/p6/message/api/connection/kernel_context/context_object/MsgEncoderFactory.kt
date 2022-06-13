package com.emeraldblast.p6.message.api.connection.kernel_context.context_object

import dagger.assisted.AssistedFactory

@AssistedFactory
interface MsgEncoderFactory{
    fun create(keyStr: String): MsgEncoderImp
}
