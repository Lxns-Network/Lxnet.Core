package net.lxns.core.rpc

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.lxns.core.NoResponse
import net.lxns.core.RemoteCall

@Serializable
@SerialName("PlayerAchievementCall")
class PlayerAchievementCall(
    val achievementId: String,
    val extra: Boolean,
    override var id: Int = 0
) : RemoteCall<NoResponse>