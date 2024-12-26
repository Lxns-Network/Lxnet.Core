package net.lxns.core.rpc

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.lxns.core.RemoteCall
import net.lxns.core.RemoteResponse
import net.lxns.core.serializers.UUIDSerializer
import java.util.UUID

@Serializable
@SerialName("FetchPlayerScoreCall")
class FetchPlayerScoreCall(
    val player: @Contextual UUID,
    override var id: Int = 0
) : RemoteCall<FetchPlayerScoreCall.Response>{

    @Serializable
    @SerialName("FetchPlayerScoreResponse")
    class Response (
        val score: Int,
        override var id: Int
    ): RemoteResponse
}