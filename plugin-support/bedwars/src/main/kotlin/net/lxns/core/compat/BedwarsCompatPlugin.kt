package net.lxns.core.compat

import com.andrei1058.bedwars.api.events.gameplay.GameEndEvent
import com.andrei1058.bedwars.api.events.player.PlayerBedBreakEvent
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent
import net.lxns.core.LxnetCorePlugin
import net.lxns.core.ScoreReason
import net.lxns.core.record.PlayerScoreRecord
import net.lxns.core.rpc.AddPlayerScoreCall
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class BedwarsCompatPlugin : JavaPlugin(), Listener {
    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this)
    }

    @EventHandler
    fun onPlayerKill(event: PlayerKillEvent) {
        val killer = event.killer
        var score = if (event.cause.isFinalKill) 10.0 else 4.0
        if (!event.cause.name.contains("PVP")) {
            score = score * 0.7
        }
        LxnetCorePlugin.rpcManager.requestCall(
            AddPlayerScoreCall(
                PlayerScoreRecord(
                    killer.uniqueId,
                    score.toInt(),
                    ScoreReason.KILL_ENEMY
                )
            )
        )
        killer.sendMessage("&a杀死敌人! ( +$score 积分 )".bukkitColor())
    }

    @EventHandler
    fun onBedBreak(event: PlayerBedBreakEvent){
        val p = event.player
        p.sendMessage("&a破坏床！( +15 积分 )".bukkitColor())
        LxnetCorePlugin.rpcManager.requestCall(
            AddPlayerScoreCall(
                PlayerScoreRecord(
                    p.uniqueId, 15, ScoreReason.OTHER
                )
            )
        )
    }

    @EventHandler
    fun onGameEnd(event: GameEndEvent) {
        for (winner in event.winners) {
            LxnetCorePlugin.rpcManager.requestCall(
                AddPlayerScoreCall(
                    PlayerScoreRecord(
                        player = winner,
                        score = 50,
                        reason = ScoreReason.GAME_WINNER
                    )
                )
            )
            Bukkit.getPlayer(winner)?.sendMessage("&6&l游戏胜利! &7( +50 积分)".bukkitColor())
        }
    }
}