package com.shiplocate.presentation.feature.loads

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shiplocate.domain.model.load.Load
import com.shiplocate.presentation.util.DateFormatter
import kotlinx.coroutines.launch
import kotlin.math.min

/**
 * Loads screen displaying list of loads with Pager and Bottom Navigation
 * Shows loading state, error state, empty state, or list of loads
 */
@Suppress("FunctionName")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadsScreen(
    paddingValues: PaddingValues,
    viewModel: LoadsViewModel,
    onLoadClick: (Long) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    
    // Pager state with 2 pages (Active and Upcoming)
    val pagerState = rememberPagerState(pageCount = { 2 }, initialPage = 0)
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(paddingValues),
    ) {
        when (val state = uiState) {
            is LoadsUiState.Loading -> LoadingContent()
            is LoadsUiState.Error ->
                ErrorContentWithRefresh(
                    message = state.message,
                    isRefreshing = isRefreshing,
                    onRetry = { viewModel.retry() },
                    onRefresh = { viewModel.refresh() },
                )
            is LoadsUiState.Success -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Pager with two pages
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.weight(1f),
                    ) { page ->
                        when (page) {
                            0 -> {
                                // Active loads (loadStatus == 1)
                                val activeLoads = state.loads.filter { it.loadStatus == 1 }
                                LoadsListWithRefresh(
                                    loads = activeLoads,
                                    isRefreshing = isRefreshing,
                                    onRefresh = { viewModel.refresh() },
                                    onLoadClick = onLoadClick,
                                )
                            }
                            1 -> {
                                // Upcoming loads (loadStatus == 0)
                                val upcomingLoads = state.loads.filter { it.loadStatus == 0 }
                                LoadsListWithRefresh(
                                    loads = upcomingLoads,
                                    isRefreshing = isRefreshing,
                                    onRefresh = { viewModel.refresh() },
                                    onLoadClick = onLoadClick,
                                )
                            }
                        }
                    }
                    
                    // Bottom Navigation Bar
                    NavigationBar {
                        NavigationBarItem(
                            selected = pagerState.currentPage == 0,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(0)
                                }
                            },
                            icon = {
                                ActiveIcon(
                                    color = if (pagerState.currentPage == 0) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                )
                            },
                            label = { Text("Active") },
                        )
                        NavigationBarItem(
                            selected = pagerState.currentPage == 1,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(1)
                                }
                            },
                            icon = {
                                UpcomingIcon(
                                    color = if (pagerState.currentPage == 1) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                )
                            },
                            label = { Text("Upcoming") },
                        )
                    }
                }
            }
        }
    }
}

@Suppress("FunctionName")
@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CircularProgressIndicator()
            Text(
                text = "Loading loads...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
    }
}

@Suppress("FunctionName")
@Composable
private fun EmptyStateItem() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp,
            ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "No loads found",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "You don't have any loads yet.\nPull down to refresh.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Suppress("FunctionName")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ErrorContentWithRefresh(
    message: String,
    isRefreshing: Boolean,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(32.dp),
            ) {
                Text(
                    text = "Error",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "Pull down to refresh",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center,
                )
                Button(
                    onClick = onRetry,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                ) {
                    Text("Retry")
                }
            }
        }
    }
}

@Suppress("FunctionName")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoadsListWithRefresh(
    loads: List<Load>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onLoadClick: (Long) -> Unit,
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (loads.isEmpty()) {
                item {
                    EmptyStateItem()
                }
            } else {
                items(loads, key = { it.id }) { load ->
                    LoadItem(
                        load = load,
                        onClick = { onLoadClick(load.id) },
                    )
                }
            }
        }
    }
}

@Suppress("FunctionName")
@Composable
private fun LoadItem(
    load: Load,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp,
            ),
    ) {
        Column(
            modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Load ID
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Load ID:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
                Text(
                    text = load.loadName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            // Description
            load.description?.let { description ->
                if (description.isNotBlank()) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "Description:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            // Load Status
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Status:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
                Text(
                    text = formatLoadStatus(load.loadStatus),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = getLoadStatusColor(load.loadStatus),
                )
            }

            // Last Updated
            load.lastUpdated?.let { lastUpdated ->
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Last Updated:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )
                    Text(
                        text = DateFormatter.formatTimestamp(lastUpdated),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            // Created At
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Created:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
                Text(
                    text = DateFormatter.formatTimestamp(load.createdAt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

private fun formatLoadStatus(status: Int): String {
    return when (status) {
        0 -> "Not connected"
        1 -> "Connected"
        2 -> "Disconnected"
        else -> "Unknown ($status)"
    }
}

/**
 * Get color for load status
 * 0 = Not connected (Gray)
 * 1 = Connected (Green)
 * 2 = Disconnected (Orange)
 * Other = Unknown (Gray)
 */
@Composable
private fun getLoadStatusColor(status: Int): Color {
    return when (status) {
        0 -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) // Gray for Not connected
        1 -> Color(0xFF4CAF50) // Green for Connected
        2 -> Color(0xFFFF9800) // Orange for Disconnected
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) // Gray for Unknown
    }
}

/**
 * Иконка для Active loads (активные/подключенные)
 * Иконка: круг с галочкой внутри (символ активности/подключения)
 */
@Composable
private fun ActiveIcon(
    color: Color,
    modifier: Modifier = Modifier,
    iconSize: androidx.compose.ui.unit.Dp = 24.dp,
) {
    Canvas(modifier = modifier.size(iconSize)) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val iconSizePx = min(size.width, size.height) * 0.8f

        // Круг
        drawCircle(
            color = color,
            radius = iconSizePx / 2f,
            center = Offset(centerX, centerY),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
        )

        // Галочка
        val checkPath = Path().apply {
            moveTo(centerX - iconSizePx * 0.2f, centerY)
            lineTo(centerX - iconSizePx * 0.05f, centerY + iconSizePx * 0.15f)
            lineTo(centerX + iconSizePx * 0.2f, centerY - iconSizePx * 0.15f)
        }
        drawPath(
            checkPath,
            color = color,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round),
        )
    }
}

/**
 * Иконка для Upcoming loads (предстоящие)
 * Иконка: календарь (символ предстоящих событий)
 */
@Composable
private fun UpcomingIcon(
    color: Color,
    modifier: Modifier = Modifier,
    iconSize: androidx.compose.ui.unit.Dp = 24.dp,
) {
    Canvas(modifier = modifier.size(iconSize)) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val iconSizePx = min(size.width, size.height) * 0.7f

        // Календарь - прямоугольник
        drawRect(
            color = color,
            topLeft = Offset(centerX - iconSizePx * 0.35f, centerY - iconSizePx * 0.3f),
            size = Size(iconSizePx * 0.7f, iconSizePx * 0.6f),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
        )

        // Верхняя часть календаря (заголовок)
        drawRect(
            color = color,
            topLeft = Offset(centerX - iconSizePx * 0.35f, centerY - iconSizePx * 0.3f),
            size = Size(iconSizePx * 0.7f, iconSizePx * 0.15f),
        )

        // Кольца для переплета
        val ringRadius = iconSizePx * 0.08f
        drawCircle(
            color = color,
            radius = ringRadius,
            center = Offset(centerX - iconSizePx * 0.2f, centerY - iconSizePx * 0.225f),
        )
        drawCircle(
            color = color,
            radius = ringRadius,
            center = Offset(centerX + iconSizePx * 0.2f, centerY - iconSizePx * 0.225f),
        )

        // Линии для дней недели
        val lineY1 = centerY - iconSizePx * 0.1f
        val lineY2 = centerY + iconSizePx * 0.1f
        val lineSpacing = iconSizePx * 0.15f

        // Три горизонтальные линии
        for (i in 0..2) {
            val y = lineY1 + i * lineSpacing
            drawLine(
                color = color,
                start = Offset(centerX - iconSizePx * 0.25f, y),
                end = Offset(centerX + iconSizePx * 0.25f, y),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
    }
}
