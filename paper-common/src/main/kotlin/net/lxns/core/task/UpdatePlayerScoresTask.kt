package net.lxns.core.task

import net.lxns.core.LxnetCore
import net.lxns.core.record.PlayerAchievementRecord
import net.lxns.core.rpc.FetchPlayerAchievementCall
import net.lxns.core.rpc.FetchPlayerScoreCall
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.UUID
import java.util.concurrent.ConcurrentMap

class UpdatePlayerScoresTask(
    private val scoreCache: ConcurrentMap<UUID, Int>,
    private val achievementCache: ConcurrentMap<UUID, Map<String, PlayerAchievementRecord>>
) : BukkitRunnable() {
    override fun run() {
        // race condition shouldn't be possible since player key was removed when leaving server.
        for ((k, v) in scoreCache) {
            if (Bukkit.getPlayer(k) == null) continue // MAY be offline or last ran at-the-time of player leaving the server
            LxnetCore.rpcManager.requestCall(FetchPlayerScoreCall(k)) {
                scoreCache.replace(k, it.score)
            }
        }
        for ((k, v) in achievementCache) {
            if (Bukkit.getPlayer(k) == null) continue
            LxnetCore.rpcManager.requestCall(FetchPlayerAchievementCall(k)) {
                achievementCache.replace(k, it.achievements.map { it.achievement to it }.toMap())
            }
        }
    }
}