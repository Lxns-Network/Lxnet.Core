package net.lxns.core.compat

import org.bukkit.ChatColor

fun String.bukkitColor() = ChatColor.translateAlternateColorCodes('&', this)