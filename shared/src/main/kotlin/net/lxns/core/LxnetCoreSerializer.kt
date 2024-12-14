package net.lxns.core

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import net.lxns.core.rpc.AddPlayerScoreCall
import net.lxns.core.rpc.FetchPlayerScoreCall
import net.lxns.core.rpc.GlobalBroadcastCall
import net.lxns.core.rpc.RaisePlayerCall

internal val module = SerializersModule {
    polymorphic(RemoteCall::class) {
        subclass(AddPlayerScoreCall::class)
        subclass(FetchPlayerScoreCall::class)
        subclass(GlobalBroadcastCall::class)
        subclass(RaisePlayerCall::class)
    }
    polymorphic(RemoteResponse::class) {
        subclass(FetchPlayerScoreCall.Response::class)
    }
}
val lxNetFormat = Json { serializersModule = module }