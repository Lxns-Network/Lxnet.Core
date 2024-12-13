package net.lxns.core

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.lxns.core.rpc.ResponseHandler
import org.bukkit.Bukkit
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class RpcManager {
    val callIndex = AtomicInteger()
    val responseHandlers = ConcurrentHashMap<Int, ResponseHandler<*>>()

    fun <R> registerCallHandler(id: Int, handler: ResponseHandler<R>) {
        responseHandlers[id] = handler
    }

    fun getCallHandler(id: Int): ResponseHandler<*>? {
        return responseHandlers[id]
    }

    fun requestCall(call: RemoteCall<*>){
        if (Bukkit.getOnlinePlayers().isEmpty()) {
            throw IllegalStateException("No one is online!")
        }
        call.id = callIndex.getAndIncrement()
        Bukkit.getServer().onlinePlayers.first().sendPluginMessage(
            LxnetCorePlugin.bukkitPlugin,
            RPC_CHANNEL_IDENTIFIER,
            Json.encodeToString(call).encodeToByteArray()
        )
    }

    inline fun <reified R> requestCall(call: RemoteCall<R>, handler: ResponseHandler<R>) {
        requestCall(call)
        registerCallHandler(call.id, handler)
    }
}