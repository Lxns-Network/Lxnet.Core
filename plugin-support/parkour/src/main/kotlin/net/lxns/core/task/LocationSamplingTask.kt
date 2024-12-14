package net.lxns.core.task

import net.lxns.core.ParkourCompat
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.scheduler.BukkitRunnable
import java.util.UUID
import kotlin.math.max
import kotlin.math.min

class LocationSamplingTask(
    val parkourCompat: ParkourCompat,
    val baseScore: Int
) : BukkitRunnable() {
    val lastLocations = mutableMapOf<UUID, Location>()
    override fun run() {
        for (player in Bukkit.getOnlinePlayers()) {
            val lastLoc = lastLocations.computeIfAbsent(player.uniqueId) { player.location }
            val current = player.location
            val originScore = parkourCompat.playerScores[player.uniqueId] ?: continue
            parkourCompat.playerScores[player.uniqueId] =
                originScore * (1+(0.001 * min(2,lastLoc.distance(current).toInt())))
            lastLocations[player.uniqueId] = current
        }
    }
}