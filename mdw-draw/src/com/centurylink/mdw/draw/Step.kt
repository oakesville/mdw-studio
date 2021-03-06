package com.centurylink.mdw.draw

import com.centurylink.mdw.constant.WorkAttributeConstant
import com.centurylink.mdw.draw.model.DrawProps
import com.centurylink.mdw.draw.model.WorkflowObj
import com.centurylink.mdw.draw.model.WorkflowType
import com.centurylink.mdw.model.project.Project
import com.centurylink.mdw.model.workflow.Activity
import com.centurylink.mdw.model.workflow.ActivityImplementor
import com.centurylink.mdw.model.workflow.MilestoneFactory
import com.centurylink.mdw.model.workflow.Process
import java.awt.Color
import java.awt.Graphics2D

class Step(private val g2d: Graphics2D, val project: Project, process: Process, val activity: Activity,
        val implementor: ActivityImplementor, val drawProps: DrawProps) :
        Shape(g2d, Display(activity.getAttribute(WorkAttributeConstant.WORK_DISPLAY_INFO))), Drawable, Resizable {

    override val workflowObj = WorkflowObj(project, process, WorkflowType.activity, activity.json, drawProps)
    private val boxStyle: Boolean = true
    private val milestone = MilestoneFactory(process).getMilestone(activity)

    override fun draw(): Display {

        val extents = Display(0, 0, display.x + display.w, display.y + display.h)

        val yAdjust = -3
        var textColor = Display.DEFAULT_COLOR
        val fillColor = milestone?.let { milestone ->
            var color = Color(MilestoneFactory.OTHER_GROUP.properties.getProperty("color").substring(1).toInt(16))
            milestone.group?.let { groupName ->
                drawProps.milestoneGroups?.let { group ->
                    group.subgroups?.let { subgroups ->
                        subgroups.find { it.name == groupName }?.let { subgroup ->
                            subgroup.properties.getProperty("color")?.let {  milestoneColor ->
                                color = Color(milestoneColor.substring(1).toInt(16))
                            }
                        }
                    }
                }
            }
            Color(color.red, color.green, color.blue, 100)
        }

        if (implementor.imageIcon != null) {
            if (boxStyle) {
                drawRect(fill = fillColor)
            }
            val iconX = display.x + display.w / 2 - 12
            val iconY = display.y + 5
            extents.w = maxOf(extents.w, iconX + implementor.imageIcon.iconWidth)
            extents.h = maxOf(extents.h, iconY + implementor.imageIcon.iconHeight)
            drawIcon(implementor.imageIcon, iconX, iconY)
        }
        else if (implementor.icon != null && implementor.icon.startsWith("shape:")) {
            val shape = implementor.icon.substring(6)
            when(shape) {
                "start" -> {
                    if (display.w == 60 && display.h == 40) {
                        // use image for better quality via feathering
                        drawIcon(Display.START_ICON, display.x, display.y, 0.8f)
                    }
                    else {
                        drawOval(fill = Display.START_COLOR)
                    }
                    textColor = Display.SHAPE_TEXT_COLOR
                }
                "stop" -> {
                    if (display.w == 60 && display.h == 40) {
                        drawIcon(Display.STOP_ICON, display.x, display.y, 0.8f)
                    }
                    else {
                        drawOval(fill = Display.STOP_COLOR)
                    }
                    textColor = Display.SHAPE_TEXT_COLOR
                }
                "pause" -> {
                    if (display.w == 60 && display.h == 40) {
                        drawIcon(Display.PAUSE_ICON, display.x, display.y, 0.8f)
                    }
                    else {
                        drawOval(fill = Display.PAUSE_COLOR)
                    }
                    textColor = Display.SHAPE_TEXT_COLOR
                }
                "decision" -> {
                    drawDiamond()
                }
                else -> {
                    drawRect(fill = fillColor)
                }
            }
        }
        else {
            drawRect()
        }

        // label
        if (activity.name != null) {
            val lines = activity.name.lines()
            var w = 0
            var y = display.y + display.h / 2
            if (lines.size == 1) {
                // center the one and only line
                y += g2d.fontMetrics.height / 2
            }
            if (implementor.imageIcon != null) {
                y += implementor.imageIcon.iconHeight / 2
            }
            if (y < 0) {
                y = 0
            }

            for (line in lines) {
                val lw = g2d.fontMetrics.stringWidth(line)
                if (lw > w) {
                    w = lw
                }
                val x = display.x + display.w / 2 - lw / 2
                extents.w = maxOf(extents.w, x + w)
                extents.h = maxOf(extents.h, y + yAdjust + g2d.fontMetrics.height)
                drawText(line, x, y + yAdjust, color = textColor)
                y += g2d.fontMetrics.height - 1
            }
        }

        // logical id
        if (activity.id > 0) {
            extents.w = maxOf(extents.w, this.display.x + 2 + g2d.fontMetrics.stringWidth(activity.id.toString()))
            extents.h = maxOf(extents.h, this.display.y - 2 + g2d.fontMetrics.height)
            drawText(activity.logicalId, this.display.x + 2, this.display.y - 2, color = Display.META_COLOR)
        }

        return extents
    }

    override fun move(deltaX: Int, deltaY: Int, limits: Display?) {
        val d = Display(display.x + deltaX, display.y + deltaY, display.w, display.h)
        limits?.let {
            if (d.x < it.x) {
                d.x = it.x
            }
            else if (d.x > it.x + it.w - d.w) {
                d.x = it.x + it.w - d.w
            }
            if (d.y < it.y) {
                d.y = it.y
            }
            else if (d.y > it.y + it.h - d.h) {
                d.y = it.y + it.h - d.h
            }
        }
        activity.setAttribute(WorkAttributeConstant.WORK_DISPLAY_INFO, d.toString())
    }

    override fun resize(anchor: Int, x: Int, y: Int, deltaX: Int, deltaY: Int, limits: Display?) {
        display = resizeDisplay(anchor, x, y, deltaX, deltaY, MIN_SIZE, limits)
        activity.setAttribute(WorkAttributeConstant.WORK_DISPLAY_INFO, display.toString())
    }

    companion object {
        const val MIN_SIZE = 4
    }
}