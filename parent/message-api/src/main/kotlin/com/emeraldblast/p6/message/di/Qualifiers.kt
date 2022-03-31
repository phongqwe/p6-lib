package com.emeraldblast.p6.message.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class RepServiceLogger

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class MsgApiCommonLogger
