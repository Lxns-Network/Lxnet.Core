package net.lxns.core.dal.impl

import net.lxns.core.dal.DataSource
import net.lxns.core.record.PlayerAchievementRecord
import net.lxns.core.record.PlayerScoreRecord
import java.util.UUID

class NoOpDataSource : DataSource {
    override fun addPlayerScore(record: PlayerScoreRecord) {

    }

    override fun getPlayerScore(player: UUID): Int {
        return 0
    }

    override fun getScoreRecords(player: UUID): Collection<PlayerScoreRecord> {
        return emptyList()
    }

    override fun getAchievements(player: UUID): Collection<PlayerAchievementRecord> {
        return emptyList()
    }

    override fun hasAchievementBefore(player: UUID, achievement: String): Boolean {
        return false
    }

    override fun addAchievement(player: UUID, achievement: String) {

    }
}