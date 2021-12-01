package com.github.xadkile.bicp.message.api.msg.protocol.message.data_interface_definition

import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgContent
import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgMetaData
import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgStatus
import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgType
import com.google.gson.annotations.SerializedName
import javax.inject.Qualifier

object IOPub {

    object Error {
        val msgType = MsgType.IOPub_error
//        data class Content():MsgContent {
//
//        }
    }

    object Status{
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
    }
    object ExecuteResult{
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
    }

    object DisplayData {
        val msgType = MsgType.IOPub_display_data
        class Content(
            val data: Map<String, Any>,
            @SerializedName("metadata")
            val metaData: Map<String, Any>,
            val transient: Map<String, Any>
        ) : MsgContent
    }
}
