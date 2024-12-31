package net.lxns.core

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.FireworkMeta
import org.bukkit.scheduler.BukkitRunnable

class CreditTask : BukkitRunnable() {
    var ticks = 0
    override fun run() {
        when (ticks++) {
            0 -> Bukkit.getOnlinePlayers().forEach { it.sendTitle("&6&l2".bukkitColor(), "") }
            20 -> Bukkit.getOnlinePlayers().forEach { it.sendTitle("&6&l20".bukkitColor(), "") }
            40 -> Bukkit.getOnlinePlayers().forEach { it.sendTitle("&6&l202".bukkitColor(), "") }
            60 -> Bukkit.getOnlinePlayers().forEach { it.sendTitle("&6&l2025".bukkitColor(), "") }
            100 -> {
                Bukkit.getOnlinePlayers().forEach {
                    it.sendTitle("&6&l2025".bukkitColor(), "新年快乐")
                }
                Bukkit.broadcastMessage("&d&l感谢参与！".bukkitColor())
                Bukkit.broadcastMessage("&8&mBuggy&r&dLxNet &fby iceBear67".bukkitColor())
                Bukkit.broadcastMessage("&a&l特别感谢：&r&f".bukkitColor())
                Bukkit.broadcastMessage(" - &aPaQiu_PAQ, HanTi_OwO, ziiipeng, _LittleC_, Infinity_rain, timetraveler314, KeStone, qierjvn... and more!".bukkitColor())
                Bukkit.broadcast(MiniMessage.miniMessage().deserialize(" - 服务器: <green><hover:show_text:去年也是他们无偿提供的>问谛居 https://www.wd-ljt.com/</hover>"))

                Bukkit.broadcastMessage("&6新年快乐，祝大家在新的一年里事业有成！".bukkitColor())

                cancel()
            }
        }
    }
}