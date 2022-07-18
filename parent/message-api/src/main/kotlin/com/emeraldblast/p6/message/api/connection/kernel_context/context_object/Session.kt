package com.emeraldblast.p6.message.api.connection.kernel_context.context_object

/**
 * Contain ipython session info
 */
interface Session {
    fun getSystemUserName():String
    fun getKey():String
    fun getSessionId():String
}
