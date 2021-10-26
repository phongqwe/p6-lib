package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.shell.execute

import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgContent

class ShellCodeExecutionContent(
    val code:String,
    val silent:Boolean,
    val storeHistory:Boolean,
    val userExpressions:Map<String,String>,
    val allowStdin:Boolean,
    val stopOnError:Boolean
) : MsgContent {
    override fun toFacade(): Facade {
        return Facade(
            code, silent, storeHistory, userExpressions, allowStdin, stopOnError
        )
    }

    class Facade(
        val code:String,
        val silent:Boolean,
        val store_history:Boolean,
        val user_expressions:Map<String,String>,
        val allow_stdin:Boolean,
        val stop_on_error:Boolean
    ):MsgContent.Facade
}
