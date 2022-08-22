package com.qxdzbc.p6.message.api.message.sender.composite

import com.qxdzbc.p6.common.exception.error.ErrorReport
import com.qxdzbc.p6.message.api.message.sender.MsgSender
import com.qxdzbc.p6.message.api.message.sender.shell.ExecuteRequest
import com.github.michaelbull.result.Result

interface CodeExecutionSender : MsgSender<ExecuteRequest, Result<ExecuteResult?, ErrorReport>>
