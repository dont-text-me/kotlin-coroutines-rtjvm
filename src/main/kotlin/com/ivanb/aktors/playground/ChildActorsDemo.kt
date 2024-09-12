package com.ivanb.aktors.playground

import com.ivanb.aktors.ActorContext
import com.ivanb.aktors.ActorRef
import com.ivanb.aktors.ActorSystem
import com.ivanb.aktors.Behaviour
import com.ivanb.aktors.Behaviours
import kotlinx.coroutines.delay

class ChildActorsDemo {
    sealed interface Command

    data class CreateChild(
        val name: String,
    ) : Command

    data class TellChild(
        val message: String,
    ) : Command

    data object StopChild : Command

    object Parent {
        operator fun invoke(): Behaviour<Command> = idle()

        private fun <T> unrecognisedCommand(
            ctx: ActorContext<T>,
            msg: Command,
        ): Behaviour<T> =
            Behaviours.same<T>().also {
                ctx.log.info("[${ctx.name}] I dont recognise this message: $msg")
            }

        private fun idle(): Behaviour<Command> =
            Behaviours.receive { ctx, msg ->
                when (msg) {
                    is CreateChild -> {
                        ctx.log.info("[${ctx.name}] Creating child with name ${msg.name}")
                        withChild(ctx.spawn("${ctx.name}/${msg.name}", Child()))
                    }
                    else -> unrecognisedCommand(ctx, msg)
                }
            }

        private fun withChild(childRef: ActorRef<String>): Behaviour<Command> =
            Behaviours.receive { ctx, msg ->
                when (msg) {
                    is TellChild -> {
                        ctx.log.info("[${ctx.name}] sending message to my child: ${msg.message}")
                        childRef `!` msg.message
                        Behaviours.same()
                    }
                    is StopChild -> {
                        ctx.log.info("[${ctx.name}] stopping my child")
                        childRef `!` "STOP"
                        idle()
                    }
                    else -> unrecognisedCommand(ctx, msg)
                }
            }
    }

    object Child {
        operator fun invoke(): Behaviour<String> =
            Behaviours.receive { ctx, msg ->
                ctx.log.info("[${ctx.name}] Ive received $msg")
                if (msg == "STOP") Behaviours.stopped() else Behaviours.same()
            }
    }
}

suspend fun main() =
    ActorSystem.app("ParentChildDemo", ChildActorsDemo.Parent()) { parent ->
        parent `!` ChildActorsDemo.CreateChild("child")
        delay(1000)
        parent `!` ChildActorsDemo.TellChild("Hello child")
        delay(1000)
        parent `!` ChildActorsDemo.TellChild("hello again")
        delay(1000)
        parent `!` ChildActorsDemo.StopChild
        delay(1000)
        parent `!` ChildActorsDemo.TellChild("hello again")
    }
