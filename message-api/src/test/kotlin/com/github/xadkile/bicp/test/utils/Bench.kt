package com.github.xadkile.bicp.test.utils

import com.github.michaelbull.result.unwrap
import com.github.xadkile.bicp.message.api.protocol.message.JPRawMessage
import org.junit.jupiter.api.Test
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import org.zeromq.ZThread
import java.util.*
import kotlin.concurrent.thread


class Bench {
//    class ZListener(val subSocket: ZMQ.Socket) : ZThread.IDetachedRunnable {
//        override fun run(args: Array<out Any>?) {
//            val msgL = mutableListOf<String>()
//            while (true) {
//                val o = subSocket.recvStr()
//                msgL.add(o)
//                while (subSocket.hasReceiveMore()) {
//                    val m = subSocket.recvStr()
//                    msgL.add(m)
//                }
//                val z = JPRawMessage.fromPayload(msgL.map { it.toByteArray(Charsets.UTF_8) }).unwrap()
//                println(z)
//                msgL.clear()
//            }
//        }
//
//    }
    @Test
    fun z2(){
//                    val ioPubSocket: ZMQ.Socket = context.createSocket(SocketType.SUB)
//            ioPubSocket.connect(connectionFile.createIOPubChannel().makeAddress())
//            ioPubSocket.subscribe("")
//            val runnable = ZListener(ioPubSocket)
//            ZThread.start(runnable)
    }
    @Test
    fun  z(){
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
            items.register(subscriber,ZMQ.Poller.POLLIN)
            items.register(subscriber2,ZMQ.Poller.POLLIN)

            update_nbr = 0
            while (update_nbr < 100) {
                //  Use trim to remove the tailing '0' character
                items.poll()
                if(items.pollin(0)){
                    val string = subscriber.recvStr(0).trim { it <= ' ' }
                    val sscanf = StringTokenizer(string, " ")
                    val zipcode = Integer.valueOf(sscanf.nextToken())
                    val temperature = Integer.valueOf(sscanf.nextToken())
                    val relhumidity = Integer.valueOf(sscanf.nextToken())
                    total_temp += temperature.toLong()
                    update_nbr++
                }

                if(items.pollin(2)){
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
