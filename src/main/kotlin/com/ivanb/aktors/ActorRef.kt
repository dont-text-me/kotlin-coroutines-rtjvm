package com.ivanb.aktors

import kotlinx.coroutines.channels.SendChannel

/**
 * - receives a message of a certain type
 * - wraps a coroutine channel
 * - a method tell (msg: the type) -> push an element to the channel
 * - a method `!` (same thing)
 */
class ActorRef<T> internal constructor(
    val channel: SendChannel<T>,
) {
    suspend fun tell(msg: T) = channel.send(msg)

    @Suppress("ktlint:standard:function-naming")
    suspend infix fun `!`(msg: T) = tell(msg)
}
