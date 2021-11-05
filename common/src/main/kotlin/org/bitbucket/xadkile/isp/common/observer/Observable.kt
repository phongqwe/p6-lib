package org.bitbucket.xadkile.isp.common.observer


/**
 * One observable can serve multiple [Observer].
 *
 * [D] is the type of data that this obj emmit.
 */
interface Observable<D> {
    fun addObserver(observer:Observer<D>)
    fun signal()
    fun removeObserver(observerId: ObserverId)
}
