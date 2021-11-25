package com.github.xadkile.bicp.test.utils

import com.github.michaelbull.result.unwrap
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteReply
import com.github.xadkile.bicp.message.api.msg.sender.shell.KernelInfoInput
import com.github.xadkile.bicp.message.api.msg.protocol.message.JPRawMessage
import com.github.xadkile.bicp.message.api.msg.protocol.message.data_interface_definition.IOPub
import com.github.xadkile.bicp.message.api.msg.protocol.message.data_interface_definition.Shell
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteRequest
import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.zeromq.*
import java.util.*
import kotlin.concurrent.thread

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class Bench : TestOnJupyter() {
    /**
     * DetatchedRunnable = autonomous thread. Have no context, does not return any PAIR socket
     * AttachedRunnable = have context, return a PAIR socket (for what?)
     * The PAIR socket AttachedRunnable returns is a "bind" socket.
     * PAIR sockets are bidirectional.
     * This way, I can write to the bind socket, so that anything listening to this will know if something happend.
     * For example:
     *      Within an AttachedRunnable, I do some work, and write the result out to the connect socket
     *      The externals code use listen using the bind socket and get the output out. Or something.
     * The socket address is: "inproc://zctx-pipe-{socket-hashCode}"
     * So AttachedRunnable is for broadcasting receiving information to the outside of its thread usig socket as the transportation channel
     *
     * For DettachedRunnable, I can accomplished the same thing by passing object reference to DettachedRunnable.
     * But I need to make sure that this is safe. Sharing state is dangerous.
     * ======
     * for io pub/sub
     * I will need to run a long running background service that constantly listen to the iopub channel.
     * When I receive a message, I will need to filter and route it to where it should be.
     * I need to define a strict set of criteria so that the message is sent to the appropriate handler
     * 1. Message type:
     *  - IOPub publish several type of events. Each type should have 1 lv0 handler
     *  - For each event type, there maybe multiple sub target or lv1 handler. The lv0 handler should handle routing message to the correct lv1 handler.
     *
     *  I need to study JP message document and write down the event type + sub type.
     *  So, I can have 1 central hub of IOPUb listener.
     *  Then use the identity field to route msg to the correct handler:
     *  eg:
     *  - identity with "execute_result" should go to executeResult handler, to display the result on the UI view or something.
     *  - identity with "status" should go to status handler service, so that it can provide kernel status to other components.
     *
     *
     */
    class ZListener(val subSocket: ZMQ.Socket) : ZThread.IDetachedRunnable {

        override fun run(args: Array<out Any>?) {
            while (true) {
                val msg = ZMsg.recvMsg(subSocket)
                val rawMsg = JPRawMessage.fromPayload(msg.map { it.data }).unwrap()
                if (rawMsg.identities.contains("execute_result")) {
                    val md = rawMsg.toModel<IOPub.ExecuteResult.MetaData, IOPub.ExecuteResult.Content>()
                    println(md)
                } else {
                    println(rawMsg)
                }
            }
        }
    }

    class ZListenerAttached : ZThread.IAttachedRunnable {
        override fun run(args: Array<out Any>, ctx: ZContext, connectPipe: ZMQ.Socket) {
            var c = 0
            val poller = ctx.createPoller(1)
            poller.register(connectPipe, ZMQ.Poller.POLLIN)
//            while (c<100) {
//                connectPipe.send("" + c)
//                c++
//            }
            while (true) {
                poller.poll(1000)
                if (poller.pollin(0)) {
                    println("Z+:" + connectPipe.recvStr())
                }
            }
        }
    }

    @Test
    fun z5(){


    }

    @Test
    fun z4(){

        runBlocking {
            println(this.hashCode())
            coroutineScope {
                println(this.hashCode())
            }
            delay(1000)
            println("END")
        }
    }
    @Test
    fun z3() {
        runBlocking {
            // coroutineScope only ends when all the launch(es) are completed
            coroutineScope {
                launch {
                    delay(2000L)
                    println("World 2")
                }
                launch {
                    delay(1000L)
                    println("World 1")
                }
                println("Hello")
            }
            // this only run after the coroutineScope above finish with all of its tasks.
            println("End")
        }
    }




    @Test
    fun z2() {
        val message: ExecuteReply = ExecuteRequest.autoCreate(
            sessionId = "session_id",
            username = "user_name",
            msgType = Shell.Execute.msgType,
            msgContent = Shell.Execute.Request.Content(
                code = "x=1+1*2;y=x*2;y",
                silent = false,
                storeHistory = true,
                userExpressions = mapOf(),
                allowStdin = false,
                stopOnError = true
            ),
            "msg_id_abc_123"
        )

        val messageKI: KernelInfoInput = KernelInfoInput.autoCreate(
            sessionId = "session_id",
            username = "user_name",
            msgType = Shell.KernelInfo.Request.msgType,
            msgContent = Shell.KernelInfo.Request.Content(),
            "msg_id_abc_123"
        )
        this.ipythonContext.startIPython()


        val sender = this.ipythonContext.getSenderProvider().unwrap().getExecuteRequestSender()
        val o = sender.send(message)

    }

    @Test
    fun z() {
        val weatherServer = thread(true) {
            ZContext().use { context ->
                val publisher = context.createSocket(SocketType.PUB)
                publisher.bind("tcp://*:5556")
                publisher.bind("ipc://weather")

                //  Initialize random number generator
                val srandom = Random(System.currentTimeMillis())
                while (!Thread.currentThread().isInterrupted) {
                    //  Get values that will fool the boss
                    var zipcode: Int
                    var temperature: Int
                    var relhumidity: Int
                    zipcode = 10000 + srandom.nextInt(10000)
                    temperature = srandom.nextInt(215) - 80 + 1
                    relhumidity = srandom.nextInt(50) + 10 + 1

                    //  Send message to all subscribers
                    val update = String.format(
                        "%05d %d %d", zipcode, temperature, relhumidity
                    )
                    publisher.send(update, 0)
                }
            }
        }


        // client
        ZContext().use { context ->
            //  Socket to talk to server
            println("Collecting updates from weather server")
            val subscriber = context.createSocket(SocketType.SUB)
            val subscriber2 = context.createSocket(SocketType.SUB)
            subscriber.connect("tcp://localhost:5556")
            subscriber2.connect("tcp://localhost:5556")

            //  Subscribe to zipcode, default is NYC, 10001
            val filter = "10001 "
            val filter2 = "10002 "
            subscriber.subscribe(filter.toByteArray(ZMQ.CHARSET))
            subscriber2.subscribe(filter2.toByteArray(ZMQ.CHARSET))

            //  Process 100 updates
            var update_nbr: Int
            var total_temp: Long = 0
            var total_temp2: Long = 0

            val items = context.createPoller(2)
            items.register(subscriber, ZMQ.Poller.POLLIN)
            items.register(subscriber2, ZMQ.Poller.POLLIN)

            update_nbr = 0
            while (update_nbr < 100) {
                //  Use trim to remove the tailing '0' character
                items.poll()
                if (items.pollin(0)) {
                    val string = subscriber.recvStr(0).trim { it <= ' ' }
                    val sscanf = StringTokenizer(string, " ")
                    val zipcode = Integer.valueOf(sscanf.nextToken())
                    val temperature = Integer.valueOf(sscanf.nextToken())
                    val relhumidity = Integer.valueOf(sscanf.nextToken())
                    total_temp += temperature.toLong()
                    update_nbr++
                }

                if (items.pollin(2)) {
                    val string = subscriber2.recvStr(0).trim { it <= ' ' }
                    val sscanf = StringTokenizer(string, " ")
                    val zipcode = Integer.valueOf(sscanf.nextToken())
                    val temperature = Integer.valueOf(sscanf.nextToken())
                    val relhumidity = Integer.valueOf(sscanf.nextToken())
                    total_temp2 += temperature.toLong()
                    update_nbr++
                }
            }
            println(String.format(
                "Average temperature for zipcode '%s' was %d.",
                filter, (total_temp / update_nbr).toInt()))
            println(String.format(
                "Average temperature for zipcode '%s' was %d.",
                filter2, (total_temp2 / update_nbr).toInt()))

        }
        weatherServer.interrupt()
    }
}
