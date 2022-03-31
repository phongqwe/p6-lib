package com.emeraldblast.p6.test.utils

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.zeromq.*
import java.math.BigInteger
import java.util.*
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class Bench : TestOnJupyter() {
    @Test
    fun nonBlockingEg(){
    }

//    @Test
    fun bb(){
        val context = ZContext()
        val repSocket = context.createSocket(SocketType.REP)
        repSocket.bind("tcp://localhost:6000")

        while(true){
            println(">>>>")
            val str = repSocket.recvStr()
            if(str!=null){
                println("Receive: $str")
            }
            repSocket.send("ok")

        }
    }
    suspend fun fs() {
        withContext(Dispatchers.Default) {
            var x = 0
            while (x < 5) {
                delay(300)
                x++
                println(x)
            }
        }
    }

//    @Test
    fun withContextEg() {
        runBlocking {
            fs()
            launch(Dispatchers.Default) {
                println("blocked by fs too")
            }
            println("block by fs")
        }
    }

    /**
     * Each suspend function call is completed before moving to the next
     */
//    @Test
    fun coroutineScopeExample2() {
        runBlocking {
            coroutineScope {
                mySusFunc1()
                println("block by mySusFunc1")
                mySusFunc1()
                println("2nd time block")
            }

            coroutineScope {
                launch(Dispatchers.Default) {
                    println("coroutineScope2: blocked by coroutine scope 1")
                }
            }
        }
    }


    /**
     * Each suspend function call is completed before moving to the next
     */
//    @Test
    fun runBlockingExample2() {
        runBlocking {
            mySusFunc1()
            println("block by mySusFunc1")
            mySusFunc1()
            println("2nd time block")
        }
    }

    //    @Test
    fun runBlockingExample() {
        runBlocking {
            val j1 = launch(Dispatchers.Default) {
                val time = measureTimeMillis {
                    mySusFunc1()
                }
                println(time)
                println("Blocked by mySusFunc1")
            }
            // x: the creation of coroutine (launch, async) is NOT block by other coroutine
            val j2 = launch(Dispatchers.Default) {
                println("This is not block by the completion of j1")
            }
            println("not blocked by j1, and j2")
        }
        // x: The completion a coroutine scope is blocked by all coroutines (launch,async) within it.
        // x: A coroutine scope will only complete, when all the coroutine (launch, async) within it are completed.
        println("This is blocked by the completion of runBlocking")
    }

    //    @Test
    fun coroutineScopeEggg() {

        runBlocking {
            coroutineScope {
                val j1 = launch(Dispatchers.Default) {
                    val time = measureTimeMillis {
                        mySusFunc1()
                        mySusFunc1()
                        mySusFunc1()
                    }
                    println(time)
                    println("Done j1")
                }
                val j2 = launch(Dispatchers.Default) {
                    println("This is NOT blocked by the completion of j1")
                }
                println("also not blocked by j1, and j2")
            }
            println("this is blocked by the completion of coroutineScope")
            launch(Dispatchers.Default) {
                println("blocked by coroutineScope")
            }
        }
    }

    val limit = 1000

    fun longOperation(label: String, limit: Int = 1000) {
        var x: Long = 0L
        while (x < limit) {
            println("$label $x")
            ++x
        }
    }

    /**
     * suspending vs blocking:
     * suspending = blocking a coroutine, but not the underneath thread
     * blocking = block the underneath thread.
     *
     * A normal function is a blocking function. A suspend function only block a coroutine.
     * =====
     * suspend function and coroutine. I must not mix-up these two concept.
     * Coroutines are a block of code that can be run asynchronously.
     * suspending functions are function that can block/pause/suspend coroutines.
     * =======
     * Coroutine builders (launch, async) run things in a suspend functions.
     * So when creating a suspend function, I should ask the question: is the logic require waiting.
     *
     * I don't really need to write suspend function in order to use coroutine.
     *
     * - Coroutine deal with asynchronous code (code runs in parallel)
     *
     * - suspending function deal with waiting code.
     *
     * Most of the time, coroutine is invoke asynchronously because it involves waiting code. But they (coroutines) do not necessarily serve only such purpose.
     *
     * Since suspending function is for waiting/blocking code. It is best that they are used within coroutine so that they don't block the UI thread.
     * =====
     *  the point of suspending function is: they can literally block (suspend) coroutines . The rule of suspending function is that they must be called within other suspending function.
     *
     *  This is because suspending function use some kind of compiler code generation in the background to generate continuation-passing code.
     *
     *  Why should data, business logic layer expose suspend function to handle waiting logic ?
     *      Answer:
     *          - I want to run my waiting logic inside coroutine so that it only block the coroutine and don't block the underneath thread. In order to block the coroutine, I must use suspending function
     *          - So that I don't have to use callback and let my code be written in direct style
     *  ======
     *  Since suspend function only run inside suspend function, and suspend function always start with coroutine, I must only make something suspend function if I plan to use it inside an inherited coroutine.
     *  For class that don't use inherited coroutine, but injected coroutine, I can use normal function.
     *  Rule for injected coroutine scope:
     *      - for services: inject coroutine scope as object properties
     *      - for multiple-purpose crap: inject in function parameter. But this is rare. For now this is only used in IOPub listener. This listener is supposed to run both as background services, and one-time object. This is bad, and should not be be exposed to external use.
     *      - for one-time blocking such as network call or long computation, use inherited coroutine scope
     */
//    @Test
    fun suspendingFunction() {
        runBlocking {
            // block of runBlocking is a suspending function, so it is legal to call mySusFunc1 here
            mySusFunc1()
            launch {
                // block of launch is a suspending function, so it is legal to call mySusFunc1 here
                mySusFunc1()
            }
        }
    }

    //    @Test
    fun suspendingFunction2() {
        val o = measureTimeMillis {
            runBlocking {
                coroutineScope {
                    launch(Dispatchers.Default) {
                        // launch block is a suspending function, so it is legal to call mySusFunc1 here
                        mySusFunc1()
                    }
                    launch(Dispatchers.Default) {
                        // this function is a normal function, I can also call it here, does not need to be a suspending function
                        myCostlyFunc()
                    }
                }
            }
        }
        println(o)
    }

    /**
     * a suspend function that takes a lot of time
     */
    suspend fun mySusFunc1() {
        println("do something that takes a lot of time")
        BigInteger(1500, Random()).nextProbablePrime()
    }

    /**
     * A normal function that takes a lot of time
     */
    fun myCostlyFunc() {
        println("do something that takes a lot of time")
        BigInteger(1500, Random()).nextProbablePrime()
    }

    /**
     * Just a demo, avoid using this, as I have to manually cancel the scope job, which is not nice at all
     */
//    @Test
    fun myOwnScope() {
        val myScope = CoroutineScope(Dispatchers.Default + Job() + CoroutineName("myCoroutine_name"))
        myScope.launch {
            longOperation("x")
        }

        myScope.launch {
            longOperation("y")
        }
    }

    //    @Test
    fun z32() {
        runBlocking {
            val time = measureTimeMillis {
                val one = async {
                    println("last")
                    doSomethingUsefulOne()
                }
                val two = async { doSomethingUsefulTwo() }
                launch(Dispatchers.Default) {
                    println("Second")
                }
                println("The answer is ${one.await() + two.await()}")
            }
            println("Completed in $time ms")
        }
    }

    /**
     * The problem of launch{}
     * How launch {...} work:
     * launch{..} are queued and executed in their order of appearance.
     * for coroutineScope{..} the direct codes are always run BEFORE any direct launch
     * for runBlocking{..} the direct code are always run AFTER any direct launch
     *
     * in order to launch{...} non blocking code, i must provide a dispatcher (Dispatchers.Default, Dispatcher.IO)
     * First, let's clear the following definition:
     *  - context
     *  - scope
     *  - dispatcher ([CoroutineDispatcher]): implements CoroutineContext
     *
     * Every coroutine builder (launch, async) + scope function (coroutineScope, withContext) provides:
     *  - their own scope + their own job instances.
     *  - they all wait for all the coroutines inside their block to complete before completing themselves.
     *
     * Every coroutine context has a Job instance representing itself. This Job is the same as the one return by "launch"
     *
     */
//    @Test
    fun coroutineScopeEg() {
        runBlocking {

            val j: Job = launch { println("first in runBlocking") }
            coroutineScope {
                // how to make this non-blocking: specify a dispatcher such as Dispatchers.Default. If i dont, it will use the dispatcher of runBlocking because it inherit it.
                launch(Dispatchers.Default) {
                    // adding delay here will postpone the execution of the following code
                    var x: Long = 0L
                    while (x < 4000_000_000) {
                        x++
                    }
                    println("Last: $x")
                }
                launch(Dispatchers.Default) {
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


    suspend fun doSomethingUsefulOne(dispatcher: CoroutineDispatcher = Dispatchers.Default): BigInteger {
        return withContext(dispatcher) {
            println("in doSomethingUsefulOne")
            BigInteger(1500, Random()).nextProbablePrime()

        }
    }

    suspend fun doSomethingUsefulTwo(dispatcher: CoroutineDispatcher = Dispatchers.Default): BigInteger {
        return withContext(dispatcher) {
            BigInteger(1500, Random()).nextProbablePrime()
        }
    }

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
