package com.emeraldblast.p6.common.exception.error

data class ErrorHeader(val errorCode: String, val errorDescription: String){
    override fun toString(): String {
        return "${errorCode}: $errorDescription"
    }
    fun isType(errorHeader: ErrorHeader):Boolean{
        return this.errorCode == errorHeader.errorCode
    }

    /**
     * convenient method for non-production operation
     */
    fun toErrorReport():ErrorReport{
        return ErrorReport(
            header =this
        )
    }
}
