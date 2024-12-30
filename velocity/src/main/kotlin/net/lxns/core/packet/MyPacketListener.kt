package net.lxns.core.packet

import com.github.retrooper.packetevents.event.PacketListener
import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.chat.ChatType
import com.github.retrooper.packetevents.protocol.chat.ChatTypes
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_19
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_19_1
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.login.server.WrapperLoginServerLoginSuccess
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChatMessage
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerJoinGame
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage

object MyPacketListener : PacketListener {
    override fun onPacketSend(event: PacketSendEvent) {
        if (event.packetType == PacketType.Play.Server.JOIN_GAME) {
            val packet = WrapperPlayServerJoinGame(event)
            packet.isEnforcesSecureChat = false
            event.markForReEncode(true)
            return
        }
        if (event.packetType != PacketType.Play.Server.CHAT_MESSAGE) return
        val packet = WrapperPlayServerChatMessage(event)
        if (packet.message.type == ChatTypes.CHAT && packet.message is ChatMessage_v1_19_1) {
            val formatting = (packet.message as ChatMessage_v1_19_1).chatFormatting

            val newPacket = WrapperPlayServerSystemChatMessage(
                false,
                ChatTypes.CHAT.chatDecoration.decorate(packet.message.chatContent, formatting)
            )
            event.user.sendPacket(newPacket)
            event.isCancelled = true
        }
    }
}