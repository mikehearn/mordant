package com.github.ajalt.mordant.components

import com.github.ajalt.mordant.internal.formatMultipleWithSiSuffixes
import com.github.ajalt.mordant.internal.formatWithSiSuffix
import com.github.ajalt.mordant.rendering.Renderable
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.rendering.Whitespace
import com.github.ajalt.mordant.table.ColumnWidth
import kotlin.math.roundToInt


internal data class ProgressState(
    val completed: Long,
    val total: Long?,
    val completedPerSecond: Double, // 0 if [completed] == 0
    val elapsedSeconds: Double,
    val frameRate: Int,
) {
    init {
        require(completed >= 0) { "completed cannot be negative" }
        require(total == null || total >= 0) { "total cannot be negative" }
        require(elapsedSeconds >= 0) { "elapsedSeconds cannot be negative" }
        require(completedPerSecond >= 0) { "completedPerSecond cannot be negative" }
    }

    val indeterminate: Boolean get() = total == null
}

internal interface ProgressCell {
    val columnWidth: ColumnWidth

    fun ProgressState.makeRenderable(): Renderable
}


internal class TextProgressCell(private val text: Text) : ProgressCell {
    override val columnWidth: ColumnWidth get() = ColumnWidth.Auto
    override fun ProgressState.makeRenderable(): Renderable = text
}

internal class PercentageProgressCell(private val style: TextStyle) : ProgressCell {
    override val columnWidth: ColumnWidth get() = ColumnWidth.Fixed(4)
    override fun ProgressState.makeRenderable(): Renderable {
        val percent = when {
            total == null || total <= 0 -> 0
            else -> (100.0 * completed / total).toInt()
        }
        return Text("$percent%", style)
    }
}

internal class CompletedProgressCell(
    private val suffix: String,
    private val includeTotal: Boolean,
    private val style: TextStyle,
) : ProgressCell {
    override val columnWidth: ColumnWidth
        get() = ColumnWidth.Fixed((if (includeTotal) 12 else 6) + suffix.length)

    override fun ProgressState.makeRenderable(): Renderable {
        val complete = completed.toDouble()
        val (nums, unit) = formatMultipleWithSiSuffixes(1, complete, total?.toDouble() ?: 0.0)

        val t = nums[0] + when {
            includeTotal && total != null -> "/${nums[1]}$unit"
            includeTotal && total == null -> "/---.-"
            else -> ""
        } + suffix
        return Text(t, style, whitespace = Whitespace.PRE)
    }
}

// TODO: this isn't thread safe
internal abstract class ThrottledProgressCell(frameRate: Int?) : ProgressCell {
    private var lastFrameTime = 0.0
    private val frameDuration = frameRate?.let { 1.0 / it }
    private fun shouldSkipUpdate(elapsed: Double): Boolean {
        if (frameDuration == null) return false
        if ((elapsed - lastFrameTime) < frameDuration) return true
        lastFrameTime = elapsed
        return false
    }

    private var renderable: Renderable? = null
    final override fun ProgressState.makeRenderable(): Renderable {
        var r = renderable
        val shouldSkipUpdate = shouldSkipUpdate(elapsedSeconds)
        if (r != null && shouldSkipUpdate) return r
        r = makeFreshRenderable()
        renderable = r
        return r
    }

    protected abstract fun ProgressState.makeFreshRenderable(): Renderable
}

internal class SpeedProgressCell(
    private val suffix: String,
    frameRate: Int?,
    private val style: TextStyle,
) : ThrottledProgressCell(frameRate) {
    override val columnWidth: ColumnWidth get() = ColumnWidth.Fixed(6 + suffix.length)

    override fun ProgressState.makeFreshRenderable(): Renderable {
        val t = when {
            indeterminate || completedPerSecond <= 0 -> "---.-"
            else -> completedPerSecond.formatWithSiSuffix(1)
        }
        return Text(t + suffix, style, whitespace = Whitespace.PRE)
    }
}

internal class EtaProgressCell(
    private val prefix: String,
    frameRate: Int?,
    private val style: TextStyle,
) : ThrottledProgressCell(frameRate) {
    override val columnWidth: ColumnWidth get() = ColumnWidth.Fixed(7 + prefix.length)

    override fun ProgressState.makeFreshRenderable(): Renderable {
        val eta = if (total == null) 0.0 else (total - completed) / completedPerSecond
        val maxEta = 35_999 // 9:59:59
        if (indeterminate || eta < 0 || completedPerSecond == 0.0 || eta > maxEta) {
            return text("$prefix-:--:--")
        }

        val h = (eta / (60 * 60)).toInt()
        val m = (eta / 60 % 60).toInt()
        val s = (eta % 60).roundToInt()

        return text("$prefix$h:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}")
    }

    private fun text(s: String) = Text(s, style, whitespace = Whitespace.PRE)
}

// TODO arguments to ProgressBar constructor
internal class BarProgressCell(val width: Int?) : ProgressCell {
    override val columnWidth: ColumnWidth
        get() = width?.let { ColumnWidth.Fixed(it) } ?: ColumnWidth.Expand()

    override fun ProgressState.makeRenderable(): Renderable {
        val period = 2 // this could be configurable
        val pulsePosition = ((elapsedSeconds % period) / period)

        return ProgressBar(total ?: 100, completed, indeterminate, width, pulsePosition.toFloat())
    }
}
