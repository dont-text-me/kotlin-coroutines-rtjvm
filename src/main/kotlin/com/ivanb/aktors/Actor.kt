package com.ivanb.aktors

import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.yield

/**
 * - name and channel args
 * - run() method, which pops elements off the channel and logs them
 * */
internal class Actor<T>(
    private val name: String,
    private val channel: Channel<T>,
    private val job: Job,
) {
    suspend fun run(startBehaviour: Behaviour<T>) {
        var behaviour = startBehaviour
        var newBehaviour = startBehaviour
        while (true) {
            /*
             * Check the behaviour type
             * run it
             * (maybe) transition to the next behaviour
             * */

            when (behaviour) {
                is Behaviours.ReceiveMessage -> {
                    val msg = channel.receive()
                    newBehaviour = behaviour.handler(msg)
                    behaviour = newBehaviour.ifSameThen(behaviour)
                }
                is Behaviours.Same -> throw IllegalStateException("The INSTANCE 'Behaviour.Same' is illegal")
                is Behaviours.Setup -> {
                    newBehaviour = behaviour.init()
                    behaviour = newBehaviour.ifSameThen(Behaviours.stopped())
                }
                is Behaviours.Stopped -> {
                    // stop and the coroutine that runs the actor, close the channel
                    channel.close()
                    job.cancel()
                }
            }
            yield()
        }
    }
}
