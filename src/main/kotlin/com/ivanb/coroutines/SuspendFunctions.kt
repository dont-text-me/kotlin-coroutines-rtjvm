package com.ivanb.coroutines

import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import org.slf4j.LoggerFactory

object SuspendFunctions {
    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun takeTheBus() { // suspend = this code can run on a coroutine
        logger.info("Taking the bus")
        (0..10).forEach {
            logger.info("${it * 10}% done")
            delay(300) // yielding point i.e. coroutine can be suspended here, state is saved
        }
        logger.info("done")
    }

    // suspend functions CANNOT be run from regular functions

    // continuation = state of the code at the point a coroutine is suspended
    suspend fun demoSuspendedCoroutine() {
        logger.info("Starting to run some code...")
        val resumedComputation =
            suspendCancellableCoroutine { continuation ->
                logger.info("This runs when im suspended") // function is not marked as finished yet
                continuation.resumeWith(Result.success(42)) // now it is
            }
        logger.info("This runs after the coroutine with result $resumedComputation")
    }

    val suspendLambda: suspend (Int) -> Int = { it + 1 }
//    fun test(){
//        val three = suspendLambda(2) // not ok
//    }
}

suspend fun main() {
    SuspendFunctions.demoSuspendedCoroutine()
}
