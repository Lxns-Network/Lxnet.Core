package net.lxns.core

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.server.RegisteredServer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.lxns.core.event.RemoteCallEvent
import net.lxns.core.rpc.GlobalBroadcastCall
import net.lxns.core.rpc.AddPlayerScoreCall
import net.lxns.core.rpc.FetchPlayerScoreCall
import net.lxns.core.rpc.RaisePlayerCall
import net.lxns.core.rpc.SendMessageCall
import kotlin.jvm.optionals.getOrNull

class RemoteCallHandler(val server: ProxyServer) {
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
        }
    }

    private fun onRaisingPlayer(
        event: RemoteCallEvent<RaisePlayerCall>,
        from: RegisteredServer
    ) {
        for (registeredServer in server.allServers) {
            if(registeredServer != from){
                registeredServer.sendPluginMessage(
                    VelocityEndpoint.rpcChannelIdentifier,
                    lxNetFormat.encodeToString(PolymorphicSerializer(RemoteCall::class), event.call).encodeToByteArray()
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
            VelocityEndpoint.rpcChannelIdentifier,
            lxNetFormat.encodeToString(resp).encodeToByteArray()
        )
    }
}