package com.ivanb.coroutines
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import kotlin.random.Random

object CoroutineBuilders {
    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun developer(index: Int) {
        logger.info("[dev $index] i am a developer, i need coffee")
        delay(Random.nextLong(1000))
        logger.info("[dev $index] i got coffee")
    }

    suspend fun projectManager() {
        logger.info("[PM] i am a PM, i need to check the progress")
        delay(Random.nextLong(1000))
        logger.info("[PM] i checked progress")
    }

    suspend fun startup() {
        logger.info("Its 9 am, lets start")
        // coroutine scope
        coroutineScope {
            // the ability to launch coroutines concurrently
            launch { developer(42) }
            launch { projectManager() }
            // we don't know which OS thread(s) run these coroutines
        } // will block until all coroutines inside are finished
        logger.info("Its 6 pm, lets finish")
    }

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun globalStartup() {
        logger.info("Its 9 am, lets start")
        // global scope - for the duration of the entire app
        val dev1 = GlobalScope.launch { developer(1) }
        val dev2 = GlobalScope.launch { developer(2) }
        // manually join coroutines
        dev1.join()
        dev2.join()
        logger.info("Its 6 pm, lets finish")
    }

    // async - return a value out of a coroutine

    suspend fun developerCoding(index: Int): String {
        logger.info("[dev $index] i am a developer, i need coffee")
        delay(Random.nextLong(1000))
        logger.info("[dev $index] i got coffee")
        return """
            fun main() {
                println("this is kotlin")
            }
            """.trimIndent()
    }

    suspend fun projectManagerEstimating(): Int {
        logger.info("[PM] i am a PM, i need to check the progress")
        delay(Random.nextLong(1000))
        logger.info("[PM] i checked progress")
        return 12
    }

    data class Project(
        val code: String,
        val estimation: Int,
    )

    suspend fun startupValues() {
        logger.info("Its 9 am, lets start")
        // coroutine scope
        val project =
            coroutineScope {
                val deferredCode: Deferred<String> = async { developerCoding(42) }
                val deferredEst: Deferred<Int> = async { projectManagerEstimating() }
                val code = deferredCode.await() // blocking
                val estimation = deferredEst.await()

                Project(code, estimation)
            } // will block until all coroutines inside are finished
        logger.info("Its 6 pm, we have the project $project")
    }
}

suspend fun main() {
    CoroutineBuilders.startupValues()
}
