package com.github.xadkile.bicp.message.api.exception

import javax.xml.crypto.Data


/**
 * An exception in which I can specify the location it occurs and the message
 */
data class ExceptionInfo(
    val message: String = "",
    val loc: String = "",
    val data: Any? = null,
    val dataStrMaker: () -> String = { data?.toString() ?: "" },
) {

    constructor(
        message: String = "",
        loc: Class<*>? = null,
        data: Any? = null,
        dataStrMaker: () -> String = { data?.toString() ?: "" },
    ) : this(
        message, loc?.simpleName ?: "", data, dataStrMaker
    )

    override fun toString(): String {
        val dataStr = dataStrMaker()
        return """
           ${this::class.java.simpleName}:
                ${if (loc.isNotEmpty()) "* at ${loc}" else ""}
                ${if (message.isNotEmpty()) "* msg: $message" else ""}
                ${if (dataStr.isNotEmpty()) "* data: $dataStr" else ""}
        """
    }

    fun occurAt(clazz: Class<*>): ExceptionInfo {
        return this.copy(
            loc = clazz.simpleName
        )
    }

    fun withMsg(msg: String): ExceptionInfo {
        return this.copy(message = msg)
    }

    fun withData(data: Any?): ExceptionInfo {
        return this.copy(data = data)
    }

    fun withDataStrMaker(dataStrMaker: () -> String): ExceptionInfo {
        return this.copy(dataStrMaker = dataStrMaker)
    }

    /**
     * construct exception from this exception info.
     * The returned exception must have a constructor that accept exactly 1 [ExceptionInfo]
     */
    inline fun <reified T : ExceptionWithInfo> toException(): T {
        val clazz = T::class.java
        val exConstr = clazz.getConstructor(this.javaClass)
        return exConstr.newInstance(this)
    }

    companion object {

        fun occurAt(obj: Any): ExceptionInfo {
            return occurAt(obj::class.java)
        }

        fun occurAt(clazz: Class<*>): ExceptionInfo {
            return ExceptionInfo(
                loc = clazz.simpleName
            )
        }

        fun withMsg(msg: String): ExceptionInfo {
            return ExceptionInfo(message = msg, loc = "")
        }
    }
}


