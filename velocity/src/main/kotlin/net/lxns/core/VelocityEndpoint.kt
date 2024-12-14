package net.lxns.core

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PluginMessageEvent
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.ServerConnection
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import net.lxns.core.dal.DataSource
import net.lxns.core.dal.impl.InMemDataSource
import net.lxns.core.event.RemoteCallEvent
import org.slf4j.Logger
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText

@Plugin(
    id = "lxnet-core",
    name = "LxnetCore",
    version = "0.1.0"
)
class VelocityEndpoint @Inject constructor(
    private val proxyServer: ProxyServer,
    @DataDirectory private val dataDir: Path,
    private val logger: Logger
) {
    companion object {
        val rpcChannelIdentifier = MinecraftChannelIdentifier.from(RPC_CHANNEL_IDENTIFIER)
        lateinit var dataSource: DataSource
            private set
        lateinit var config: LxnetConfig
            private set
    }

    @Subscribe
    fun onInit(event: ProxyInitializeEvent) {
        if (Files.notExists(dataDir)) {
            Files.createDirectory(dataDir);
        }
        config = loadConfig()
        dataSource = loadDataSource()
        proxyServer.channelRegistrar.register(rpcChannelIdentifier)
        proxyServer.eventManager.register(this, RemoteCallHandler(proxyServer))
    }

    private fun loadDataSource(): DataSource {
        return InMemDataSource()
    }

    private fun loadConfig(): LxnetConfig {
        val configLoader = YamlConfigurationLoader.builder()
        val file = dataDir.resolve("config.yml");
        if (file.exists()) {
            return configLoader.buildAndLoadString(file.readText()).get(LxnetConfig::class.java)
                ?: throw IllegalStateException("config.yml is invalid")
        } else {
            return LxnetConfig().also {
                config = it
                val node = configLoader.build().createNode().set(config)
                configLoader.buildAndSaveString(node);
                logger.info("Default config created.")
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Subscribe
    fun onRemoteCall(event: PluginMessageEvent) {
        if (event.identifier != rpcChannelIdentifier) return
        if (event.source !is ServerConnection) return
        event.result = PluginMessageEvent.ForwardResult.handled()
        val globalEvent = lxNetFormat.decodeFromStream<RemoteCall<*>>(event.dataAsInputStream())
        proxyServer.eventManager.fireAndForget(RemoteCallEvent(
            globalEvent,
            (event.source as ServerConnection).server
        ))
    }
}