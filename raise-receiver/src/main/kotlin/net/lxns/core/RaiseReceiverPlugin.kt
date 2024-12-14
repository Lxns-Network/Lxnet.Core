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

class RaiseReceiverPlugin : JavaPlugin(){
    override fun onEnable() {
        LxnetCore.rpcManager.registerListener<RaisePlayerCall>{
            for (player in Bukkit.getOnlinePlayers()) {
                player.sendMessage(it.message)
            }
        }
    }
}