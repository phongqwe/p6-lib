package com.github.xadkile.bicp.test.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger

class ATest {
    class S1 {
        var i = true
        var x = AtomicInteger(0)
//        var x = 0
        suspend fun start(){
            while(i){
                delay(500)
                x.incrementAndGet()
//                x++
            }
        }
        fun isRunning():Boolean{
            return x.get()>3
//            return x > 3
        }
        fun cancel(){
            i=false
        }
    }

    @Test
    fun aTest(){
        val s1 = S1()
        GlobalScope.launch(Dispatchers.Default) {
            s1.start()
        }
        while(s1.isRunning().not()){
//           println("Q")  // THIS LINE
        }
        if(s1.isRunning()){
            println("cancel")
            s1.cancel()
        }else{
            println("abc")
        }
    }
}
