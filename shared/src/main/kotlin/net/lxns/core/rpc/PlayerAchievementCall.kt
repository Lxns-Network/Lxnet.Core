package net.lxns.core.rpc

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.lxns.core.NoResponse
import net.lxns.core.RemoteCall
import net.lxns.core.serializers.UUIDSerializer
import java.util.UUID

@Serializable
@SerialName("PlayerAchievementCall")
class PlayerAchievementCall(
    val player: @Serializable(UUIDSerializer::class) UUID,
    val achievementId: String,
    override var id: Int = 0
) : RemoteCall<NoResponse>