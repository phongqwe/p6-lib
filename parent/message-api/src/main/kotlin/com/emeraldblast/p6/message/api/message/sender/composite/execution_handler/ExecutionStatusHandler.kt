package com.emeraldblast.p6.message.api.message.sender.composite.execution_handler

import com.emeraldblast.p6.message.api.message.protocol.data_interface_definition.IOPub

/**
 * A handler for catching execution status
 */
interface ExecutionStatusHandler : DeferredJobHandler<IOPub.Status.ExecutionState>
