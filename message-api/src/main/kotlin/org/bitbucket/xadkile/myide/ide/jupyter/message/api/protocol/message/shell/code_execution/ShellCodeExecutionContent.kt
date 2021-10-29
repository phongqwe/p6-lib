package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.shell.code_execution

import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.OutMsgContent

class ShellCodeExecutionContent(
    val code:String,
    val silent:Boolean,
    val storeHistory:Boolean,
    val userExpressions:Map<String,String>,
    val allowStdin:Boolean,
    val stopOnError:Boolean
) : OutMsgContent {
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
    ): OutMsgContent.Facade {
//        override fun toModel(): ShellCodeExecutionContent {
//            return ShellCodeExecutionContent(
//                code,silent,store_history,user_expressions,allow_stdin,stop_on_error
//            )
//        }
    }

//    class InFacadeParser(val gson:Gson) : MsgContentInFacadeParser<Facade>{
//        override fun parse(input: String): Facade {
//            return gson.fromJson(input, Facade::class.java)
//        }
//    }
//
//    class ModelParser: MsgContentInParser<Facade, ShellCodeExecutionContent>{
//        override fun parse(input: Facade): ShellCodeExecutionContent {
//            return input.toModel()
//        }
//    }
}
