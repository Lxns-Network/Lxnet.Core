package net.lxns.core.rpc

import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import net.lxns.core.Nothing
import net.lxns.core.RemoteCall

@Serializable
class RaisePlayerCall(
    val message: Component,
    override var id: Int = 0
) : RemoteCall<Nothing>