package net.lxns.core.dal.impl

import net.lxns.core.Achievements
import net.lxns.core.ScoreReason
import net.lxns.core.dal.DataSource
import net.lxns.core.dal.impl.PlayerAchievementTable.achievement
import net.lxns.core.dal.impl.PlayerAchievementTable.time
import net.lxns.core.dal.impl.PlayerScoreTable.reason
import net.lxns.core.dal.impl.PlayerScoreTable.score
import net.lxns.core.record.PlayerAchievementRecord
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
        transaction(db) {
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
        val uuidStr = player.toString()
        return transaction(db) {
            PlayerScoreTable.selectAll().where { PlayerScoreTable.player eq uuidStr }.map {
                PlayerScoreRecord(
                    UUID.fromString(it[PlayerScoreTable.player]),
                    it[score],
                    it[reason]
                )
            }
        }
    }

    override fun getAchievements(player: UUID): Collection<PlayerAchievementRecord> {
        return transaction(db) {
            PlayerAchievementTable.selectAll().where {
                PlayerAchievementTable.player eq player.toString()
            }.map {
                PlayerAchievementRecord(
                    it[achievement],
                    it[time]
                )
            }
        }
    }

    override fun addAchievement(player: UUID, achievement: Achievements.Achievement) {
        val uuidStr = player.toString()
        transaction(db) {
            val notExists = PlayerAchievementTable.selectAll().where {
                PlayerAchievementTable.player eq uuidStr
                PlayerAchievementTable.achievement eq achievement.id
            }.empty()
            if (notExists) {
                PlayerAchievementTable.insert {
                    it[PlayerAchievementTable.player] = uuidStr
                    it[PlayerAchievementTable.achievement] = achievement.id
                    it[time] = System.currentTimeMillis()
                }
            }
        }
    }
}

private object PlayerAchievementTable : Table() {
    val id = integer("id").autoIncrement()
    val player = varchar("player", 36)
    val achievement = varchar("achievement", 128)
    val time = long("time")
    override val primaryKey = PrimaryKey(id)
}

private object PlayerScoreTable : Table() {
    val id: Column<Int> = integer("id").autoIncrement()
    val player: Column<String> = varchar("player", 36)
    val score: Column<Int> = integer("score")
    val reason: Column<ScoreReason> = enumeration("reason", ScoreReason::class)
    val time: Column<Long> = long("time")
    override val primaryKey: PrimaryKey = PrimaryKey(id)
}