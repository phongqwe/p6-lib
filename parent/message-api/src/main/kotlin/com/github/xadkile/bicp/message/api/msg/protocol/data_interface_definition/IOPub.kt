package com.github.xadkile.bicp.message.api.msg.protocol.data_interface_definition

import com.github.xadkile.bicp.message.api.msg.protocol.MsgContent
import com.github.xadkile.bicp.message.api.msg.protocol.MsgMetaData
import com.github.xadkile.bicp.message.api.msg.protocol.MsgType
import com.google.gson.annotations.SerializedName

object IOPub {

    object ExecuteError : MsgDefinitionEncapsulation {
        val msgType = MsgType.IOPub_error

        class Content(
            val traceback: List<String>,
            val ename:String,
            val evalue:String
        ) : MsgContent

        class MetaData : MsgMetaData

        override fun getMsgType2(): MsgType {
            return msgType
        }
    }

    object Status: MsgDefinitionEncapsulation {
        val msgType = MsgType.IOPub_status

        /**
         * busy: When the kernel starts to handle a message.
         * idle: when it finishes running code
         * starting: The kernel will publish state 'starting' exactly ONCE at process startup.
         */
        data class Content(
            @SerializedName("execution_state")
            val executionState: ExecutionState
        ) : MsgContent

        class MetaData() : MsgMetaData

        enum class ExecutionState{
            busy, idle, starting, undefined
        }

        override fun getMsgType2(): MsgType {
            return msgType
        }
    }
    object ExecuteResult: MsgDefinitionEncapsulation {

        val msgType = MsgType.IOPub_execute_result

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

        class MetaData: MsgMetaData

        override fun getMsgType2(): MsgType {
            return msgType
        }
    }

    object DisplayData: MsgDefinitionEncapsulation {

        val msgType = MsgType.IOPub_display_data

        class Content(
            val data: Map<String, Any>,
            @SerializedName("metadata")
            val metaData: Map<String, Any>,
            val transient: Map<String, Any>
        ) : MsgContent

        override fun getMsgType2(): MsgType {
            return msgType
        }
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
