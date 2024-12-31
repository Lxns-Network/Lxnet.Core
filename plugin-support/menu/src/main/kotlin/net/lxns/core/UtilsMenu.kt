package net.lxns.core

import com.destroystokyo.paper.profile.ProfileProperty
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.ipvp.canvas.template.ItemStackTemplate
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

private val achievementDateTimeFormat = DateTimeFormatter.ISO_LOCAL_DATE_TIME
private val hiddenAchievement = ItemStack(Material.PLAYER_HEAD).withMeta {
    with(this as SkullMeta) {
        setDisplayName("&c未解锁！".bukkitColor())
        this.playerProfile = Bukkit.createProfileExact(UUID.randomUUID(), "questionmark").also {
            it.setProperty(
                ProfileProperty(
                    "textures",
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTY4M2RjN2JjNmRiZGI1ZGM0MzFmYmUyOGRjNGI5YWU2MjViOWU1MzE3YTI5ZjJjNGVjZmU3YmY1YWU1NmMzOCJ9fX0="
                )
            )
        }
    }
}

@Suppress("Deprecation")
fun itemTemplateAchievement(
    mat: Material, achievement: Achievements.Achievement
): ItemStackTemplate = ItemStackTemplate { p: Player ->
    val cache = LxnetCore.getCachedAchievements(p.uniqueId)
    val achiRcrd = cache[achievement.id]
    if (achievement.extra && achiRcrd == null) {
        hiddenAchievement
    } else {
        ItemStack(mat).withMeta {
            setDisplayName(ChatColor.GREEN + achievement.name)
            val lores = mutableListOf<String>()
            achievement.description.forEach { lores.add(ChatColor.WHITE + it) }
            if (achiRcrd != null) {
                lores.add("")
                lores.add("&6&l您已达成该成就！".bukkitColor())
                lores.add("&8${achievementDateTimeFormat.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(achiRcrd.obtainTime), ZoneId.systemDefault()))}".bukkitColor())
                setEnchantmentGlintOverride(true)
            }
            lore = lores
        }
    }
}


operator fun ChatColor.plus(str: String): String = this.toString() + str

fun newItem(mat: Material,name: String, vararg lore: String): ItemStack {
    return ItemStack(mat).withMeta {
        setDisplayName(name.bukkitColor())
        this.lore = lore.toMutableList().map { it.bukkitColor() }
    }
}