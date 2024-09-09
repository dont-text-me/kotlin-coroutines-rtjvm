package com.ivanb.jvmConcurrency

import java.util.concurrent.Executors
import kotlin.random.Random

object VirtualThreads {
    // virtual threads - simpler data structures, managed/scheduled by the JVM
    // CPUs <-- (OS scheduler) OS threads <-- (JVM scheduler) virtual threads

    fun indefinitely() {
        val threads =
            (1..1000000).map {
                Thread.ofVirtual().start {
                    while (true) {
                        // do nothing
                    }
                }
            }
        Thread.sleep(5000)
        println("Virtual threads complete")
    }

    fun demoVTFactory() {
        val factory = Thread.ofVirtual().name("rtjvm-", 0).factory()
        (1..1000000).map {
            factory
                .newThread {
                    while (true) {
                        Thread.sleep(Random.nextLong(1000))
                        println("[${Thread.currentThread().name}] i am a virtual thread")
                    }
                }.start()
        }

        Thread.sleep(5000)
        println("Virtual threads complete")
    }

    fun demoVTExecutor() {
        val executor = Executors.newVirtualThreadPerTaskExecutor()
        (1..1000000).map {
            executor.submit {
                while (true) {
                    Thread.sleep(Random.nextLong(1000))
                    println("[${Thread.currentThread().name}] i am a virtual thread")
                }
            }
        }

        Thread.sleep(7000)
        println("Virtual threads complete")
        executor.shutdown()
    }

    fun threadWhichNeverYields() =
        Runnable {
            println("I will never block")
            while (true) {
            }
        }

    fun threadWhichWantsToRun() =
        Runnable {
            println("I would like to run. if this prints, i ran")
        }

    fun cooperativeFailureDemo() {
        val factory = Thread.ofVirtual().name("routine-", 0).factory()
        val executor = Executors.newThreadPerTaskExecutor(factory)
        executor.submit(threadWhichNeverYields())
        executor.submit(threadWhichWantsToRun())
        Thread.sleep(5000)
        executor.shutdown()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        // requires the following JVM arg:
        // -Djdk.virtualThreadScheduler.maxPoolSize=1 - max number of OS threads on the JVM

        cooperativeFailureDemo()
    }
}
