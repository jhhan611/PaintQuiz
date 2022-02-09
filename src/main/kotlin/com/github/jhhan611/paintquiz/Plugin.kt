package com.github.jhhan611.paintquiz

import com.comphenix.protocol.ProtocolLibrary
import io.github.monun.kommand.StringType
import io.github.monun.kommand.getValue
import org.bukkit.plugin.java.JavaPlugin
import io.github.monun.kommand.kommand
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class Plugin : JavaPlugin() {
    override fun onEnable() {
        logger.info("PaintQuiz has been enabled")

        configLoad()
        Quiz.quizDuration = config.getInt("quiz-duration")
        Quiz.quizCooldown = config.getInt("quiz-cooldown")
        Canvas.brushRadius = config.getDouble("initial-radius")

        val pm = ProtocolLibrary.getProtocolManager()
        pm.addPacketListener(RadiusSign(this, pm))

        this.server.pluginManager.registerEvents(WandListener(), this)
        this.server.pluginManager.registerEvents(BrushListener(), this)
        this.server.pluginManager.registerEvents(ChatListener(), this)

        kommand {
            register("canvaswand") {
                requires { playerOrNull != null && player.isOp }
                executes {
                    player.inventory.addItem(ItemStack(Material.WOODEN_SHOVEL))
                }
            }
            register("setcanvas") {
                requires { playerOrNull != null && player.isOp }
                executes {
                    Canvas.setCanvas(sender)
                }
            }
            register("resetcanvas") {
                requires { playerOrNull != null && player.isOp }
                executes {
                    player.sendMessage(Canvas.resetCanvas())
                }
            }
            register("paintquiz") {
                requires { playerOrNull != null }
                executes {
                    player.sendMessage("${ChatColor.RED}답을 입력해주세요")
                }
                then("answer" to string(StringType.GREEDY_PHRASE)) {
                    executes {
                        val answer: String by it
                        Quiz.requestQuiz(player, answer)
                    }
                }
            }
        }
    }

    private fun configLoad() {
        config.addDefault("quiz-duration", 120)
        config.addDefault("quiz-cooldown", 20)
        config.addDefault("initial-radius", 1.0)
        config.options().copyDefaults(true)
        saveConfig()
    }
}