package net.lxns.core.compat.task

import com.andrei1058.bedwars.api.BedWars
import com.andrei1058.bedwars.api.arena.GameState
import net.kyori.adventure.text.Component
import net.lxns.core.LxnetCore
import net.lxns.core.rpc.RaisePlayerCall
import org.bukkit.scheduler.BukkitRunnable

class CheckPlayerNumTask(
    val bw: BedWars,
    val message: Component
) : BukkitRunnable() {
    override fun run() {
        for (arena in bw.arenaUtil.arenas) {
            if(arena.status == GameState.waiting
                && arena.players.isNotEmpty()){
                LxnetCore.rpcManager.requestCall(
                    RaisePlayerCall(
                        message
                    )
                )
            }
        }
    }
}