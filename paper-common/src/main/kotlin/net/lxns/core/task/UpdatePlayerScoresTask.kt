package net.lxns.core.task

import net.lxns.core.LxnetCore
import net.lxns.core.rpc.FetchPlayerScoreCall
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.concurrent.ConcurrentMap

class UpdatePlayerScoresTask(
    private val scoreCache: ConcurrentMap<Player, Int>
) : BukkitRunnable(){
    override fun run() {
        for ((k,v) in scoreCache) {
            LxnetCore.rpcManager.requestCall(FetchPlayerScoreCall(k.uniqueId)) {
                scoreCache.replace(k, it.score)
            }
        }
    }
}