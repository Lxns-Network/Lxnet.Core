package net.lxns.core

import net.lxns.core.record.PlayerScoreRecord
import net.lxns.core.rpc.AddPlayerScoreCall
import net.lxns.core.rpc.PlayerAchievementCall
import net.lxns.core.task.LocationSamplingTask
import net.lxns.core.task.TipPlayerTask
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID

class ParkourCompat : JavaPlugin(), Listener {
    val playerScores = mutableMapOf<UUID, Double>()
    val claimedPlayers = mutableSetOf<UUID>()
    lateinit var pdcKeyFirstJoin: NamespacedKey
    override fun onEnable() {
        dataFolder.mkdir()
        saveDefaultConfig()
        reloadConfig()
        loadData()
        TipPlayerTask(
            this,
            config.getString("message")!!
        ).runTaskTimer(this, 0, 60 * 20L)
        LocationSamplingTask(this, config.getDouble("score-initial").toInt()).runTaskTimer(this, 0, 35L)
        Bukkit.getPluginManager().registerEvents(this, this)
        pdcKeyFirstJoin = NamespacedKey(this, "first_join")
    }

    override fun onDisable() {
        val list = dataFolder.resolve("claimed_players")
        list.writeText(claimedPlayers.joinToString("\n"))
    }

    private fun loadData() {
        val list = dataFolder.resolve("claimed_players")
        if (!list.exists()) return
        list.readLines().forEach {
            claimedPlayers.add(UUID.fromString(it))
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (label.equals("reward", ignoreCase = true)) {
            if (sender is Player) {
                if (claimedPlayers.contains(sender.uniqueId)) {
                    sender.sendMessage("&c你已经领取过了！".bukkitColor())
                } else {
                    claimedPlayers.add(sender.uniqueId)
                    val score = config.getInt("score-win")
                    sender.sendMessage("&6恭喜你完成了跑酷挑战! ( +%d硬币 )".format(score).bukkitColor())
                    LxnetCore.rpcManager.requestCall(
                        AddPlayerScoreCall(
                            PlayerScoreRecord(
                                sender.uniqueId,
                                score,
                                ScoreReason.GAME_WINNER
                            )
                        )
                    )
                    val timeElasped = System.currentTimeMillis() -
                            sender.persistentDataContainer.get(pdcKeyFirstJoin, PersistentDataType.LONG)!!
                    LxnetCore.rpcManager.requestCall(
                        PlayerAchievementCall(
                            sender.uniqueId,
                            Achievements.Parkour.PARKOUR_PLAYER.id
                        )
                    )
                    if (timeElasped < 30 * 60 * 1000) {
                        LxnetCore.rpcManager.requestCall(
                            PlayerAchievementCall(
                                sender.uniqueId,
                                Achievements.Parkour.PARKOUR_MASTER.id
                            )
                        )
                        if (timeElasped < 20 * 60 * 1000) {
                            LxnetCore.rpcManager.requestCall(
                                PlayerAchievementCall(
                                    sender.uniqueId,
                                    Achievements.Parkour.PARKOUR_MASTER_PLUS.id
                                )
                            )
                        }
                    }
                }
            }
        }
        return true
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        playerScores.computeIfAbsent(event.player.uniqueId) { config.getDouble("score-initial") }
        val pdc = event.player.persistentDataContainer
        if (!pdc.has(pdcKeyFirstJoin)) {
            pdc.set(pdcKeyFirstJoin, PersistentDataType.LONG, System.currentTimeMillis())
        }
    }
}