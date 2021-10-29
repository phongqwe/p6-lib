package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.data_definition.shell.code_execution

import com.google.gson.annotations.SerializedName
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgContentIn
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgContentOut
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgStatus
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.rin.MetaDataIn
import java.util.*

class ShellCodeExecutionContent(
    val code: String,
    val silent: Boolean,
    val storeHistory: Boolean,
    val userExpressions: Map<String, String>,
    val allowStdin: Boolean,
    val stopOnError: Boolean,
) : MsgContentOut {
    override fun toFacade(): Facade {
        return Facade(
            code, silent, storeHistory, userExpressions, allowStdin, stopOnError
        )
    }

    class Facade(
        val code: String,
        val silent: Boolean,
        val store_history: Boolean,
        val user_expressions: Map<String, String>,
        val allow_stdin: Boolean,
        val stop_on_error: Boolean,
    ) : MsgContentOut.Facade {
    }

    class ResponseContentIn(
        val status: MsgStatus,
        @SerializedName("execution_count")
        val executionCount: Int,
        @SerializedName("user_expressions")
        val userExpressions: Map<String, Any>,
        val payload: List<Map<String,Any>>,
    ) : MsgContentIn,MsgContentIn.Facade<ResponseContentIn> {
        override fun toModel(): ResponseContentIn {
            return this
        }
    }

    class ResponseMetaDataIn(
        @SerializedName("started")
        val startedTime: Date,
        @SerializedName("dependencies_met")
        val dependencyMet: Boolean,
        val engine: String,
        val status: MsgStatus,
    ) : MetaDataIn,MetaDataIn.InFacade<ResponseMetaDataIn> {
        override fun toModel(): ResponseMetaDataIn {
            return this
        }
    }
}
