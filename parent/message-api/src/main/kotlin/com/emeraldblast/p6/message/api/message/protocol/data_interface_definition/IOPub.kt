package com.emeraldblast.p6.message.api.message.protocol.data_interface_definition

import com.emeraldblast.p6.message.api.message.protocol.MapMsgMetaData
import com.emeraldblast.p6.message.api.message.protocol.MsgContent
import com.emeraldblast.p6.message.api.message.protocol.MsgType
import com.emeraldblast.p6.message.api.message.sender.exception.SenderErrors
import com.google.gson.annotations.SerializedName

object IOPub {

    object ExecuteError : MsgDefinitionEncapsulation {
        override val msgType = MsgType.IOPub_error

        open class Content(
            val traceback: List<String>,
            val ename:String,
            val evalue:String
        ) : MsgContent

        class MetaData : MapMsgMetaData()

    }

    object Status: MsgDefinitionEncapsulation {
        override val msgType = MsgType.IOPub_status

        /**
         * busy: When the kernel starts to handle a message.
         * idle: when it finishes running code
         * starting: The kernel will publish state 'starting' exactly ONCE at process startup.
         */
        data class Content(
            @SerializedName("execution_state")
            val executionState: ExecutionState
        ) : MsgContent

        class MetaData() : MapMsgMetaData()

        enum class ExecutionState{
            busy, idle, starting, undefined
        }

    }
    object ExecuteResult: MsgDefinitionEncapsulation {

        override val msgType = MsgType.IOPub_execute_result

        data class Content(
            val data: Map<String, Any>,
            @SerializedName("metadata")
            val metaData: Map<String, Any>,
            val transient: Map<String, Any>,
            @SerializedName("execution_count")
            val executionCount:Int
        ) : MsgContent {
            fun getTextPlain():String?{
                return data["text/plain"]?.toString()
            }
        }

        class MetaData: MapMsgMetaData()

    }

    object DisplayData: MsgDefinitionEncapsulation {

        override val msgType = MsgType.IOPub_display_data

        class Content(
            val data: Map<String, Any>,
            @SerializedName("metadata")
            val metaData: Map<String, Any>,
            val transient: Map<String, Any>
        ) : MsgContent
        class MetaData : MapMsgMetaData()
    }

    object Stream {
        // TODO later
    }

    object UpdateDisplayData{
        // TODO later
    }

    object CodeInput{
        // TODO later
    }

    object ClearOutput{
        // TODO later
    }

    object DebugEvent{
        // TODO later
    }

    object Comm{
        // comm_open, comm_close, comm_msg
        // TODO later
    }
}
