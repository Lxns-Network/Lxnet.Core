package net.lxns.core.event

import com.velocitypowered.api.proxy.server.RegisteredServer
import com.velocitypowered.api.proxy.server.ServerInfo
import net.lxns.core.RemoteCall

data class RemoteCallEvent<T: RemoteCall<*>>(
    val call: T,
    val server: RegisteredServer
) {
}