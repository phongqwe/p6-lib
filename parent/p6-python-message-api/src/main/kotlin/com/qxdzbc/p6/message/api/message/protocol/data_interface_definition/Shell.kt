package com.qxdzbc.p6.message.api.message.protocol.data_interface_definition

import com.qxdzbc.p6.message.api.message.protocol.*
import com.qxdzbc.p6.message.api.message.sender.exception.SenderErrors
import com.google.gson.annotations.SerializedName
import java.util.*

object Shell{
    object Execute{
        object Request : MsgDefinitionEncapsulation {

            override val msgType = MsgType.Shell_execute_request

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

            class MetaData : MapMsgMetaData() {}


        }

        object Reply: MsgDefinitionEncapsulation {

            override val msgType = MsgType.Shell_execute_reply

            open class Content(
                status: MsgStatus = MsgStatus.OK,
                @SerializedName("execution_count")
                val executionCount: Int = 0,
                @SerializedName("user_expressions")
                val userExpressions: Map<String, Any> = emptyMap(),
                val payload: List<Map<String, Any>> = emptyList(),
                traceback: List<String> = emptyList(),
                ename:String = "",
                evalue:String = "",
            ) : MsgContent, CommonReplyContent(status,traceback, ename, evalue)

            data class MetaData(
                @SerializedName("started")
                val startedTime: Date,
                @SerializedName("dependencies_met")
                val dependenciesMet: Boolean,
                val engine: String,
                val status: MsgStatus,
            ) : MsgMetaData


        }
    }

    object KernelInfo{

        object Request: MsgDefinitionEncapsulation {

            override val msgType = MsgType.Shell_kernel_info_request

            class Content : MsgContent

            class MetaData: MapMsgMetaData()


        }
        object Reply: MsgDefinitionEncapsulation {

            override val msgType = MsgType.Shell_kernel_info_reply

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

            class MetaData: MapMsgMetaData()

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

        /**
         * comm_open
         */
        object Open :MsgDefinitionEncapsulation{
            override val msgType: MsgType = MsgType.Shell_comm_open
            class MetaData: MapMsgMetaData()
            class Content(
                @SerializedName("comm_id")
                val commId:String,
                @SerializedName("target_name")
                val targetName:String,
                // data = any json for init the comm
                val data:Any
            ) : MsgContent

        }

        /**
         * comm_close
         */
        object Close:MsgDefinitionEncapsulation{
            override val msgType: MsgType = MsgType.Shell_comm_close
            class MetaData: MapMsgMetaData()
            class Content(
                @SerializedName("comm_id")
                val commId:String,
                @SerializedName("target_name")
                val data:Any
            ) : MsgContent
        }
        /**
         * comm_msg
         */
        object Msg:MsgDefinitionEncapsulation{
            override val msgType: MsgType = MsgType.Shell_comm_msg
            class MetaData: MapMsgMetaData()
            class Content(
                @SerializedName("comm_id")
                val commId:String,
                @SerializedName("target_name")
                val data:Any
            ) : MsgContent
        }
    }
}
