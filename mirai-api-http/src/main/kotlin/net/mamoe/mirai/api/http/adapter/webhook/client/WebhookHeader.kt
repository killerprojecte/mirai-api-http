/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.api.http.adapter.webhook.client

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.*

class WebhookHeader(configuration: Configuration) {

    private val defaultHeaders: Headers = configuration.buildHeaders()

    private fun intercept(builder: HttpRequestBuilder) {
        defaultHeaders.forEach { n, v -> builder.header(n, v)}
        appendBotHeader(builder)
    }

    private fun appendBotHeader(builder: HttpRequestBuilder) {
        val botHeader = builder.attributes[webhookHeaderValue]
        builder.header("qq", botHeader)
        builder.header("X-qq", botHeader)
        builder.header("bot", botHeader)
        builder.header("X-bot", botHeader)
    }

    class Configuration {

        private val headers = HeadersBuilder()

        fun header(name: String, value: String) = headers.append(name, value)

        internal fun buildHeaders() = headers.build()
    }

    companion object Feature : HttpClientPlugin<Configuration, WebhookHeader> {

        override fun install(plugin: WebhookHeader, scope: HttpClient) {
            scope.requestPipeline.intercept(HttpRequestPipeline.State) {
                plugin.intercept(context)
            }
        }

        override fun prepare(block: Configuration.() -> Unit): WebhookHeader {
            val config = Configuration().apply(block)
            return WebhookHeader(config)
        }

        override val key: AttributeKey<WebhookHeader> = AttributeKey("WebhookHeader")

        val webhookHeaderValue: AttributeKey<String> = AttributeKey("WebhookHeaderValue")
    }
}

internal fun HttpRequestBuilder.botHeader(botHeader: String) {
    attributes.put(WebhookHeader.webhookHeaderValue, botHeader)
}
