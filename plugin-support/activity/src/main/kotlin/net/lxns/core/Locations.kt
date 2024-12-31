package net.lxns.core

import org.bukkit.Bukkit
import org.bukkit.Location

class Locations {
    val RIGHT_BACK = parseLocation("-215.55 107.00 -2.67 -177.35 83.10")
    val LEFT_BACK = parseLocation("-215.49 107.00 -22.67 -361.10 86.40")
    val SEAT_BACK_A = parseLocation("-206.32 110.50 -15.84 -91.25 33.75")
    val SEAT_BACK_B = parseLocation("-206.02 110.50 -8.82 -89.75 31.65")
    val SEAT_BACK_C = parseLocation("-205.98 110.50 -1.18 -87.95 42.00")

    val FRONT_RIGHT = parseLocation("-182.45 109.00 -21.74 -317.90 30.90")
    val FRONT_LEFT = parseLocation("-182.97 109.00 4.04 -216.20 17.55")

    fun parseLocation(str: String): Location {
        val split = str.trim().split(" ")
        return Location(Bukkit.getWorld("world"), split[0].toDouble(), split[1].toDouble(), split[2].toDouble())
    }
}