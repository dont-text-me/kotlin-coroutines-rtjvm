package com.ivanb.coroutines

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.slf4j.LoggerFactory
import kotlin.random.Random

object CooperativeScheduling {
    val logger = LoggerFactory.getLogger(CooperativeScheduling::class.java)

    suspend fun greedyDeveloper() {
        logger.info("I want all the coffee")
        while (System.currentTimeMillis() % 10000 != 0L) {
        }
        logger.info("Done with coffee")
    }

    suspend fun almostGreedyDeveloper() {
        logger.info("I want all the coffee")
        while (System.currentTimeMillis() % 10000 != 0L) {
            yield() // fundamental suspension point
        }
        logger.info("Done with coffee")
    }

    suspend fun developer(index: Int) {
        logger.info("[dev $index]I want some coffee")
        delay(Random.nextLong(1000))
        logger.info("[dev $index]done with coffee")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun startup() {
        logger.info("Starting up...")
        val singleThread = Dispatchers.Default.limitedParallelism(1)
        coroutineScope {
            launch(context = singleThread) { almostGreedyDeveloper() }
            launch(context = singleThread) { developer(42) }
        }
        logger.info("Stopping...")
    }

    // never run heavy computations in coroutines without some suspension points
}

suspend fun main() {
    CooperativeScheduling.startup()
}
