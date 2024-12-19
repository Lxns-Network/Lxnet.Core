package net.lxns.core

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.kyori.adventure.text.minimessage.MiniMessage
import net.lxns.core.record.PlayerScoreRecord
import net.lxns.core.rpc.AddPlayerScoreCall
import net.lxns.core.rpc.FetchPlayerScoreCall
import net.lxns.core.rpc.SendMessageCall
import org.bukkit.Bukkit
import org.bukkit.entity.Player

@CommandAlias("coin")
object Commands : BaseCommand() {
    @Subcommand("show")
    fun showBalance(player: Player) {
        val uniqueId = player.uniqueId
        LxnetCore.rpcManager.requestCall(FetchPlayerScoreCall(uniqueId)) {
            Bukkit.getPlayer(uniqueId)?.sendMessage("当前硬币数量为: &a%d".format(it.score).bukkitColor())
        }
    }

    @Subcommand("add")
    @CommandPermission("lxnet.admin")
    fun commandAdd(player: Player, target: String, amount: Int) {
        val targetPlayer = Bukkit.getPlayerExact(target)
            ?: Bukkit.getOfflinePlayer(target)
        if (!targetPlayer.hasPlayedBefore()) {
            player.sendMessage("该玩家没有在这个服务器上登陆过，无法获取到正确的 UUID。")
            return
        }
        LxnetCore.rpcManager.requestCall(
            AddPlayerScoreCall(
                PlayerScoreRecord(
                    player = targetPlayer.uniqueId,
                    score = amount,
                    reason = ScoreReason.ADMIN_GIVE
                )
            )
        )
        player.sendMessage("命令操作完成。")
        val msg = if(amount < 0) "<red> -$amount 硬币！(管理员扣除)" else "<green> +$amount 硬币！(管理员赠送)"
        LxnetCore.rpcManager.requestCall(
            SendMessageCall(
                player = targetPlayer.uniqueId,
                message = MiniMessage.miniMessage().deserialize(msg)
            )
        )
    }
}