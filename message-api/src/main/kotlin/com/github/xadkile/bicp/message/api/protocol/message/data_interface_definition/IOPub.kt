package com.github.xadkile.bicp.message.api.protocol.message.data_interface_definition

import com.github.xadkile.bicp.message.api.protocol.message.MsgContent
import com.github.xadkile.bicp.message.api.protocol.message.MsgMetaData
import com.github.xadkile.bicp.message.api.protocol.message.MsgType
import com.google.gson.annotations.SerializedName
import javax.inject.Qualifier

object IOPub {
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
