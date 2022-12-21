/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.api.http.adapter.internal.serializer

import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import net.mamoe.mirai.api.http.adapter.common.StateCode
import net.mamoe.mirai.api.http.adapter.internal.dto.*
import kotlin.reflect.KClass

@OptIn(InternalSerializationApi::class)
internal class BuiltinJsonSerializer : InternalSerializer {

    /**
     * Json解析规则，需要注册支持的多态的类
     */
    private val json by lazy {
        Json {
            encodeDefaults = true
            isLenient = true
            ignoreUnknownKeys = true

            serializersModule = SerializersModule {
                polymorphicSealedClass(EventDTO::class, MessagePacketDTO::class)
                polymorphicSealedClass(EventDTO::class, BotEventDTO::class)
            }
        }
    }

    /**
     * 从 sealed class 里注册到多态序列化
     */
    @InternalSerializationApi
    @Suppress("UNCHECKED_CAST")
    private fun <B : Any, S : B> SerializersModuleBuilder.polymorphicSealedClass(
        baseClass: KClass<B>,
        sealedClass: KClass<S>
    ) {
        sealedClass.sealedSubclasses.forEach {
            val c = it as KClass<S>
            polymorphic(baseClass, c, c.serializer())
        }
    }

    override fun <T : Any> encode(dto: T, clazz: KClass<T>): String = when (dto) {
        is StateCode -> json.encodeToString(StateCode.serializer(), dto)
        else -> json.encodeToString(clazz.serializer(), dto)
    }

    override fun <T : Any> encode(list: List<T>, clazz: KClass<T>): String {
        return json.encodeToString(ListSerializer(clazz.serializer()), list)
    }

    override fun <T : Any> encodeElement(dto: T, clazz: KClass<T>): JsonElement = when (dto) {
        is StateCode -> json.encodeToJsonElement(StateCode.serializer(), dto)
        else -> json.encodeToJsonElement(clazz.serializer(), dto)
    }

    override fun <T : Any> encodeElement(list: List<T>, clazz: KClass<T>): JsonElement {
        return json.encodeToJsonElement(ListSerializer(clazz.serializer()), list)
    }

    override fun <T : Any> decode(content: String, clazz: KClass<T>): T {
        return json.decodeFromString(clazz.serializer(), content)
    }

    override fun <T : Any> decode(element: JsonElement, clazz: KClass<T>): T {
        return json.decodeFromJsonElement(clazz.serializer(), element)
    }

}
