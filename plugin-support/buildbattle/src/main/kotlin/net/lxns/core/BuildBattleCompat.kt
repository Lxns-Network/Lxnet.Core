package net.lxns.core

import net.lxns.core.record.PlayerScoreRecord
import net.lxns.core.rpc.AddPlayerScoreCall
import net.lxns.core.rpc.RaisePlayerCall
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import plugily.projects.buildbattle.Main
import plugily.projects.buildbattle.api.event.guess.PlayerGuessRightEvent

class BuildBattleCompat : JavaPlugin(), Listener {
    override fun onEnable() {
        if(!dataFolder.exists())
            dataFolder.mkdir()
        saveDefaultConfig()
        reloadConfig()
        LxnetCore.rpcManager.registerListener<RaisePlayerCall>{ event ->
            Bukkit.getOnlinePlayers().forEach {
                if(!getPlugin(Main::class.java).arenaRegistry.isInArena(it)){
                    it.sendMessage(event.message)
                }
            }
        }
        Bukkit.getPluginManager().registerEvents(this, this)
    }

    @EventHandler
    fun onGuessRight(event: PlayerGuessRightEvent){
        //todo: add score for builder
        val baseScore = config.getInt("score.guess-right")
        val score = baseScore + when(event.guesserPosition){
            0 ->  config.getInt("bonus.first")
            1 ->  config.getInt("bonus.second")
            2 ->  config.getInt("bonus.third")
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