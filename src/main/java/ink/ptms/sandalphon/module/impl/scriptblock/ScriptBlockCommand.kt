package ink.ptms.sandalphon.module.impl.scriptblock

import ink.ptms.sandalphon.Sandalphon
import ink.ptms.sandalphon.module.Helper
import ink.ptms.sandalphon.module.impl.scriptblock.data.BlockData
import ink.ptms.sandalphon.util.Utils
import io.izzel.taboolib.cronus.CronusUtils
import io.izzel.taboolib.module.command.base.*
import io.izzel.taboolib.module.db.local.Local
import io.izzel.taboolib.util.item.ItemBuilder
import org.bukkit.Bukkit
import org.bukkit.FluidCollisionMode
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.io.File

/**
 * @Author sky
 * @Since 2020-05-20 17:51
 */
@BaseCommand(name = "scriptblock", aliases = ["sb"], permission = "admin")
class ScriptBlockCommand : BaseMainCommand(), Helper {

    /**
     * /sb create        新建
     * /sb remove        删除
     * /sb edit          修改
     * /sb link          链接
     */

    @SubCommand(priority = 0.0, description = "新建脚本", type = CommandType.PLAYER)
    fun create(sender: CommandSender, args: Array<String>) {
        if (Bukkit.getPluginManager().getPlugin("Cronus") == null) {
            sender.error("该功能依赖 Cronus 插件.")
            return
        }
        val block = (sender as Player).getTargetBlockExact(10, FluidCollisionMode.NEVER)
        if (block == null || block.blockData.material == Material.AIR) {
            sender.error("无效的方块.")
            return
        }
        val blockData = ScriptBlock.getBlock(block)
        if (blockData != null) {
            block.display()
            sender.error("该方块已存在脚本.")
            return
        }
        block.display()
        sender.info("脚本方块已创建.")
        ScriptBlock.blocks.add(BlockData(block.location).run {
            this.openEdit(sender)
            this
        })
        ScriptBlock.export()
    }

    @SubCommand(priority = 0.1, description = "移除脚本", type = CommandType.PLAYER)
    fun remove(sender: CommandSender, args: Array<String>) {
        if (Bukkit.getPluginManager().getPlugin("Cronus") == null) {
            sender.error("该功能依赖 Cronus 插件.")
            return
        }
        val block = (sender as Player).getTargetBlockExact(10, FluidCollisionMode.NEVER)
        if (block == null || block.blockData.material == Material.AIR) {
            sender.error("无效的方块.")
            return
        }
        val blockData = ScriptBlock.getBlock(block)
        if (blockData == null) {
            block.display()
            sender.error("该方块不存在脚本.")
            return
        }
        block.display()
        sender.info("脚本方块已移除.")
        ScriptBlock.blocks.remove(blockData)
        ScriptBlock.delete(Utils.fromLocation(blockData.block))
        ScriptBlock.export()
    }

    @SubCommand(priority = 0.2, description = "编辑脚本", type = CommandType.PLAYER)
    fun edit(sender: CommandSender, args: Array<String>) {
        if (Bukkit.getPluginManager().getPlugin("Cronus") == null) {
            sender.error("该功能依赖 Cronus 插件.")
            return
        }
        val block = (sender as Player).getTargetBlockExact(10, FluidCollisionMode.NEVER)
        if (block == null || block.blockData.material == Material.AIR) {
            sender.error("无效的方块.")
            return
        }
        val blockData = ScriptBlock.getBlock(block)
        if (blockData == null) {
            block.display()
            sender.error("该方块不存在脚本.")
            return
        }
        blockData.openEdit(sender)
        sender.info("正在编辑脚本.")
    }

    @SubCommand(priority = 0.4, description = "链接脚本", type = CommandType.PLAYER)
    fun link(sender: CommandSender, args: Array<String>) {
        if (Bukkit.getPluginManager().getPlugin("Cronus") == null) {
            sender.error("该功能依赖 Cronus 插件.")
            return
        }
        val block = (sender as Player).getTargetBlockExact(10, FluidCollisionMode.NEVER)
        if (block == null || block.blockData.material == Material.AIR) {
            sender.error("无效的方块.")
            return
        }
        val blockData = ScriptBlock.getBlock(block)
        if (blockData == null) {
            block.display()
            sender.error("该方块不存在脚本.")
            return
        }
        block.display()
        sender.info("使用§f链接魔杖§7右键方块创建连接, 左键方块移除连接.")
        CronusUtils.addItem(sender, ItemBuilder(Material.BLAZE_ROD).name("§f§f§f链接魔杖").lore("§7${Utils.fromLocation(blockData.block)}").shiny().build())
    }

    @SubCommand(priority = 0.5, description = "重载脚本")
    fun import(sender: CommandSender, args: Array<String>) {
        ScriptBlock.data.load(File(Sandalphon.getPlugin().dataFolder, "module/scriptblock.yml"))
        ScriptBlock.import()
        sender.info("操作成功.")
    }
}