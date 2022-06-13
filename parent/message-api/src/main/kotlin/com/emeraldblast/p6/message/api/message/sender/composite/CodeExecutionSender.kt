package com.emeraldblast.p6.message.api.message.sender.composite

import com.emeraldblast.p6.common.exception.error.ErrorReport
import com.emeraldblast.p6.message.api.message.sender.MsgSender
import com.emeraldblast.p6.message.api.message.sender.shell.ExecuteRequest
import com.github.michaelbull.result.Result

interface CodeExecutionSender : MsgSender<ExecuteRequest, Result<ExecuteResult?, ErrorReport>>
