package net.lxns.core

import net.lxns.core.gui.AchievementMenu
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.ipvp.canvas.MenuFunctionListener

class LobbyMenuPlugin : JavaPlugin() {
    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(MenuFunctionListener(), this);
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if(sender !is Player) return true
        if(label.equals("achievements", ignoreCase = true)){
            AchievementMenu.show(sender)
        }else if(label.equals("shop", ignoreCase = true)){
            TODO()
        }
        return true
    }
}