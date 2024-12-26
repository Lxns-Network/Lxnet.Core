package net.lxns.core

import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.decodeFromStream
import net.lxns.core.rpc.ResponseHandler
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener
import java.io.ByteArrayInputStream

class ChannelListener(
    private val rpcManager: RpcManager
) : PluginMessageListener {
    override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {
        when(channel){
            RPC_CALL_CHANNEL_IDENTIFIER -> handleChannelCall(message)
            RPC_RESPONSE_IDENTIFIER -> handleResponseCall(message)
        }
    }

    private fun handleResponseCall(bytes: ByteArray) {
        val call = lxNetFormat.decodeFromStream<RemoteResponse>(PolymorphicSerializer(RemoteResponse::class), ByteArrayInputStream(bytes))
        val handler = rpcManager.getAndRevokeCallHandler(call.id) as? ResponseHandler<Any> ?: run {
            LxnetCore.logger.warning("No rpc handler is correspond to id ${call.id}")
            return
        }
        handler.onResponse(call)
    }

    private fun handleChannelCall(message: ByteArray) {
        val call = lxNetFormat.decodeFromStream<RemoteCall<RemoteResponse>>(ByteArrayInputStream(message))
        for (handler in rpcManager.listeners) {
            (handler as ResponseHandler<Any>).onResponse(call)
        }
    }
}