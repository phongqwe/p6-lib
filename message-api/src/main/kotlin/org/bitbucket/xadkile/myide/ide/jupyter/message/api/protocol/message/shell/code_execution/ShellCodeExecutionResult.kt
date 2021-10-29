package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.shell.code_execution

import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.InMsgContent
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.OutMsgContent
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgStatus


/**
{
"identities": "kernel.86559535-a5da-46bd-861d-a1a74794b9ab.execute_result",
"delimiter": "\u003cIDS|MSG\u003e",
"hmacSig": "0d53a7787a5c10be291dca0c1f82ef8c474037ba79f6d4fdad6a0616cd5a5fd8",
"header": "{\"msg_id\":\"2184d1e6-e43e7950568e7d7b3c207c6e_3\",\"msg_type\":\"execute_result\",\"username\":\"abc\",\"session\":\"2184d1e6-e43e7950568e7d7b3c207c6e\",\"date\":\"2021-10-28T15:35:55.643967Z\",\"version\":\"5.3\"}",
"parentHeader": "{\"msg_id\":\"fb236422-9843-4844-98f6-feabd9c2d094_1\",\"msg_type\":\"execute_request\",\"username\":\"abc\",\"session\":\"fb236422-9843-4844-98f6-feabd9c2d094\",\"date\":\"2021-10-28T15:35:55.000333Z\",\"version\":\"5.3\"}",
"metaData": "{}",
"content": "{\"data\":{\"text/plain\":\"6\"},\"metadata\":{},\"execution_count\":1}",
"buffer": []
}

 */

// TODO complete this
class ShellCodeExecutionResult(
    val data:Map<String,Any>,
    val metadata: Any,
    val executionCount:Int

) : InMsgContent {
    class Facade(
        val status: MsgStatus,
        val execution_count:Int,
        val payload:List<Any>,
        val user_expressions:Map<String,String>
    ):InMsgContent.Facade{
        override fun toModel(): ShellCodeExecutionResult {
            TODO()
        }

    }

    class Payload(
        val source:String,
        val data:Any,
        val start:Int
    )
}
