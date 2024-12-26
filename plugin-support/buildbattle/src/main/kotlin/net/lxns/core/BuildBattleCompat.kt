package net.lxns.core

import net.lxns.core.record.PlayerScoreRecord
import net.lxns.core.rpc.AddPlayerScoreCall
import net.lxns.core.rpc.PlayerAchievementCall
import net.lxns.core.rpc.RaisePlayerCall
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import plugily.projects.buildbattle.Main
import plugily.projects.buildbattle.api.event.guess.GuessRoundEndEvent
import plugily.projects.buildbattle.api.event.guess.PlayerGuessRightEvent
import plugily.projects.buildbattle.arena.GuessArena
import plugily.projects.buildbattle.minigamesbox.api.arena.IPluginArena
import plugily.projects.buildbattle.minigamesbox.api.events.game.PlugilyGameStartEvent
import plugily.projects.buildbattle.minigamesbox.api.events.game.PlugilyGameStopEvent
import java.util.UUID

class BuildBattleCompat : JavaPlugin(), Listener {
    val arenaData = mutableMapOf<IPluginArena, ArenaData>()
    val playerWinStreak = mutableMapOf<UUID, MutableList<WinType>>()
    val wonAchivementPlayers = mutableSetOf<UUID>()
    val masterAchievementPlayers = mutableSetOf<UUID>()
    override fun onEnable() {
        if (!dataFolder.exists())
            dataFolder.mkdir()
        saveDefaultConfig()
        reloadConfig()
        LxnetCore.rpcManager.registerListener<RaisePlayerCall> { event ->
            Bukkit.getOnlinePlayers().forEach {
                if (!getPlugin(Main::class.java).arenaRegistry.isInArena(it)) {
                    it.sendMessage(event.message)
                }
            }
        }
        Bukkit.getPluginManager().registerEvents(this, this)
    }

    @EventHandler
    fun onArenaBegin(event: PlugilyGameStartEvent) {
        if (event.arena is GuessArena) {
            arenaData[event.arena] = ArenaData()
        }
    }

    @EventHandler
    fun onArenaEnd(event: PlugilyGameStopEvent) {
        val data = arenaData[event.arena]
        data!!.failedPlayers.forEach {
            if (!data.playerWinType.contains(it)) playerWinStreak.remove(it)
        }
        data.playerWinType.forEach { (player, winType) ->
            val record = playerWinStreak.computeIfAbsent(player) { mutableListOf<WinType>() }
            if (winType == WinType.BUILDER_GUESSED_ALL_RIGHT) {
                if (data.failedPlayers.contains(player)) {
                    // 完美建筑但是没有全部猜对
                    record.add(WinType.BUILDER_GUESSED_ALL_RIGHT)
                } else {
                    // full win
                    record.add(WinType.FULL_WIN)
                }
            } else {
                // 猜对了所有建筑，但是没有建出让所有人猜对的建筑
                record.add(WinType.ALL_GUESSED)
            }
        }
        checkAchievements()
    }

    private fun checkAchievements() {
        playerWinStreak.forEach { (player, winTypes) ->
            if ((winTypes.contains(WinType.ALL_GUESSED) || winTypes.contains(WinType.FULL_WIN))
                && !wonAchivementPlayers.contains(player)
            ) {
                wonAchivementPlayers.add(player)
                LxnetCore.rpcManager.requestCall(
                    PlayerAchievementCall(
                        player,
                        Achievements.BuildBattle.GUESS_ALL_RIGHT.id
                    )
                )
            }
            if (winTypes.size >= 3 && !masterAchievementPlayers.contains(player)) {
                if (winTypes.subList(winTypes.size - 3, winTypes.size).all { it == WinType.FULL_WIN }) {
                    LxnetCore.rpcManager.requestCall(
                        PlayerAchievementCall(
                            player,
                            Achievements.BuildBattle.ABSTRACT_MASTER.id
                        )
                    )
                    masterAchievementPlayers.add(player)
                }
            }
        }
    }

    @EventHandler
    fun onGuessRoundEnd(event: GuessRoundEndEvent) {
        val arena = event.arena
        if (event.isAllGuessed) return
        val arenaData = arenaData[arena]!!
        val guessed = arena.whoGuessed
        val builders = event.arena.currentBuilders
        builders.forEach {
            if (event.isAllGuessed) {
                // 建出满分建筑
                arenaData.playerWinType[it.uniqueId] = WinType.BUILDER_GUESSED_ALL_RIGHT
            }
        }
        arena.playersLeft
            .filterNot { guessed.contains(it) && builders.contains(it) }
            .forEach {
                val originalType = arenaData.playerWinType[it.uniqueId]
                arenaData.failedPlayers.add(it.uniqueId)
                if (originalType != WinType.BUILDER_GUESSED_ALL_RIGHT) {
                    // 没有猜对而且也没有建出满分建筑
                    arenaData.playerWinType.remove(it.uniqueId)
                }
            }
    }

    @EventHandler
    fun onGuessRight(event: PlayerGuessRightEvent) {
        val baseScore = config.getInt("score.guess-right")
        val score = baseScore + when (event.guesserPosition) {
            0 -> config.getInt("bonus.first")
            1 -> config.getInt("bonus.second")
            2 -> config.getInt("bonus.third")
            else -> 0
        }
        val builderScore = config.getInt("guess-builder-per-answer")
        event.player.sendMessage(config.getString("message.guess-right")!!.format(score).bukkitColor())
        LxnetCore.rpcManager.requestCall(
            AddPlayerScoreCall(
                PlayerScoreRecord(
                    event.player.uniqueId,
                    score,
                    ScoreReason.PLAYING_GAME
                )
            )
        )
        event.builders.forEach {
            it.sendMessage(config.getString("message.build-right")!!.format(builderScore).bukkitColor())
            LxnetCore.rpcManager.requestCall(
                AddPlayerScoreCall(
                    PlayerScoreRecord(
                        it.uniqueId,
                        builderScore,
                        ScoreReason.PLAYING_GAME
                    )
                )
            )
        }
    }
}