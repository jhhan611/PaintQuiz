package com.github.jhhan611.paintquiz

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import java.util.*

object Quiz {
    var drawer : Player? = null
    var answer : String? = null
    private var timer : Int = 0
    private var cooldown : Int = 0
    private var timerSchedulerID : Int? = null
    private var cooldownSchedulerID : Int? = null
    var radiusSignData: BlockData? = null

    var quizCooldown: Int = 20
    var quizDuration: Int = 120

    private fun startQuiz(player: Player, ans: String) {
        drawer = player
        answer = ans
        timer = quizDuration
        Canvas.resetCanvas()

        player.sendMessage("${ChatColor.GOLD}---------------------")
        player.sendMessage("${ChatColor.YELLOW}퀴즈가 시작되었습니다!")
        player.sendMessage("${ChatColor.YELLOW}답: ${ChatColor.GREEN}$answer")
        player.sendMessage("${ChatColor.GRAY}색칠할 블록을 왼손에 올려놓으세요")
        player.sendMessage("${ChatColor.GOLD}---------------------")
        player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f)

        val onlinePlayers = Bukkit.getOnlinePlayers()
        for (p in onlinePlayers) {
            if(p == player) continue
            p.sendMessage("${ChatColor.GOLD}---------------------")
            p.sendMessage("${ChatColor.YELLOW}퀴즈가 시작되었습니다!")
            p.sendMessage("${ChatColor.YELLOW}출제자: ${ChatColor.GREEN}${player.name}")
            p.sendMessage("${ChatColor.GOLD}---------------------")
            p.playSound(p.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f)
        }

        player.inventory.setQuizInv()

        val plugin = Bukkit.getPluginManager().getPlugin("PaintQuiz")
        timerSchedulerID = plugin!!.server.scheduler.scheduleSyncRepeatingTask(plugin, {
            timer--

            val players = Bukkit.getOnlinePlayers()
            for (p in players) {
                p.sendActionBar(Component.text("${ChatColor.GOLD}남은 시간: ${ChatColor.YELLOW}${timer}초"))
            }

            if(timer <= 0) {
                for (p in players) {
                    p.sendMessage("${ChatColor.GOLD}---------------------")
                    p.sendMessage("${ChatColor.RED}아무도 답을 맞추지 못했습니다!")
                    player.sendMessage("${ChatColor.YELLOW}답: ${ChatColor.GREEN}$answer")
                    p.sendMessage("${ChatColor.GOLD}---------------------")
                    p.playSound(p.location, Sound.ENTITY_VILLAGER_NO, 1f, 1f)
                }
                plugin.server.scheduler.cancelTask(timerSchedulerID!!)
                endQuiz()
            }
        }, 0, 20)
    }

    fun endQuiz() {
        drawer!!.inventory.clear()

        answer = null
        timer = 0
        drawer = null
        cooldown = quizCooldown

        val plugin = Bukkit.getPluginManager().getPlugin("PaintQuiz")
        plugin!!.server.scheduler.cancelTask(timerSchedulerID!!)
        timerSchedulerID = null

        cooldownSchedulerID = plugin.server.scheduler.scheduleSyncRepeatingTask(plugin, {
            cooldown--

            val players = Bukkit.getOnlinePlayers()
            for (p in players) {
                p.sendActionBar(Component.text("${ChatColor.GRAY}쿨타임: ${ChatColor.YELLOW}${cooldown}초"))
            }

            if(cooldown <= 0) {
                plugin.server.scheduler.cancelTask(cooldownSchedulerID!!)
            }
        }, 0, 20)
    }

    private fun PlayerInventory.setQuizInv() {
        this.clear()

        val initBlock = ItemStack(Material.BLACK_CONCRETE).apply {
            itemMeta = itemMeta.apply {
                displayName(Component.text("${ChatColor.YELLOW}블록 선택"))
                lore(listOf(Component.text("${ChatColor.GRAY}이곳에 색칠할 블록을 올려놓으세요")))
            }
        }

        val dotBrush = ItemStack(Material.STONE_SHOVEL).apply {
            itemMeta = itemMeta.apply {
                displayName(Component.text("${ChatColor.YELLOW}일반 브러시"))
                lore(listOf(Component.text("${ChatColor.GRAY}선택한 지점에 원을 그립니다")))
            }
        }

        val lineBrush = ItemStack(Material.STICK).apply {
            itemMeta = itemMeta.apply {
                displayName(Component.text("${ChatColor.YELLOW}선 브러시"))
                lore(listOf(Component.text("${ChatColor.GRAY}두 지점을 선택해 선을 그립니다")))
            }
        }

        val squareBrush = ItemStack(Material.PAPER).apply {
            itemMeta = itemMeta.apply {
                displayName(Component.text("${ChatColor.YELLOW}사각형 브러시"))
                lore(listOf(Component.text("${ChatColor.GRAY}두 지점을 선택해 사각형 윤곽을 그립니다")))
            }
        }

        val fillBrush = ItemStack(Material.BUCKET).apply {
            itemMeta = itemMeta.apply {
                displayName(Component.text("${ChatColor.YELLOW}채우기"))
                lore(listOf(Component.text("${ChatColor.GRAY}선택한 지점과 같은 블록을 채웁니다")))
            }
        }

        val radiusBook = ItemStack(Material.BOOK).apply {
            itemMeta = itemMeta.apply {
                displayName(Component.text("${ChatColor.YELLOW}브러시 반지름"))
                lore(listOf(Component.text("${ChatColor.GRAY}클릭하여 반지름을 설정하세요")))
            }
        }

        this.setItem(0, dotBrush)
        this.setItem(1, lineBrush)
        this.setItem(2, squareBrush)
        this.setItem(3, fillBrush)
        this.setItem(8, radiusBook)
        this.setItem(EquipmentSlot.OFF_HAND, initBlock)
    }

    fun requestQuiz(player: Player, answer: String) {
        if(cooldown > 0) {
            player.sendMessage("${ChatColor.RED}아직 퀴즈 출제를 할 수 없습니다. (${cooldown}초)")
            player.playSound(player.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f)
            return
        }
        if(drawer != null) {
            player.sendMessage("${ChatColor.RED}이미 누군가가 출제중입니다")
            player.playSound(player.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f)
            return
        }
        if(Canvas.hasNull()) {
            player.sendMessage("${ChatColor.RED}캔버스가 설정되지 않았습니다")
            player.playSound(player.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f)
            return
        }
        startQuiz(player, answer)
    }
}

class ChatListener: Listener {
    @EventHandler
    fun onChat(e: AsyncChatEvent) {
        Quiz.drawer ?: return
        if(e.player == Quiz.drawer) return

        if (e.originalMessage().toString().lowercase(Locale.getDefault()) == Component.text(Quiz.answer!!).toString()
                .lowercase(Locale.getDefault())) {
            val onlinePlayers = Bukkit.getOnlinePlayers()

            for (p in onlinePlayers) {
                p.sendMessage("${ChatColor.GOLD}---------------------")
                p.sendMessage("${ChatColor.YELLOW}${e.player.name}${ChatColor.GREEN}님이 답을 맞추셨습니다!")
                p.sendMessage("${ChatColor.GREEN}답: ${ChatColor.YELLOW}${Quiz.answer}")
                p.sendMessage("${ChatColor.GOLD}---------------------")
                p.playSound(p.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
            }

            Quiz.endQuiz()
        }
    }
}