package net.lxns.core

import net.lxns.core.NoResponse.id
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.Collections
import java.util.Random
import java.util.UUID
import kotlin.math.max

class LotteryManager(
    val purchasedPlayer: MutableSet<UUID>
) {
    fun purchaseLottery(player: Player) {
        purchasedPlayer.add(player.uniqueId)
    }

    fun isPurchased(player: Player): Boolean {
        return purchasedPlayer.contains(player.uniqueId)
    }

    fun canPurchaseLottery(player: Player): Boolean {
        return !isPurchased(player)
    }

    fun openLottery(numPlayer: Int) {
        val players = ArrayList(purchasedPlayer)
        val seed = System.currentTimeMillis() % 1000000
        val random = Random(seed)
        Collections.shuffle(players, random)
        val onlinePlayers = players.map { Bukkit.getPlayer(it) }.filterNotNull()
        val count = max(onlinePlayers.size, numPlayer)
        for (i in 0 until count) {
            val p = onlinePlayers[i]
            val comp = TextComponent.fromLegacy("&a&l新年快乐！这是你的兑换码(点击复制)：".bukkitColor())
            val key = random.nextLong()
            val cdk = genCDK(i, key, p)
            println("Key for player ${p.name} is ${cdk}")
            comp.addExtra(TextComponent.fromLegacy("&6&k${cdk}".bukkitColor()).also {
                it.clickEvent = ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, cdk)
            })
            p.spigot().sendMessage(comp)
        }
        Bukkit.broadcastMessage(
            "&6彩票已开奖，让我们恭喜这几位玩家：&f${
                onlinePlayers.map { it.displayName }.joinToString(", ")
            }".bukkitColor()
        )
    }

    private fun genCDK(seq: Int, key: Long, player: Player): String {
        val cdkUUID = UUID(
            player.uniqueId.mostSignificantBits * seq,
            player.uniqueId.leastSignificantBits xor key
        )
        return "lxnet-2025-$seq-$key-${cdkUUID.toString().replace("-", "")}${
            player.uniqueId.toString().replace("-", "")
        }"
    }
}