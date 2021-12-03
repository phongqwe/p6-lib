package com.github.xadkile.bicp.message.api.exception

class CompositeException(val exceptions:List<Exception>) : Exception() {
    constructor(vararg exceptions: Exception):this(exceptions.toList())
    override fun toString(): String {
        return this.exceptions.joinToString("\n") { it.toString() }
    }
}
