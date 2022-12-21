/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.api.http.adapter.http.router

import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import net.mamoe.mirai.api.http.adapter.http.HttpAdapter
import net.mamoe.mirai.api.http.adapter.http.feature.auth.Authorization
import net.mamoe.mirai.api.http.adapter.http.feature.handler.HttpRouterAccessHandler
import net.mamoe.mirai.api.http.context.MahContextHolder


fun Application.httpModule(adapter: HttpAdapter) {
    install(DefaultHeaders)
    install(CORS) {
        allowNonSimpleContentTypes = true
        maxAgeInSeconds = 86_400 // aka 24 * 3600

        adapter.setting.cors.forEach {
            allowHost(it, schemes = listOf("http", "https"))
        }
    }

    install(Authorization)
    install(HttpRouterAccessHandler) { enableAccessLog = MahContextHolder.debug }

    authRouter(adapter.setting)
    messageRouter()
    eventRouter()
    infoRouter()
    friendManageRouter()
    groupManageRouter()
    aboutRouter()
    fileRouter()
    commandRouter()
    announcementRouter()
}
