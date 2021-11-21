package com.github.xadkile.bicp.test.utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import java.util.*
import kotlin.concurrent.thread


class Bench {
    @Test
    fun z2(){

    }
    @Test
    fun  z(){
        val server = thread(false) {
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

        server.start()

        // client
        ZContext().use { context ->
            //  Socket to talk to server
            println("Collecting updates from weather server")
            val subscriber = context.createSocket(SocketType.SUB)
            subscriber.connect("tcp://localhost:5556")

            //  Subscribe to zipcode, default is NYC, 10001
            val filter = "10001 "
            subscriber.subscribe(filter.toByteArray(ZMQ.CHARSET))

            //  Process 100 updates
            var update_nbr: Int
            var total_temp: Long = 0
            update_nbr = 0
            while (update_nbr < 100) {
                //  Use trim to remove the tailing '0' character
                val string = subscriber.recvStr(0).trim { it <= ' ' }
                val sscanf = StringTokenizer(string, " ")
                val zipcode = Integer.valueOf(sscanf.nextToken())
                val temperature = Integer.valueOf(sscanf.nextToken())
                val relhumidity = Integer.valueOf(sscanf.nextToken())
//                println("Recv: ${temperature}")
                total_temp += temperature.toLong()
                update_nbr++
            }
            println(String.format(
                "Average temperature for zipcode '%s' was %d.",
                filter, (total_temp / update_nbr).toInt()))

        }
        server.interrupt()
//
    }
}
