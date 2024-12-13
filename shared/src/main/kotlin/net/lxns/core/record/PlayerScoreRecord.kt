package net.lxns.core.record

import kotlinx.serialization.Serializable
import net.lxns.core.ScoreReason
import net.lxns.core.serializers.UUIDSerializer
import java.util.UUID

@Serializable
data class PlayerScoreRecord(
    val player: @Serializable(UUIDSerializer::class) UUID,
    val score: Int,
    val reason: ScoreReason,
    val time: Long = System.currentTimeMillis()
)
