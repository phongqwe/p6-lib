package com.emeraldblast.p6.message.api.message.protocol.data_interface_definition

interface WithTraceBack {
    val traceback:List<String>
    fun traceBackAsStr():String{
        return traceback.joinToString(System.lineSeparator())
    }
}
