package com.github.xadkile.bicp.message.api.msg.sender.composite

import com.github.michaelbull.result.*
import com.github.xadkile.bicp.message.api.connection.kernel_context.KernelContextReadOnlyConv
import com.github.xadkile.bicp.message.api.connection.kernel_context.KernelIsDownException
import com.github.xadkile.bicp.message.api.exception.UnknownException
import com.github.xadkile.bicp.message.api.msg.listener.IOPubListener
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

/**
 * If the kernel die midway, this would wait forever
 */
class CodeExecutionSender(
    val kernelContext: KernelContextReadOnlyConv,
    val executeSender: MsgSender<ExecuteRequest, Result<ExecuteReply, Exception>>,
    val ioPubListener: IOPubListener,
) : MsgSender<ExecuteRequest, Result<ExecuteResult, Exception>> {

    override suspend fun send(
        message: ExecuteRequest,
        dispatcher: CoroutineDispatcher,
    ): Result<ExecuteResult, Exception> {

        if (kernelContext.isNotRunning()) {
            return Err(KernelIsDownException.occurAt(this))
        }

        var rt: Result<ExecuteResult, Exception>? = null

        // p: config listener
        ioPubListener.addHandler(
            MsgHandlers.withUUID(IOPub.ExecuteResult.msgType,
                handlerFunction = { m, l ->
                    val receivedMsg: ExecuteResult = m.toModel()
                    if (receivedMsg.parentHeader == message.header) {
                        rt = Ok(receivedMsg)
                        l.stop()
                    }
                }
            )
        )

        // ph: sending the computing request
        coroutineScope {
            // rmd: start the iopub listener, it is on a separated coroutine.
            val startRs = ioPubListener.start(this, dispatcher)

            // ph: only send the message if the ioPubListener was started successfully
            if (startRs is Ok) {
                // rmd: wait until ioPubListener to go online
                Sleeper.waitUntil { ioPubListener.isRunning() }
                launch(dispatcher) {
                    val sendStatus = executeSender.send(message, dispatcher)
                    if(sendStatus is Ok){
                        val msgIsOk: Boolean = sendStatus.get()!!.content.status == MsgStatus.ok
                        if(msgIsOk.not()){
                            rt = Err(UnableToSendMsgException(message))
                            ioPubListener.stop()
                        }
                    }else{
                        rt=Err(sendStatus.unwrapError())
                        ioPubListener.stop()
                    }
                }
            } else {
                rt = Err(startRs.unwrapError())
                ioPubListener.stop()
            }
        }

        // ph: this ensure that this sender will wait until either rt has a result or the kernel died
        Sleeper.waitUntil {
            val hasResult = (rt != null)
            val kernelDie = kernelContext.getConvHeartBeatService().unwrap().isHBAlive().not()
            val kernelTurnIdle= false  // TODO add code to listen to event when kernel status turn to idle
            hasResult  || kernelDie || kernelTurnIdle
        }

        ioPubListener.stop()

        if(rt!=null){
            return rt!!
        }
        else{
            if(kernelContext.isNotRunning()){
                return Err(KernelIsDownException.occurAt(this))
            }else{
                return Err(UnknownException.occurAt(this))
            }
        }
    }
}
