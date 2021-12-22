package com.github.xadkile.p6.exception


/**
 * An exception in which I can specify the location it occurs and the message
 * [msg]: a message briefly describe the exception
 * [loc]: location where an exception occurs
 * [data]: data about the exception
 * [dataToStrConverter] : a function that convert [data] to String
 */
data class ExceptionInfo<D>(
    val msg: String = "",
    val loc: String = "",
    val data: D,
    val dataToStrConverter: () -> String = { data?.toString() ?: "" },
) {

    constructor(
        msg: String="",
        loc: Any,
        data: D,
        dataStrMaker: () -> String = { data?.toString() ?: "" },
    ) : this(
        msg, if(loc is String) loc else loc::class.java.canonicalName, data, dataStrMaker
    )

    override fun toString(): String {
        val dataStr = dataToStrConverter()
        return """
        ${if (msg.isNotEmpty()) "* msg: $msg" else ""}
        ${if (loc.isNotEmpty()) "* at: $loc" else ""}
        ${if (dataStr.isNotEmpty()) "* data: $dataStr" else ""}
        """
    }

    fun occurAt(o: Any): ExceptionInfo<D> {
        return this.copy(
            loc = o::class.java.canonicalName
        )
    }

    fun withMsg(msg: String): ExceptionInfo<D> {
        return this.copy(msg = msg)
    }

    fun withData(data: D): ExceptionInfo<D> {
        return this.copy(data = data)
    }

    fun withDataToStrConverter(dataStrMaker: () -> String): ExceptionInfo<D> {
        return this.copy(dataToStrConverter = dataStrMaker)
    }
}


