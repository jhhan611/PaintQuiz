package com.github.jhhan611.paintquiz

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.BlockPosition
import com.comphenix.protocol.wrappers.WrappedBlockData
import com.comphenix.protocol.wrappers.nbt.NbtFactory
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class RadiusSign(plugin : JavaPlugin, private val pm : ProtocolManager) :
    PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Client.UPDATE_SIGN) {

    companion object {
        fun Player.generateSign(pm : ProtocolManager, loc : Location) {
            val pos = BlockPosition(loc.toVector())

            PacketContainer(PacketType.Play.Server.BLOCK_CHANGE).apply {
                blockPositionModifier.write(0, pos)
                blockData.write(0, WrappedBlockData.createData(Material.BIRCH_SIGN))
            }.also { pm.sendServerPacket(this, it) }

            PacketContainer(PacketType.Play.Server.TILE_ENTITY_DATA).apply {
                blockPositionModifier.write(0, pos)
                integers.write(0, 9)
                nbtModifier.write(
                    0, NbtFactory.ofCompound("").apply {
                        put("Text1", "{\"text\":\"\"}")
                        put("Text2", "{\"text\":\"^^^^^^^^^^^^^\"}")
                        put("Text3", "{\"text\":\"반지름 값을\"}")
                        put("Text4", "{\"text\":\"입력하세요\"}")
                        put("id", "minecraft:sign")
                        put("x", pos.x)
                        put("y", pos.y)
                        put("z", pos.z)
                    }
                )
            }.also { pm.sendServerPacket(this, it) }

            PacketContainer(PacketType.Play.Server.OPEN_SIGN_EDITOR).apply {
                blockPositionModifier.write(0, pos)
            }.also { pm.sendServerPacket(this, it) }

            this.world.getBlockAt(loc).blockData.let { Quiz.radiusSignData = it }
        }
    }

    override fun onPacketReceiving(event: PacketEvent?) {
        if(event == null) return
        if(event.packetType != PacketType.Play.Client.UPDATE_SIGN) return
        if(Quiz.radiusSignData == null) return

        val p = event.player

        val pos = event.packet.blockPositionModifier.values[0]
        val block = Quiz.radiusSignData
        Quiz.radiusSignData = null
        val value = event.packet.stringArrays.values[0][0]

        PacketContainer(PacketType.Play.Server.BLOCK_CHANGE).apply {
            blockPositionModifier.write(0, pos)
            blockData.write(0, WrappedBlockData.createData(block))
        }.also { pm.sendServerPacket(p, it) }

        if(!value.isNullOrEmpty()) {
            val valueDouble = value.toDoubleOrNull()
            if(valueDouble == null) {
                p.playSound(p.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f)
                p.sendMessage("${ChatColor.RED}유리수를 입력해주세요")
            } else {
                if(valueDouble < 0.0) {
                    p.playSound(p.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f)
                    p.sendMessage("${ChatColor.RED}0보다 큰 수를 입력해주세요")
                } else {
                    p.playSound(p.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f)
                    Canvas.brush.radius = valueDouble
                    p.sendMessage("${ChatColor.GREEN}브러시 반지름이 ${valueDouble}로 설정되었습니다")
                }
            }
        } else {
            p.playSound(p.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f)
            p.sendMessage("${ChatColor.RED}유리수를 입력해주세요")
        }
    }
}