package com.emeraldblast.p6.formula

import kotlin.reflect.KFunction

interface FunctionMap : Map<String,KFunction<Any>> {
    fun getFunc(name: String): KFunction<Any>?
    fun addFunc(name:String, func: KFunction<Any>): FunctionMap
    fun removeFunc(name:String): FunctionMap
}
