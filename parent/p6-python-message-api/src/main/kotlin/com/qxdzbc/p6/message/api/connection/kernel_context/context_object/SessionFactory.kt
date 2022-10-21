package com.qxdzbc.p6.message.api.connection.kernel_context.context_object

import dagger.assisted.AssistedFactory

@AssistedFactory
interface SessionFactory{
    fun create(key: String): SessionImp
}
