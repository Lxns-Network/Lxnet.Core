package net.lxns.core

import org.bukkit.ChatColor
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

inline fun String.bukkitColor() = ChatColor.translateAlternateColorCodes('&', this)

inline fun ItemStack.withMeta(crossinline s: ItemMeta.() -> Unit): ItemStack {
    this.itemMeta = this.itemMeta.also(s)
    return this
}