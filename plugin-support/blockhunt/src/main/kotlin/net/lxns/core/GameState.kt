package net.lxns.core

import org.bukkit.scheduler.BukkitTask
import java.util.UUID

class GameState {
    companion object {
        const val TAG_KILL_HUNTER = "kill_hunter"
        const val TAG_KILL_BLOCK = "kill_block"
    }
    val playerTags = mutableMapOf<UUID, MutableSet<String>>()
}