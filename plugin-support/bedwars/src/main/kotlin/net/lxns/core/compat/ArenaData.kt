package net.lxns.core.compat

import java.util.UUID

class ArenaData {
    // victim -> killer
    val lastKiller = mutableMapOf<UUID, UUID>()
}