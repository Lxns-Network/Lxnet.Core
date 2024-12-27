package net.lxns.core

import com.github.retrooper.packetevents.PacketEvents
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.proxy.ConsoleCommandSource
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.text.minimessage.MiniMessage
import net.lxns.core.packet.UpdateAdvancementPacket
import net.lxns.core.packet.achievementPopup

internal fun registerShoutCommand(plugin: VelocityEndpoint, proxyServer: ProxyServer) {
    val cmd = BrigadierCommand.literalArgumentBuilder("gshout")
        .then(
            BrigadierCommand.requiredArgumentBuilder("message", StringArgumentType.greedyString())
                .executes { ctx ->
                    val source = ctx.source
                    val name = when (source) {
                        is Player -> source.username
                        is ConsoleCommandSource -> "CONSOLE"
                        else -> throw AssertionError("IMPOSSIBLE")
                    }
                    val message = MiniMessage.miniMessage().deserialize(
                        "<green>[SHOUT]</green> ${name}: ${ctx.getArgument<String>("message", String::class.java)}",
                    )
                    proxyServer.allPlayers.forEach {
                        it.sendMessage(message)
                    }
                    Command.SINGLE_SUCCESS
                }
        ).build()
    val meta = proxyServer.commandManager.metaBuilder("gshout")
        .plugin(plugin)
        .build()
    proxyServer.commandManager.register(meta, BrigadierCommand(cmd))
}

internal fun registerLobbyCommand(plugin: VelocityEndpoint, proxyServer: ProxyServer) {
    val cmd = BrigadierCommand.literalArgumentBuilder("lobby")
        .executes {
            if (it.source !is Player) return@executes Command.SINGLE_SUCCESS
            (it.source as Player).createConnectionRequest(
                proxyServer.getServer("lobby").orElseThrow()
            ).fireAndForget()
            return@executes Command.SINGLE_SUCCESS
        }
    val meta = proxyServer.commandManager.metaBuilder("lobby")
        .plugin(plugin).build()
    proxyServer.commandManager.register(meta, BrigadierCommand(cmd))
}