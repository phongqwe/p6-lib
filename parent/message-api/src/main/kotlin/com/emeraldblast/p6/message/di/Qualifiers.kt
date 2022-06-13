package com.emeraldblast.p6.message.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class RepServiceLogger

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class MsgApiCommonLogger

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ServiceInitTimeOut

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class SystemUsername

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class SessionId


@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ServiceCoroutineDispatcher
