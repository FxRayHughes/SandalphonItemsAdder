package ink.ptms.sandalphon.module.impl.blockmine

import dev.lone.itemsadder.api.CustomBlock
import dev.lone.itemsadder.api.CustomFurniture
import dev.lone.itemsadder.api.Events.CustomBlockBreakEvent
import ink.ptms.sandalphon.module.Helper
import ink.ptms.sandalphon.module.impl.blockmine.data.BlockState
import ink.ptms.sandalphon.module.impl.blockmine.data.BlockStructure
import ink.ptms.sandalphon.module.impl.blockmine.data.openEdit
import ink.ptms.sandalphon.util.Pair
import ink.ptms.sandalphon.util.Utils
import ink.ptms.sandalphon.util.getStringList
import ink.ptms.sandalphon.util.ifAir
import org.bukkit.Effect
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Directional
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.util.random
import taboolib.module.chat.uncolored
import taboolib.module.nms.ItemTagList
import taboolib.module.nms.MinecraftVersion
import taboolib.platform.util.hasLore
import taboolib.platform.util.hasName
import kotlin.math.max
import kotlin.math.min

/**
 * @author sky
 * @since 2020-06-01 21:43
 */
object BlockEvents : Helper {

    val catcher = HashMap<String, Pair<Location?, Location?>>()

    operator fun MutableCollection<String>.plusAssign(element: String) {
        this.add(element)
    }

    operator fun MutableCollection<String>.plusAssign(element: Array<String>) {
        this.addAll(element)
    }

    @SubscribeEvent(priority = EventPriority.LOW, ignoreCancelled = true)
    fun cbreak(e: CustomBlockBreakEvent) {
        val result = BlockMine.find(e.block.location)
        if (e.block.type == result?.blockStructure?.origin) {
            e.isCancelled = true
            // ??????????????????
            if (result.blockStructure.tool != null) {
                val item = e.player.inventory.itemInMainHand.ifAir()
                if (item == null || item.getStringList("lotus.blokmine").contains("null")) {
                    return
                }
                val blockmineTag = item.getStringList("lotus.blokmine")
                if (!blockmineTag.contains(result.blockStructure.tool)) {
                    return
                }
            }
            val event = ink.ptms.sandalphon.module.impl.blockmine.event.BlockBreakEvent(
                e.player,
                result.blockData,
                result.blockState,
                result.blockStructure,
                itemsAdderEvent = e
            )
            event.call()
            if (event.isCancelled) {
                return
            }
            e.block.type = result.blockStructure.replace
            e.block.world.players.filter { it != e.player }.forEach {
                it.playEffect(e.block.location, Effect.STEP_SOUND, result.blockStructure.origin)
            }
            result.blockState.update = true
            result.blockStructure.drop.filter { random(it.chance) }.forEach {
                val itemStack = Utils.getItem(e.player, it.item).ifAir() ?: return@forEach
                itemStack.amount = it.amount
                e.block.world.dropItem(e.block.location.add(0.5, 0.5, 0.5), itemStack).pickupDelay = 20
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW, ignoreCancelled = true)
    fun player(e: BlockBreakEvent) {
        if (CustomBlock.byAlreadyPlaced(e.block) != null) {
            return
        }
        val result = BlockMine.find(e.block.location)
        if (e.block.type == result?.blockStructure?.origin) {
            e.isCancelled = true
            // ??????????????????
            if (result.blockStructure.tool != null) {
                val item = e.player.inventory.itemInMainHand.ifAir()
                if (item == null || item.getStringList("lotus.blokmine").contains("null")) {
                    return
                }
                val blockmineTag = item.getStringList("lotus.blokmine")
                if (!blockmineTag.contains(result.blockStructure.tool)) {
                    return
                }
            }
            val event = ink.ptms.sandalphon.module.impl.blockmine.event.BlockBreakEvent(
                e.player,
                result.blockData,
                result.blockState,
                result.blockStructure,
                e
            )
            event.call()
            if (event.isCancelled) {
                return
            }
            e.block.type = result.blockStructure.replace
            e.block.world.players.filter { it != e.player }.forEach {
                it.playEffect(e.block.location, Effect.STEP_SOUND, result.blockStructure.origin)
            }
            result.blockState.update = true
            result.blockStructure.drop.filter { random(it.chance) }.forEach {
                val itemStack = Utils.getItem(e.player, it.item).ifAir() ?: return@forEach
                itemStack.amount = it.amount
                e.block.world.dropItem(e.block.location.add(0.5, 0.5, 0.5), itemStack).pickupDelay = 20
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun e(e: BlockBreakEvent) {
        if (e.player.isOp) {
            if (e.player.inventory.itemInMainHand.hasName("????????????") && e.player.inventory.itemInMainHand.hasLore("BlockMine")) {
                e.isCancelled = true
                val blockData = BlockMine.getBlock(e.player.inventory.itemInMainHand.itemMeta!!.lore!![1].uncolored())
                if (blockData == null) {
                    e.player.error("??????????????????.")
                    return
                }
                if (blockData.find(e.block.location) != null) {
                    e.player.error("????????????????????????.")
                    return
                }
                val blockState = if (e.player.isSneaking) {
                    BlockState(e.block.location.add(0.0, 1.0, 0.0), blockData.progress.size - 1, 0, false)
                } else {
                    BlockState(e.block.location, blockData.progress.size - 1, 0, false)
                }
                blockData.blocks.add(blockState)
                blockData.build(blockState)
                e.player.info("???????????????.")
                BlockMine.export()
            }
            if (e.player.inventory.itemInMainHand.hasName("????????????") && e.player.inventory.itemInMainHand.hasLore("BlockMine")) {
                e.isCancelled = true
                val blockData = BlockMine.getBlock(e.player.inventory.itemInMainHand.itemMeta!!.lore!![1].uncolored())
                if (blockData == null) {
                    e.player.error("??????????????????.")
                    return
                }
                val pair = blockData.find(e.block.location)
                if (pair == null) {
                    e.player.error("????????????????????????.")
                    return
                }
                blockData.build(pair.first)
                e.player.info("???????????????.")
            }
            if (e.player.inventory.itemInMainHand.hasName("????????????") && e.player.inventory.itemInMainHand.hasLore("BlockMine")) {
                e.isCancelled = true
                val args = e.player.inventory.itemInMainHand.itemMeta!!.lore!![1].uncolored().split(" ")
                val blockData = BlockMine.getBlock(args[0])
                if (blockData == null) {
                    e.player.error("??????????????????.")
                    return
                }
                val blockProgress = blockData.progress.getOrNull(args[1].toInt())
                if (blockProgress == null) {
                    e.player.error("??????????????????.")
                    return
                }
                val pair = catcher.computeIfAbsent(e.player.name) { Pair(null, null) }
                pair.key = e.block.location
                if (pair.value != null) {
                    build(e.player, pair.key!!, pair.value!!) { loc ->
                        e.player.spawnParticle(Particle.FLAME, loc.add(0.5, 0.5, 0.5), 5, 0.0, 0.0, 0.0, 0.0)
                    }
                }
                e.player.info("???????????????.")
                BlockMine.export()
            }
        }
    }

    @SubscribeEvent
    fun e(e: PlayerInteractEvent) {
        if (e.hand != EquipmentSlot.HAND) {
            return
        }
        if (e.player.isOp && e.action == Action.RIGHT_CLICK_BLOCK) {
            if (e.player.inventory.itemInMainHand.hasName("????????????") && e.player.inventory.itemInMainHand.hasLore("BlockMine")) {
                e.isCancelled = true
                val blockData = BlockMine.getBlock(e.player.inventory.itemInMainHand.itemMeta!!.lore!![1].uncolored())
                if (blockData == null) {
                    e.player.error("??????????????????.")
                    return
                }
                val mid = blockData.find(e.clickedBlock!!.location)
                if (mid == null) {
                    e.player.error("????????????????????????.")
                    return
                }
                blockData.clean(mid.first)
                blockData.blocks.remove(mid.first)
                e.player.info("???????????????.")
                BlockMine.export()
            }
            if (e.player.inventory.itemInMainHand.hasName("????????????") && e.player.inventory.itemInMainHand.hasLore("BlockMine")) {
                e.isCancelled = true
                val blockData = BlockMine.getBlock(e.player.inventory.itemInMainHand.itemMeta!!.lore!![1].uncolored())
                if (blockData == null) {
                    e.player.error("??????????????????.")
                    return
                }
                val pair = blockData.find(e.clickedBlock!!.location)
                if (pair == null) {
                    e.player.error("????????????????????????.")
                    return
                }
                blockData.clean(pair.first)
                pair.first.current =
                    if (pair.first.current + 1 == blockData.progress.size) 0 else pair.first.current + 1
                blockData.build(pair.first)
                e.player.info("?????????????????????.")
            }
            if (e.player.inventory.itemInMainHand.hasName("????????????") && e.player.inventory.itemInMainHand.hasLore("BlockMine")) {
                e.isCancelled = true
                val args = e.player.inventory.itemInMainHand.itemMeta!!.lore!![1].uncolored().split(" ")
                val blockData = BlockMine.getBlock(args[0])
                if (blockData == null) {
                    e.player.error("??????????????????.")
                    return
                }
                val blockProgress = blockData.progress.getOrNull(args[1].toInt())
                if (blockProgress == null) {
                    e.player.error("??????????????????.")
                    return
                }
                val pair = catcher.computeIfAbsent(e.player.name) { Pair(null, null) }
                pair.value = e.clickedBlock!!.location
                if (pair.key != null) {
                    build(e.player, pair.key!!, pair.value!!) { loc ->
                        e.player.spawnParticle(Particle.FLAME, loc.add(0.5, 0.5, 0.5), 5, 0.0, 0.0, 0.0, 0.0)
                    }
                }
                e.player.info("???????????????.")
                BlockMine.export()
            }
        }
    }

    @SubscribeEvent
    fun e(e: PlayerDropItemEvent) {
        if (e.player.isOp) {
            val item = e.itemDrop.itemStack
            if (item.hasName("????????????") && item.hasLore("BlockMine")) {
                e.isCancelled = true
                val args = item.itemMeta!!.lore!![1].uncolored().split(" ")
                val blockData = BlockMine.getBlock(args[0])
                if (blockData == null) {
                    e.player.error("??????????????????.")
                    return
                }
                val blockProgress = blockData.progress.getOrNull(args[1].toInt())
                if (blockProgress == null) {
                    e.player.error("??????????????????.")
                    return
                }
                val pair = catcher.remove(e.player.name) ?: Pair(null, null)
                if (pair.key == null || pair.value == null) {
                    e.player.error("?????????????????????.")
                    return
                }
                val mid = pair.key!!.toVector().midpoint(pair.value!!.toVector()).toLocation(e.player.world).run {
                    this.y = min(pair.key!!.y, pair.value!!.y)
                    this.block.location
                }
                blockProgress.structures.clear()
                build(e.player, pair.key!!, pair.value!!, true) { loc ->
                    e.player.spawnParticle(Particle.VILLAGER_HAPPY, loc.add(0.5, 0.5, 0.5), 5, 0.0, 0.0, 0.0, 0.0)
                    val block = loc.block
                    if (block.type == Material.AIR || block.type == Material.BEDROCK) {
                        return@build
                    }
                    val direction = getBlockFace(block)
                    val cblock = CustomBlock.byAlreadyPlaced(block)
                    val structure = if (cblock == null) {
                        BlockStructure(direction, block.type, Material.AIR, block.location.subtract(mid).toVector())
                    } else {
                        BlockStructure(
                            direction,
                            block.type,
                            Material.AIR,
                            block.location.subtract(mid).toVector(),
                            cblock.namespacedID
                        )
                    }
                    blockProgress.structures.add(structure)
                }
                e.player.info("???????????????.")
                BlockMine.export()
                BlockMine.loadBlockCache()
                blockData.openEdit(e.player)
            }
        }
    }

    fun build(player: Player, locA: Location, locB: Location, filled: Boolean = false, action: (Location) -> (Unit)) {
        val maxX = max(locA.x, locB.x)
        val minX = min(locA.x, locB.x)
        val maxY = max(locA.y, locB.y)
        val minY = min(locA.y, locB.y)
        val maxZ = max(locA.z, locB.z)
        val minZ = min(locA.z, locB.z)
        buildCube(
            Location(player.world, minX, minY, minZ),
            Location(player.world, maxX, maxY, maxZ),
            1.0,
            filled
        ) { loc ->
            action.invoke(loc)
        }
    }

    fun buildCube(start: Location, end: Location, rate: Double, filled: Boolean = false, action: (Location) -> (Unit)) {
        val maxX = max(start.x, end.x)
        val minX = min(start.x, end.x)
        val maxY = max(start.y, end.y)
        val minY = min(start.y, end.y)
        val maxZ = max(start.z, end.z)
        val minZ = min(start.z, end.z)
        var x = minX
        while (x <= maxX) {
            var y = minY
            while (y <= maxY) {
                var z = minZ
                while (z <= maxZ) {
                    if (filled || (y == minY || y + rate > maxY || x == minX || x + rate > maxX || z == minZ || z + rate > maxZ)) {
                        action.invoke(start.clone().add(x - minX, y - minY, z - minZ))
                    }
                    z += rate
                }
                y += rate
            }
            x += rate
        }
    }

    fun getBlockFace(block: Block): BlockFace {
        return if (MinecraftVersion.majorLegacy >= 11300) {
            if (block.blockData is Directional) {
                (block.blockData as Directional).facing
            } else {
                BlockFace.SELF
            }
        } else {
            getBlockFace(block.data.toInt())
        }
    }

    fun getBlockFace(data: Int): BlockFace {
        return when (data) {
            0 -> BlockFace.DOWN
            1 -> BlockFace.UP
            2 -> BlockFace.NORTH
            3 -> BlockFace.SOUTH
            4 -> BlockFace.WEST
            5 -> BlockFace.EAST
            else -> BlockFace.SELF
        }
    }
}