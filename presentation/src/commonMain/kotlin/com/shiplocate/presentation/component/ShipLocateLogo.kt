package com.shiplocate.presentation.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

/**
 * Expect функция для получения Painter логотипа на разных платформах
 */
@Composable
expect fun rememberShipLocateLogoPainter(): Painter

/**
 * Логотип ShipLocate - использует векторный ресурс
 * Включает иконку (круг с фигурой "9") и текст "SHIPLOCATE"
 */
@Composable
fun ShipLocateLogo(
    iconColor: Color = Color.Black,
    textColor: Color = Color.Black,
    modifier: Modifier = Modifier,
    iconSize: androidx.compose.ui.unit.Dp ,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Векторная иконка из ресурсов
        Image(
            painter = rememberShipLocateLogoPainter(),
            contentDescription = "ShipLocate Logo",
            modifier = Modifier.size(iconSize),
            colorFilter = ColorFilter.tint(iconColor),
        )

        // Текст "SHIPLOCATE"
        Text(
            text = "SHIPLOCATE",
            style = TextStyle(
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

