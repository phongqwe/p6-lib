package com.github.xadkile.bicp.message.api.connection.ipython_context

fun interface OnIPythonContextEvent {
    fun run(context: IPythonContext)
    companion object {
        val Nothing = OnIPythonContextEvent {
            // do nothing
        }
    }
}
