package com.github.xadkile.bicp.message.api.exception


class UnknownException(message:String) : Exception(message){
    companion object {
        fun occurAt(o:Any): UnknownException {
            return UnknownException("occur at ${o.javaClass.simpleName}")
        }
    }
}
