package com.ivanb.aktors.playground

import com.ivanb.aktors.ActorSystem
import com.ivanb.aktors.Behaviour
import com.ivanb.aktors.Behaviours
import org.slf4j.LoggerFactory

object WordCounter {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    operator fun invoke(): Behaviour<String> =
        Behaviours.Setup {
            logger.info("setting up...")
            var total = 0
            Behaviours.receiveMessage { msg ->
                val newCount = msg.split(" ").size
                total += newCount
                logger.info("Updated word count to $total")
                Behaviours.same()
            }
        }
}

object WordCounterStateless {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    operator fun invoke(): Behaviour<String> = active(0)

    private fun active(currentCount: Int): Behaviour<String> =
        Behaviours.receive { ctx, msg ->
            val newCount = msg.split(" ").size
            val newTotal = currentCount + newCount
            ctx.log.info("Updated word count to $newTotal")
            active(newTotal)
        }
}

object StatefulActorDemo {
    suspend fun main() =
        ActorSystem.app("WordcounterSystem", WordCounterStateless()) { guardian ->
            guardian `!` "This is an actor framework on top of coroutines"
            guardian `!` "Coroutines rock"
        }
}

suspend fun main() = StatefulActorDemo.main()
