package ink.ptms.sandalphon.module.api

import net.minecraft.core.BlockPosition
import net.minecraft.network.protocol.game.PacketPlayOutBlockAction
import net.minecraft.world.level.block.Blocks
import org.bukkit.block.Block
import org.bukkit.craftbukkit.v1_18_R2.block.CraftBlock
import org.bukkit.entity.Player
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.sendPacket

/**
 * @author sky
 * @since 2020-05-30 17:00
 */
class NMSImpl : NMS() {

    override fun sendBlockAction(player: Player, block: Block, a: Int, b: Int) {
        if (MinecraftVersion.majorLegacy >= 11300) {
            val position = BlockPosition(block.location.x, block.location.y, block.location.z)
            player.sendPacket(PacketPlayOutBlockAction(position, Blocks.CHEST, a, b))
        } else {
            val position = net.minecraft.server.v1_12_R1.BlockPosition(block.location.x, block.location.y, block.location.z)
            player.sendPacket(net.minecraft.server.v1_12_R1.PacketPlayOutBlockAction(position, net.minecraft.server.v1_12_R1.Blocks.CHEST, a, b))
        }
    }

    override fun setBlockData(block: Block, data: Byte) {
        (block as CraftBlock).setData(data, false)
    }
}