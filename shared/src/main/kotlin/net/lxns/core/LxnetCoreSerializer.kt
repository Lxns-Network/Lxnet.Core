package net.lxns.core

import kotlinx.serialization.json.Json
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
val lxNetFormat = Json { serializersModule = module }