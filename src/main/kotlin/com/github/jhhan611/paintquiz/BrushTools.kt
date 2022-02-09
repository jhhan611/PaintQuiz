package com.github.jhhan611.paintquiz

import org.bukkit.Location

class BrushTools {
    private var currTool: ToolType? = null
    private var pos1: Location? = null

    fun check(tool: ToolType, pos: Location): Location? {
        if (currTool != tool) {
            pos1 = pos
            currTool = tool
            return null
        }
        currTool = null
        val tmp = pos1
        pos1 = null
        return tmp
    }
}

enum class ToolType {
    LINE, SQUARE
}