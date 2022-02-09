package com.github.jhhan611.paintquiz

import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.lang.Integer.min
import java.lang.Integer.max

object Canvas {
    private var pos1 : Location? = null
    private var pos2 : Location? = null
    var brushRadius: Double = 1.0
    var brush : Brush = Brush(brushRadius, Material.BLACK_CONCRETE)

    fun hasNull() : Boolean {
        return pos1 == null || pos2 == null
    }

    private fun isDifferentWorld() : Boolean {
        if(hasNull()) return true
        return pos1!!.world != pos2!!.world
    }

    fun resetCanvas() : String {
        if(hasNull()) return "${ChatColor.RED}캔버스를 선정해주세요"
        if(isDifferentWorld()) return "${ChatColor.RED}캔버스의 두 위치의 차원이 일치하지 않습니다"

        val world = pos1!!.world

        for(x : Int in min(pos1!!.x.toInt(), pos2!!.x.toInt())..max(pos1!!.x.toInt(), pos2!!.x.toInt())) {
            for(y : Int in min(pos1!!.y.toInt(), pos2!!.y.toInt())..max(pos1!!.y.toInt(), pos2!!.y.toInt())) {
                for(z : Int in min(pos1!!.z.toInt(), pos2!!.z.toInt())..max(pos1!!.z.toInt(), pos2!!.z.toInt())) {
                    world.setType(x, y, z, Material.WHITE_CONCRETE)
                }
            }
        }

        brush = Brush(brushRadius, Material.BLACK_CONCRETE)

        return "${ChatColor.GREEN}캔버스가 초기화되었습니다"
    }

    fun isOutOfBounds(loc : Location) : Boolean {
        if(loc.x < min(pos1!!.x.toInt(), pos2!!.x.toInt()) || loc.x > max(pos1!!.x.toInt(), pos2!!.x.toInt())) return true
        if(loc.y < min(pos1!!.y.toInt(), pos2!!.y.toInt()) || loc.y > max(pos1!!.y.toInt(), pos2!!.y.toInt())) return true
        if(loc.z < min(pos1!!.z.toInt(), pos2!!.z.toInt()) || loc.z > max(pos1!!.z.toInt(), pos2!!.z.toInt())) return true
        return false
    }

    fun setCanvas(sender: CommandSender) {
        (sender as? Player)?.let {
            if (!wandData.containsKey(it) || wandData[it]!!.pos1 == null || wandData[it]!!.pos2 == null) {
                it.sendMessage("${ChatColor.RED}위치를 먼저 선정해주세요")
                return
            }

            pos1 = wandData[it]!!.pos1
            pos2 = wandData[it]!!.pos2

            it.sendMessage("${ChatColor.GREEN}캔버스가 설정되었습니다: ${pos1.simplify()}, ${pos2.simplify()}")
        }
    }
}

