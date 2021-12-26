package com.github.xadkile.p6.exception.error

class ErrorReport(val errorHeader: ErrorHeader,val message:String, val data:Any) {
    fun <T> getCastedData():T{
        return this.data as T
    }
}


