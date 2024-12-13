package net.lxns.core.compat

import com.andrei1058.bedwars.api.events.gameplay.GameEndEvent
import com.andrei1058.bedwars.api.events.player.PlayerBedBreakEvent
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent
import net.lxns.core.LxnetCore
import net.lxns.core.ScoreReason
import net.lxns.core.record.PlayerScoreRecord
import net.lxns.core.rpc.AddPlayerScoreCall
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class BedwarsCompatPlugin : JavaPlugin(), Listener {
    override fun onEnable() {
        if(!dataFolder.exists())
            dataFolder.mkdir()
        saveDefaultConfig()
        reloadConfig()
        Bukkit.getPluginManager().registerEvents(this, this)
    }

    @EventHandler
    fun onPlayerKill(event: PlayerKillEvent) {
        val killer = event.killer
        var score = if (event.cause.isFinalKill) config.getInt("score.final-kill") else config.getInt("score.kill")
        if (!event.cause.name.contains("PVP")) {
            score = (score * config.getDouble("score.not-pvp-multiplier")).toInt()
        }
        LxnetCore.rpcManager.requestCall(
            AddPlayerScoreCall(
                PlayerScoreRecord(
                    killer.uniqueId,
                    score,
                    ScoreReason.KILL_ENEMY
                )
            )
        )
        killer.sendMessage(config.getString("lang.kill")!!.format(score).bukkitColor())
    }

    @EventHandler
    fun onBedBreak(event: PlayerBedBreakEvent){
        val p = event.player
        val scoreBed = config.getInt("score.break-bed")
        p.sendMessage(config.getString("lang.break-bed")!!.format(scoreBed).bukkitColor())
        LxnetCore.rpcManager.requestCall(
            AddPlayerScoreCall(
                PlayerScoreRecord(
                    p.uniqueId, scoreBed, ScoreReason.OTHER
                )
            )
        )
    }

    @EventHandler
    fun onGameEnd(event: GameEndEvent) {
        val score = config.getInt("score.winner")
        for (winner in event.winners) {
            LxnetCore.rpcManager.requestCall(
                AddPlayerScoreCall(
                    PlayerScoreRecord(
                        player = winner,
                        score = score,
                        reason = ScoreReason.GAME_WINNER
                    )
                )
            )
            Bukkit.getPlayer(winner)?.sendMessage(config.getString("lang.win")!!.format(score).bukkitColor())
        }
    }
}