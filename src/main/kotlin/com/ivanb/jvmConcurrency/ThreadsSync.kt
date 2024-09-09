package com.ivanb.jvmConcurrency

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.random.Random

object ThreadsSync {
    // race condition
    var coffeeMachine = 0
    val coffeeMachineLock = ReentrantLock()

    fun developer(index: Int) =
        Runnable {
            println("[$index] I am a developer, i need coffee")
            Thread.sleep(Random.nextLong(1000))
            coffeeMachine += 1 // race condition
            println("[$index] got coffee")
        }

    fun syncDeveloper(index: Int) =
        Runnable {
            println("[$index] I am a developer, i need coffee")
            Thread.sleep(Random.nextLong(1000))
            // block other threads if here
            coffeeMachineLock.lock() // no two threads can lock at the same time
            coffeeMachine += 1 // thread safe: only 1 thread can access this area
            coffeeMachineLock.unlock()
            // unblock
            println("[$index] got coffee")
        }

    fun developerRaceCondition() {
        (1..10000).forEach {
            Thread(syncDeveloper(it)).start()
        }
        Thread.sleep(3000)
        println("Coffee machine has served $coffeeMachine coffees")
    }

    fun developersAndMaintenance() {
        (1..10000).forEach {
            Thread(syncDeveloper(it)).start()
        }
        // maintainer

        thread {
            Thread.sleep(500)
            coffeeMachineLock.lock()
            println("Maintenance in progress")
            Thread.sleep(Random.nextLong(1000))
            println("Maintenance complete")
            coffeeMachineLock.unlock()
        }

        Thread.sleep(3000)
        println("Coffee machine has served $coffeeMachine coffees")
    }

    // deadlock
    var userStories = 0
    var estimation = 0
    val usLock = ReentrantLock()
    val estLock = ReentrantLock()

    fun manager() =
        Thread {
            println("I am a PM, i need an estimation to proceed")
            estLock.lock()
            Thread.sleep(1000)
            usLock.lock()
            userStories = 4
            println("I am a PM, user stories are completed")
            estLock.unlock()
            usLock.unlock()
        }

    fun developer() =
        Thread {
            println("I am a developer, i need user stories to make an estimation")
            usLock.lock()
            Thread.sleep(1000)
            estLock.lock()
            println("I am a developer, estimation is complete")
            estimation = 15
            usLock.unlock()
            estLock.unlock()
        }

    fun demoDeadlock() {
        manager().start()
        developer().start()
    }
    // livelock - multiple threads do work, but dont make any progress

    data class Friend(
        val name: String,
    ) {
        var side = "right"
        val lock = ReentrantLock()

        fun bow(another: Friend) {
            println("$name: i am bowing to my friend ${another.name}")
            another.rise(this)
            println("$name: my friend ${another.name} has risen")
            another.pass(this)
        }

        fun rise(another: Friend) = println("$name: I am rising to my friend ${another.name}")

        fun switchSide() {
            lock.lock()
            side = if (side == "right") "left" else "right"
            lock.unlock()
        }

        fun pass(another: Friend) {
            while (this.side == another.side) {
                println("$name: ${another.name}, please go first...")
                switchSide()
                bow(another)
            }
        }
    }

    fun demoLiveLock() {
        val friend1 = Friend("John")
        val friend2 = Friend("Steve")
        Thread { friend1.bow(friend2) }.start()
        Thread { friend2.bow(friend1) }.start()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        demoLiveLock()
    }
}
