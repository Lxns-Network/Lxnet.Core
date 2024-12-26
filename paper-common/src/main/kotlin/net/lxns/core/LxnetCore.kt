package net.lxns.core

import co.aikar.commands.PaperCommandManager
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import net.lxns.core.record.PlayerAchievementRecord
import net.lxns.core.task.UpdatePlayerScoresTask
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger

class LxnetCore : JavaPlugin(), Listener {
    companion object {
        lateinit var logger: Logger
            private set
        lateinit var bukkitPlugin: JavaPlugin
            private set
        lateinit var rpcManager: RpcManager
            private set
    }

    private val playerScoreCache = ConcurrentHashMap<UUID, Int>()
    private val playerAchievementCache = ConcurrentHashMap<UUID, Map<String, PlayerAchievementRecord>>() // player -> (id -> record(id, time))
    private lateinit var commandManager: PaperCommandManager

    override fun onEnable() {
        logger.info("Loading")
        LxnetCore.logger = this.logger
        bukkitPlugin = this
        rpcManager = RpcManager()
        commandManager = PaperCommandManager(this)
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            UpdatePlayerScoresTask(playerScoreCache, playerAchievementCache).runTaskTimerAsynchronously(this, 0L, 10 * 20L)
            registerCommonPlaceholders()
            registerAchievementPlaceholders()
        }
        server.pluginManager.registerEvents(this, this)
        commandManager.registerCommand(Commands)
        val channelListener = ChannelListener(rpcManager)
        server.messenger.registerIncomingPluginChannel(this, RPC_CALL_CHANNEL_IDENTIFIER, channelListener)
        server.messenger.registerIncomingPluginChannel(this, RPC_RESPONSE_IDENTIFIER, channelListener)
        server.messenger.registerOutgoingPluginChannel(this, RPC_CALL_CHANNEL_IDENTIFIER)
    }

    @EventHandler
    fun onLeave(event: PlayerQuitEvent) {
        playerScoreCache.remove(event.player.uniqueId)
        playerAchievementCache.remove(event.player.uniqueId)
    }

    private fun registerCommonPlaceholders() {
        object : PlaceholderExpansion() {
            override fun getIdentifier(): String = "lxnet"

            override fun getAuthor(): String = "iceBear67"

            override fun getVersion(): String = "0.1.0"

            override fun onPlaceholderRequest(player: Player?, params: String): String? {
                if (player == null) return null
                return when (params) {
                    "coin" -> playerScoreCache.computeIfAbsent(player.uniqueId) { -1 }.toString()
                    else -> "???"
                }
            }
        }.register()
    }

    private fun registerAchievementPlaceholders() {
        object : PlaceholderExpansion() {
            override fun getIdentifier(): String = "la" // lxnet achievement

            override fun getAuthor(): String = "iceBear67"

            override fun getVersion(): String = "0.1.0"

            override fun onPlaceholderRequest(player: Player?, params: String): String? {
                if (player == null) return null
                val achievements = playerAchievementCache.computeIfAbsent(player.uniqueId) { emptyMap<String, PlayerAchievementRecord>() }
                return achievements[params]?.obtainTime.toString()
            }

        }
    }
}