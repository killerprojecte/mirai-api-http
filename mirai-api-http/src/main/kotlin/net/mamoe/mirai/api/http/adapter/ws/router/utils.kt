/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.api.http.adapter.ws.router

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.util.*
import io.ktor.websocket.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.api.http.adapter.common.StateCode
import net.mamoe.mirai.api.http.adapter.internal.serializer.toJson
import net.mamoe.mirai.api.http.adapter.internal.serializer.toJsonElement
import net.mamoe.mirai.api.http.adapter.ws.dto.WsOutgoing
import net.mamoe.mirai.api.http.adapter.ws.extension.FrameLogExtension
import net.mamoe.mirai.api.http.context.MahContextHolder


@KtorDsl
internal inline fun Route.miraiWebsocket(
    path: String,
    crossinline body: suspend DefaultWebSocketServerSession.(String) -> Unit
) {
    webSocket(path) {
        val verifyKey = call.request.headers["verifyKey"] ?: call.parameters["verifyKey"]
        val sessionKey = call.request.headers["sessionKey"] ?: call.parameters["sessionKey"]
        val qq = (call.request.headers["qq"] ?: call.parameters["qq"])?.toLongOrNull()

        // 注入无协商的扩展
        installExtension(FrameLogExtension)

        // 校验
        if (MahContextHolder.enableVerify && MahContextHolder.sessionManager.verifyKey != verifyKey) {
            closeWithCode(StateCode.AuthKeyFail)
            return@webSocket
        }

        // single 模式
        if (MahContextHolder.singleMode) {
            body(MahContextHolder.createSingleSession(verified = true).key)
            return@webSocket
        }

        // 注册新 session
        if (sessionKey == null && qq != null) {
            val bot = Bot.getInstanceOrNull(qq)
            if (bot == null) {
                closeWithCode(StateCode.NoBot)
                return@webSocket
            }

            val session = with(MahContextHolder.sessionManager) {
                authSession(bot, createTempSession().key)
            }

            body(session.key)
            return@webSocket
        }

        // 非 single 模式校验已有 session
        if (sessionKey == null) {
            closeWithCode(StateCode.InvalidParameter)
            return@webSocket
        }

        val session = MahContextHolder[sessionKey]

        if (session == null) {
            closeWithCode(StateCode.IllegalSession)
            return@webSocket
        }

        if (!session.isAuthed) {
            closeWithCode(StateCode.NotVerifySession)
            return@webSocket
        }

        session.ref()
        body(session.key)
    }
}

internal suspend fun DefaultWebSocketServerSession.closeWithCode(code: StateCode) {
    outgoing.send(Frame.Text(
        WsOutgoing(syncId = "", code.toJsonElement()).toJson()
    ))
    close(CloseReason(CloseReason.Codes.NORMAL, code.msg))
}


internal fun <T: WebSocketExtension<*>> WebSocketServerSession.installExtension(factory: WebSocketExtensionFactory<*, T>) {
    application.plugin(WebSockets).extensionsConfig.build().find { it.factory.key == factory.key }?.let {
        (extensions as MutableList<WebSocketExtension<*>>).add(it)
    }
}
