package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.utils

import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZonedDateTime

object ProtocolUtils {
    val msgGson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, object : TypeAdapter<LocalDateTime?>() {
            @Throws(IOException::class)
            override fun read(jsonReader: JsonReader): LocalDateTime? {
                return LocalDateTime.parse(jsonReader.nextString())
            }
            @Throws(IOException::class)
            override fun write(jsonWriter: JsonWriter?, value: LocalDateTime?) {
                jsonWriter?.value(value.toString())
            }
        })
        .enableComplexMapKeySerialization()
        .create()
}
