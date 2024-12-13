package net.lxns.core

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.proxy.ProxyServer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.lxns.core.event.RemoteCallEvent
import net.lxns.core.rpc.GlobalBroadcastCall
import net.lxns.core.rpc.AddPlayerScoreCall
import net.lxns.core.rpc.FetchPlayerScoreCall

class RemoteCallHandler(val server: ProxyServer) {
    @Subscribe
    fun onRPCEvent(event: RemoteCallEvent<*>) {
        when (event.call) {
            is GlobalBroadcastCall -> {
                for (player in server.allPlayers) {
                    player.sendMessage(event.call.message)
                }
            }

            is AddPlayerScoreCall -> {
                VelocityEndpoint.dataSource.addPlayerScore(event.call.record)
            }

            is FetchPlayerScoreCall -> onFetchPlayerScore(event as RemoteCallEvent<FetchPlayerScoreCall>)
        }
    }

    private fun onFetchPlayerScore(event: RemoteCallEvent<FetchPlayerScoreCall>) {
        val id = event.call.id
        val player = event.call.player
        val score = VelocityEndpoint.dataSource.getPlayerScore(player)
        val resp = FetchPlayerScoreCall.Response(score, id)
        event.server.sendPluginMessage(VelocityEndpoint.rpcChannelIdentifier, Json.encodeToString(resp).toByteArray())
    }
}