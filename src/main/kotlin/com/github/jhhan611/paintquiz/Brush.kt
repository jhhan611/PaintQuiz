package com.github.jhhan611.paintquiz

import com.comphenix.protocol.ProtocolLibrary
import com.github.jhhan611.paintquiz.RadiusSign.Companion.generateSign
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.lang.Integer.max
import java.lang.Integer.min
import java.util.*

data class Brush(var radius: Double, var block: Material)

var selectedTool = HashMap<Player, BrushTools>()

class BrushListener : Listener {
    @EventHandler
    fun onClick(e: PlayerInteractEvent) {
        if (e.player != Quiz.drawer) return
        if (e.action != Action.RIGHT_CLICK_BLOCK && e.action != Action.RIGHT_CLICK_AIR) return

        val offHandMaterial = e.player.inventory.itemInOffHand.type
        if (offHandMaterial.isOccluding) Canvas.brush.block = offHandMaterial
        else {
            Canvas.brush.block = Material.AIR
            e.player.inventory.setItem(EquipmentSlot.OFF_HAND, ItemStack(Material.AIR))
            e.player.sendMessage("${ChatColor.RED}유효한 블럭이 아닙니다")
            e.player.playSound(e.player.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f)
            return
        }

        if (Canvas.brush.block == Material.AIR) return

        val eyeLocation = e.player.eyeLocation
        val direction = eyeLocation.direction
        val raytrace =
            e.player.world.rayTraceBlocks(eyeLocation, direction, 100.0, FluidCollisionMode.SOURCE_ONLY, false)
                ?: return

        val hitLocation = raytrace.hitBlock!!.location

        if (e.item == null) return
        if (selectedTool[e.player] == null) selectedTool[e.player] = BrushTools()
        val tool = selectedTool[e.player]
        when (e.item!!.type) {
            Material.STONE_SHOVEL -> drawDot(hitLocation)
            Material.STICK -> drawLine(tool!!.check(ToolType.LINE, hitLocation) ?: return, hitLocation)
            Material.PAPER -> drawSquare(tool!!.check(ToolType.SQUARE, hitLocation) ?: return, hitLocation)
            Material.BUCKET -> fill(hitLocation)
            Material.BOOK -> setRadius()
            else -> return
        }

    }

    private fun drawLine(pos1: Location, pos2: Location) {
        val distance: Double = pos1.distance(pos2)
        val p1: Vector = pos1.toVector()
        val p2: Vector = pos2.toVector()
        val vector: Vector = p2.clone().subtract(p1).normalize()
        var length = 0.0
        while (length < distance) {
            drawDot(p1.toLocation(pos1.world))
            length += 1
            p1.add(vector)
        }
    }

    private fun drawSquare(pos1: Location, pos2: Location) {
        val minX = min(pos1.x.toInt(), pos2.x.toInt())
        val maxX = max(pos1.x.toInt(), pos2.x.toInt())
        val minY = min(pos1.y.toInt(), pos2.y.toInt())
        val maxY = max(pos1.y.toInt(), pos2.y.toInt())
        val minZ = min(pos1.z.toInt(), pos2.z.toInt())
        val maxZ = max(pos1.z.toInt(), pos2.z.toInt())
        val r = Canvas.brush.radius.toInt()

        for (x: Int in minX-r..maxX+r) {
            for (y: Int in minY-r..maxY+r) {
                for (z: Int in minZ-r..maxZ+r) {
                    if (Canvas.isOutOfBounds(Location(pos1.world, x.toDouble(), y.toDouble(), z.toDouble()))) continue

                    var countMinMax = 0
                    if (kotlin.math.abs(x - minX) <= r || kotlin.math.abs(x - maxX) <= r) countMinMax++
                    if (kotlin.math.abs(y - minY) <= r || kotlin.math.abs(y - maxY) <= r) countMinMax++
                    if (kotlin.math.abs(z - minZ) <= r || kotlin.math.abs(z - maxZ) <= r) countMinMax++

                    if (countMinMax < 2) continue
                    pos1.world.setType(x, y, z, Canvas.brush.block)
                }
            }
        }
    }

    private fun fill(initLoc: Location) {
        val bfs: Queue<Location> = LinkedList()
        val visited = HashSet<Location>()

        val checkBlock = initLoc.world.getBlockAt(initLoc).type

        bfs.add(initLoc)

        while (bfs.isNotEmpty()) {
            val curr = bfs.remove()

            if (visited.contains(curr)) continue
            visited.add(curr)

            if (curr.block.type != checkBlock) continue
            if (Canvas.isOutOfBounds(curr)) continue

            initLoc.world.setType(curr, Canvas.brush.block)

            bfs.add(Location(curr.world, curr.x + 1, curr.y, curr.z))
            bfs.add(Location(curr.world, curr.x, curr.y + 1, curr.z))
            bfs.add(Location(curr.world, curr.x, curr.y, curr.z + 1))
            bfs.add(Location(curr.world, curr.x - 1, curr.y, curr.z))
            bfs.add(Location(curr.world, curr.x, curr.y - 1, curr.z))
            bfs.add(Location(curr.world, curr.x, curr.y, curr.z - 1))
        }
    }

    private fun drawDot(initLoc: Location) {
        val bfs: Queue<Location> = LinkedList()
        val visited = HashSet<Location>()

        bfs.add(initLoc)

        while (bfs.isNotEmpty()) {
            val curr = bfs.remove()

            if (visited.contains(curr)) continue
            visited.add(curr)

            if (curr.block.type.isAir) continue
            if (Canvas.isOutOfBounds(curr)) continue
            if (curr.distance(initLoc) > Canvas.brush.radius) continue

            initLoc.world.setType(curr, Canvas.brush.block)

            bfs.add(Location(curr.world, curr.x + 1, curr.y, curr.z))
            bfs.add(Location(curr.world, curr.x, curr.y + 1, curr.z))
            bfs.add(Location(curr.world, curr.x, curr.y, curr.z + 1))
            bfs.add(Location(curr.world, curr.x - 1, curr.y, curr.z))
            bfs.add(Location(curr.world, curr.x, curr.y - 1, curr.z))
            bfs.add(Location(curr.world, curr.x, curr.y, curr.z - 1))
        }
    }

    private fun setRadius() {
        val pm = ProtocolLibrary.getProtocolManager()
        Quiz.drawer!!.generateSign(pm, Quiz.drawer!!.location)
    }
}