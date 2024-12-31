package net.lxns.core

import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.event.DespawnReason
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin

class ActivityPlugin : JavaPlugin(), Listener {
    companion object {
        lateinit var locations: Locations
        lateinit var plugin: ActivityPlugin
    }

    override fun onEnable() {
        plugin = this
        locations = Locations()
        server.pluginManager.registerEvents(this, this)
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        for (nPC in CitizensAPI.getNPCRegistry()) {
            if (event.player.name.equals(nPC.name)) {
                nPC.despawn(DespawnReason.PLUGIN)
                Bukkit.getScheduler().runTaskLater(this, { ->
                    event.player.sendMessage("&6欢迎回来，您的占位 NPC 已经被移除，请准备合影。".bukkitColor())
                }, 30L)
            }
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.isOp) return true;
        CreditTask().runTaskTimer(plugin, 0L, 1L)
        return true;
    }
}