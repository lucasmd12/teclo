package com.teclo.keyboard.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.teclo.keyboard.ime.TecloIME
import kotlin.math.sin
import kotlin.math.cos

class TecloKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var isEnglish = false
    private var bgColor1 = Color.parseColor("#6C63FF")
    private var bgColor2 = Color.parseColor("#3ECFCF")
    private var keyColor = Color.parseColor("#1E1E2E")
    private var textColor = Color.WHITE
    private var parallaxX = 0f
    private var parallaxY = 0f
    private var animOffset = 0f

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bgGradientPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val ptRows = listOf(
        listOf("q","w","e","r","t","y","u","i","o","p"),
        listOf("a","s","d","f","g","h","j","k","l"),
        listOf("⇧","z","x","c","v","b","n","m","⌫"),
        listOf("PT/EN","  espaço  ",".")
    )

    private val enRows = listOf(
        listOf("q","w","e","r","t","y","u","i","o","p"),
        listOf("a","s","d","f","g","h","j","k","l"),
        listOf("⇧","z","x","c","v","b","n","m","⌫"),
        listOf("PT/EN","  space  ",".")
    )

    private var isCaps = false
    private val keys = mutableListOf<KeyData>()

    data class KeyData(val label: String, val rect: RectF)

    private val animRunnable = object : Runnable {
        override fun run() {
            animOffset += 0.02f
            invalidate()
            postDelayed(this, 16)
        }
    }

    init {
        post(animRunnable)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        buildKeys(w, h)
    }

    private fun buildKeys(w: Int, h: Int) {
        keys.clear()
        val rows = if (isEnglish) enRows else ptRows
        val rowH = h.toFloat() / rows.size
        val pad = 8f

        rows.forEachIndexed { rowIdx, row ->
            val keyW = w.toFloat() / row.size
            row.forEachIndexed { colIdx, label ->
                val left = colIdx * keyW + pad
                val top = rowIdx * rowH + pad
                val right = left + keyW - pad * 2
                val bottom = top + rowH - pad * 2
                keys.add(KeyData(label, RectF(left, top, right, bottom)))
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawParallaxBg(canvas)
        drawKeys(canvas)
    }

    private fun drawParallaxBg(canvas: Canvas) {
        val shader = LinearGradient(
            parallaxX + sin(animOffset) * 80,
            parallaxY + cos(animOffset * 0.7f) * 60,
            width.toFloat() + cos(animOffset) * 80,
            height.toFloat() + sin(animOffset * 0.5f) * 60,
            intArrayOf(bgColor1, bgColor2, bgColor1),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        bgGradientPaint.shader = shader
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgGradientPaint)

        // bolhas parallax 3D
        paint.color = Color.argb(40, 255, 255, 255)
        for (i in 0..5) {
            val cx = (width * (i * 0.18f + 0.05f)) + sin(animOffset + i) * 20f * (i * 0.3f + 0.5f)
            val cy = (height * 0.5f) + cos(animOffset * 0.6f + i) * 25f
            val r = 18f + i * 12f
            canvas.drawCircle(cx, cy, r, paint)
        }
    }

    private fun drawKeys(canvas: Canvas) {
        keys.forEach { key ->
            keyPaint.color = Color.argb(200, 30, 30, 46)
            keyPaint.setShadowLayer(6f, 2f, 4f, Color.argb(120, 0, 0, 0))
            canvas.drawRoundRect(key.rect, 14f, 14f, keyPaint)

            val label = if (isCaps) key.label.uppercase() else key.label
            textPaint.color = textColor
            textPaint.textSize = if (key.label.length > 2) 22f else 32f
            textPaint.textAlign = Paint.Align.CENTER
            val ty = key.rect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2
            canvas.drawText(label, key.rect.centerX(), ty, textPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
            parallaxX = (event.x - width / 2) * 0.04f
            parallaxY = (event.y - height / 2) * 0.04f
        }
        if (event.action == MotionEvent.ACTION_UP) {
            keys.forEach { key ->
                if (key.rect.contains(event.x, event.y)) {
                    handleKey(key.label)
                    return true
                }
            }
        }
        return true
    }

    private fun handleKey(label: String) {
        val ime = context as? TecloIME
        when (label) {
            "⌫" -> ime?.deleteChar()
            "⇧" -> { isCaps = !isCaps; invalidate() }
            "PT/EN" -> {
                isEnglish = !isEnglish
                buildKeys(width, height)
                invalidate()
            }
            "  espaço  ", "  space  " -> ime?.commitText(" ")
            else -> {
                val c = if (isCaps) label.uppercase() else label
                ime?.commitText(c)
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks(animRunnable)
    }
}
