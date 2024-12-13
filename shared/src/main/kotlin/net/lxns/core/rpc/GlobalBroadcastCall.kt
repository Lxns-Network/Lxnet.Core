package net.lxns.core.rpc

import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import net.lxns.core.RemoteCall
import net.lxns.core.serializers.ComponentSerializer

@Serializable
class GlobalBroadcastCall(
    @Serializable(ComponentSerializer::class)
    val message: Component,
    override var id: Int = 0
) : RemoteCall<Nothing>