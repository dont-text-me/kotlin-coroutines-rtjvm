package com.ivanb.aktors.playground

import com.ivanb.aktors.ActorSystem
import com.ivanb.aktors.Behaviours
import org.slf4j.LoggerFactory

object AktorPlayground {
    val logger = LoggerFactory.getLogger(this.javaClass)
    val loggingBehaviour =
        Behaviours.receiveMessage<String> {
            logger.info("Message received: $it")
            Behaviours.same()
        }

    suspend fun main() {
        ActorSystem.app<String>("FirstActorSystem", loggingBehaviour) { guardian ->
            (1..100).forEach {
                guardian `!` "$it"
            }
        }
    }
}

suspend fun main() {
    AktorPlayground.main()
}
