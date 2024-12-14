package net.lxns.core

import dev.tylerm.khs.Main
import dev.tylerm.khs.event.GameEndEvent
import dev.tylerm.khs.event.PlayerKillEvent
import dev.tylerm.khs.event.StartTimerUpdateEvent
import dev.tylerm.khs.game.util.Status.STANDBY
import dev.tylerm.khs.game.util.WinType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.lxns.core.record.PlayerScoreRecord
import net.lxns.core.rpc.AddPlayerScoreCall
import net.lxns.core.rpc.RaisePlayerCall
import net.lxns.core.task.RaisePlayerTask
import net.lxns.core.task.RewardTask
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.plugin.java.JavaPlugin

class BlockHuntCompat : JavaPlugin(), Listener {
    lateinit var raiseMessage: Component
    override fun onEnable() {
        if (!dataFolder.exists()) dataFolder.mkdir()
        saveDefaultConfig()
        reloadConfig()
        raiseMessage = MiniMessage.miniMessage().deserialize(config.getString("lang.raise-player")!!)
        server.pluginManager.registerEvents(this, this)
        RewardTask(
            config.getInt("score.tip-seeker"),
            config.getInt("score.tip-block"),
            config.getString("lang.tip")!!
        ).runTaskTimer(this,0,10*20L)
        RaisePlayerTask(raiseMessage)
            .runTaskTimer(this,0,20L)
        LxnetCore.rpcManager.registerListener<RaisePlayerCall> {
            if(Main.getInstance().game.status == STANDBY) {
                Bukkit.broadcast(it.message)
            }
        }
    }

    @EventHandler
    fun onEnd(event: GameEndEvent) {
        val isHiderWin = event.type == WinType.HIDER_WIN
        val winners = if (isHiderWin)
            Main.getInstance().board.hiders
        else Main.getInstance().board.seekers
        val score = config.getInt("score.win-" + if (isHiderWin) "hider" else "seeker")
        for (player in winners) {
            player.sendMessage(config.getString("lang.winner")!!.format(score).bukkitColor())
            LxnetCore.rpcManager.requestCall(
                AddPlayerScoreCall(
                    PlayerScoreRecord(
                        player.uniqueId,
                        score,
                        ScoreReason.GAME_WINNER
                    )
                )
            )
        }
    }

    @EventHandler
    fun onKill(event: PlayerKillEvent) {
        val killer = event.killer
        if (killer == null) return
        var score: Int
        if (Main.getInstance().board.isHider(killer.uniqueId)) {
            score = config.getInt("score.kill-seeker")
            killer.sendMessage(config.getString("lang.kill-seeker")!!.format(score).bukkitColor())
        } else {
            score = config.getInt("score.kill-block")
            killer.sendMessage(config.getString("lang.kill-block")!!.format(score).bukkitColor())
        }
        LxnetCore.rpcManager.requestCall(
            AddPlayerScoreCall(
                PlayerScoreRecord(
                    player = killer.uniqueId,
                    score = score,
                    reason = ScoreReason.KILL_ENEMY
                )
            )
        )
    }

    @EventHandler
    fun onCountingDown(event: StartTimerUpdateEvent){
        if(event.timeLeft == config.getInt("broadcast.at")){
            if(Bukkit.getOnlinePlayers().size > config.getInt("broadcast.at-most")){
                return
            }
            LxnetCore.rpcManager.requestCall(
                RaisePlayerCall(
                    raiseMessage
                )
            )
        }
    }
}