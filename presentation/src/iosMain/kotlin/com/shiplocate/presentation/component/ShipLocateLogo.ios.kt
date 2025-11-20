package com.shiplocate.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter

/**
 * iOS реализация - рисует иконку через Canvas (так как векторные drawable не поддерживаются)
 */
@Composable
actual fun rememberShipLocateLogoPainter(): Painter {
    return object : Painter() {
        override val intrinsicSize: androidx.compose.ui.geometry.Size
            get() = androidx.compose.ui.geometry.Size(240f, 186f)

        override fun DrawScope.onDraw() {
                val svgWidth = 240f
                val svgHeight = 186f
                val scaleX = size.width / svgWidth
                val scaleY = size.height / svgHeight

                // Внешний круг
                val centerX = size.width / 2
                val iconCenterY = (70f / svgHeight) * size.height
                val circleRadius = (64f / svgWidth) * size.width
                val strokeWidth = (4f / svgWidth) * size.width

                drawCircle(
                    color = Color.Black,
                    radius = circleRadius,
                    center = androidx.compose.ui.geometry.Offset(centerX, iconCenterY),
                    style = Stroke(width = strokeWidth),
                )

                // Фигура "9" (пин)
                val topCircleCenterX = 120f * scaleX
                val topCircleCenterY = 38f * scaleY
                val topCircleRadius = 32f * scaleX

                // Верхний круг
                drawArc(
                    color = Color.Black,
                    startAngle = -90f,
                    sweepAngle = 359.5f,
                    useCenter = true,
                    topLeft = androidx.compose.ui.geometry.Offset(
                        topCircleCenterX - topCircleRadius,
                        topCircleCenterY - topCircleRadius,
                    ),
                    size = androidx.compose.ui.geometry.Size(
                        topCircleRadius * 2,
                        topCircleRadius * 2,
                    ),
                )

                // Нижний круг
                val bottomCircleCenterX = 120f * scaleX
                val bottomCircleCenterY = 66f * scaleY
                val bottomCircleRadius = 14f * scaleX

                drawArc(
                    color = Color.Black,
                    startAngle = -90f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = androidx.compose.ui.geometry.Offset(
                        bottomCircleCenterX - bottomCircleRadius,
                        bottomCircleCenterY - bottomCircleRadius,
                    ),
                    size = androidx.compose.ui.geometry.Size(
                        bottomCircleRadius * 2,
                        bottomCircleRadius * 2,
                    ),
                )
                drawArc(
                    color = Color.Black,
                    startAngle = 90f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = androidx.compose.ui.geometry.Offset(
                        bottomCircleCenterX - bottomCircleRadius,
                        bottomCircleCenterY - bottomCircleRadius,
                    ),
                    size = androidx.compose.ui.geometry.Size(
                        bottomCircleRadius * 2,
                        bottomCircleRadius * 2,
                    ),
                )

                // Хвост пина
                val pinPath = Path().apply {
                    moveTo(132f * scaleX, 82f * scaleY)
                    lineTo(140f * scaleX, 108f * scaleY)
                    quadraticBezierTo(
                        x1 = 129f * scaleX,
                        y1 = 103f * scaleY,
                        x2 = 122f * scaleX,
                        y2 = 92f * scaleY,
                    )
                    close()
                }
                drawPath(pinPath, color = Color.Black)
        }
    }
}

