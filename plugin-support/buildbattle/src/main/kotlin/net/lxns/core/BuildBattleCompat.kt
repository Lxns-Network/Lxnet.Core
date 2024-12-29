package net.lxns.core

import net.lxns.core.record.PlayerScoreRecord
import net.lxns.core.rpc.AddPlayerScoreCall
import net.lxns.core.rpc.PlayerAchievementCall
import net.lxns.core.rpc.RaisePlayerCall
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
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
    fun onPlayerJoin(event: PlayerJoinEvent){
        if(event.player.world.name != "world"){
            event.player.performCommand("bb randomjoin")
        }
    }

    @EventHandler
    fun onArenaBegin(event: PlugilyGameStartEvent) {
        if (event.arena is GuessArena) {
            arenaData[event.arena] = ArenaData().also { data ->
                event.arena.players.forEach {
                    data.playerWinType[it.uniqueId] = null
                }
            }
        }
    }

    @EventHandler
    fun onArenaEnd(event: PlugilyGameStopEvent) {
        val data = arenaData[event.arena]
        data!!.failedPlayers.forEach {
            if (!data.playerWinType.contains(it)) playerWinStreak.remove(it)
        }
        data.playerWinType.forEach { (player, winType) ->
            println("wintype $player $winType")
            val record = playerWinStreak.computeIfAbsent(player) { mutableListOf<WinType>() }
            if (winType == WinType.BUILDER_GUESSED_ALL_RIGHT) {
                if (data.failedPlayers.contains(player)) {
                    // 完美建筑但是没有全部猜对
                    record.add(WinType.BUILDER_GUESSED_ALL_RIGHT)
                    println("Winstreak: ALL BUILT RIGHT, $player")
                } else {
                    // full win
                    record.add(WinType.FULL_WIN)
                    println("Winstreak: FULL WIN, $player")
                }
            } else {
                // 猜对了所有建筑，但是没有建出让所有人猜对的建筑
                record.add(WinType.ALL_GUESSED)
                println("Winstreak: ALL GUESSED, $player")
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
                println("Achievement for $player, master")
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
                    println("Achievement for $player, master+")
                }
            }
        }
    }

    @EventHandler
    fun onGuessRoundEnd(event: GuessRoundEndEvent) {
        val arena = event.arena
        val arenaData = arenaData[arena]!!
        val guessed = event.guessed
        val builders = event.currentBuilders
        println(event.isAllGuessed)
        println(builders.map { it.name }.joinToString(", "))
        println(guessed.map { it.name }.joinToString(", "))
        if (event.isAllGuessed) {
            builders.forEach {
                // 建出满分建筑 TODO NOT TRIGGETED
                arenaData.playerWinType[it.uniqueId] = WinType.BUILDER_GUESSED_ALL_RIGHT
                println("player ${it.name} == BUILD ALL RIGHT")
            }
        }
        event.playersLeft
            .filter { !guessed.contains(it) && !builders.contains(it) }
            .forEach {
                val originalType = arenaData.playerWinType[it.uniqueId]
                arenaData.failedPlayers.add(it.uniqueId)
                if (originalType != WinType.BUILDER_GUESSED_ALL_RIGHT) {
                    // 没有猜对而且也没有建出满分建筑
                    arenaData.playerWinType.remove(it.uniqueId)
                    println("player ${it.name} == REMOVED FROM WINTYPE")
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
        val builderScore = config.getInt("score.guess-builder-per-answer")
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