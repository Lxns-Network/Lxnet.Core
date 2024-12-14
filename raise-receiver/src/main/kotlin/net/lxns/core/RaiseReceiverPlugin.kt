package net.lxns.core

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import net.kyori.adventure.Adventure
import net.lxns.core.rpc.RaisePlayerCall
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.messaging.PluginMessageListener
import java.io.ByteArrayInputStream

class RaiseReceiverPlugin : JavaPlugin(), PluginMessageListener {
    override fun onEnable() {
        server.messenger.registerIncomingPluginChannel(this, RPC_CHANNEL_IDENTIFIER, this)
    }

    override fun onPluginMessageReceived(
        channel: String,
        player: Player,
        message: ByteArray
    ) {
        if (channel != RPC_CHANNEL_IDENTIFIER) return
        val message = Json.decodeFromStream<RemoteCall<*>>(ByteArrayInputStream(message))
        if (message !is RaisePlayerCall) {
            return
        }
        for (player in Bukkit.getOnlinePlayers()) {
            player.sendMessage(message.message)
        }
    }
}