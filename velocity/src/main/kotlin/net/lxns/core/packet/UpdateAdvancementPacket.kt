package net.lxns.core.packet

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.protocol.item.ItemStack
import com.github.retrooper.packetevents.protocol.item.type.ItemType
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.protocol.recipe.display.slot.ItemStackSlotDisplay
import com.github.retrooper.packetevents.resources.ResourceLocation
import com.github.retrooper.packetevents.wrapper.PacketWrapper
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.minimessage.MiniMessage
import net.lxns.core.Achievements
import java.util.Date

fun achievementPopup(
    player: Player,
    achievement: Achievements.Achievement,
    item: ItemType = ItemTypes.EMERALD,
) {
    with(PacketEvents.getAPI()) {
        playerManager.sendPacketSilently(player, UpdateAdvancementPacket(achievement, item))
        playerManager.sendPacketSilently(player, UpdateAdvancementPacket.ClearAdvancementPacket(ResourceLocation("lxnet", achievement.id)))
    }
}

internal class UpdateAdvancementPacket(
    val achievement: Achievements.Achievement,
    val item: ItemType,
) : PacketWrapper<UpdateAdvancementPacket>(
    PacketType.Play.Server.UPDATE_ADVANCEMENTS
) {
    override fun write() {
        writeBoolean(false) // do not reset
        writeVarInt(1) // mapping size 1
        val rl = ResourceLocation("lxnet", achievement.id)
        writeIdentifier(rl) // id of advancement
        run { // write advancement
            writeBoolean(false) // no parent
            writeBoolean(true) // has display
            // write display
            writeComponent(MiniMessage.miniMessage().deserialize(achievement.name)) // title
            writeComponent(MiniMessage.miniMessage().deserialize(achievement.description)) // desc
            writePresentItemStack(ItemStack.builder().type(item).build()) // icon
            if (achievement.extra) {
                writeVarInt(1) // frame type challenge
            } else {
                writeVarInt(2) // goal
            }
            writeInt(0x02) // flags show_toast
            writeFloat(0.5f) // x coord
            writeFloat(0.5f) // y coord
            //end
        }
        writeVarInt(1) // requirements 1 criteria
        writeArray<String>(arrayOf(rl.toString())) { a, b -> a.writeString(b) }
        writeBoolean(false) // no telemetry
        // list size -- advancements should be removed
        writeVarInt(0) // zero. no advancements to remove
        writeVarInt(1) // progress arr size
        //progress
        writeIdentifier(rl) // key of advancement
        writeVarInt(1) // size of critaria array
        writeIdentifier(rl) // criterion id
        //criterion progress begin
        writeBoolean(true) // achieved
        writeLong(Date().time)
    }

    internal class ClearAdvancementPacket(
        val rl: ResourceLocation
    ) : PacketWrapper<ClearAdvancementPacket>(PacketType.Play.Server.UPDATE_ADVANCEMENTS) {

        override fun write() {
            writeBoolean(true)
            writeArray(arrayOf(Unit)) { a, b ->
                a.writeIdentifier(rl)
                writeBoolean(false)
                writeBoolean(false)
                writeVarInt(0)
                writeBoolean(false)
            }
            writeArray<ResourceLocation>(arrayOf(rl)) { a, b -> a.writeIdentifier(b) }
            writeVarInt(0)
        }

    }
}