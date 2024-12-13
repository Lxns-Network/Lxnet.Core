package net.lxns.core.dal

import net.lxns.core.record.PlayerScoreRecord
import java.util.UUID

interface DataSource {
    fun addPlayerScore(record: PlayerScoreRecord)
    fun getPlayerScore(player: UUID): Int
    fun getScoreRecords(player: UUID): Collection<PlayerScoreRecord>
}