package com.emeraldblast.p6.test.utils

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.zeromq.*
import java.util.*
import kotlin.concurrent.thread


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class Bench {

    suspend fun susFunc1() {
        delay(2000)
        println("f1")
    }

    suspend fun susFunc2() {
        delay(1000)
        println("f2")
    }

//    @Test
    fun sender(){
        runBlocking {
            val zContext = ZContext()
            val socket = zContext.createSocket(SocketType.DEALER)
            val port = 7777

            socket.connect("tcp://localhost:${port}")

            for (x in 0 .. 10){
                launch (Dispatchers.IO){
                    val msg = ZMsg().apply {
                        add("")
                        add("ABC_${x}")
                    }
                    msg.send(socket)
                    val res=ZMsg.recvMsg(socket)
                    println(res.toString())
                }
            }
        }
    }

    @Test
    fun coroutineAndSusFunctionExample() {
        // all three jobs are performed in the same main thread
        runBlocking {
            // this create a coroutine that run in parallel with the one below
            launch {
                susFunc1()
                println("Done f1 (this is blocked by f1 because f1 is a sus function that block its coroutine)")
                println("coroutine 1 is in: ${Thread.currentThread()}")
                println("======")
            }
            // this create a coroutine runs in parallel along the previous launch
            launch {
                susFunc2()
                println("Done f2(this is blocked by f2 because f2 is a sus function that block its coroutine")
                println("coroutine 2 is in: ${Thread.currentThread()}")
                println("======")
            }
            println("j3")
            println("done job3")
            println("job 3 is in: ${Thread.currentThread()}")
            println("======")
        }
    }

    suspend fun susF3(f:suspend ()->Unit, coroutineScope: CoroutineScope){
        withContext(Dispatchers.IO) {
            launch(Dispatchers.IO)  {
                susFunc1()
                println("f1 is on: ${Thread.currentThread()}")
                println("======")
            }
            launch(Dispatchers.IO)  {
                delay(500)
                println("done f3")
                println("f3 is on: ${Thread.currentThread()}")
                println("======")
            }
        }
    }

    /**
     * suspending vs blocking:
     * suspending = blocking a coroutine, but not the underneath thread
     * blocking = block the underneath thread.
     *
     * A normal function is a blocking function. A suspend function only block a coroutine in which it resides.
     * The point of sus function is to house long-running code, letting such code run in its own coroutine, and not block the thread.
     * Sus functions can only called inside a sus function.
     * All coroutine launchers (async, launch) are sus function.
     * The point of suspending function is that it forces certain action to be run inside a coroutine. This prevents miscalling long-running-action on undesirable thread (such as main thread).
     */

    //    @Test
    fun zmqExample() {
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
            println(
                String.format(
                    "Average temperature for zipcode '%s' was %d.",
                    filter, (total_temp / update_nbr).toInt()
                )
            )
            println(
                String.format(
                    "Average temperature for zipcode '%s' was %d.",
                    filter2, (total_temp2 / update_nbr).toInt()
                )
            )

        }
        weatherServer.interrupt()
    }
}
