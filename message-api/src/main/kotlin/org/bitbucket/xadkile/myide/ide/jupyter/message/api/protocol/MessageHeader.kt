package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol

import org.bitbucket.xadkile.myide.common.TimeUtils
import java.util.*


class MessageHeader(
    private val msgId: String,
    private val msgType: String,
    private val username: String,
    private val sessionId: String,
    private val date: Date,
    private val version: String,
)  {
    companion object {
        private val MSG_VERSION = "5.3"
        /**
         * [msgId] is auto-generated
         * [date] is now
         * [version] is [MSG_VERSION]
         */
        fun autoCreate(msgType:String, sessionId:String, username:String): MessageHeader {
            return MessageHeader(
                msgId = UUID.randomUUID().toString(),
                msgType = msgType,
                username=username,
                sessionId=sessionId,
                date = Date(),
                version = MSG_VERSION
            )
        }
    }

    fun toFacade(): Facade {
        return Facade(
            msgId,
            msgType,
            username,
            sessionId,
            TimeUtils.now.fromDate(date),
            version
        )
    }

    /**
     * [date] is ISO 8601 date
     * this need to implement an interface, so that JsonEmptyObj (aslo implement such interface) can be assigned to them
     */
    data class Facade (
        val msg_id: String?,
        val msg_type: String?,
        val username: String?,
        val session: String?,
        val date: String?,
        val version: String?,
    ) {
        private constructor():this("","","","","","")
        companion object {
            val empty = Facade(null,null,null,null,null,null)
        }
    }
}
