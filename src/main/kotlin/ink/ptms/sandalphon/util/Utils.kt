package ink.ptms.sandalphon.util

import com.google.common.base.Enums
import com.google.gson.*
import github.saukiya.sxitem.data.item.ItemManager
import ink.ptms.adyeshach.api.AdyeshachAPI
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.NumberConversions
import org.bukkit.util.Vector
import taboolib.common.platform.function.submit
import taboolib.library.xseries.parseToMaterial
import taboolib.module.chat.colored

object Utils {

    val serializer = GsonBuilder().excludeFieldsWithoutExposeAnnotation()
        .registerTypeAdapter(
            Vector::class.java,
            JsonSerializer<Vector> { a, _, _ -> JsonPrimitive("${a.x},${a.y},${a.z}") })
        .registerTypeAdapter(
            Vector::class.java,
            JsonDeserializer { a, _, _ ->
                a.asString.split(",").run { Vector(this[0].asDouble(), this[1].asDouble(), this[2].asDouble()) }
            })
        .registerTypeAdapter(Material::class.java, JsonSerializer<Material> { a, _, _ -> JsonPrimitive(a.name) })
        .registerTypeAdapter(Material::class.java, JsonDeserializer { a, _, _ -> a.asString.parseToMaterial() })
        .registerTypeAdapter(
            Location::class.java,
            JsonSerializer<Location> { a, _, _ -> JsonPrimitive(fromLocation(a)) })
        .registerTypeAdapter(Location::class.java, JsonDeserializer { a, _, _ -> toLocation(a.asString) })
        .registerTypeAdapter(BlockFace::class.java, JsonSerializer<BlockFace> { a, _, _ -> JsonPrimitive(a.name) })
        .registerTypeAdapter(
            BlockFace::class.java,
            JsonDeserializer { a, _, _ -> Enums.getIfPresent(BlockFace::class.java, a.asString).or(BlockFace.SELF) })
        .create()!!

    fun itemId(itemStack: ItemStack): String? {
        val itemStream = ItemManager().getGenerator(itemStack)
        if (itemStream != null) {
            return itemStream.key
        }
        return null
    }

    fun getItem(player: Player, id: String, vararg adder: String): ItemStack {
        return ItemManager().getItem(id, player, adder) ?: ItemStack(Material.AIR)
    }

    fun format(json: JsonElement): String {
        return GsonBuilder().setPrettyPrinting().create().toJson(json)
    }

    fun fromLocation(location: Location): String {
        return "${location.world?.name},${location.x},${location.y},${location.z}"
    }

    fun toLocation(source: String): Location {
        return source.split(",").run {
            Location(
                Bukkit.getWorld(get(0)),
                getOrElse(1) { "0" }.asDouble(),
                getOrElse(2) { "0" }.asDouble(),
                getOrElse(3) { "0" }.asDouble()
            )
        }
    }

    fun String.asDouble(): Double {
        return NumberConversions.toDouble(this)
    }

    fun buildHologram(info: List<String>, location: Location, stay: Long = 20L) {
        Bukkit.getOnlinePlayers().forEach { player ->
            val data = AdyeshachAPI.createHologram(player, location, info.colored())
            submit(delay = stay) {
                data.delete()
            }
        }
    }

    fun buildHologramNear(sender: Entity, info: List<String>, stay: Long = 20L) {
        sender.getNearbyEntities(30.0, 30.0, 30.0).mapNotNull { it as? Player }.forEach { player ->
            val data = AdyeshachAPI.createHologram(player, getRandom(sender.location), info.colored())
            submit(delay = stay) {
                data.delete()
            }
        }
    }

    private fun getRandom(location: Location): Location {
        var locations = getRandomLocation(location)
        while (locations.block.type != Material.AIR) {
            locations = getRandomLocation(location)
        }
        return locations
    }

    private fun getRandomLocation(location: Location): Location {
        val radius = 1.0
        val radians = Math.toRadians((0..360).random().toDouble())
        val x = kotlin.math.cos(radians) * radius
        val z = kotlin.math.sin(radians) * radius
        return location.add(x, 1.0, z)
    }
}