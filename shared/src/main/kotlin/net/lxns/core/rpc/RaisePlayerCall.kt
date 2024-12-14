package net.lxns.core.rpc

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import net.lxns.core.NoResponse
import net.lxns.core.RemoteCall

@Serializable
@SerialName("RaisePlayerCall")
class RaisePlayerCall(
    @Contextual
    val message: Component,
    override var id: Int = 0
) : RemoteCall<NoResponse>