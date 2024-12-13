package net.lxns.core.rpc

import kotlinx.serialization.Serializable
import net.lxns.core.RemoteCall
import net.lxns.core.serializers.UUIDSerializer
import java.util.UUID

@Serializable
class FetchPlayerScoreCall(
    val player: @Serializable(UUIDSerializer::class) UUID,
    override var id: Int
) : RemoteCall<FetchPlayerScoreCall.Response>{
    @Serializable
    class Response (
        val score: Int,
        override var id: Int
    ): RemoteCall<Nothing>
}