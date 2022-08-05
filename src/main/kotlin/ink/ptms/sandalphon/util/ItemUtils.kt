package ink.ptms.sandalphon.util

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import taboolib.module.nms.ItemTagData
import taboolib.module.nms.ItemTagList
import taboolib.module.nms.getItemTag
import taboolib.platform.util.isAir

fun ItemStack?.ifAir(): ItemStack? {
    if (this == null) {
        return null
    }
    if (this.isAir) {
        return null
    }
    if (this.type == Material.AIR) {
        return null
    }
    return this
}

fun ItemStack.getString(key: String, def: String = "null"): String {
    if (key.contains(".")) {
        return this.getItemTag().getDeepOrElse(key, ItemTagData(def)).asString()
    }
    return this.getItemTag().getOrElse(key, ItemTagData(def)).asString()
}

fun ItemStack.getStringList(key: String): List<String> {
    val def = ItemTagData(ItemTagList.of(ItemTagData("null")))
    if (key.contains(".")) {
        return this.getItemTag()
            .getDeepOrElse(key, def)
            .asList().map { it.asString() }
    }
    return this.getItemTag()
        .getOrElse(key, def)
        .asList().map { it.asString() }
}

fun ItemStack.getInt(key: String, def: Int = -1): Int {
    if (key.contains(".")) {
        return this.getItemTag().getDeepOrElse(key, ItemTagData(def)).asInt()
    }
    return this.getItemTag().getDeepOrElse(key, ItemTagData(def)).asInt()
}

fun ItemStack.getDouble(key: String, def: Double = -1.0): Double {
    if (key.contains(".")) {
        return this.getItemTag().getDeepOrElse(key, ItemTagData(def)).asDouble()
    }
    return this.getItemTag().getOrElse(key, ItemTagData(def)).asDouble()
}

fun ItemStack.set(key: String, value: Any) {
    val tag = getItemTag()

    if (key.contains(".")) {
        tag.putDeep(key, value)
    } else {
        tag.put(key, value)
    }
    tag.saveTo(this)
}