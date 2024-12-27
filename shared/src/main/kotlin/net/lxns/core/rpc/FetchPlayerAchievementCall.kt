package net.lxns.core.rpc

import kotlinx.serialization.Serializable
import net.lxns.core.RemoteCall
import net.lxns.core.RemoteResponse
import net.lxns.core.record.PlayerAchievementRecord
import net.lxns.core.serializers.UUIDSerializer
import java.util.UUID

class FetchPlayerAchievementCall(
    val player: @Serializable(UUIDSerializer::class) UUID,
    override var id: Int = 0
) : RemoteCall<FetchPlayerAchievementCall.Response> {
    class Response(
        val achievements: Collection<PlayerAchievementRecord>,
        override val id: Int
    ) : RemoteResponse
}