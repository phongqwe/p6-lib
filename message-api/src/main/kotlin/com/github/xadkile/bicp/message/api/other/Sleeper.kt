package com.github.xadkile.bicp.message.api.other

object Sleeper {
    /**
     * sleep until predicate become true.
     */
    fun sleepUntil(waitTime:Long,predicate:() -> Boolean){
        while(predicate() == false){
            Thread.sleep(waitTime)
        }
    }
}
