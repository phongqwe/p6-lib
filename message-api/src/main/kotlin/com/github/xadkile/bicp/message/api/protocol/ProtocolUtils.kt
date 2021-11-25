package com.github.xadkile.bicp.message.api.protocol

import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZonedDateTime

object ProtocolUtils {

    /**
     * A gson instance for parsing zmq message
     */
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
        .registerTypeAdapter(ZonedDateTime::class.java, object : TypeAdapter<ZonedDateTime?>() {
            @Throws(IOException::class)
            override fun read(jsonReader: JsonReader): ZonedDateTime? {
                return ZonedDateTime.parse(jsonReader.nextString())
            }
            @Throws(IOException::class)
            override fun write(jsonWriter: JsonWriter?, value: ZonedDateTime?) {
                jsonWriter?.value(value.toString())
            }
        })
        .enableComplexMapKeySerialization()
        .setPrettyPrinting()
        .create()
}
