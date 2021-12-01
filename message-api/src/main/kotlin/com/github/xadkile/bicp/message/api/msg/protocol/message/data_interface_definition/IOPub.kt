package com.github.xadkile.bicp.message.api.msg.protocol.message.data_interface_definition

import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgContent
import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgMetaData
import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgType
import com.google.gson.annotations.SerializedName

object IOPub {

    object Error : MsgDefinitionEncapsulation{
        val msgType = MsgType.IOPub_error

        // TODO this structure is extract from real message, not from document
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

    object Status:MsgDefinitionEncapsulation{
        val msgType = MsgType.IOPub_status

        /**
         * When the kernel starts to handle a message, it will enter the 'busy'
         * state and when it finishes, it will enter the 'idle' state.
         * The kernel will publish state 'starting' exactly ONCE at process startup.
         */
        data class Content(
            @SerializedName("execution_state")
            val executionState: ExecutionState
        ) : MsgContent

        class MetaData() : MsgMetaData

        enum class ExecutionState{
            busy, idle, starting
        }

        override fun getMsgType2(): MsgType {
            return msgType
        }
    }
    object ExecuteResult:MsgDefinitionEncapsulation{

        val msgType = MsgType.IOPub_execute_result

        data class Content(
            val data: Map<String, Any>,
            @SerializedName("metadata")
            val metaData: Map<String, Any>,
            val transient: Map<String, Any>,
            @SerializedName("execution_count")
            val executionCount:Int
        ) :MsgContent{
            fun getTextPlain():String?{
                return data["text/plain"]?.toString()
            }
        }

        class MetaData:MsgMetaData

        override fun getMsgType2(): MsgType {
            return msgType
        }
    }

    object DisplayData:MsgDefinitionEncapsulation {

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
}
