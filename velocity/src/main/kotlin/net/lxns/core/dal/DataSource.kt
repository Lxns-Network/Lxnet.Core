package net.lxns.core.dal

import net.lxns.core.Achievements.Achievement
import net.lxns.core.record.PlayerAchievementRecord
import net.lxns.core.record.PlayerScoreRecord
import java.util.UUID

interface DataSource {
    fun addPlayerScore(record: PlayerScoreRecord)
    fun getPlayerScore(player: UUID): Int
    fun getScoreRecords(player: UUID): Collection<PlayerScoreRecord>
    // achievements
    fun getAchievements(player: UUID): Collection<PlayerAchievementRecord>
    fun addAchievement(player: UUID, achievement: String)
}