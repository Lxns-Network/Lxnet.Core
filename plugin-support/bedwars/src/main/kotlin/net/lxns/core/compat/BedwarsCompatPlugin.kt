package net.lxns.core.compat

import com.andrei1058.bedwars.api.BedWars
import com.andrei1058.bedwars.api.events.gameplay.GameEndEvent
import com.andrei1058.bedwars.api.events.player.PlayerBedBreakEvent
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import net.kyori.adventure.text.minimessage.MiniMessage
import net.lxns.core.LxnetCore
import net.lxns.core.RPC_CHANNEL_IDENTIFIER
import net.lxns.core.RemoteCall
import net.lxns.core.ScoreReason
import net.lxns.core.bukkitColor
import net.lxns.core.compat.task.CheckPlayerNumTask
import net.lxns.core.record.PlayerScoreRecord
import net.lxns.core.rpc.AddPlayerScoreCall
import net.lxns.core.rpc.RaisePlayerCall
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.messaging.PluginMessageListener
import java.io.ByteArrayInputStream

class BedwarsCompatPlugin : JavaPlugin(), Listener, PluginMessageListener {
    lateinit var bwApi: BedWars
    override fun onEnable() {
        if (!dataFolder.exists())
            dataFolder.mkdir()
        saveDefaultConfig()
        reloadConfig()
        bwApi = Bukkit.getServicesManager().getRegistration(BedWars::class.java)!!.getProvider();
        Bukkit.getPluginManager().registerEvents(this, this)
        Bukkit.getServer().messenger.registerIncomingPluginChannel(this, RPC_CHANNEL_IDENTIFIER, this)
        CheckPlayerNumTask(
            bwApi,
            MiniMessage.miniMessage().deserialize(config.getString("lang.raise-player")!!)
        ).runTaskTimer(this,0L,20*10L)
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
    fun onBedBreak(event: PlayerBedBreakEvent) {
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

    override fun onPluginMessageReceived(p0: String, p1: Player, p2: ByteArray?) {
        if (p0 != RPC_CHANNEL_IDENTIFIER) return
        val call = Json.decodeFromStream<RemoteCall<*>>(ByteArrayInputStream(p2))
        if (call !is RaisePlayerCall) return
        for (player in Bukkit.getWorld(bwApi.lobbyWorld)!!.players) {
            player.sendMessage(call.message)
        }
    }
}