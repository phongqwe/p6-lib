package com.github.xadkile.bicp.message.api.msg.protocol.message.data_interface_definition

import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgContent
import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgMetaData
import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgStatus
import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgType
import com.google.gson.annotations.SerializedName
import java.util.*
import javax.inject.Qualifier

object Shell{
    object Execute{
        object Request : MsgDefinitionEncapsulation{

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
                return this.msgType
            }
        }

        object Reply:MsgDefinitionEncapsulation{

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

            // TODO this is extract from the an actually received message, not from the document
            data class MetaData(
                @SerializedName("started")
                val startedTime: Date,
                @SerializedName("dependencies_met")
                val dependenciesMet: Boolean,
                val engine: String,
                val status: MsgStatus,
            ) : MsgMetaData

            override fun getMsgType2(): MsgType {
                return this.msgType
            }
        }
    }

    object KernelInfo{

        object Request:MsgDefinitionEncapsulation{

            val msgType = MsgType.Shell_kernel_info_request

            class Content : MsgContent

            class MetaData: MsgMetaData

            override fun getMsgType2(): MsgType {
                return msgType
            }
        }
        object Reply:MsgDefinitionEncapsulation{

            val msgType = MsgType.Shell_kernel_info_reply

            class Content(
                status:MsgStatus,
                @SerializedName("protocol_version")
                val protocolVersion:String,
                val implementation:String,
                @SerializedName("implementation_version")
                val implementationVersion:String,
                @SerializedName("language_info")
                val languageInfo:LanguageInfo,
                val banner:String,
                val debugger:Boolean,
                @SerializedName("help_links")
                val helpLinks:List<HelpLink>,
                traceback: List<String>,
                ename:String,
                evalue:String
            ) : MsgContent,CommonReplyContent(status,traceback, ename, evalue)

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

    sealed class CommonReplyContent(
        val status: MsgStatus,
        val traceback: List<String>,
        val ename:String,
        val evalue:String
    )
}
