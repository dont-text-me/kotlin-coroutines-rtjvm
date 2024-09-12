package com.ivanb.advanced

import jdk.internal.net.http.common.Log.channel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import kotlin.random.Random

object Channels {
    val logger = LoggerFactory.getLogger(javaClass)

    // channel = concurrent queue
    data class StockPrice(
        val symbol: String,
        val price: Double,
        val timestamp: Long,
    )

    val aChannel = Channel<StockPrice>()

    suspend fun pushStocks(channel: SendChannel<StockPrice>) {
        channel.send(StockPrice("AAPL", 100.0, System.currentTimeMillis()))
        delay(Random.nextLong(1000))
        channel.send(StockPrice("GOOG", 200.0, System.currentTimeMillis()))
        delay(Random.nextLong(1000))
        channel.send(StockPrice("MSFT", 50.0, System.currentTimeMillis()))

        channel.close()
    }

    suspend fun readStocks(channel: ReceiveChannel<StockPrice>) {
        repeat(5) {
            val result = channel.receiveCatching()
            when {
                result.isSuccess -> logger.info("received result: ${result.getOrNull()}")
                result.isClosed -> logger.info("No results. Channel is closed")
                result.isFailure -> logger.info("No results. Error: ${result.exceptionOrNull()}")
            }
        }
    }

    suspend fun stockMarketTerminal() =
        coroutineScope {
            val stockChannel = Channel<StockPrice>()
            launch { pushStocks(stockChannel) }
            launch { readStocks(stockChannel) }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun stockMarketBetter() =
        coroutineScope {
            val stocksChannel =
                produce {
                    // run some operations on a channel and then close it
                    pushStocks(channel)
                } // will automatically close the channel
            launch { readStocks(stocksChannel) }
        }

    suspend fun demoCustomChannels() =
        coroutineScope {
            val stocksChannel =
                Channel<StockPrice>(
                    capacity = 2,
                    onBufferOverflow =
                        BufferOverflow
                            .SUSPEND, // what to do when buffer is full (wait or drop elements)
                )
            launch {
                pushStocks(stocksChannel)
                // if the buffer is full, any send() will block
            }
            launch {
                logger.info("waiting for consumer to start...")
                delay(5000)
                readStocks(stocksChannel)
            }
        }

    // closing = cannot send() any more elements, but can receive() elements currently in the
    // channel
    // cancelling = closing + dropping all current elements in the channel

    suspend fun demoOnUndelivered() =
        coroutineScope {
            val channel =
                Channel<StockPrice>(
                    capacity = 10,
                    onUndeliveredElement = { logger.info("Just dropped $it") },
                )

            launch {
                channel.send(StockPrice("AAPL", 100.0, System.currentTimeMillis()))
                delay(50)
                channel.send(StockPrice("GOOG", 200.0, System.currentTimeMillis()))
                delay(50)
                channel.send(StockPrice("MSFT", 50.0, System.currentTimeMillis()))
                delay(50)
                channel.send(StockPrice("AMZN", 250.0, System.currentTimeMillis()))
            }
            launch {
                repeat(2) {
                    logger.info("Received ${channel.receive()}")
                    delay(1000)
                }
                channel.cancel()
            }
        }
}

suspend fun main() {
    Channels.demoOnUndelivered()
}
