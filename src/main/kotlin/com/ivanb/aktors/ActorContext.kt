package com.ivanb.aktors

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext

class ActorContext<T>(
    val self: ActorRef<T>,
    val name: String,
    val scope: CoroutineScope,
    val job: Job,
) : ActorScope() {
    fun <S> spawn(
        name: String,
        behaviour: Behaviour<S>,
    ): ActorRef<S> = createActor(name, scope, buildCoroutineContext(job, name), behaviour)

    val log = LoggerFactory.getLogger(name)

    private fun buildCoroutineContext(
        parentJob: Job,
        childName: String,
    ): CoroutineContext = parentJob + CoroutineName("$name/$childName")
}
