package com.ivanb.aktors

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * - call scope.launch() - start a new coroutine with a new actor
 * - call the run() on the actor in that coroutine
 * - return an actorRef with the actor's channel
 * */

open class ActorScope {
    protected fun <T> createActor(
        name: String,
        scope: CoroutineScope,
        context: CoroutineContext,
        behaviour: Behaviour<T>,
    ): ActorRef<T> {
        val channel = Channel<T>(capacity = Channel.UNLIMITED)
        scope.launch(context) {
            val actor = Actor<T>(name, channel, coroutineContext.job, scope)
            actor.run(behaviour)
        }
        return ActorRef(channel)
    }
}
