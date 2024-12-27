package net.lxns.core.dal.impl

import net.lxns.core.Achievements
import net.lxns.core.dal.DataSource
import net.lxns.core.record.PlayerAchievementRecord
import net.lxns.core.record.PlayerScoreRecord
import org.slf4j.Logger
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantReadWriteLock

class ReadCacheDataSource(
    val upstream: DataSource,
    val logger: Logger
) : DataSource {
    private val scores = ConcurrentHashMap<UUID, Int>()
    private val achievements = mutableMapOf<UUID, MutableList<PlayerAchievementRecord>>()
    private val playerAchievementHashSetCache = ConcurrentHashMap<String, AchievementState>()
    private val readLock: Lock
    private val writeLock: Lock

    init {
        val lock = ReentrantReadWriteLock()
        readLock = lock.readLock()
        writeLock = lock.writeLock()
    }

    override fun addPlayerScore(record: PlayerScoreRecord) {
        upstream.addPlayerScore(record)
        scores.computeIfPresent(record.player) { k, v -> v + record.score }
    }

    override fun getPlayerScore(player: UUID): Int {
        return scores.computeIfAbsent(player) {
            upstream.getPlayerScore(it)
        }
    }

    override fun getScoreRecords(player: UUID): Collection<PlayerScoreRecord> {
        return upstream.getScoreRecords(player)
    }

    override fun getAchievements(player: UUID): Collection<PlayerAchievementRecord> {
        var result: Collection<PlayerAchievementRecord>? = null
        readLock {
            if (!achievements.containsKey(player)) { // sl0w path
                writeLock {
                    if (!achievements.containsKey(player)) {
                        achievements.put(player, upstream.getAchievements(player).toMutableList())
                    }
                }
            }
            result = achievements.get(player)!!
        }
        return result ?: run {
            logger.error("Cannot find achievements for player $player")
            emptyList()
        }
    }

    override fun hasAchievementBefore(player: UUID, achievement: String): Boolean {
        return playerAchievementHashSetCache.computeIfAbsent(player.toString() + achievement) {
            writeLock.lock()
            try{
                if (upstream.hasAchievementBefore(player, achievement)) {
                    AchievementState.OBTAINED
                } else {
                    AchievementState.NOT_OBTAINED
                }
            }finally {
                writeLock.unlock()
            }
        } == AchievementState.OBTAINED
    }

    override fun addAchievement(player: UUID, achievement: String) {
        writeLock {
            if (!achievements.containsKey(player)) {
                achievements.put(player, upstream.getAchievements(player).toMutableList())
            }
            val record = PlayerAchievementRecord(achievement, System.currentTimeMillis())
            upstream.addAchievement(player, achievement)
            achievements.get(player)!!.add(record)
            playerAchievementHashSetCache[player.toString()+achievement] = AchievementState.OBTAINED
        }
    }
}

enum class AchievementState {
    OBTAINED, NOT_OBTAINED
}

private inline operator fun Lock.invoke(crossinline scope: () -> Unit) {
    lock()
    try {
        scope()
    } finally {
        unlock()
    }

}