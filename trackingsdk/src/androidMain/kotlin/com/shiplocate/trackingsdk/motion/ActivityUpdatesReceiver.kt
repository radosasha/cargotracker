package com.shiplocate.trackingsdk.motion

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.trackingsdk.motion.models.MotionEvent
import com.shiplocate.trackingsdk.motion.models.MotionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.getKoin

internal object MotionEventBus {
    val flow = MutableSharedFlow<MotionEvent>(replay = 0)
}

class ActivityUpdatesReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val logger: Logger = getKoin().get()
        val result = ActivityRecognitionResult.extractResult(intent)

        if (result == null) {
            return
        }
        val activities = result.probableActivities

        // Приоритет типов при равном confidence
        val priority = listOf(
            DetectedActivity.IN_VEHICLE,
            DetectedActivity.ON_BICYCLE,
            DetectedActivity.RUNNING,
            DetectedActivity.WALKING,
            DetectedActivity.STILL,
            DetectedActivity.ON_FOOT,
            DetectedActivity.UNKNOWN,
        )
        val best = activities.sortedWith(
            compareByDescending<DetectedActivity> { it.confidence }
                .thenBy { t ->
                    val idx = priority.indexOf(t.type)
                    if (idx == -1) Int.MAX_VALUE else idx
                }
        ).first()

        val state = when (best.type) {
            DetectedActivity.IN_VEHICLE -> MotionState.IN_VEHICLE
            DetectedActivity.ON_BICYCLE -> MotionState.ON_BICYCLE
            DetectedActivity.RUNNING -> MotionState.RUNNING
            DetectedActivity.WALKING -> MotionState.WALKING
            DetectedActivity.STILL -> MotionState.STATIONARY
            else -> MotionState.UNKNOWN
        }

        val confidence = best.confidence

        logger.debug(
            LogCategory.LOCATION,
            "AR best: ${best.type} -> $state ($confidence%)"
        )

        scope.launch {
            MotionEventBus.flow.emit(
                MotionEvent(state, confidence, System.currentTimeMillis())
            )
        }

        /*activities.forEach { activity ->
            val state = when (activity.type) {
                DetectedActivity.IN_VEHICLE -> MotionState.IN_VEHICLE
                DetectedActivity.ON_BICYCLE -> MotionState.ON_BICYCLE
                DetectedActivity.WALKING -> MotionState.WALKING
                DetectedActivity.RUNNING -> MotionState.RUNNING
                DetectedActivity.STILL -> MotionState.STATIONARY
                else -> MotionState.UNKNOWN
            }
            val confidence = activity.confidence
            logger.debug(
                LogCategory.LOCATION,
                "ActivityUpdatesReceiver: ${activity.type} -> $state (confidence: ${confidence}%)"
            )
            MotionEventBus.flow.tryEmit(
                MotionEvent(
                    motionState = state,
                    confidence = confidence,
                    timestamp = System.currentTimeMillis()
                )
            )
        }*/
    }
}


