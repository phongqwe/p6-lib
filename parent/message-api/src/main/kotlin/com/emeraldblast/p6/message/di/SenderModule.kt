package com.emeraldblast.p6.message.di

import com.emeraldblast.p6.message.api.connection.kernel_context.context_object.SenderProvider
import com.emeraldblast.p6.message.api.connection.kernel_context.context_object.SenderProviderImp
import com.emeraldblast.p6.message.api.message.sender.composite.CodeExecutionSender
import com.emeraldblast.p6.message.api.message.sender.composite.CodeExecutionSenderImp
import com.emeraldblast.p6.message.api.message.sender.shell.ExecuteSender
import com.emeraldblast.p6.message.api.message.sender.shell.ExecuteSenderImp
import com.emeraldblast.p6.message.api.message.sender.shell.KernelInfoSender
import com.emeraldblast.p6.message.api.message.sender.shell.KernelInfoSenderImp
import dagger.Binds

@dagger.Module
interface SenderModule {
    @Binds
    fun ExecuteSender(i:ExecuteSenderImp): ExecuteSender

    @Binds
    fun KernelInfoSender(i: KernelInfoSenderImp): KernelInfoSender

    @Binds
    fun CodeExecutionSender(i: CodeExecutionSenderImp):CodeExecutionSender

}
