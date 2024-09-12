package com.ivanb.aktors

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.coroutineScope

/**
 * - spawn the first actor of this system - "guardian actor" (parent of all actors)
 * - run some action on the guardian actor
 * - args:
 *      - name of the system = name of the guardian
 *      - lambda that runs some code on the guardian actor (ActorRef)
 * */

object ActorSystem : ActorScope() {
    suspend fun <T> app(
        name: String,
        behaviour: Behaviour<T>,
        action: suspend (ActorRef<T>) -> Unit,
    ) = coroutineScope {
        val guardian = createActor<T>(name, this, CoroutineName(name), behaviour)
        action(guardian)
    }
}
