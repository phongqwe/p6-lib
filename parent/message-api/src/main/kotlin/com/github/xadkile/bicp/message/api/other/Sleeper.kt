package com.github.xadkile.bicp.message.api.other

object Sleeper {
    /**
     * run Thread.sleep until predicate become true.
     */
    fun threadSleepUntil(waitTime:Long, predicate:() -> Boolean){
        while(predicate() == false){
            Thread.sleep(waitTime)
        }
    }
}


