package net.lxns.core.dal.impl

import com.sun.org.apache.xpath.internal.operations.Bool
import net.lxns.core.Achievements
import net.lxns.core.dal.DataSource
import net.lxns.core.record.PlayerAchievementRecord
import net.lxns.core.record.PlayerScoreRecord
import org.slf4j.Logger
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.ReentrantReadWriteLock

class ReadCacheDataSource(
    val upstream: DataSource,
    val logger: Logger
) : DataSource {
    private val scores = HashMap<UUID, Int>()
    private val achievements = HashMap<UUID, MutableList<PlayerAchievementRecord>>()
    private val playerAchievementHashSetCache = HashMap<String, AchievementState>()

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
        return achievements.computeIfAbsent(player) {
            upstream.getAchievements(player).toMutableList()
        }
    }

    override fun hasAchievementBefore(player: UUID, achievement: String): Boolean {
        val key = player.toString() + achievement
        var cacheState = playerAchievementHashSetCache.get(key)
        if (cacheState != null) {
            return cacheState == AchievementState.OBTAINED
        }
        return playerAchievementHashSetCache.computeIfAbsent(key) {
            val result = upstream.hasAchievementBefore(player, achievement)
            if (result) {
                AchievementState.OBTAINED
            } else {
                AchievementState.NOT_OBTAINED
            }
        } == AchievementState.OBTAINED
    }

    override fun addAchievement(player: UUID, achievement: String) {
        if (!achievements.containsKey(player)) {
            achievements.put(player, upstream.getAchievements(player).toMutableList())
        }
        val record = PlayerAchievementRecord(achievement, System.currentTimeMillis())
        upstream.addAchievement(player, achievement)
        achievements.get(player)!!.add(record)
    }
}

enum class AchievementState {
    OBTAINED, NOT_OBTAINED
}

private inline operator fun <T> Lock.invoke(crossinline scope: () -> T): T {
    lock()
    try {
        return scope()
    } finally {
        unlock()
    }
}