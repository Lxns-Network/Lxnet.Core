package net.lxns.core

import net.lxns.core.task.TipPlayerTask
import org.bukkit.plugin.java.JavaPlugin

class ParkourCompat : JavaPlugin() {
    override fun onEnable() {
        saveDefaultConfig()
        reloadConfig()
        TipPlayerTask(
            config.getInt("move-threshold"),
            config.getInt("score-per-threshold"),
            config.getString("message")!!
        )
    }
}