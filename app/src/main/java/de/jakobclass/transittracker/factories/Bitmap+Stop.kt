package de.jakobclass.transittracker.factories

import android.graphics.*
import de.jakobclass.transittracker.models.Stop

fun Bitmap(stop: Stop, scale: Float): Bitmap {
    val typeRectHeight = (12 * scale).toInt()
    val typeRectHeightPadding = (3 * scale).toInt()
    val containerWidth = stop.vehicleTypes.size * (typeRectHeight + typeRectHeightPadding) + typeRectHeightPadding
    val containerHeight = typeRectHeight + 2 * typeRectHeightPadding
    val pinHeight = (15 * scale).toInt()
    val height = containerHeight + pinHeight

    val bitmap = android.graphics.Bitmap.createBitmap(containerWidth, containerHeight + pinHeight, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val container = Rect(0, 0, containerWidth, containerHeight)
    val cornerRadius = 4.0f * scale
    var style = Paint()
    style.style = Paint.Style.FILL
    style.color = Color.WHITE
    style.alpha = 191
    canvas.drawRoundRect(RectF(container), cornerRadius, cornerRadius, style)
    style = Paint()
    style.style = Paint.Style.STROKE
    style.color = Color.LTGRAY
    canvas.drawRoundRect(RectF(container), cornerRadius, cornerRadius, style)

    style = Paint()
    style.strokeWidth = 1.0f * scale
    style.color = Color.DKGRAY
    canvas.drawLine(containerWidth.toFloat() / 2,
            containerHeight.toFloat(),
            containerWidth.toFloat() / 2,
            height.toFloat(),
            style)

    style = Paint()
    style.textSize = 12.0f * scale
    style.typeface = Typeface.DEFAULT_BOLD
    for ((index, vehicleType) in stop.vehicleTypes.withIndex()) {
        val x = index * (typeRectHeight + typeRectHeightPadding) + typeRectHeightPadding
        val typeRect = Rect(x, typeRectHeightPadding, x + typeRectHeight, typeRectHeightPadding + typeRectHeight)
        style.color = vehicleType.color
        canvas.drawRect(typeRect, style)

        style.color = Color.WHITE
        val text = vehicleType.abbreviation
        val textWidth = style.measureText(text)
        val textBounds = Rect()
        style.getTextBounds(text, 0, text.length, textBounds)
        val textX = typeRect.left + (typeRect.width() - textWidth).toFloat() / 2
        val textY = typeRect.bottom - (typeRect.height() - textBounds.height()).toFloat() / 2
        canvas.drawText(text, textX, textY, style)
    }

    return bitmap
}
