package net.lxns.core

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.PacketEventsAPI
import com.github.retrooper.packetevents.event.PacketEvent
import com.github.retrooper.packetevents.event.PacketListenerPriority
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes
import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PluginMessageEvent
import com.velocitypowered.api.event.player.configuration.PlayerFinishedConfigurationEvent
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.PluginContainer
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.ServerConnection
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import io.github.retrooper.packetevents.velocity.factory.VelocityPacketEventsBuilder
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import net.lxns.core.Achievements.Achievement
import net.lxns.core.dal.DataSource
import net.lxns.core.dal.impl.NoOpDataSource
import net.lxns.core.dal.impl.ReadCacheDataSource
import net.lxns.core.dal.impl.SQLDataSource
import net.lxns.core.event.RemoteCallEvent
import net.lxns.core.packet.MyPacketListener
import net.lxns.core.packet.UpdateAdvancementPacket
import org.jetbrains.exposed.dao.flushCache
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.TimeUnit
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
    private val logger: Logger,
    private val container: PluginContainer
) {
    lateinit var rpcHandler: RemoteCallHandler
    companion object {
        val callChannelId = MinecraftChannelIdentifier.from(RPC_CALL_CHANNEL_IDENTIFIER)
        val respChannelId = MinecraftChannelIdentifier.from(RPC_RESPONSE_IDENTIFIER)
        val eventExecutor = Executors.newSingleThreadExecutor()
        lateinit var dataSource: DataSource
            private set
        lateinit var config: LxnetConfig
            private set
    }

    @Subscribe
    fun onInit(event: ProxyInitializeEvent) {
        PacketEvents.setAPI(VelocityPacketEventsBuilder.build(
            proxyServer,
            container,
            logger,
            dataDir
        ))
        PacketEvents.getAPI().init()
        PacketEvents.getAPI().eventManager.registerListener(MyPacketListener, PacketListenerPriority.NORMAL)
        if (Files.notExists(dataDir)) {
            Files.createDirectory(dataDir);
        }
        config = loadConfig()
        dataSource = loadDataSource()
        proxyServer.channelRegistrar.register(callChannelId)
        rpcHandler = RemoteCallHandler(proxyServer, logger)
        registerShoutCommand(this, proxyServer)
        registerLobbyCommand(this, proxyServer)
    }

    @Subscribe
    fun onFini(event: ProxyShutdownEvent) {
    }

    private fun loadDataSource(): DataSource {
        //return NoOpDataSource()
        return ReadCacheDataSource(SQLDataSource(Database.connect("jdbc:sqlite:${dataDir}/scores.db")), logger)
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
        if (event.identifier != callChannelId) return
        if (event.source !is ServerConnection) return
        event.result = PluginMessageEvent.ForwardResult.handled()
        val globalEvent = lxNetFormat.decodeCall(event.dataAsInputStream())
        eventExecutor.submit {
            rpcHandler.onRPCEvent(
                RemoteCallEvent(
                    globalEvent,
                    (event.source as ServerConnection).server
                )
            )
        }
    }
}