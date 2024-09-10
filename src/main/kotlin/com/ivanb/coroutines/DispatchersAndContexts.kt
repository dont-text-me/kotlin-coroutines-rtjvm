package com.ivanb.coroutines

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

object DispatchersAndContexts {
    val logger = LoggerFactory.getLogger(this::class.java)

    // dispatcher = thread pool + scheduler of coroutines
    private val basicDispatcher = Dispatchers.Default

    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    suspend fun demoDispatchers() {
        val limitedDispatcher = basicDispatcher.limitedParallelism(1)
        val singleThreadDispatcher = newSingleThreadContext("single-thread")
        logger.info("demo limited dispatcher")
        coroutineScope {
            launch(limitedDispatcher) {
                (1..100).forEach {
                    logger.info("Running task $it")
                }
            }

            launch(limitedDispatcher) {
                logger.info("First tasks are over")
            }
            launch(limitedDispatcher) {
                (200..300).forEach {
                    logger.info("Running task $it")
                }
            }
            launch(limitedDispatcher) {
                logger.info("other tasks are over")
            }
        }
    }

    // coroutine context ~= Map
    val aContext = basicDispatcher
    val coroutineName = CoroutineName("myCoroutine")
    val combinedContext = aContext + coroutineName
    val nameExtracted = combinedContext[CoroutineName]

    suspend fun developer() {
        val coroutineName = coroutineContext[CoroutineName]?.name ?: "unknown"
        logger.info("I am a developer: $coroutineName")
        delay(1000)
        logger.info("i wrote code today")
    }

    suspend fun developerWithTeam() {
        val coroutineName = coroutineContext[CoroutineName]?.name ?: "unknown"
        val teamName = coroutineContext[TeamName]?.name ?: "unknown"
        logger.info("I am a developer: $coroutineName from team $teamName")
        delay(1000)
        logger.info("i wrote code today")
    }

    class TeamName(
        val name: String,
    ) : CoroutineContext.Element {
        override val key: CoroutineContext.Key<*> = Key

        companion object Key : CoroutineContext.Key<TeamName>
    }

    suspend fun startup() {
        logger.info("starting...")
        coroutineScope {
            launch(context = CoroutineName("team A")) {
                launch { developer() }
                launch { developer() } // child coroutines, inherit the team name
            }
            launch(context = CoroutineName("bob")) { developer() }
            withContext(CoroutineName("team B")) {
                launch { developer() }
                launch { developer() }
            }
        }
        logger.info("stopping...")
    }

    suspend fun startupTeams() {
        logger.info("starting...")
        coroutineScope {
            launch(context = CoroutineName("alice") + TeamName("frontend")) {
                launch { developerWithTeam() }
            }
            launch(context = CoroutineName("bob") + TeamName("backend")) { developerWithTeam() }
            withContext(CoroutineName("charlie")) {
                launch { developerWithTeam() }
            }
        }
        logger.info("stopping...")
    }
}

suspend fun main() {
    DispatchersAndContexts.startupTeams()
}
