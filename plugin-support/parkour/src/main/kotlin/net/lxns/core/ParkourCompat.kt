package net.lxns.core

import net.lxns.core.task.LocationSamplingTask
import net.lxns.core.task.TipPlayerTask
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID

class ParkourCompat : JavaPlugin(), Listener {
    val playerScores = mutableMapOf<UUID, Double>()
    override fun onEnable() {
        saveDefaultConfig()
        reloadConfig()
        TipPlayerTask(
            this,
            config.getString("message")!!
        ).runTaskTimer(this, 0, 60 * 20L)
        LocationSamplingTask(this,config.getDouble("score-initial").toInt()).runTaskTimer(this, 0, 35L)
        Bukkit.getPluginManager().registerEvents(this, this)
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        playerScores.computeIfAbsent(event.player.uniqueId) { config.getDouble("score-initial") }
    }
}