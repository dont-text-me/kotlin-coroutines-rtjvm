package com.ivanb.coroutines

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import kotlin.random.Random

object Cancellation {
    val logger = LoggerFactory.getLogger(javaClass)

    suspend fun developer(index: Int) {
        logger.info("[dev $index] I am a developer")
        while (true) {
            delay(1000) // if a coroutine doesnt have a suspension point, it is NOT cancellable
            logger.info("[dev $index] developing")
        }
    }

    suspend fun ceo(employee: Job) {
        logger.info("[CEO] Im the CEO")
        delay(Random.nextLong(3000))
        employee.cancel()
        logger.info("[CEO] I have fired the developer")
        employee.invokeOnCompletion { logger.info("employee was fired") } // job will be cancelled at the next suspension point
    }

    suspend fun developerWithTry(index: Int) {
        logger.info("[dev $index] I am a developer")
        try {
            while (true) {
                delay(1000) // if a coroutine doesnt have a suspension point, it is NOT cancellable
                logger.info("[dev $index] developing")
            }
        } catch (e: CancellationException) {
            logger.info("[dev $index] i am being fired")
            // NOTE: need to bubble up the exception
            // otherwise, cancellation is IGNORED
            throw e
        } finally {
            logger.info("[dev $index] i have left")
        }
    }

    class Laptop(
        val name: String,
    ) : AutoCloseable {
        init {
            logger.info("Providing the laptop $name")
        }

        override fun close() {
            logger.info("Closing the laptop $name")
        }
    }

    suspend fun developerAtWork(index: Int) {
        Laptop("macbook").use { laptop ->
            logger.info("[dev $index] I am a developer using laptop ${laptop.name}]")
            while (true) {
                delay(1000) // if a coroutine doesnt have a suspension point, it is NOT cancellable
                logger.info("[dev $index] developing on laptop ${laptop.name}")
            }
        }
    }

    suspend fun startup() {
        logger.info("starting...")
        coroutineScope {
            val lazyDev = launch { developerWithTry(42) }
            val otherDev = launch { developer(3) }
            launch { ceo(lazyDev) }
            delay(4000)
            otherDev.cancel()
        }
        logger.info("stopping...")
    }

    suspend fun startup2() {
        logger.info("starting...")
        coroutineScope {
            val developer = launch { developerAtWork(3) }
            launch { ceo(developer) } // NOTE: laptop gets automatically released when developer is cancelled
        }
        logger.info("stopping...")
    }
}

suspend fun main() {
    Cancellation.startup()
}
