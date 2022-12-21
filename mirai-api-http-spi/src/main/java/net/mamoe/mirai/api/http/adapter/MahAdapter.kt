/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.api.http.adapter

import net.mamoe.mirai.api.http.context.session.Session
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.utils.MiraiLogger

/**
 * Mah 接口规范，用于处理接收、发送消息后的处理逻辑
 * 不同接口格式请实现该接口
 */
abstract class MahAdapter(val name: String = "Abstract MahAdapter") {

    protected val log = MiraiLogger.Factory.create(this::class, "$name adapter")

    /**
     * 初始化
     */
    abstract fun initAdapter()

    /**
     * 启用
     */
    abstract fun enable()

    /**
     * 停止
     */
    abstract fun disable()

    abstract suspend fun onReceiveBotEvent(event: BotEvent, session: Session)
}
