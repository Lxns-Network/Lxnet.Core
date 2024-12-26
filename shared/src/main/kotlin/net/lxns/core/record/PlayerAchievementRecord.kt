package net.lxns.core.record

import kotlinx.serialization.Serializable
import net.lxns.core.Achievements

@Serializable
data class PlayerAchievementRecord(
    val achievement: String,
    val obtainTime: Long
)