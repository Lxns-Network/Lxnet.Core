package net.lxns.core

import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import net.kyori.adventure.text.Component
import net.lxns.core.rpc.AddPlayerScoreCall
import net.lxns.core.rpc.FetchPlayerScoreCall
import net.lxns.core.rpc.GlobalBroadcastCall
import net.lxns.core.rpc.RaisePlayerCall
import net.lxns.core.rpc.SendMessageCall
import net.lxns.core.serializers.ComponentSerializer
import net.lxns.core.serializers.UUIDSerializer
import java.io.InputStream

internal val module = SerializersModule {
    polymorphic(RemoteCall::class) {
        subclass(AddPlayerScoreCall::class)
        subclass(FetchPlayerScoreCall::class)
        subclass(GlobalBroadcastCall::class)
        subclass(RaisePlayerCall::class)
        subclass(SendMessageCall::class)
    }
    contextual(Component::class) { ComponentSerializer }
    contextual(UUIDSerializer)
    polymorphic(RemoteResponse::class) {
        subclass(FetchPlayerScoreCall.Response::class)
        subclass(NoResponse::class)
    }
}

typealias LxNetFormatter = Json

val lxNetFormat: LxNetFormatter = Json { serializersModule = module }

private val responsePolymorphicSerializer = PolymorphicSerializer(RemoteResponse::class)
private val callPolymorphicSerializer = PolymorphicSerializer(RemoteCall::class)

fun LxNetFormatter.encodeResponse(response: RemoteResponse) =
    encodeToString(responsePolymorphicSerializer, response)

fun LxNetFormatter.decodeResponse(response: String) =
    decodeFromString(responsePolymorphicSerializer, response)

fun LxNetFormatter.decodeResponse(response: InputStream) =
    decodeFromStream(responsePolymorphicSerializer, response)

fun LxNetFormatter.encodeCall(call: RemoteCall<*>) =
    encodeToString(callPolymorphicSerializer, call)

fun LxNetFormatter.decodeCall(call: String) =
    decodeFromString(callPolymorphicSerializer, call)

fun LxNetFormatter.decodeCall(call: InputStream) =
    decodeFromStream(callPolymorphicSerializer, call)

