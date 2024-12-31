package net.lxns.core

import net.lxns.core.ActivityPlugin.Companion.plugin
import org.bukkit.scheduler.BukkitRunnable
import java.time.LocalDate

class WaitTask : BukkitRunnable() {
    override fun run() {
        if (LocalDate.now().year == 2024) return
        cancel()
        CreditTask().runTaskTimer(plugin, 0L, 1L)
    }
}