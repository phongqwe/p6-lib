package com.qxdzbc.p6.message.api.message.sender.composite

import com.qxdzbc.p6.message.api.message.protocol.JPMessage
import com.qxdzbc.p6.message.api.message.protocol.data_interface_definition.IOPub

typealias ExecuteResult = JPMessage<IOPub.ExecuteResult.MetaData, IOPub.ExecuteResult.Content>
