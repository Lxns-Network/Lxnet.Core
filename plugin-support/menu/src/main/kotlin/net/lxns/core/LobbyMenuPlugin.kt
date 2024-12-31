package net.lxns.core

import kotlinx.serialization.json.Json
import net.lxns.core.data.Price
import net.lxns.core.gui.AchievementMenu
import net.lxns.core.gui.ShopMenu
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.ipvp.canvas.MenuFunctionListener
import java.util.UUID

class LobbyMenuPlugin : JavaPlugin() {
    private lateinit var lottery: LotteryManager
    private lateinit var shopMenu: ShopMenu

    companion object {
        lateinit var plugin: JavaPlugin
    }

    override fun onEnable() {
        plugin = this
        dataFolder.mkdir()
        val purchasedPlayers = loadPurchasedPlayers()
        lottery = LotteryManager(purchasedPlayers.toMutableSet())
        val price = loadPrice()
        shopMenu = ShopMenu(price, lottery)
        Bukkit.getPluginManager().registerEvents(MenuFunctionListener(), this);
    }

    override fun onDisable() {
        val playersList = dataFolder.resolve("players")
        playersList.writeText(lottery.purchasedPlayer.map { it.toString() }.joinToString("\n"))
    }

    private fun loadPurchasedPlayers(): Set<UUID> {
        val playersList = dataFolder.resolve("players")
        if (playersList.exists()) {
            return playersList.readLines().map { UUID.fromString(it) }.toSet()
        }
        return emptySet()
    }

    private fun loadPrice(): Price {
        val priceFile = dataFolder.resolve("price.json")
        if (!priceFile.exists()) {
            priceFile.writeText(Json.encodeToString<Price>(Price.serializer(), Price()))
            return Price()
        } else {
            return Json.decodeFromString(priceFile.readText())
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return true
        when (label) {
            "achievements" -> AchievementMenu.show(sender)
            "shop" -> shopMenu.show(sender)
            "golottery" -> {
                if (!sender.isOp) return true
                if (args.isEmpty()) {
                    sender.sendMessage("需要提供个数。")
                    return true
                }
                val num = args[0].toIntOrNull()
                if (num == null) {
                    sender.sendMessage("请提供数字")
                    return true
                }
                lottery.openLottery(num)
            }
        }
        return true
    }
}