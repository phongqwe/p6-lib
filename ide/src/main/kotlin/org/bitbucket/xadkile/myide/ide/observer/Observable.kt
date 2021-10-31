package org.bitbucket.xadkile.myide.ide.observer

/**
 * One observable can serve multiple [Observer]
 * [D] data that this observable emmit
 */
interface Observable<D> {
    fun addObserver(observer:Observer<D>)
    fun signal()
    fun removeObserver(observerId: ObserverId)
}
