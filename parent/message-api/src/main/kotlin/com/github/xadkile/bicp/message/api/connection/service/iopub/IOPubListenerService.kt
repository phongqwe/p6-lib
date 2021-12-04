package com.github.xadkile.bicp.message.api.connection.service.iopub

import com.github.xadkile.bicp.message.api.connection.service.Service
import com.github.xadkile.bicp.message.api.connection.service.ServiceReadOnly
import com.github.xadkile.bicp.message.api.msg.listener.MsgHandlerContainer
import com.github.xadkile.bicp.message.api.system.SystemEventReactor

interface IOPubListenerService : IOPubListenerServiceReadOnly,Service{

    /**
     * Convert this interface to a more restrictive and read-only interface
     */
    fun toReadOnly():IOPubListenerServiceReadOnly{
        return this
    }
}

/**
 * An interface only for interacting with a service, cannot stop nor start the service.
 * This interface is for safer consuming services, prevent accidental start(), stop() call
 */
interface IOPubListenerServiceReadOnly:MsgHandlerContainer, ServiceReadOnly
