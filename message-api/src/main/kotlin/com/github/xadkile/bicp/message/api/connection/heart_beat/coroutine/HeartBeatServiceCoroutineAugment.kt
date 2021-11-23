package com.github.xadkile.bicp.message.api.connection.heart_beat.coroutine

/**
 * A perpetual background service that check the heart beat channel periodically.
 */
interface HeartBeatServiceCoroutineAugment {
    suspend fun stopSuspend():Boolean
}

