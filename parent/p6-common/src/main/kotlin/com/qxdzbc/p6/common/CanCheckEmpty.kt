package com.qxdzbc.p6.common

interface CanCheckEmpty {
    fun isEmpty():Boolean
    fun isNotEmpty():Boolean{
        return !this.isEmpty()
    }
}
