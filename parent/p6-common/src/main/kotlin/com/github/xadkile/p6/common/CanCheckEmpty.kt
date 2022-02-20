package com.github.xadkile.p6.common

interface CanCheckEmpty {
    fun isEmpty():Boolean
    fun isNotEmpty():Boolean{
        return !this.isEmpty()
    }
}
