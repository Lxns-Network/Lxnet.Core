package net.lxns.core

import org.bukkit.ChatColor

fun String.bukkitColor() = ChatColor.translateAlternateColorCodes('&', this)