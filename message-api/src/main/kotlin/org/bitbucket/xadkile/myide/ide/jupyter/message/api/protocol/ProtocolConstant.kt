package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol

import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.IOException
import java.time.ZonedDateTime

object ProtocolConstant {
    val messageDelimiter =  "<IDS|MSG>"
}
