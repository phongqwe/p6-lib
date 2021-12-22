package com.github.xadkile.p6.message.api.connection.kernel_context.context_object

/**
 * Provide user name, encryption key, and session id. These are for making message
 */
interface Session {
    fun getUserName():String
    fun getKey():String
    fun getSessionId():String
}
