package com.ivanb.jvmConcurrency

import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.concurrent.thread

object ThreadsBasics {
    // thread = independent unit of execution

    // Thread = data structure (maps to OS threads)
    // Runnable = piece of code to run

    val takingTheBus =
        Runnable {
            println("Taking the bus")
            (0..10).forEach {
                println("${it * 10}% done")
                Thread.sleep(300)
            }
            println("done")
        }

    fun runThread() {
        val thread = Thread(takingTheBus) // thread is just data at this point
        thread.start() // the code runs independently
    }

    fun runMultipleThreads() {
        val takingTheBus = Thread(takingTheBus)
        val listeningToMusic =
            thread {
                // same as Thread(Runnable {}) ALSO starts the thread
                println("Listening to music")
                Thread.sleep(2000)
                println("Done listening")
            }
        takingTheBus.start()
        // join threads = block until they finish
        takingTheBus.join()
        listeningToMusic.join()
    }

    // interruption

    val scrollingSocialMedia =
        thread(start = false) {
            while (true) {
                try {
                    println("scrolling....")
                    Thread.sleep(1000)
                } catch (_: InterruptedException) {
                    println("interrupted, stopping...")
                    return@thread // non-local return, stop gracefully
                }
            }
        }

    fun demoInterruption() {
        scrollingSocialMedia.start()
        // block after 3 secondds
        Thread.sleep(3000)
        scrollingSocialMedia.interrupt() // throws InterruptedException on that thread
        scrollingSocialMedia.join() // will block forever
    }

    // executors

    fun demoExecutorsAndFutures() {
        // thread pool
        val executor = Executors.newFixedThreadPool(8)
        executor.submit {
            (1..100).forEach {
                println("Counting to $it")
                Thread.sleep(100)
            }
        }

        // make a thread return a value = Future
        val future: Future<Int> =
            executor.submit(
                Callable {
                    // runs on one of the threads
                    println("Computing the meaning of life")
                    Thread.sleep(3000)
                    42
                },
            )
        println(
            "The meaning of life is ${future.get()}",
        ) // blocks the thread until the code is done
        executor.shutdown() // wait for all tasks to be done, no new tasks may be submitted
    }

    @JvmStatic
    fun main(args: Array<String>) {
        demoExecutorsAndFutures()
    }
}
