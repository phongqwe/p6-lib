package com.github.xadkile.p6.app.coderunner

import com.github.michaelbull.result.*
import com.github.xadkile.p6.app.context.AppContext
import com.github.xadkile.p6.formula.translator.ScriptFormulaTranslator
import com.github.xadkile.p6.formula.translator.SumFormulaTranslator
import com.github.xadkile.p6.message.api.connection.kernel_context.KernelContextReadOnlyConv
import com.github.xadkile.p6.message.api.msg.protocol.data_interface_definition.Shell
import com.github.xadkile.p6.message.api.msg.sender.shell.ExecuteRequest
import kotlinx.coroutines.CoroutineDispatcher
import java.util.*

class CodeRunnerImp(private val appContext: AppContext) : CodeRunner {

    override suspend fun run(code: String, dispatcher: CoroutineDispatcher): Result<String, Exception> {
        val kernelContext: KernelContextReadOnlyConv = appContext.getKernelContextReadOnly()
        val senderRs = kernelContext.getSenderProvider().map { it.codeExecutionSender() }
        val rt2 = senderRs.mapBoth(
            success = { sender ->
                val sumTrans = SumFormulaTranslator()
                val codeTrans = ScriptFormulaTranslator()
                val sumTransRs = sumTrans.translate(code)
                val codeTransRs = codeTrans.translate(code)
                var parsedText = code
                if (sumTransRs is Ok) {
                    parsedText = sumTransRs.value
                }

                if (codeTransRs is Ok) {
                    parsedText = codeTransRs.value
                }

                val message: ExecuteRequest = ExecuteRequest.autoCreate(
                    sessionId = kernelContext.getSession().unwrap().getSessionId(),
                    username = "user_name", // TODO add a mechanism to get the user name of the current user. AppContext or something
                    msgType = Shell.Execute.Request.msgType,
                    msgContent = Shell.Execute.Request.Content(
                        code = parsedText,
                        silent = false,
                        storeHistory = true,
                        userExpressions = mapOf(),
                        allowStdin = false,
                        stopOnError = true
                    ),
                    kernelContext.getMsgIdGenerator().map { it.next() }.get() ?: UUID.randomUUID().toString()
                )
                val o = sender.send(message, dispatcher)

                val rt = if (o is Ok) {
                    Ok(o.get()?.content?.getTextPlain() ?: "")
                } else {
                    Err(o.unwrapError())
                }
                rt
            },
            failure = { Err(it) }
        )
        return rt2
    }
}
