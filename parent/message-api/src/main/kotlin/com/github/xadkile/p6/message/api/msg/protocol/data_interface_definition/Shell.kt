package com.github.xadkile.p6.message.api.msg.protocol.data_interface_definition

import com.github.xadkile.p6.message.api.msg.protocol.MsgContent
import com.github.xadkile.p6.message.api.msg.protocol.MsgMetaData
import com.github.xadkile.p6.message.api.msg.protocol.MsgStatus
import com.github.xadkile.p6.message.api.msg.protocol.MsgType
import com.google.gson.annotations.SerializedName
import java.util.*

object Shell{
    object Execute{
        object Request : MsgDefinitionEncapsulation {

            val msgType = MsgType.Shell_execute_request

            data class Content(
                val code: String,
                val silent: Boolean,
                @SerializedName("store_history")
                val storeHistory: Boolean,
                @SerializedName("user_expressions")
                val userExpressions: Map<String, String>,
                @SerializedName("allow_stdin")
                val allowStdin: Boolean,
                @SerializedName("stop_on_error")
                val stopOnError: Boolean,
            ): MsgContent

            class MetaData : MsgMetaData {}

            override fun getMsgType2(): MsgType {
                return msgType
            }
        }

        object Reply: MsgDefinitionEncapsulation {

            val msgType = MsgType.Shell_execute_reply

            class Content(
                status: MsgStatus,
                @SerializedName("execution_count")
                val executionCount: Int,
                @SerializedName("user_expressions")
                val userExpressions: Map<String, Any>,
                val payload: List<Map<String, Any>>,
                traceback: List<String>,
                ename:String,
                evalue:String
            ) : MsgContent, CommonReplyContent(status,traceback, ename, evalue)

            data class MetaData(
                @SerializedName("started")
                val startedTime: Date,
                @SerializedName("dependencies_met")
                val dependenciesMet: Boolean,
                val engine: String,
                val status: MsgStatus,
            ) : MsgMetaData

            override fun getMsgType2(): MsgType {
                return msgType
            }
        }
    }

    object KernelInfo{

        object Request: MsgDefinitionEncapsulation {

            val msgType = MsgType.Shell_kernel_info_request

            class Content : MsgContent

            class MetaData: MsgMetaData

            override fun getMsgType2(): MsgType {
                return msgType
            }
        }
        object Reply: MsgDefinitionEncapsulation {

            val msgType = MsgType.Shell_kernel_info_reply

            class Content(
                status: MsgStatus,
                @SerializedName("protocol_version")
                val protocolVersion:String,
                val implementation:String,
                @SerializedName("implementation_version")
                val implementationVersion:String,
                @SerializedName("language_info")
                val languageInfo: LanguageInfo,
                val banner:String,
                val debugger:Boolean,
                @SerializedName("help_links")
                val helpLinks:List<HelpLink>,
                traceback: List<String>,
                ename:String,
                evalue:String
            ) : MsgContent, CommonReplyContent(status,traceback, ename, evalue)

            class MetaData: MsgMetaData

            data class HelpLink(
                val text:String,
                val url:String,
            )

            data class LanguageInfo(
                val name:String,
                val version:String,
                val mimetype:String,
                @SerializedName("file_extension")
                val fileExtension:String,
                @SerializedName("pygments_lexer")
                val pygmentsLexer:String,
                @SerializedName("codemirror_mode")
                val codeMirrorMode:Map<String,Any>,
                @SerializedName("nbconvert_exporter")
                val nbConvertExporter:String
            )

            override fun getMsgType2(): MsgType {
                return msgType
            }
        }
    }

    object Introspection{
        // TODO later
    }

    object Completion{
        // TODO later
    }

    object History{
        // TODO later
    }

    object CommInfo{
        // TODO later
    }

    object Comm{
        // comm_open, comm_close,comm_msg
        // TODO later
    }
}
