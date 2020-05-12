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

class ProgressiveRectCircleView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float)-> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class PRCNode(var i : Int, val state : State = State()) {

        private var next : PRCNode? = null
        private var prev : PRCNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = PRCNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawPRCNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : PRCNode {
            var curr : PRCNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class ProgressiveRectCircle(var i : Int) {

        private val root : PRCNode = PRCNode(0)
        private var curr : PRCNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : ProgressiveRectCircleView) {

        private val animator : Animator = Animator(view)
        private val prc : ProgressiveRectCircle = ProgressiveRectCircle(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            prc.draw(canvas, paint)
            animator.animate {
                prc.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            prc.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : ProgressiveRectCircleView {
            val view : ProgressiveRectCircleView = ProgressiveRectCircleView(activity)
            activity.setContentView(view)
            return view
        }
    }
}