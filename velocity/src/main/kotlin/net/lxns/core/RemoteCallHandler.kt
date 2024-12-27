package net.lxns.core

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.server.RegisteredServer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.encodeToString
import net.lxns.core.event.RemoteCallEvent
import net.lxns.core.rpc.GlobalBroadcastCall
import net.lxns.core.rpc.AddPlayerScoreCall
import net.lxns.core.rpc.FetchPlayerAchievementCall
import net.lxns.core.rpc.FetchPlayerScoreCall
import net.lxns.core.rpc.PlayerAchievementCall
import net.lxns.core.rpc.RaisePlayerCall
import net.lxns.core.rpc.SendMessageCall
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
            is FetchPlayerScoreCall -> onFetchPlayerScore(event as RemoteCallEvent<FetchPlayerScoreCall>)
            is RaisePlayerCall -> onRaisingPlayer(event as RemoteCallEvent<RaisePlayerCall>, event.server)
            is SendMessageCall -> server.getPlayer(event.call.player).getOrNull()?.sendMessage(event.call.message)
            is PlayerAchievementCall -> onAchievementCall(event.call)
            is FetchPlayerAchievementCall -> onFetchPlayerAchievement(event as RemoteCallEvent<FetchPlayerAchievementCall>)
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
        VelocityEndpoint.dataSource.addAchievement(call.player, call.achievementId)
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