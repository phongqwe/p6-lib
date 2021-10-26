package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.shell.execute

import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgStatus

class ShellCodeExecutionReply {
    class Facade(
        val status: MsgStatus,
        val execution_count:Int,
        val payload:List<Any>,
        val user_expressions:Map<String,String>
    )

    class Payload(
        val source:String,
        val data:Any,
        val start:Int
    )
}
