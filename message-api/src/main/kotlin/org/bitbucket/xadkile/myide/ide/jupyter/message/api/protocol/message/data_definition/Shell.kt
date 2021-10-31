package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.data_definition

import com.google.gson.annotations.SerializedName
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgContent
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgMetaData
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgStatus
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgType
import java.util.*

object Shell{
    object ExecuteRequest {
        val msgType = MsgType.Shell_execute_request
        object Out{

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
            ):MsgContent
        }
        object In{
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
            ) : org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgMetaData
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
}
