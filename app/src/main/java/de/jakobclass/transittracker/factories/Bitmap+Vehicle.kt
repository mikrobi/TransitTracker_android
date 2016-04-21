package de.jakobclass.transittracker.factories

import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import de.jakobclass.transittracker.models.Vehicle
import de.jakobclass.transittracker.utilities.neighbourWithDistance

fun Bitmap(vehicle: Vehicle, scale: Float): Bitmap {
    val radius = 15 * scale
    val directionIndicatorHeight = 5 * scale
    val imageWidth = radius * 2 + directionIndicatorHeight * 2
    val center = imageWidth.toFloat() / 2
    val fillColor = vehicle.type.color
    var fillColorHSV = floatArrayOf(0.0f, 0.0f, 0.0f)
    Color.colorToHSV(fillColor, fillColorHSV)
    fillColorHSV[2] *= 0.7f
    val strokeColor = Color.HSVToColor(fillColorHSV)

    val bitmap = android.graphics.Bitmap.createBitmap(imageWidth.toInt(), imageWidth.toInt(), android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    vehicle.position.direction?.let {
        val centerPoint = Point(center.toInt(), center.toInt())
        val distance = radius + directionIndicatorHeight
        val pi = Math.PI
        val directionMaxValue = 34
        val directionFactor = (2 * pi) / directionMaxValue
        val directionOffset = pi / 2
        val angle = directionFactor * it + directionOffset
        val arrowAngle = pi / 2
        val arrowTopPoint = centerPoint.neighbourWithDistance(distance, angle)
        val arrowBasePoint1 = centerPoint.neighbourWithDistance(radius, angle - (arrowAngle / 2))
        val arrowBasePoint2 = centerPoint.neighbourWithDistance(radius, angle + (arrowAngle / 2))
        val style = Paint()
        style.style = Paint.Style.FILL
        style.color = strokeColor

        val arrowPath = Path()
        arrowPath.moveTo(arrowBasePoint1.x.toFloat(), arrowBasePoint1.y.toFloat())
        arrowPath.lineTo(arrowTopPoint.x.toFloat(), arrowTopPoint.y.toFloat())
        arrowPath.lineTo(arrowBasePoint2.x.toFloat(), arrowBasePoint2.y.toFloat())
        arrowPath.close()

        canvas.drawPath(arrowPath, style)
    }

    var style = Paint()
    style.style = Paint.Style.FILL
    style.color = fillColor
    canvas.drawCircle(center, center, radius.toFloat(), style)
    style = Paint()
    style.style = Paint.Style.STROKE
    style.color = strokeColor
    style.strokeWidth = scale
    canvas.drawCircle(center, center, radius.toFloat(), style)

    val textStyle = TextPaint()
    textStyle.color = Color.WHITE
    textStyle.textSize = 10 * scale
    //textStyle.textAlign = Paint.Align.CENTER
    val textWidth = (radius * 2)
    val layout = StaticLayout(vehicle.name, textStyle, textWidth.toInt(), Layout.Alignment.ALIGN_CENTER, 1.2f, 1.0f, false)
    canvas.translate((imageWidth - layout.width) / 2, (imageWidth - layout.height) / 2)
    layout.draw(canvas)

    return bitmap
}
