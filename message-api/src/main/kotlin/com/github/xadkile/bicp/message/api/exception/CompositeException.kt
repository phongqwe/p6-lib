package com.github.xadkile.bicp.message.api.exception

class CompositeException(val exceptions:List<Exception>) : Exception() {
    override fun toString(): String {
        return this.exceptions.joinToString("\n") { it.toString() }
    }
}
