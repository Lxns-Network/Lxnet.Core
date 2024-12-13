package net.lxns.core.dal.impl

import com.google.common.collect.Multimaps
import com.velocitypowered.api.proxy.Player
import net.lxns.core.ScoreReason
import net.lxns.core.dal.DataSource
import net.lxns.core.record.PlayerScoreRecord
import java.util.UUID

class InMemDataSource : DataSource {
    private val players = Multimaps.newMultimap<UUID, PlayerScoreRecord>(mutableMapOf()) { mutableListOf() }
    override fun addPlayerScore(record: PlayerScoreRecord) {
        players.put(record.player, record)
    }
}