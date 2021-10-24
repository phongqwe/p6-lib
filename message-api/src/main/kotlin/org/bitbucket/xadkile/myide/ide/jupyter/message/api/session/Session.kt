package org.bitbucket.xadkile.myide.ide.jupyter.message.api.session

import java.util.*

class Session(val sessionId: String, val username: String, val key: String) {
    companion object {
        /**
         * [sessionId] autogenerated
         * [username] fetched from system
         */
        fun autoCreate(key: String): Session {
            return Session(
                sessionId = UUID.randomUUID().toString(),
                username = System.getProperty("user.name"),
                key = key
            )
        }
    }
}
