package net.lxns.core

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import net.lxns.core.rpc.ResponseHandler
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener
import java.io.ByteArrayInputStream

class ChannelListener(
    private val rpcManager: RpcManager
) : PluginMessageListener {
    override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray?) {
        if(channel != RPC_CHANNEL_IDENTIFIER) return
        val call = Json.decodeFromStream<RemoteCall<*>>(ByteArrayInputStream(message))
        val handler = rpcManager.getCallHandler(call.id) as? ResponseHandler<Any> ?: run {
            LxnetCorePlugin.logger.warning("No rpc handler is correspond to id ${call.id}")
            return
        }
        handler.onResponse(call)
    }
}