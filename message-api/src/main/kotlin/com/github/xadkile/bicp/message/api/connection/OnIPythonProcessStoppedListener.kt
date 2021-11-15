package com.github.xadkile.bicp.message.api.connection

fun interface OnIPythonProcessStoppedListener {
    fun run(context:IPythonContext)
    companion object {
        val Nothing = object : OnIPythonProcessStoppedListener{
            override fun run(context:IPythonContext) {
                // do nothing
            }
        }
    }
}
