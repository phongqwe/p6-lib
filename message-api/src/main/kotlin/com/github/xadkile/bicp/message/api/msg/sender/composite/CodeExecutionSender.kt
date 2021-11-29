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
 * TODO This is essentially a state machine, think of a way to structure it better. The current structure is messy.
 * TODO make it clearer what exception is returned and when they are returned
 * The state machine revolves around:
 *  - the status of the kernel: idle, busy
 *  - the status of heart beat channel: on or off
 *  - the status of return value: null or not null
 *  - the status of the listener: running or not
 *  anything else?
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
            MsgHandlers.withUUID(IOPub.ExecuteResult.msgType) { m, l ->
                val receivedMsg: ExecuteResult = m.toModel()
                if (receivedMsg.parentHeader == message.header) {
                    rt = Ok(receivedMsg)
//                    l.stop()
                }
            }
        )
        ioPubListener.addHandler(
            // ph: stop when computing status become "idle"
            MsgHandlers.withUUID(IOPub.Status.msgType) { m,l->
                val msg:JPMessage<IOPub.Status.MetaData, IOPub.Status.Content> = m.toModel()
                if(msg.parentHeader == message.header){
                    if(msg.content.executionState == IOPub.Status.ExecutionState.idle){
                        println("Reach idle state-> stop")
                        l.stop()
                    }
                }
            }
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
                    if (sendStatus is Ok) {
                        val msgIsOk: Boolean = sendStatus.get()!!.content.status == MsgStatus.ok
                        if (msgIsOk.not()) {
                            rt = Err(UnableToSendMsgException(message))
                            ioPubListener.stop()
                        }
                    } else {
                        rt = Err(sendStatus.unwrapError())
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
            val ioPubListenerIsStopped = ioPubListener.isRunning().not()
            hasResult || kernelDie || ioPubListenerIsStopped
        }

        ioPubListener.stop()

        if (rt != null) {
            return rt!!
        } else {
            if (kernelContext.isNotRunning()) {
                return Err(KernelIsDownException.occurAt(this))
            } else {
                return Err(UnknownException.occurAt(this))
            }
        }
    }
}
