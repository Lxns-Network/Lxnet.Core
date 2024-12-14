package net.lxns.core.task

import net.kyori.adventure.text.minimessage.MiniMessage
import net.lxns.core.LxnetCore
import net.lxns.core.ParkourCompat
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
import kotlin.math.max

class TipPlayerTask(
    private val plugin: ParkourCompat,
    private val message: String
): BukkitRunnable() {
    override fun run() {
        for ((id, score) in plugin.playerScores) {
            val player = Bukkit.getPlayer(id) ?: continue
            player.sendMessage(MiniMessage.miniMessage().deserialize(message.format(score.toInt())))
            LxnetCore.rpcManager.requestCall(
                AddPlayerScoreCall(
                    PlayerScoreRecord(
                        player.uniqueId,
                        score.toInt(),
                        ScoreReason.PLAYING_GAME
                    )
                )
            )
        }
    }
}