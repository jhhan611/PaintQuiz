package com.github.jhhan611.paintquiz

import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

data class WandData(var pos1 : Location?, var pos2 : Location?)

var wandData = HashMap<Player, WandData>()

class WandListener : Listener {
    @EventHandler
    fun onClick(e: PlayerInteractEvent) {
        if (e.action == Action.LEFT_CLICK_BLOCK && e.item == ItemStack(Material.WOODEN_SHOVEL)) {
            e.isCancelled = true

            val blockPos = e.clickedBlock!!.location

            if(!wandData.containsKey(e.player)) wandData[e.player] = WandData(blockPos, null)
            else wandData[e.player]!!.pos1 = blockPos
            e.player.sendMessage(ChatColor.GOLD.toString() + "위치 #1: " + ChatColor.YELLOW.toString() + blockPos.simplify())
        }

        if (e.action == Action.RIGHT_CLICK_BLOCK && e.item == ItemStack(Material.WOODEN_SHOVEL)) {
            e.isCancelled = true

            val blockPos = e.clickedBlock!!.location

            if(!wandData.containsKey(e.player)) wandData[e.player] = WandData(null, blockPos)
            else wandData[e.player]!!.pos2 = blockPos
            e.player.sendMessage(ChatColor.GOLD.toString() + "위치 #2: " + ChatColor.YELLOW.toString() + blockPos.simplify())
        }
    }


}

fun Location?.simplify() : String {
    return "(${this?.x?.toInt()}, ${this?.y?.toInt()}, ${this?.z?.toInt()})"
}