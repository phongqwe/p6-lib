package org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.message.data_interface_definition

import com.google.gson.annotations.SerializedName
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.message.MsgContent
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.message.MsgMetaData
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.message.MsgStatus
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.message.MsgType
import java.util.*
import javax.inject.Qualifier

object Shell{
    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Address

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Channel

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
            ): MsgContent
            class MetaData : MsgMetaData{}
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
}
