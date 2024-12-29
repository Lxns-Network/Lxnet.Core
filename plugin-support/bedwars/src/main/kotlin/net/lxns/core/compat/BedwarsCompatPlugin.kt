package net.lxns.core.compat

import com.andrei1058.bedwars.api.BedWars
import com.andrei1058.bedwars.api.events.gameplay.GameEndEvent
import com.andrei1058.bedwars.api.events.player.PlayerBedBreakEvent
import com.andrei1058.bedwars.api.events.player.PlayerJoinArenaEvent
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent
import com.andrei1058.bedwars.api.events.server.ArenaDisableEvent
import com.andrei1058.bedwars.api.events.server.ArenaEnableEvent
import com.andrei1058.bedwars.api.events.team.TeamEliminatedEvent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.lxns.core.Achievements
import net.lxns.core.LxnetCore
import net.lxns.core.ScoreReason
import net.lxns.core.bukkitColor
import net.lxns.core.compat.task.CheckPlayerNumTask
import net.lxns.core.record.PlayerScoreRecord
import net.lxns.core.rpc.AddPlayerScoreCall
import net.lxns.core.rpc.PlayerAchievementCall
import net.lxns.core.rpc.RaisePlayerCall
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.plugin.java.JavaPlugin
import java.lang.IllegalStateException

class BedwarsCompatPlugin : JavaPlugin(), Listener {
    lateinit var bwApi: BedWars
    lateinit var efficiencyEnchantment: Enchantment
    val arenaData = mutableMapOf<String, ArenaData?>()
    override fun onEnable() {
        if (!dataFolder.exists())
            dataFolder.mkdir()
        saveDefaultConfig()
        reloadConfig()
        if (Bukkit.getPluginManager().getPlugin("BedWars1058") == null) {
            logger.severe("Cannot find bedwars1058")
            isEnabled = false
            return
        }
        bwApi = Bukkit.getServicesManager().getRegistration(BedWars::class.java)?.getProvider()
            ?: throw IllegalStateException("Cannot find bedwars plugin")
        Bukkit.getPluginManager().registerEvents(this, this)
        CheckPlayerNumTask(
            bwApi,
            MiniMessage.miniMessage().deserialize(config.getString("lang.raise-player")!!)
        ).runTaskTimer(this, 0L, 2 * 60 * 20L)
        LxnetCore.rpcManager.registerListener<RaisePlayerCall> {
            for (player in Bukkit.getWorld(bwApi.lobbyWorld)!!.players) {
                player.sendMessage(it.message)
            }
        }
        efficiencyEnchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("efficiency")) !!
    }

    @EventHandler
    fun onTeamDestroyed(event: TeamEliminatedEvent) {
        val data = arenaData[event.arena.arenaName] ?: return
        val killers = event.team.members.map { data.lastKiller[it.uniqueId] }.distinct()
        if (killers.size == 1) {
            val killer = killers.first()!!
            // must be online.
            val player = Bukkit.getPlayer(killer) ?: return
            LxnetCore.rpcManager.requestCall(
                PlayerAchievementCall(
                    player.uniqueId,
                    Achievements.BedWars.GENOSIDE.id
                )
            )
        }
    }

    @EventHandler
    fun onPlayerKill(event: PlayerKillEvent) {
        val killer = event.killer ?: return
        arenaData[event.arena.arenaName]?.lastKiller[event.victim.uniqueId] = killer.uniqueId
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
        if (event.playerTeam.isBedDestroyed) {
            LxnetCore.rpcManager.requestCall(
                PlayerAchievementCall(
                    p.uniqueId,
                    Achievements.BedWars.BREAK_BED_IN_LOW_SITUATION.id
                )
            )
        }
    }

    @EventHandler
    fun onArenaEnabled(event: ArenaEnableEvent) {
        arenaData[event.arena.arenaName] = ArenaData()
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinArenaEvent) {
        event.player.setPlayerTime(1200, false)
    }

    @EventHandler
    fun onArenaDisabled(event: ArenaDisableEvent) {
        arenaData[event.arenaName] = null
    }

    @EventHandler
    fun onDamageByOther(evt: EntityDamageByEntityEvent) {
        val damager = evt.damager
        if (damager is Player) {
            val itemInHand = damager.equipment.itemInMainHand
            if(itemInHand.hasItemMeta()){
                if(itemInHand.itemMeta.hasEnchant(efficiencyEnchantment)){
                    evt.isCancelled = true
                }
            }
        }
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