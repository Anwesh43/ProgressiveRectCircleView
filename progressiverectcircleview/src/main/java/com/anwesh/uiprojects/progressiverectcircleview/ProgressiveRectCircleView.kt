package com.anwesh.uiprojects.progressiverectcircleview

/**
 * Created by anweshmishra on 12/05/20.
 */

import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Canvas
import android.graphics.RectF
import android.app.Activity
import android.content.Context

val nodes : Int = 5
val parts : Int = 3
val arcs : Int = 2
val scGap : Float = 0.02f
val strokeFactor : Int = 90
val sizeFactor : Float = 2.9f
val foreColor : Int = Color.parseColor("#4CAF50")
val backColor : Int = Color.parseColor("#BDBDBD")
val delay : Long = 20
val arcSizeFactor : Float = 3.2f

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawProgressiveArc(i : Int, scale : Float, size : Float, paint : Paint) {
    val r : Float = size / arcSizeFactor
    val sf : Float = scale.sinify()
    val sf1 : Float = sf.divideScale(0, parts)
    val sfi : Float = sf1.divideScale(i, arcs)
    val sj : Float = 1f - 2 * i
    paint.style = Paint.Style.STROKE
    save()
    translate(-(size - r) * sj, 0f)
    drawArc(RectF(-r, -r, r, r), -90f, 180f, false, paint)
    restore()
}

fun Canvas.drawSweepArc(scale : Float, size : Float, paint : Paint) {
    val r : Float = size / arcSizeFactor
    val sf : Float = scale.sinify()
    val sf2 : Float = sf.divideScale(2, parts)
    paint.style = Paint.Style.FILL
    drawArc(RectF(-r, -r, r, r), 0f, 360f * sf2, true, paint)
}

fun Canvas.drawProgressiveRectCircle(scale : Float, size : Float, paint : Paint) {
    paint.style = Paint.Style.STROKE
    drawRect(RectF(-size, -size, size, size), paint)
    for (j in 0..(arcs - 1)) {
        drawProgressiveArc(j, scale, size, paint)
    }
    drawSweepArc(scale, size, paint)
}

fun Canvas.drawPRCNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = h / (nodes + 1)
    val size : Float = gap / sizeFactor
    paint.color = foreColor
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    save()
    drawRect(-size, -size, size, size, paint)
    drawProgressiveRectCircle(scale, size, paint)
    restore()
}
