package net.lxns.core.rpc

import kotlinx.serialization.Serializable
import net.lxns.core.RemoteCall
import net.lxns.core.serializers.UUIDSerializer
import java.util.UUID

@Serializable
class FetchPlayerScoreCall(
    val player: @Serializable(UUIDSerializer::class) UUID,
    override val id: Int
) : RemoteCall{
    @Serializable
    class Response (
        val score: Int,
        override val id: Int
    ): RemoteCall
}