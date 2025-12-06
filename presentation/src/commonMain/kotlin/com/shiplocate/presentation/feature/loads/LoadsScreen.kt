package com.shiplocate.presentation.feature.loads

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shiplocate.presentation.component.StopsTimeline
import com.shiplocate.presentation.model.ActiveLoadUiModel
import com.shiplocate.presentation.model.LoadUiModel

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
    onNavigateToPermissions: () -> Unit = {},
    onNavigateToMessages: (Long) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val currentPage by viewModel.currentPage.collectAsStateWithLifecycle()
    val showRejectSuccessDialog by viewModel.showRejectSuccessDialog.collectAsStateWithLifecycle()
    val showLoadDeliveredDialog by viewModel.showLoadDeliveredDialog.collectAsStateWithLifecycle()
    val showRejectLoadDialog by viewModel.showRejectLoadDialog.collectAsStateWithLifecycle()
    val isLoadingAction by viewModel.isLoadingAction.collectAsStateWithLifecycle()
    val showTrackingStartedSuccessDialog by viewModel.showTrackingStartedSuccessDialog.collectAsStateWithLifecycle()
    val showLogoutDialog by viewModel.showLogoutDialog.collectAsStateWithLifecycle()
    val isLoggingOut by viewModel.isLoggingOut.collectAsStateWithLifecycle()
    val logoutError by viewModel.logoutError.collectAsStateWithLifecycle()
    
    // Pager state with 2 pages (Active and Upcoming)
    val pagerState = rememberPagerState(pageCount = { 2 }, initialPage = currentPage)

    // Sync pagerState with ViewModel
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != currentPage) {
            viewModel.setCurrentPage(pagerState.currentPage)
        }
    }

    // Sync ViewModel with pagerState when currentPage changes externally
    LaunchedEffect(currentPage) {
        if (pagerState.currentPage != currentPage) {
            pagerState.animateScrollToPage(currentPage)
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(paddingValues),
    ) {
        // Блокируем весь UI во время logout
        if (isLoggingOut) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Logging out...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

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
                Box(modifier = Modifier.fillMaxSize()) {
                    // Pager with two pages
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                    ) { page ->
                        when (page) {
                             0 -> {
                                // Active load (один Load из UiState)
                                if (state.activeLoad != null) {
                                    // Показываем один Load из UiState без Card
                                    ActiveLoadListWithRefresh(
                                        load = state.activeLoad,
                                        isRefreshing = isRefreshing,
                                        isLoadingAction = isLoadingAction,
                                        onRefresh = { viewModel.refresh() },
                                        onConfirmLoadDelivered = { viewModel.showLoadDeliveredDialog() },
                                        onUpdateStopCompletion = { stopId, completion ->
                                            viewModel.updateStopCompletion(stopId, completion)
                                        },
                                    )
                                } else {
                                    // Показываем пустое состояние
                                    LoadsListWithRefresh(
                                        loads = emptyList(),
                                        isRefreshing = isRefreshing,
                                        onRefresh = { viewModel.refresh() },
                                        onLoadClick = onLoadClick,
                                    )
                                }
                            }
                            1 -> {
                                // Upcoming loads (список Load из UiState) - в Card
                                LoadsListWithRefresh(
                                    loads = state.upcomingLoads,
                                    isRefreshing = isRefreshing,
                                    onRefresh = { viewModel.refresh() },
                                    onLoadClick = onLoadClick,
                                )
                            }
                        }
                    }
                    
                    // Красное окно снизу с предупреждением о разрешениях
                    // Показывается только на вкладке Active (page 0) когда есть activeLoad и не все разрешения получены
                    if (currentPage == 0 && state.showPermissionsWarning && state.activeLoad != null) {
                        PermissionsWarningBanner(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(16.dp),
                            onNavigateToPermissions = onNavigateToPermissions,
                        )
                    }

                    // Floating Action Button for Messages
                    // Показывается только на вкладке Active (page 0) когда есть activeLoad
                    if (currentPage == 0 && state.activeLoad != null) {
                        FloatingActionButton(
                            onClick = { onNavigateToMessages(state.activeLoad.id) },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp),
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            shape = CircleShape,
                        ) {
                            MessageIcon(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                }
            }
        }
    }

    // Диалог успешного reject
    if (showRejectSuccessDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissRejectSuccessDialog() },
            title = {
                Text(
                    text = "Load Rejected",
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                Text(
                    text = "You have successfully rejected the load.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissRejectSuccessDialog() }) {
                    Text("OK")
                }
            },
        )
    }

    // Диалог подтверждения "Load delivered"
    val activeLoadForDialog = (uiState as? LoadsUiState.Success)?.activeLoad
    if (showLoadDeliveredDialog && activeLoadForDialog != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissLoadDeliveredDialog() },
            title = {
                Text(
                    text = "Confirm Load Delivery",
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to mark this load as delivered? This will stop tracking and disconnect from the load.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmLoadDelivered(activeLoadForDialog.id) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissLoadDeliveredDialog() }) {
                    Text("Cancel")
                }
            },
        )
    }

    // Диалог подтверждения "Reject load"
    if (showRejectLoadDialog && activeLoadForDialog != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissRejectLoadDialog() },
            title = {
                Text(
                    text = "Confirm Load Rejection",
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to reject this load? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmRejectLoad(activeLoadForDialog.id) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissRejectLoadDialog() }) {
                    Text("Cancel")
                }
            },
        )
    }

    // Диалог успешного запуска трекинга
    if (showTrackingStartedSuccessDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissTrackingStartedSuccessDialog() },
            title = {
                Text(
                    text = "Tracking Started",
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                Text(
                    text = "All required permissions have been granted and tracking has been started successfully.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissTrackingStartedSuccessDialog() }) {
                    Text("OK")
                }
            },
        )
    }

    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isLoggingOut) {
                    viewModel.dismissLogoutDialog()
                }
            },
            title = {
                Text(
                    text = "Confirm Logout",
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to logout? All your data will be cleared.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmLogout() },
                    enabled = !isLoggingOut,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) {
                    if (isLoggingOut) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onError,
                        )
                    } else {
                        Text("Logout")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.dismissLogoutDialog() },
                    enabled = !isLoggingOut,
                ) {
                    Text("Cancel")
                }
            },
        )
    }

    // Logout error dialog
    logoutError?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissLogoutError() },
            title = {
                Text(
                    text = "Logout Failed",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.error,
                )
            },
            text = {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissLogoutError() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) {
                    Text("OK")
                }
            },
        )
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
    loads: List<LoadUiModel>,
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
    load: LoadUiModel,
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

           /* // Last Updated
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
            }*/

        }
    }
}

@Suppress("FunctionName")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActiveLoadListWithRefresh(
    load: ActiveLoadUiModel,
    isRefreshing: Boolean,
    isLoadingAction: Boolean,
    onRefresh: () -> Unit,
    onConfirmLoadDelivered: () -> Unit,
    onUpdateStopCompletion: (Long, Int) -> Unit,
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
            item {
                ActiveLoadItem(
                    load = load,
                    isLoadingAction = isLoadingAction,
                    onConfirmLoadDelivered = onConfirmLoadDelivered,
                    onUpdateStopCompletion = onUpdateStopCompletion,
                    isLoadingCompletion = isLoadingAction,
                )
            }
        }
    }
}

@Suppress("FunctionName")
@Composable
private fun ActiveLoadItem(
    load: ActiveLoadUiModel,
    isLoadingAction: Boolean,
    onConfirmLoadDelivered: () -> Unit,
    onUpdateStopCompletion: (Long, Int) -> Unit,
    isLoadingCompletion: Boolean = false,
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

        // ETA from route
        load.routeDuration?.let { duration ->
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "ETA:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
                Text(
                    text = duration,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }

        /*// Last Updated
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
        }*/

        // Stops Timeline (если есть stops)
        if (load.stops.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            StopsTimeline(
                stops = load.stops,
                showCompletionButtons = true,
                onUpdateStopCompletion = onUpdateStopCompletion,
                isLoadingCompletion = isLoadingCompletion,
            )
        }

        // Кнопка действия
        Spacer(modifier = Modifier.height(16.dp))

        // Кнопка "Confirm Load Delivery"
        Button(
            onClick = onConfirmLoadDelivered,
            enabled = !isLoadingAction,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
            ),
        ) {
            if (isLoadingAction) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onError,
                )
            } else {
                Text("Confirm Delivery")
            }
        }
    }
}

private fun formatLoadStatus(status: com.shiplocate.presentation.model.LoadStatus): String {
    return when (status) {
        com.shiplocate.presentation.model.LoadStatus.LOAD_STATUS_NOT_CONNECTED -> "Not connected"
        com.shiplocate.presentation.model.LoadStatus.LOAD_STATUS_CONNECTED -> "Connected"
        com.shiplocate.presentation.model.LoadStatus.LOAD_STATUS_DISCONNECTED -> "Disconnected"
        com.shiplocate.presentation.model.LoadStatus.LOAD_STATUS_REJECTED -> "Rejected"
        com.shiplocate.presentation.model.LoadStatus.LOAD_STATUS_UNKNOWN -> "Unknown"
    }
}

/**
 * Get color for load status
 */
@Composable
private fun getLoadStatusColor(status: com.shiplocate.presentation.model.LoadStatus): Color {
    return when (status) {
        com.shiplocate.presentation.model.LoadStatus.LOAD_STATUS_NOT_CONNECTED -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) // Gray for Not connected
        com.shiplocate.presentation.model.LoadStatus.LOAD_STATUS_CONNECTED -> Color(0xFF4CAF50) // Green for Connected
        com.shiplocate.presentation.model.LoadStatus.LOAD_STATUS_DISCONNECTED -> Color(0xFFFF9800) // Orange for Disconnected
        com.shiplocate.presentation.model.LoadStatus.LOAD_STATUS_REJECTED -> MaterialTheme.colorScheme.error // Red for Rejected
        com.shiplocate.presentation.model.LoadStatus.LOAD_STATUS_UNKNOWN -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) // Gray for Unknown
    }
}

/**
 * Красное окно с предупреждением о разрешениях
 * Показывается снизу экрана, когда есть активный Load, но не все разрешения получены
 */
@Composable
private fun PermissionsWarningBanner(
    modifier: Modifier = Modifier,
    onNavigateToPermissions: () -> Unit,
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.error,
                shape = RoundedCornerShape(12.dp),
            )
            .padding(16.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Permissions Required",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onError,
            )
            Text(
                text = "To continue tracking, please grant all required permissions.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onError,
                textAlign = TextAlign.Start,
            )
            Button(
                onClick = onNavigateToPermissions,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onError,
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text("Grant Permissions")
            }
        }
    }
}

/**
 * Custom message icon for FloatingActionButton
 */
@Composable
private fun MessageIcon(
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val iconSize = size.width * 0.6f

        // Speech bubble shape
        val bubblePath = Path().apply {
            // Main rectangle
            moveTo(centerX - iconSize * 0.35f, centerY - iconSize * 0.25f)
            lineTo(centerX + iconSize * 0.35f, centerY - iconSize * 0.25f)
            lineTo(centerX + iconSize * 0.35f, centerY + iconSize * 0.25f)
            lineTo(centerX - iconSize * 0.35f, centerY + iconSize * 0.25f)
            close()

            // Tail (pointing down-left)
            moveTo(centerX - iconSize * 0.35f, centerY + iconSize * 0.25f)
            lineTo(centerX - iconSize * 0.2f, centerY + iconSize * 0.4f)
            lineTo(centerX - iconSize * 0.1f, centerY + iconSize * 0.25f)
            close()
        }

        drawPath(
            path = bubblePath,
            color = color,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
    }
}

