package com.qxdzbc.p6.message.api.message.protocol

import com.qxdzbc.p6.message.api.message.protocol.data_interface_definition.IOPub
import kotlin.test.*

internal class ProtocolUtilsTest {

    @Test
    fun parseJsonToMap() {
        val metaJson = "{\"meta1\":1,\"meta2\":\"xxxx\"}"
        val o = ProtocolUtils.msgGson.fromJson(metaJson,IOPub.DisplayData.MetaData::class.java)
        assertEquals(1.0, o["meta1"])
        assertEquals("xxxx", o["meta2"])
    }
}
