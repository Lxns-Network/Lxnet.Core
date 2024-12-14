package net.lxns.core.task

import net.lxns.core.LxnetCore
import net.lxns.core.ScoreReason
import net.lxns.core.bukkitColor
import net.lxns.core.record.PlayerScoreRecord
import net.lxns.core.rpc.AddPlayerScoreCall
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.UUID
import kotlin.math.abs

class TipPlayerTask(
    private val moveThreshold: Int,
    private val score: Int,
    private val message: String
): BukkitRunnable() {
    private val yCounter = mutableMapOf<UUID, Int>()
    override fun run() {
        for (player in Bukkit.getOnlinePlayers()) {
            val current = player.location.y.toInt()
            if(yCounter.contains(player.uniqueId)) {
                handlePlayer(player,current)
            }else{
                yCounter[player.uniqueId] = current
            }
        }
    }

    private fun handlePlayer(player: Player, currentY: Int){
        val lastY = yCounter[player.uniqueId]
        val delta = currentY - lastY!!
        if(delta < moveThreshold) return
        yCounter[player.uniqueId] = currentY
        // tip
        val score = score * delta / moveThreshold
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(message.format(score)))
        LxnetCore.rpcManager.requestCall(
            AddPlayerScoreCall(
                PlayerScoreRecord(
                    player.uniqueId,
                    score,
                    ScoreReason.PLAYING_GAME
                )
            )
        )
    }
}