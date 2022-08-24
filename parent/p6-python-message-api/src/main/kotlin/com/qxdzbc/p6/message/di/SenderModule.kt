package com.qxdzbc.p6.message.di

import com.qxdzbc.p6.message.api.message.sender.composite.CodeExecutionSender
import com.qxdzbc.p6.message.api.message.sender.composite.CodeExecutionSenderImp
import com.qxdzbc.p6.message.api.message.sender.shell.ExecuteSender
import com.qxdzbc.p6.message.api.message.sender.shell.ExecuteSenderImp
import com.qxdzbc.p6.message.api.message.sender.shell.KernelInfoSender
import com.qxdzbc.p6.message.api.message.sender.shell.KernelInfoSenderImp
import dagger.Binds

@dagger.Module
interface SenderModule {
    @Binds
    @MsgApiScope
    fun ExecuteSender(i:ExecuteSenderImp): ExecuteSender

    @Binds
    @MsgApiScope
    fun KernelInfoSender(i: KernelInfoSenderImp): KernelInfoSender

    @Binds
    @MsgApiScope
    fun CodeExecutionSender(i: CodeExecutionSenderImp):CodeExecutionSender
}
