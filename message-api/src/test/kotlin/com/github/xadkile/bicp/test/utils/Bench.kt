package com.github.xadkile.bicp.test.utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import kotlin.concurrent.thread


class Bench {
    @Test
    fun z2(){
        runBlocking {
            var x = 0
            while (x<5){
                delay(1000)
                println(x)
                x+=1
            }
        }
        println("end")
    }
    @Test
    fun  z(){
        val t1 = thread(false) {
            ZContext().use { context ->
                // Socket to talk to clients
                val socket: ZMQ.Socket = context.createSocket(SocketType.REP)
                socket.bind("tcp://*:5555")
                while (!Thread.currentThread().isInterrupted) {
                    val reply: ByteArray = socket.recv(0)
                    println(
                        "Received " + ": [" + String(reply, ZMQ.CHARSET) + "]"
                    )
                    Thread.sleep(1000) //  Do some 'work'
                    val response = "world"
                    socket.send(response.toByteArray(), 0)
                }
            }
        }

        t1.start()

        ZContext().use { context ->
            //  Socket to talk to server
            println("Connecting to hello world server")
            val socket = context.createSocket(SocketType.REQ)
            socket.connect("tcp://localhost:5555")
//            t1.interrupt()
            for (requestNbr in 0..2) {
                val request = "Hello"
                println("Sending Hello $requestNbr")
                val ss = socket.send(request.toByteArray(ZMQ.CHARSET), 0)
                println("SS ${ss}")
                val reply = socket.recv(0)
                println(
                    "Received " + String(reply, ZMQ.CHARSET) + " " +
                            requestNbr
                )
            }
        }
//        t1.interrupt()
    }
}
