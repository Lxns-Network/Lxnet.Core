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
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger

class LxnetCore : JavaPlugin(), Listener {
    companion object {
        lateinit var logger: Logger
            private set
        lateinit var bukkitPlugin: LxnetCore
            private set
        lateinit var rpcManager: RpcManager
            private set

        fun getCachedScore(player: UUID): Int {
            return bukkitPlugin.playerScoreCache.computeIfAbsent(player) { -1 }
        }

        fun getCachedAchievements(player: UUID): Map<String, PlayerAchievementRecord> {
            return bukkitPlugin.playerAchievementCache.computeIfAbsent(player) { emptyMap() }
        }
    }

    private val playerScoreCache = ConcurrentHashMap<UUID, Int>()
    private val playerAchievementCache =
        ConcurrentHashMap<UUID, Map<String, PlayerAchievementRecord>>() // player -> (id -> record(id, time))
    private lateinit var commandManager: PaperCommandManager

    override fun onEnable() {
        dataFolder.mkdir()
        logger.info("Loading")
        saveDefaultConfig()
        reloadConfig()
        LxnetCore.logger = this.logger
        bukkitPlugin = this
        rpcManager = RpcManager()
        commandManager = PaperCommandManager(this)
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            UpdatePlayerScoresTask(playerScoreCache, playerAchievementCache).runTaskTimerAsynchronously(
                this,
                0L,
                10 * 20L
            )
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
        if (!config.getBoolean("always-fetch")) {
            playerScoreCache.remove(event.player.uniqueId)
            playerAchievementCache.remove(event.player.uniqueId)
        }
    }

    private fun registerCommonPlaceholders() {
        object : PlaceholderExpansion() {
            private val extraAchievements = mutableSetOf<String>()
            private val countAllAchievements = Achievements.allAchievements.keys.size.toString()
            private var blinkCounter = 0
            private val newYearBlinkA = "&cHAPPY NEW YEAR".bukkitColor()
            private val newYearBlinkB = "&eHAPPY NEW YEAR".bukkitColor()

            init {
                for ((k, v) in Achievements.allAchievements) {
                    if (v.extra) extraAchievements.add(k)
                }
            }

            override fun getIdentifier(): String = "lxnet"

            override fun getAuthor(): String = "iceBear67"

            override fun getVersion(): String = "0.1.0"

            override fun onPlaceholderRequest(player: Player?, params: String): String? {
                if (player == null) return null
                return when (params) {
                    "coin" -> getCachedScore(player.uniqueId).toString()
                    "achievement_accomplished_total" -> getCachedAchievements(player.uniqueId).size.toString()
                    "achievement_accomplished_extra" -> getCachedAchievements(player.uniqueId)
                        .values.count { extraAchievements.contains(it.achievement) }.toString()

                    "achievement_total" -> countAllAchievements
                    "countdown" -> {
                        var current = LocalDateTime.now()
                        var sb = StringBuilder()
                        sb.append(23 - current.hour);
                        if (current.year > 2024) {
                            if (++blinkCounter % 5 == 0) {
                                return newYearBlinkA
                            }
                            return newYearBlinkB
                        }
                        return "${23-current.hour}h ${59-current.minute}m ${59-current.second}s"
                    }

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
                val achievements = getCachedAchievements(player.uniqueId)
                return achievements[params]?.obtainTime.toString()
            }

        }.register()
    }
}