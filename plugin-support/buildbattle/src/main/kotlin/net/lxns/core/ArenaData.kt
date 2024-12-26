package net.lxns.core

import java.util.UUID

class ArenaData {
    val playerWinType = mutableMapOf<UUID, WinType?>()
    val failedPlayers = mutableSetOf<UUID>()
}