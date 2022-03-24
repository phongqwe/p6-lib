package com.github.xadkile.p6.message.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ServiceLogger

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class MsgApiCommonLogger
