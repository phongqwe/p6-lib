package com.github.xadkile.bicp.message.api.connection.kernel_context.exception

class KernelIsDownException(val msg:String=""):Exception(msg) {
    companion object {
        fun occurAt(o:Any): KernelIsDownException {
            return KernelIsDownException("occur at ${o.javaClass.simpleName}")
        }
    }
}
