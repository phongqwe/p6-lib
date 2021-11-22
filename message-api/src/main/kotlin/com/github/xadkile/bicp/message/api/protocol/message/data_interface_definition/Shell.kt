package com.github.xadkile.bicp.message.api.protocol.message.data_interface_definition

import com.github.xadkile.bicp.message.api.protocol.message.MsgContent
import com.github.xadkile.bicp.message.api.protocol.message.MsgMetaData
import com.github.xadkile.bicp.message.api.protocol.message.MsgStatus
import com.github.xadkile.bicp.message.api.protocol.message.MsgType
import com.google.gson.annotations.SerializedName
import java.util.*
import javax.inject.Qualifier

object Shell{

    object ExecuteRequest {
        val msgType = MsgType.Shell_execute_request
        object Input{
            class Content(
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
            class MetaData : MsgMetaData{}
        }
        object Output{
            class Content(
                val status: MsgStatus,
                @SerializedName("execution_count")
                val executionCount: Int,
                @SerializedName("user_expressions")
                val userExpressions: Map<String, Any>,
                val payload: List<Map<String, Any>>,
            ) : MsgContent

            class MetaData(
                @SerializedName("started")
                val startedTime: Date,
                @SerializedName("dependencies_met")
                val dependencyMet: Boolean,
                val engine: String,
                val status: MsgStatus,
            ) : MsgMetaData
        }
    }
    object ExecuteReply{
        val msgType = MsgType.Shell_execute_reply
        /**
        {
        "metaData": "{}",
        "content": "{\"data\":{\"text/plain\":\"6\"},\"metadata\":{},\"execution_count\":1}",
        "buffer": []
        }
         */
        class Content(
            val data:Map<String,Any>,
            @SerializedName("metadata")
            val metaData: Any,
            @SerializedName("execution_count")
            val executionCount:Int
        ) : MsgContent
        class MetaData : MsgMetaData
    }
    object KernelInfo{

        object Request{
            val msgType = MsgType.Shell_kernel_info_request
            class Content : MsgContent{}
            class MetaData: MsgMetaData{}
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
            ) : MsgContent{}
            class MetaData: MsgMetaData{}
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
