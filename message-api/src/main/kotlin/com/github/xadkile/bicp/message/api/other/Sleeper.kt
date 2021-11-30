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

    /**
     * continuous waiting until [isTrue] return true
     */
    fun waitUntil(isTrue: () -> Boolean){
        while(isTrue()==false){}
    }

    fun waitAsLongAs(isTrue: () -> Boolean){
        while(isTrue()){}
    }
}

class Sleeper2{

}

