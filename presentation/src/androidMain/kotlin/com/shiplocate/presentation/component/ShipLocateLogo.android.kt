package com.shiplocate.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.shiplocate.presentation.R

/**
 * Android реализация - использует XML Vector Drawable
 */
@Composable
actual fun rememberShipLocateLogoPainter(): Painter {
    return painterResource(R.drawable.ship_locate_logo)
}

