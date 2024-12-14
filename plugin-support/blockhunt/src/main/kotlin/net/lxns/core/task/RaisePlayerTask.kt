package net.lxns.core.task

import dev.tylerm.khs.Main
import dev.tylerm.khs.game.util.Status.STANDBY
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.lxns.core.LxnetCore
import net.lxns.core.rpc.RaisePlayerCall
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

class RaisePlayerTask(
    private val message: Component
) : BukkitRunnable() {
    var time = System.currentTimeMillis()
    override fun run() {
        if(Bukkit.getOnlinePlayers().isEmpty()){
            time = System.currentTimeMillis()
            return
        }
        if(Main.getInstance().game.status == STANDBY){
            if(System.currentTimeMillis() - time > 20 * 1000){
                LxnetCore.rpcManager.requestCall(
                    RaisePlayerCall(
                        message,
                        -1
                    )
                )
                time = System.currentTimeMillis()
            }
        }
    }
}