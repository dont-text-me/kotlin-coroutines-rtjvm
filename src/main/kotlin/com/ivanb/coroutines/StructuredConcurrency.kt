package com.ivanb.coroutines

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import java.net.URI
import kotlin.random.Random

object StructuredConcurrency {
    val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun fetchHTML(url: String): String {
        delay(1000)
        return URI(url).toURL().readText()
    }

    suspend fun processData(data: String): String {
        delay(500)
        return "Processed: ${data.split("\n")[0]}"
    }

    suspend fun fetchAndProcessData(): String =
        coroutineScope {
            val urls =
                listOf("https://www.google.com", "https://www.apple.com", "https://www.samsung.com")
            val deferredResults = urls.map { async { fetchHTML(it) } }
            val results = deferredResults.awaitAll()

            val deferredData = results.map { async { processData(it) } }

            deferredData.awaitAll().joinToString("\n")
        }

    suspend fun fetchAndProcessDataNested(): String =
        coroutineScope {
            val urls =
                listOf("https://www.google.com", "https://www.apple.com", "https://www.samsung.com")
            val htmls = coroutineScope { urls.map { async { fetchHTML(it) } }.awaitAll() }
            val data = coroutineScope { htmls.map { async { processData(it) } }.awaitAll() }
            data.joinToString("\n")
        }

    suspend fun fetchDataFromPage(pageUrl: String): String {
        delay(Random.nextLong(1000))
        return "Data from $pageUrl"
    }

    suspend fun scrape(
        site: String,
        pages: List<String>,
    ): String =
        coroutineScope {
            pages.map { async { fetchDataFromPage("$site/$it") } }.awaitAll().joinToString("\n")
        }

    suspend fun scrapeAll(locations: List<Pair<String, List<String>>>): List<String> =
        coroutineScope {
            locations.map { (site, pages) -> async { scrape(site, pages) } }.awaitAll()
        }
}

suspend fun main() {
    println(
        StructuredConcurrency.scrapeAll(
            listOf(
                "rockthejvm.com" to listOf("courses/kotlin", "courses/scala"),
                "google.com" to listOf("search", "store"),
            ),
        ),
    )
}
