package com.qxdzbc.p6.message.api.connection.kernel_context

import javax.inject.Qualifier

/**
 * This annotation is for annotating application scope in Dagger2
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class KernelCoroutineScope()
