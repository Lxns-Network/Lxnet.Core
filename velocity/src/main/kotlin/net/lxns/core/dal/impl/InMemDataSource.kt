package net.lxns.core.dal.impl

import com.google.common.collect.Multimaps
import net.lxns.core.dal.DataSource
import net.lxns.core.record.PlayerScoreRecord
import java.util.UUID

class InMemDataSource : DataSource {
    private val players = Multimaps.newMultimap<UUID, PlayerScoreRecord>(mutableMapOf()) { mutableListOf() }
    override fun addPlayerScore(record: PlayerScoreRecord) {
        players.put(record.player, record)
    }

    override fun getPlayerScore(player: UUID): Int = players[player].sumOf { it.score }

    override fun getScoreRecords(player: UUID): Collection<PlayerScoreRecord>
        = players[player]
}