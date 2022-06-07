package com.emeraldblast.p6.common.exception.error

class ErrorReport(
    val header: ErrorHeader,
    val data: Any,
    val locList: List<String>,
) {

    constructor(header: ErrorHeader, data: Any = "", loc: String = "") : this(
        header = header,
        data = data,
        locList = if (loc.isNotEmpty()) listOf(loc) else emptyList()
    )

    val loc: String
        get() = locList.fold("") { acc, str ->
            acc + "\n" + str
        }

    fun <T> getCastedData(): T {
        return this.data as T
    }

    fun toException(): ErrorException {
        return ErrorException(this)
    }

    override fun toString(): String {
        val rt = """
type: ${this.header.toString()}
data: ${data}
loc: ${loc} 
        """.trimIndent()
        return rt
    }

    val repStr:String get()=this.header.toString()

    fun isType(errorHeader: ErrorHeader): Boolean {
        return this.header.isType(errorHeader)
    }

    fun isType(errorReport: ErrorReport): Boolean {
        return this.header.isType(errorReport.header)
    }

    fun addLoc(loc: String): ErrorReport {
        return ErrorReport(
            header, data, this.locList + loc
        )
    }

    fun stackTraceStr(): String {
        val s = this.toException().stackTraceToString()
        return s
    }

    fun stackTraceWithLoc(): String {
        return "${loc}\n${this.stackTraceStr()}"
    }

    fun toStringWithStackTrace(): String {
        val rt = """
type: ${this.header.toString()}
data: ${data}
loc: ${this.stackTraceWithLoc()} 
        """.trimIndent()
        return rt
    }
}


