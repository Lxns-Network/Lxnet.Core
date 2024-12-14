package net.lxns.core.task

import dev.tylerm.khs.Main
import dev.tylerm.khs.game.util.Status.PLAYING
import net.lxns.core.LxnetCore
import net.lxns.core.ScoreReason
import net.lxns.core.bukkitColor
import net.lxns.core.record.PlayerScoreRecord
import net.lxns.core.rpc.AddPlayerScoreCall
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

class RewardTask(
    private val scoreTipSeeker: Int,
    private val scoreTipBlock: Int,
    private val tipText: String
) : BukkitRunnable() {
    override fun run() {
        if (Main.getInstance().game.status != PLAYING) return
        val board = Main.getInstance().board
        for (player in Bukkit.getOnlinePlayers()) {
            if (board.isHider(player)) {
                tipPlayer(player, scoreTipBlock)
            }else if(board.isSeeker(player)){
                tipPlayer(player, scoreTipSeeker)
            }
        }
    }

    private fun tipPlayer(player: Player, score: Int) {
        player.sendMessage(tipText.format(score).bukkitColor())
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