package com.github.xadkile.bicp.message.api.connection.service.iopub

import com.github.xadkile.bicp.message.api.connection.service.Service
import com.github.xadkile.bicp.message.api.msg.listener.MsgHandlerContainer
import com.github.xadkile.bicp.message.api.system.SystemEventReactor

interface IOPubListenerService : MsgHandlerContainer, Service
