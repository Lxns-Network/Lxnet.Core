package net.lxns.core.rpc

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import net.lxns.core.NoResponse
import net.lxns.core.RemoteCall
import net.lxns.core.serializers.UUIDSerializer
import java.util.UUID

@Serializable
@SerialName("SendMessageCall")
class SendMessageCall(
    val player: @Serializable(UUIDSerializer::class) UUID,
    val message: @Contextual Component,
    override var id: Int = 0
): RemoteCall<NoResponse>