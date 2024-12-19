package net.lxns.core.dal.impl

import net.lxns.core.ScoreReason
import net.lxns.core.dal.DataSource
import net.lxns.core.dal.impl.PlayerScoreTable.reason
import net.lxns.core.dal.impl.PlayerScoreTable.score
import net.lxns.core.record.PlayerScoreRecord
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class SQLDataSource(
    private val db: Database
) : DataSource {
    init {
        transaction(db){
            SchemaUtils.create(PlayerScoreTable)
        }
    }
    override fun addPlayerScore(record: PlayerScoreRecord) {
        transaction(db) {
            PlayerScoreTable.insert {
                it[player] = record.player.toString()
                it[score] = record.score
                it[reason] = record.reason
                it[time] = record.time
            }
        }
    }

    override fun getPlayerScore(player: UUID): Int {
        return transaction(db) {
            PlayerScoreTable.selectAll().where { PlayerScoreTable.player eq player.toString() }
                .sumOf { it[score] }
        }
    }

    override fun getScoreRecords(player: UUID): Collection<PlayerScoreRecord> {
        return transaction(db) {
            PlayerScoreTable.selectAll().map {
                PlayerScoreRecord(
                    UUID.fromString(it[PlayerScoreTable.player]),
                    it[score],
                    it[reason]
                )
            }
        }
    }
}

private object PlayerScoreTable : Table() {
    val id: Column<Int> = integer("id").autoIncrement()
    val player: Column<String> = varchar("player", 36)
    val score: Column<Int> = integer("score")
    val reason: Column<ScoreReason> = enumeration("reason", ScoreReason::class)
    val time: Column<Long> = long("time")
    override val primaryKey: PrimaryKey = PrimaryKey(id)
}