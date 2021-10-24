package org.bitbucket.xadkile.taiga.jupyterclient.client.message

import org.bitbucket.xadkile.taiga.TimeUtils
import java.util.*


class MessageHeaderImp(
    private val msgId: String,
    private val msgType: String,
    private val username: String,
    private val sessionId: String,
    private val date: Date,
    private val version: String,
)  : MessageHeader{
    companion object {
        private val MSG_VERSION = "5.3"
        /**
         * [msgId] is auto-generated
         * [date] is now
         * [version] is [MSG_VERSION]
         */
        fun convenientCreate(msgType:String,sessionId:String, username:String):MessageHeaderImp{
            return MessageHeaderImp(
                msgId = UUID.randomUUID().toString(),
                msgType = msgType,
                username=username,
                sessionId=sessionId,
                date = Date(),
                version = MSG_VERSION
            )
        }
    }

    override fun toFacade():MessageHeader.Facade{
        return FacadeImp(
            msgId,
            msgType,
            username,
            sessionId,
            TimeUtils.now.fromDate(date),
            version
        )
    }

    override fun getMsgId(): String {
        return this.msgId
    }

    override fun getMsgType(): String {
        return this.msgType
    }

    override fun getUsername(): String {
        return this.username
    }

    override fun getSessionId(): String {
        return this.sessionId
    }

    override fun getDate(): Date {
        return this.date
    }

    override fun getVersion(): String {
        return this.version
    }

    /**
     * [date] is ISO 8601 date
     */
    private data class FacadeImp (
        val msg_id: String,
        val msg_type: String,
        val username: String,
        val session: String,
        val date: String,
        val version: String,
    ):MessageHeader.Facade{
        private constructor():this("","","","","","")
    }
}
