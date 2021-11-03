package org.bitbucket.xadkile.isp.ide.observer

/**
 * [D] data that this observer receive from its observable
 * One observer can subscribe to multiple [Observable]
 */
interface Observer<D> {
    fun subscribe(target:Observable<D>)
    fun getId():ObserverId
    fun onSignal(data:D)
}
