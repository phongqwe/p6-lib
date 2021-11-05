package org.bitbucket.xadkile.isp.common.observer

/**
 * This obj reacts to signals from [Observable] which it subscribe to.
 *
 * [D] is the type of data that this observer received from its observable.
 *
 * One observer can subscribe to multiple [Observable].
 */
interface Observer<D> {
    fun subscribe(target:Observable<D>)
    fun getId():ObserverId
    fun onSignal(data:D)
}
