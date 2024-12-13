package net.lxns.core

import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Logger

class LxnetCorePlugin : JavaPlugin() {
    companion object {
        lateinit var logger: Logger
            private set
        lateinit var bukkitPlugin: JavaPlugin
            private set
        lateinit var rpcManager: RpcManager
            private set
    }
    override fun onEnable() {
        logger.info("Loading")
        LxnetCorePlugin.logger = this.logger
        bukkitPlugin = this
        rpcManager = RpcManager()
        server.messenger.registerIncomingPluginChannel(this, RPC_CHANNEL_IDENTIFIER, ChannelListener(rpcManager))

    }
}