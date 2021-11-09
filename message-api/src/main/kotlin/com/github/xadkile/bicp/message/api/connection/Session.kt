package com.github.xadkile.bicp.message.api.connection

/**
 * Provide user name, encryption key, and session id. These are for making message
 */
interface Session {
    fun getUserName():String
    fun getKey():String
    fun getSessionId():String
}
