@file:Suppress("unused")

package com.ivanb.advanced

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import kotlin.random.Random

data class Product(
    val id: Int,
    val name: String,
    val price: Double,
)

object Flows {
    val logger = LoggerFactory.getLogger(javaClass)

    // flow = potentially infinite "list"
    val products =
        listOf(
            Product(1, "phone", 999.99),
            Product(2, "laptop", 1999.99),
            Product(3, "tablet", 399.99),
            Product(4, "watch", 499.99),
        )

    val productsFlow: Flow<Product> = flowOf(*products.toTypedArray()) // emitted at a later point
    val productsFlow2 = products.asFlow()

    // emitting values
    val delayedProducts: Flow<Product> =
        flow {
            // emit elements in this scope
            products.forEach {
                emit(it)
                delay(500)
            }
        }

    // transformers
    val productNamesCaps = delayedProducts.map { it.name.uppercase() }

    suspend fun totalValue(): Double = delayedProducts.fold(0.0) { acc, product -> acc + product.price } // emits the last value

    val scannedValue: Flow<Double> =
        delayedProducts.scan(0.0) { acc, product ->
            acc + product.price
        } // emits all intermediate values

    val flowWithExceptions: Flow<Product> =
        flow {
            emit(Product(1, "phone", 999.99))
            if (Random.nextBoolean()) {
                throw RuntimeException("Cannot fetch next product")
            }
            emit(Product(2, "laptop", 1999.99))
            delay(500)
            emit(Product(3, "tablet", 399.99))
        }.retry { e ->
            e is RuntimeException // retry from beginning
        }.catch { e ->
            logger.info("Caught error $e")
            emit(Product(0, "unknown", 0.0)) // emit fallback
        }

    val productsWithSideEffects: Flow<Product> =
        delayedProducts.onEach { logger.info("Generated product $it") }

    // combine multiple flows: merge, concat, zip

    val orders =
        flow {
            (1..4).forEach {
                delay(500)
                emit(it)
            }
        }

    data class Order(
        val id: Int,
        val quantity: Int,
    )

    val zippedOrders: Flow<Order> =
        delayedProducts.zip(orders) { prod, q ->
            Order(prod.id, q) // do not emit UNTIL there is a value from each flow is available
        }

    val mergedProducts = merge(productsFlow, delayedProducts)
    val concatenatedProducts =
        flow {
            emitAll(delayedProducts)
            emitAll(productsFlow)
        }

    // assume these are some external API i.e. cannot be modified ==========

    /**
     * Exercise: weather station
     * 1. transform temps to Fahrenheit (9/5 * c + 32)
     * 2. calculate the latest average across all locations - emit all the averages
     * 3. catch any exception and retry the flow 3 times max
     * 4. print the avg temperatures
     * 5. run this flow for 10 seconds, then cancel
     * 6. do the same thing per location
     */
    data class TemperatureReading(
        val location: String,
        val temperature: Double,
        val timestamp: Long,
    )

    suspend fun readTemperatures(): Flow<TemperatureReading> =
        flow {
            val locations = listOf("Paris", "Berlin", "London", "Manchester")
            while (true) {
                val location = locations.random()
                val temperature = (15..40).random() + Random.nextInt(10) * 0.1
                val timestamp = System.currentTimeMillis()
                if (Random.nextInt(1000) < 10) {
                    throw RuntimeException("Weather station error")
                }
                emit(TemperatureReading(location, temperature, timestamp))
                delay(Random.nextLong(1000))
            }
        }

    // =====================================================================

    fun Double.toFahrenheit() = this * (9 / 5) + 32

    suspend fun getFahrenheitTemps(): Flow<TemperatureReading> =
        readTemperatures()
            .map { it.copy(temperature = it.temperature.toFahrenheit()) }
            .retry(3) { (it is RuntimeException).also { logger.info("retrying....") } }
            .catch { logger.info("Caught more than 3 errors, stopping... ($it)") }

    suspend fun getAvgFahrenheitTemps(): Flow<Double> =
        getFahrenheitTemps()
            .scan(emptyMap<String, Double>()) { acc, (loc, temp, _) -> acc + (loc to temp) }
            .map { if (it.isNotEmpty()) (it.values.sum() / it.values.size) else 0.0 }

    suspend fun getAvgTempsPerLocation(): Flow<List<Pair<String, Double>>> =
        getFahrenheitTemps()
            .scan(emptyMap<String, List<Double>>()) { acc, (loc, temp, _) ->
                val currentReadingsForCity = acc.getOrElse(loc) { emptyList() }
                acc + (loc to currentReadingsForCity + temp)
            }.map {
                it.map { (loc, readings) ->
                    if (readings.isEmpty()) loc to 0.0 else loc to readings.sum() / readings.size
                }
            }

    suspend fun demoTempFlows() =
        coroutineScope {
            logger.info("starting...")
            val job = launch { getAvgTempsPerLocation().collect { logger.info("$it") } }
            launch {
                delay(10000)
                logger.info("stopping...")
                job.cancel()
            }
        }
}

suspend fun main() {
    Flows.demoTempFlows()
}
