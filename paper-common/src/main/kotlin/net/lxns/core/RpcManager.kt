package net.lxns.core

import kotlinx.serialization.encodeToString
import net.lxns.core.rpc.ResponseHandler
import org.bukkit.Bukkit
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass

class RpcManager {
    private val callIndex = AtomicInteger()
    private val pendingCallHandlers = ConcurrentHashMap<Int, ResponseHandler<*>>()
    internal val listeners = mutableListOf<ResponseHandler<*>>()

    fun <R> registerCallHandler(id: Int, handler: ResponseHandler<R>) {
        pendingCallHandlers[id] = handler
    }

    fun <R> registerListener(clazz: Class<*>, handler: ResponseHandler<R>) {
        listeners.add {
            if(clazz.isInstance(it)) handler.onResponse(it as R)
        }
    }

    inline fun <reified R> registerListener(handler: ResponseHandler<R>) {
        registerListener(R::class.java, handler)
    }

    fun getAndRevokeCallHandler(id: Int): ResponseHandler<*>? {
        return pendingCallHandlers[id].also { pendingCallHandlers.remove(id) }
    }

    fun getCallHandler(id: Int): ResponseHandler<*>? {
        return pendingCallHandlers[id]
    }

    fun requestCall(call: RemoteCall<out RemoteResponse>){
        if (Bukkit.getOnlinePlayers().isEmpty()) {
            throw IllegalStateException("No one is online!")
        }
        call.id = callIndex.getAndIncrement()
        Bukkit.getServer().onlinePlayers.first().sendPluginMessage(
            LxnetCore.bukkitPlugin,
            RPC_CHANNEL_IDENTIFIER,
            lxNetFormat.encodeToString(call).encodeToByteArray()
        )
    }

    inline fun <reified R: RemoteResponse> requestCall(call: RemoteCall<R>, handler: ResponseHandler<R>) {
        requestCall(call)
        registerCallHandler(call.id, handler)
    }
}