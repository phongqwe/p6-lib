package com.emeraldblast.p6.message.di

import com.emeraldblast.p6.message.api.message.sender.composite.execution_handler.*
import dagger.Binds

@dagger.Module
interface HandlerModule {
    @Binds
    @MsgApiScope
    fun ExecutionStatusHandler_Idle(i: ExecutionStatusHandlerImp.Idle):IdleExecutionStatusHandler

    @Binds
    @MsgApiScope
    fun ExecutionStatusHandler_Busy(i: ExecutionStatusHandlerImp.Busy):BusyExecutionStatusHandler


    @Binds
    @MsgApiScope
    fun ExecuteResultHandler(i: ExecuteResultHandlerImp):ExecuteResultHandler

    @Binds
    @MsgApiScope
    fun ExecuteErrorHandler(i: ExecuteErrorHandlerImp):ExecuteErrorHandler
}
