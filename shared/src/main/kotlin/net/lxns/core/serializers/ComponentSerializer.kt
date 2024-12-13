package net.lxns.core.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

class ComponentSerializer : KSerializer<Component> {
    override val descriptor: SerialDescriptor = String.serializer().descriptor

    override fun deserialize(decoder: Decoder): Component {
        val raw = decoder.decodeString()
        return MiniMessage.miniMessage().deserialize(raw)
    }

    override fun serialize(encoder: Encoder, value: Component) {
        encoder.encodeString(MiniMessage.miniMessage().serialize(value))
    }
}