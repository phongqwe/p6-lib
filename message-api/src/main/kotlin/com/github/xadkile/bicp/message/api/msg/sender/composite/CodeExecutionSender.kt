package com.github.xadkile.bicp.message.api.msg.sender.composite

import com.github.michaelbull.result.*
import com.github.xadkile.bicp.message.api.connection.kernel_context.KernelContextReadOnly
import com.github.xadkile.bicp.message.api.connection.kernel_context.KernelContextReadOnlyConv
import com.github.xadkile.bicp.message.api.msg.listener.MsgHandlers
import com.github.xadkile.bicp.message.api.msg.listener.MsgListener
import com.github.xadkile.bicp.message.api.msg.protocol.message.JPMessage
import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgStatus
import com.github.xadkile.bicp.message.api.msg.protocol.message.data_interface_definition.IOPub
import com.github.xadkile.bicp.message.api.msg.sender.MsgSender
import com.github.xadkile.bicp.message.api.msg.sender.exception.UnableToSendMsgException
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteReply
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteRequest
import com.github.xadkile.bicp.message.api.other.Sleeper
import kotlinx.coroutines.*


typealias ExecuteResult = JPMessage<IOPub.ExecuteResult.MetaData, IOPub.ExecuteResult.Content>

class CodeExecutionSender(
    val kernelContext: KernelContextReadOnlyConv,
    val executeSender:MsgSender<ExecuteRequest, Result<ExecuteReply, Exception>>,
    val ioPubListener: MsgListener
) : MsgSender<ExecuteRequest, Result<ExecuteResult, Exception>> {

    override suspend fun send(
        message: ExecuteRequest,
        dispatcher: CoroutineDispatcher,
    ): Result<ExecuteResult, Exception> {
        return this.checkContextRunningThen {
            var rt: Result<ExecuteResult, Exception>? = null
            // p: config listener
            ioPubListener.also { listener ->
                listener.addHandler(
                    MsgHandlers.withUUID(IOPub.ExecuteResult.msgType) {m,l->
                        val receivedMsg: ExecuteResult = m.toModel()
                        if (receivedMsg.parentHeader == message.header) {
                            rt = Ok(receivedMsg)
                            listener.stop()
                        }
                    }
                )
            }
            ioPubListener.use {
                coroutineScope {
                    // rmd: start the iopub listener, it is on a separated coroutine.
                    val startRs=ioPubListener.start(this, dispatcher)
                    val listenerStartOk = startRs is Ok
                    if(listenerStartOk){
                        launch(dispatcher) {
                            // rmd: wait until ioPubListener to go online
                            Sleeper.waitUntil { ioPubListener.isRunning() }
                            val sendStatus = executeSender.send(message, Dispatchers.Default)
                            val sendOk = sendStatus is Ok
                            val sendRes:Result<MsgStatus,Exception> = if(sendOk){
                                val msgOk: Boolean = sendStatus.get()!!.content.status == MsgStatus.ok
                                if (msgOk) {
                                    Ok(MsgStatus.ok)
                                } else {
                                     Err(UnableToSendMsgException(message))
                                }
                            }else{
                                Err(sendStatus.unwrapError())
                            }
                            if (sendRes is Err) {
                                rt = sendRes
                            }
                        }
                    }else{
                        rt = Err(startRs.unwrapError())
                    }
                }
                Sleeper.waitUntil { rt != null }
                rt!!
            }
        }
    }

    override fun getKernelContext(): KernelContextReadOnly {
        return this.kernelContext
    }
}
