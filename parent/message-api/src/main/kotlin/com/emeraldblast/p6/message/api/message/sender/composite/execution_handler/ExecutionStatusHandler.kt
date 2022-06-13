package com.emeraldblast.p6.message.api.message.sender.composite.execution_handler

import com.emeraldblast.p6.message.api.message.protocol.data_interface_definition.IOPub

interface ExecutionStatusHandler : DeferredJobHandler<IOPub.Status.ExecutionState>
