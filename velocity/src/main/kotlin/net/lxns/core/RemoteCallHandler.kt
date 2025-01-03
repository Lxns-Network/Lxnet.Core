package net.lxns.core

import com.github.retrooper.packetevents.PacketEvents
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.server.RegisteredServer
import net.kyori.adventure.text.minimessage.MiniMessage
import net.lxns.core.event.RemoteCallEvent
import net.lxns.core.packet.UpdateAdvancementPacket
import net.lxns.core.packet.achievementPopup
import net.lxns.core.record.PlayerScoreRecord
import net.lxns.core.rpc.AddPlayerScoreCall
import net.lxns.core.rpc.FetchPlayerAchievementCall
import net.lxns.core.rpc.FetchPlayerScoreCall
import net.lxns.core.rpc.GlobalBroadcastCall
import net.lxns.core.rpc.PlayerAchievementCall
import net.lxns.core.rpc.RaisePlayerCall
import net.lxns.core.rpc.SendMessageCall
import net.lxns.core.rpc.WithdrawPlayerScoreCall
import org.slf4j.Logger
import kotlin.jvm.optionals.getOrNull

class RemoteCallHandler(
    private val server: ProxyServer,
    private val logger: Logger
) {
    @Subscribe
    fun onRPCEvent(event: RemoteCallEvent<*>) {
        when (event.call) {
            is GlobalBroadcastCall -> {
                for (player in server.allPlayers) {
                    player.sendMessage(event.call.message)
                }
            }

            is AddPlayerScoreCall -> VelocityEndpoint.dataSource.addPlayerScore(event.call.record)
            is WithdrawPlayerScoreCall -> onWithdrawPlayerScore(event as RemoteCallEvent<WithdrawPlayerScoreCall>)
            is FetchPlayerScoreCall -> onFetchPlayerScore(event as RemoteCallEvent<FetchPlayerScoreCall>)
            is RaisePlayerCall -> onRaisingPlayer(event as RemoteCallEvent<RaisePlayerCall>, event.server)
            is SendMessageCall -> server.getPlayer(event.call.player).getOrNull()?.sendMessage(event.call.message)
            is PlayerAchievementCall -> onAchievementCall(event.call)
            is FetchPlayerAchievementCall -> onFetchPlayerAchievement(event as RemoteCallEvent<FetchPlayerAchievementCall>)
        }
    }

    private fun onWithdrawPlayerScore(event: RemoteCallEvent<WithdrawPlayerScoreCall>) {
        val record = event.call.record
        val scores = VelocityEndpoint.dataSource.getPlayerScore(record.player)
        if (record.score > scores) {
            // fail.
            event.server.sendPluginMessage(
                VelocityEndpoint.respChannelId,
                lxNetFormat.encodeResponse(WithdrawPlayerScoreCall.Response(false, scores, event.call.id)).toByteArray()
            )
        }else{
            // success
            VelocityEndpoint.dataSource.addPlayerScore(PlayerScoreRecord(
                record.player,
                record.score * -1,
                ScoreReason.PURCHASE,
                record.time
            ))
            event.server.sendPluginMessage(
                VelocityEndpoint.respChannelId,
                lxNetFormat.encodeResponse(WithdrawPlayerScoreCall.Response(true, scores - record.score, event.call.id)).toByteArray()
            )
        }
    }

    private fun onFetchPlayerAchievement(event: RemoteCallEvent<FetchPlayerAchievementCall>) {
        val records = VelocityEndpoint.dataSource.getAchievements(event.call.player)
        event.server.sendPluginMessage(
            VelocityEndpoint.respChannelId,
            lxNetFormat.encodeResponse(FetchPlayerAchievementCall.Response(records, event.call.id)).toByteArray()
        )
    }

    private fun onAchievementCall(call: PlayerAchievementCall) {
        if (!Achievements.allAchievements.containsKey(call.achievementId)) {
            logger.warn("Achievement ${call.achievementId} not found")
        }
        val ds = VelocityEndpoint.dataSource

        // A typical check-then-set doesn't guarantee thread-safe.
        // It's an effort to avoid large queries. There's another check in the underlying implementation
        // to make this operation behaves correctly.
        if (!ds.hasAchievementBefore(call.player, call.achievementId)) {
            ds.addAchievement(call.player, call.achievementId)
            // send achievement dialog
            val player = server.getPlayer(call.player).getOrNull() ?: return
            val achievement = Achievements.allAchievements[call.achievementId] ?: return
            achievementPopup(player, achievement)
            player.sendMessage(
                MiniMessage.miniMessage().deserialize(
                    "<hover:show_text:'${achievement.description}'><gold> <obf>qwq</obf> >></gold> <dark_green>解锁成就:</dark_green> ${achievement.name} <gold><<  <obf>qwq</obf> </gold></hover>"
                )
            )
        }
        println()
    }

    private fun onRaisingPlayer(
        event: RemoteCallEvent<RaisePlayerCall>,
        from: RegisteredServer
    ) {
        for (registeredServer in server.allServers) {
            if (registeredServer != from) {
                registeredServer.sendPluginMessage(
                    VelocityEndpoint.callChannelId,
                    lxNetFormat.encodeCall(event.call).encodeToByteArray()
                )
            }
        }
    }

    private fun onFetchPlayerScore(event: RemoteCallEvent<FetchPlayerScoreCall>) {
        val id = event.call.id
        val player = event.call.player
        val score = VelocityEndpoint.dataSource.getPlayerScore(player)
        val resp = FetchPlayerScoreCall.Response(score, id)
        event.server.sendPluginMessage(
            VelocityEndpoint.respChannelId,
            lxNetFormat.encodeResponse(resp).encodeToByteArray()
        )
    }
}