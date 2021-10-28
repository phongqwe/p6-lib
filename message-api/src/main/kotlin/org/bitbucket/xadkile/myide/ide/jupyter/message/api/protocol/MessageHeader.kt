package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import org.bitbucket.xadkile.myide.common.TimeUtils
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgType
import java.text.ParseException
import java.util.*


class MessageHeader(
    private val msgId: String,
    private val msgType: MsgType,
    private val username: String,
    private val sessionId: String,
    private val date: Date,
    private val version: String,
) {
    companion object {
        private val MSG_VERSION = "5.3"

        /**
         * [date] is now
         * [version] is [MSG_VERSION]
         * [msgId] is created as "sessionId_<universalCounter>"
         */
        fun autoCreate(
            msgType: MsgType,
            msgId: String,
            sessionId: String,
            username: String
        ): MessageHeader {
            return MessageHeader(
                msgId = msgId,
                msgType = msgType,
                username = username,
                sessionId = sessionId,
                date = Date(),
                version = MSG_VERSION
            )
        }
    }

    fun getMsgId(): String {
        return this.msgId
    }

    fun toFacade(): Facade {
        return Facade(
            msgId,
            msgType.toString(),
            username,
            sessionId,
            TimeUtils.now.fromDate(date),
            version
        )
    }

    /**
     * [date] is ISO 8601 date
     * this need to implement an interface, so that JsonEmptyObj (aslo implement such interface) can be assigned to them
     *
     */
    data class Facade(
        val msg_id: String,
        val msg_type: String,
        val username: String,
        val session: String,
        val date: String,
        val version: String,
    ) {
        private constructor() : this("", "", "", "", "", "")

        companion object {
//            val empty = Facade(null, null, null, null, null, null)
        }

        // TODO this need more refining on the null value
        fun toModel() :Result<MessageHeader,Exception>{
            val timeResult: Result<Date, ParseException> = TimeUtils.parseJupyterTime(this.date)
            val rt = timeResult.andThen { date ->
                val msgTypeEither: Result<MsgType, IllegalArgumentException> = MsgType.parse(msg_type ?: "")
                msgTypeEither.andThen { msgType ->
                    Ok(
                        MessageHeader(
                            msgId = msg_id ?: "",
                            msgType = msgType,
                            username = this.username ?: "",
                            sessionId = this.session ?: "",
                            date = date,
                            version = version ?: ""
                        )
                    )
                }
            }
            return rt
        }
    }
}
