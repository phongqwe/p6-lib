package com.github.xadkile.bicp.message.api.exception


/**
 * An exception in which I can specify the location it occurs and the message
 */
data class ExceptionInfo<D>(
    val message: String = "",
    val loc: String = "",
    val data: D,
    val dataStrMaker: () -> String = { data?.toString() ?: "" },
) {

    constructor(
        msg: String="",
        loc: Any,
        data: D,
        dataStrMaker: () -> String = { data?.toString() ?: "" },
    ) : this(
        msg, loc::class.java.simpleName, data, dataStrMaker
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

    fun occurAt(o: Any): ExceptionInfo<D> {
        return this.copy(
            loc = o::class.java.simpleName
        )
    }

    fun withMsg(msg: String): ExceptionInfo<D> {
        return this.copy(message = msg)
    }

    fun withData(data: D): ExceptionInfo<D> {
        return this.copy(data = data)
    }

    fun withDataStrMaker(dataStrMaker: () -> String): ExceptionInfo<D> {
        return this.copy(dataStrMaker = dataStrMaker)
    }
}


