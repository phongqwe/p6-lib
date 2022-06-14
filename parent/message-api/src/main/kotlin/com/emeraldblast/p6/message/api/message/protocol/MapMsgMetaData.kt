package com.emeraldblast.p6.message.api.message.protocol

/**
 * default metadata class. This is a map. This can be created by gson like any other normal class
 */
abstract class MapMsgMetaData : MsgMetaData, HashMap<Any,Any>()
