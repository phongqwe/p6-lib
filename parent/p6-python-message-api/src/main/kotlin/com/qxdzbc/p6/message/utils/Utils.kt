package com.qxdzbc.p6.message.utils

import kotlinx.coroutines.CompletableDeferred

object Utils {
    fun <T> CompletableDeferred<T>.cancelIfPossible(){
        if(this.isActive){
            this.cancel()
        }
    }
}
