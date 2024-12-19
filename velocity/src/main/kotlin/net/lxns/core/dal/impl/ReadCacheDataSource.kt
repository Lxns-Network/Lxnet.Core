package net.lxns.core.dal.impl

import net.lxns.core.dal.DataSource
import net.lxns.core.record.PlayerScoreRecord
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class ReadCacheDataSource(
    val upstream: DataSource
) : DataSource {
    private val players = ConcurrentHashMap<UUID, Int>()
    override fun addPlayerScore(record: PlayerScoreRecord) {
        upstream.addPlayerScore(record)
        players.computeIfPresent(record.player) { k,v -> v + record.score }
    }

    override fun getPlayerScore(player: UUID): Int {
        return players.computeIfAbsent(player) {
            upstream.getPlayerScore(it)
        }
    }

    override fun getScoreRecords(player: UUID): Collection<PlayerScoreRecord> {
        return upstream.getScoreRecords(player)
    }
}