package com.github.xadkile.bicp.test.utils

import com.github.michaelbull.result.unwrap
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteRequest
import com.github.xadkile.bicp.message.api.msg.sender.shell.KernelInfoInput
import com.github.xadkile.bicp.message.api.msg.protocol.message.JPRawMessage
import com.github.xadkile.bicp.message.api.msg.protocol.message.data_interface_definition.IOPub
import com.github.xadkile.bicp.message.api.msg.protocol.message.data_interface_definition.Shell
import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.zeromq.*
import java.util.*
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class Bench : TestOnJupyter() {

    @Test
    fun z4() {

        runBlocking {
            coroutineScope {
                launch {
                    var x: Long = 0L
                    while (x < 10) {
                        println("x $x")
                        ++x
                    }
                }

                launch {
                    var x: Long = 0L
                    while (x < 10) {
                        println("y $x")
                        ++x
                    }
                }
            }
        }
    }

    @Test
    fun z32() {
        runBlocking {
            launch { println("first in runBlocking") }
            coroutineScope {
                // how to make this non-blocking
                coroutineScope {
                    // adding delay here will postpone the execution of the following code
                    delay(1000)
                    var x: Long = 0L
                    while (x < 4000_000_000) {
                        x++
                    }
                    println("Last: $x")
                }
                coroutineScope {
                    // coroutine C2
                    println("Second")
                    println("Third")
                }
                println("ZEEZEEZ")
            }

            // out here,
            println("end of run Blocking")
            // C1 and C2 are independent coroutine
        }
    }

    /**
     * The problem of launch{}
     * How launch {...} work:
     * launch{..} are queued and executed in their order of appearance.
     * for coroutineScope{..} the direct codes are always run BEFORE any direct launch
     * for runBlocking{..} the direct code are always run AFTER any direct launch
     * to have true non-blocking behavior, see z32
     */
    @Test
    fun z3() {
        runBlocking {
            launch { println("first in runBlocking") }
            coroutineScope {
                // how to make this non-blocking ??????
                launch {
                    // adding delay here will postpone the execution of the following code
                    delay(1000)
                    var x: Long = 0L
                    while (x < 4000_000_000) {
                        x++
                    }
                    println("Last: $x")
                }
                launch {
                    // coroutine C2
                    println("Second")
                    println("Third")
                }
                // the non blocking behavior is only recorded between launch and outside code.
                // launch does not block outside code
                // out here, the code is not block, and always run before launch/async
                println("ZEEZEEZ")
            }
            // out here,
            println("end of run Blocking")
            // C1 and C2 are independent coroutine
        }
    }


    /**
     * So suspending function is like a delay(). It can block (suspend) a coroutine's operation in the middle, but not the thread underneath, so that the underlying thread can freely go on launching other coroutine or do other things.
     * A suspending function ensures that calling it does not block the underlying function.
     * It actually not directly related to creating any coroutine.
     * I must be aware that: suspending function does not have a coroutine scope, or a context, or a dispatcher inherently sticked to it. It's sole purpose is the ability block the caller coroutine.
     */
    suspend fun doSomethingUsefulOne(): Int {
        delay(1000L) // pretend we are doing something useful here
        return 13
    }

    suspend fun doSomethingUsefulTwo(): Int {
        delay(1000L) // pretend we are doing something useful here, too
        return 29
    }

    fun heavyWorkLoad(): Long {
        var x: Long = 0L
        while (x < 4000_000_000) {
            x++
        }
        return x
    }

    suspend fun heavyWorkLoadSus(): String {
        return coroutineScope {
            withContext(Dispatchers.Default) {
                var x: Long = 0L
                while (x < 4000_000_000) {
                    x++
                }
                "Sus"
            }
        }
        // this blocks
//        return coroutineScope {
//                var x: Long = 0L
//                while (x < 4000_000_000) {
//                    x++
//                }
//                "Sus"
//        }
    }

    suspend fun heavyWorkLoadBareSus(): String {
        var x: Long = 0L
        while (x < 4000_000_000) {
            x++
        }
        return "Bare sus"
    }

    @Test
    fun z2() {
        val t = measureTimeMillis {
            var x: Long = 0L
            while (x < 4000_000_000) {
                x++
            }
        }
        println(t)

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
