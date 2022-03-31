package com.emeraldblast.p6.message.api.connection.kernel_context.context_object

/**
 * Provide username, encryption key, and session id. These are for making message
 */
interface Session {
    fun getSystemUserName():String
    fun getKey():String
    fun getSessionId():String
}
