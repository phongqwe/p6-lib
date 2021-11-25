package com.github.xadkile.bicp.message.api.msg.protocol.message.data_interface_definition

import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgContent
import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgMetaData
import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgStatus
import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgType
import com.google.gson.annotations.SerializedName
import java.util.*
import javax.inject.Qualifier

object Shell{

    object Execute {

        val msgType = MsgType.Shell_execute_request

        object Request{
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
        }

        object Reply{
            data class Content(
                val status: MsgStatus,
                @SerializedName("execution_count")
                val executionCount: Int,
                @SerializedName("user_expressions")
                val userExpressions: Map<String, Any>,
                val payload: List<Map<String, Any>>,
            ) : MsgContent

            data class MetaData(
                @SerializedName("started")
                val startedTime: Date,
                @SerializedName("dependencies_met")
                val dependencyMet: Boolean,
                val engine: String,
                val status: MsgStatus,
            ) : MsgMetaData
        }
    }

    object KernelInfo{
        object Request{
            val msgType = MsgType.Shell_kernel_info_request
            class Content : MsgContent
            class MetaData: MsgMetaData
        }
        object Reply{
            val msgType = MsgType.Shell_kernel_info_reply

            data class Content(
                val status:MsgStatus,
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
                val helpLinks:List<HelpLink>
            ) : MsgContent

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
        }
    }
}
