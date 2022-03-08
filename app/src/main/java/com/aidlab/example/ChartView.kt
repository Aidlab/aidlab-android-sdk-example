package com.aidlab.example
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.annotation.Keep
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View


class ChartView : View {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val ecgDataSource: VitalsDataSource = VitalsDataSource(2000)
    private val respirationDataSource: VitalsDataSource = VitalsDataSource(2000)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    private val path = Path()

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (canvas == null) {
            return
        }

        canvas.drawColor(Color.BLACK)

        drawBackground(canvas)
        drawDataSource(canvas, Color.GREEN, ecgDataSource)
        drawDataSource(canvas, Color.BLUE, respirationDataSource)

    }

    private fun drawDataSource(canvas: Canvas, color: Int, dataSource: VitalsDataSource) {
        paint.color = color
        paint.strokeWidth = convertDpToPixel(2f)
        dataSource.samples()?.array()?.let {
            path.reset()
            for (i in it.indices) {
                val value = it[i]
                val x = (i / it.size.toFloat()) * width
                val y = value * height

                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            canvas.drawPath(path, paint)
        }
    }

    private fun drawBackground(canvas: Canvas) {
        // grid
        paint.color = Color.rgb(100, 100, 100)
        paint.strokeWidth = convertDpToPixel(1f)
        // vertical
        val verticals = 8
        for (i in 1 until verticals) {
            val x = (i / verticals.toFloat()) * width
            canvas.drawLine(x, 0f, x, height.toFloat(), paint)
        }
        val horizontals = 8
        for (i in 1 until horizontals) {
            val y = (i / horizontals.toFloat()) * height
            canvas.drawLine(0f, y, width.toFloat(), y, paint)
        }

        // bold lines
        paint.color = Color.rgb(200, 200, 200)
        paint.strokeWidth = convertDpToPixel(2f)
        // center line
        canvas.drawLine(width / 2f, 0f, width / 2f, height.toFloat(), paint)
        // upper line
        canvas.drawLine(0f, height * 0.25f, width.toFloat(), height * 0.25f, paint)
        // lower line line
        canvas.drawLine(0f, height * 0.75f, width.toFloat(), height * 0.75f, paint)
    }

    @Keep
    fun addECGSample(ecgSample: Float) {
        ecgDataSource.add(ecgSample)
    }

    @Keep
    fun addRespirationSample(respirationSample: Float) {
        respirationDataSource.add(respirationSample)
    }

    @Keep
    fun update() {
        ecgDataSource.normalize(0f, 0.5f)
        respirationDataSource.normalize(0.5f, 1f)
        invalidate()
    }

    @Keep
    fun clearDataSources() {
        ecgDataSource.clear()
        respirationDataSource.clear()
        invalidate()
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    fun convertDpToPixel(dp: Float): Float {
        return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    fun convertPixelsToDp(px: Float): Float {
        return px / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }
}
