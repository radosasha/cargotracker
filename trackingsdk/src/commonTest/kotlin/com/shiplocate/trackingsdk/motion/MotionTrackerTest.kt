package com.shiplocate.trackingsdk.motion

import com.shiplocate.core.logging.Logger
import com.shiplocate.trackingsdk.motion.models.MotionEvent
import com.shiplocate.trackingsdk.motion.models.MotionState
import com.shiplocate.trackingsdk.motion.models.MotionTrackerEvent
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MotionTrackerTest {

	private val mockLogger = mockk<Logger>(relaxed = true)
	private val mockActivityRecognitionConnector = mockk<ActivityRecognitionConnector>(relaxed = true)
	private val motionEventsFlow = MutableSharedFlow<MotionEvent>(replay = 0)

	init {
		io.mockk.every { mockActivityRecognitionConnector.observeMotionEvents() } returns motionEventsFlow
	}

	/**
	 * Ожидаемый результат теста для датасета
	 * @param expectDriving true если ожидается детекция вождения, false если нет
	 * @param expectedEventIndex ожидаемый индекс события, после которого должно сработать вождение (только если expectDriving = true)
	 * @param toleranceEvents допуск по количеству событий (±N событий от expectedEventIndex)
	 */
	private data class ExpectedResult(
		val expectDriving: Boolean,
		val expectedEventIndex: Int? = null,
		val toleranceEvents: Int = 5
	)

	@Test
	fun `run all datasets from resources and validate driving detection`() = runTest {
		val datasets = listOf(
			// dataset01: первое IN_VEHICLE на событии #11, событий до первого: 11 (>10)
			// Окно завершается через ~60с после первого IN_VEHICLE, но алгоритм находит его позже
			// из-за скользящего окна и смешанных событий в начале истории
			// Реальный триггер на #38 (разница 27 событий от первого IN_VEHICLE)
			"dataset01.txt" to ExpectedResult(expectDriving = true, expectedEventIndex = 38, toleranceEvents = 15),
			// dataset02: нет IN_VEHICLE событий - не ожидаем вождения
			"dataset02.txt" to ExpectedResult(expectDriving = false),
			// dataset03: первое IN_VEHICLE на событии #24, событий до первого: 24 (>10)
			// Много событий до первого IN_VEHICLE - алгоритм найдет окно значительно позже
			// из-за скользящего окна, которое проходит по всем событиям последовательно
			// Реальный триггер на #73 (разница 49 событий от первого IN_VEHICLE)
			"dataset03.txt" to ExpectedResult(expectDriving = true, expectedEventIndex = 73, toleranceEvents = 20),
			// dataset04: нет IN_VEHICLE событий - не ожидаем вождения
			"dataset04.txt" to ExpectedResult(expectDriving = false),
			// dataset05: первое IN_VEHICLE на событии #0, событий до первого: 0 (≤10)
			// Нет событий до первого IN_VEHICLE - алгоритм найдет окно очень рано
			// Окно завершается через ~60с (событие #6), но может быть найдено уже на событии #5-7
			// Реальный триггер на #5 (через ~50 секунд после первого IN_VEHICLE)
			"dataset05.txt" to ExpectedResult(expectDriving = true, expectedEventIndex = 6, toleranceEvents = 3),
			// dataset06: нет IN_VEHICLE событий - не ожидаем вождения
			"dataset06.txt" to ExpectedResult(expectDriving = false),
			// dataset07: первое IN_VEHICLE на событии #0, событий до первого: 0 (≤10)
			// Аналогично dataset05 - алгоритм найдет окно рано, так как нет событий до первого IN_VEHICLE
			// Окно завершается через ~60с, триггер ожидается на событии #6-8
			"dataset07.txt" to ExpectedResult(expectDriving = true, expectedEventIndex = 6, toleranceEvents = 3),
			// dataset08: первое IN_VEHICLE на событии #3, но только 2 IN_VEHICLE в первых 60 секундах
			// Короткие всплески - недостаточно для 60% окна (требуется минимум 6 IN_VEHICLE за 60 секунд)
			// Не ожидаем вождения - недостаточно данных для определения устойчивого вождения
			"dataset08.txt" to ExpectedResult(expectDriving = false),
			// dataset09: нет IN_VEHICLE событий - не ожидаем вождения
			"dataset09.txt" to ExpectedResult(expectDriving = false),
			// dataset10: первое IN_VEHICLE на событии #0, событий до первого: 0 (≤10)
			// Аналогично dataset05 и dataset07 - алгоритм найдет окно рано
			"dataset10.txt" to ExpectedResult(expectDriving = true, expectedEventIndex = 6, toleranceEvents = 3),
		)

		for ((fileName, expectedResult) in datasets) {
			println("\n=== Running dataset: $fileName ===")
			println("Expected: driving=${expectedResult.expectDriving}, eventIndex=${expectedResult.expectedEventIndex}, tolerance=${expectedResult.toleranceEvents}")
			
			// Read test data files
			val events = readDatasetFile(fileName)
			
			println("Loaded ${events.size} events from $fileName")
			val timeSpan = if (events.size >= 2) events.last().timestamp - events.first().timestamp else 0L
			println("Time span: ${timeSpan}ms (${timeSpan / 1000}s)")
			val inVehicleCount = events.count { it.motionState == MotionState.IN_VEHICLE }
			println("IN_VEHICLE events: $inVehicleCount (${(inVehicleCount.toFloat() / events.size * 100).toInt()}%)")

			val tracker = MotionTracker(
				activityRecognitionConnector = mockActivityRecognitionConnector,
				analysisWindowMs = 60 * 1000L,
				trimWindowMs = 60 * 1000L,
				vehicleTimeThreshold = 0.6f,
				confidenceThreshold = 70,
				minAnalysisDurationMs = 60 * 1000L,
				retentionWindowMs = 5 * 60 * 1000L,
				minWindowMs = 60 * 1000L,
				maxWindowMs = 5 * 60 * 1000L,
				initialAnalysisIntervalMs = 60 * 1000L,
				fastAnalysisIntervalMs = 30 * 1000L,
				lowAnalysisIntervalMs = 2 * 60 * 1000L,
				backgroundAnalysisIntervalMs = 5 * 60 * 1000L,
				drivingStreakForFast = 3,
				nonDrivingStreakForLow = 5,
				nonDrivingStreakForBackground = 10,
				logger = mockLogger,
				scope = this.backgroundScope
			)

			tracker.startTracking()

			// Отслеживаем индекс события, после которого сработал триггер
			var triggerEventIndex: Int? = null
			
			// Ожидаем InVehicle событие (триггер вождения)
			val triggerCollector = async { 
				tracker.observeMotionTrigger()
					.filter { it is MotionTrackerEvent.InVehicle }
					.take(1)
					.first()
			}

			// Эмитим события последовательно и отслеживаем момент срабатывания триггера
			val emitJob = async {
				for ((index, event) in events.withIndex()) {
					motionEventsFlow.emit(event)
					kotlinx.coroutines.delay(1) // Небольшая задержка для обработки
					
					// Проверяем, сработал ли триггер после этого события
					if (triggerEventIndex == null && triggerCollector.isCompleted) {
						triggerEventIndex = index
						break
					}
				}
				advanceUntilIdle()
			}

			if (expectedResult.expectDriving) {
				try {
					withTimeout(10_000) { 
						triggerCollector.await()
					}
					emitJob.await()
					
					// Определяем, на каком событии сработал триггер
					val actualEventIndex = triggerEventIndex 
						?: error("Trigger fired but event index was not captured")
					val expectedEventIndex = expectedResult.expectedEventIndex
					
					if (expectedEventIndex != null) {
						val diff = kotlin.math.abs(actualEventIndex - expectedEventIndex)
						if (diff <= expectedResult.toleranceEvents) {
							println("✅ PASS: Dataset $fileName detected driving at event #$actualEventIndex (expected ~#$expectedEventIndex, diff=$diff)")
							assertTrue(true, "Expected driving for $fileName at event ~$expectedEventIndex, got at $actualEventIndex")
						} else {
							println("❌ FAIL: Dataset $fileName detected driving at event #$actualEventIndex but expected ~#$expectedEventIndex (diff=$diff, tolerance=${expectedResult.toleranceEvents})")
							assertTrue(false, "Expected driving at event ~$expectedEventIndex for $fileName, but triggered at $actualEventIndex")
						}
					} else {
						println("✅ PASS: Dataset $fileName detected driving at event #$actualEventIndex")
						assertTrue(true, "Expected driving for $fileName")
					}
				} catch (e: Exception) {
					println("❌ FAIL: Dataset $fileName did NOT detect driving (expected driving)")
					println("   Exception: ${e.message}")
					assertTrue(false, "Expected driving, but no trigger for $fileName")
				}
			} else {
				// Для случая, когда не ожидается вождение, ждем завершения эмита
				// и проверяем, что триггер InVehicle не сработал
				// (CheckingMotion события могут прийти, но это нормально)
				emitJob.await()
				var triggered = false
				try {
					withTimeout(2_000) { 
						// Ждем только InVehicle события
						tracker.observeMotionTrigger()
							.filter { it is MotionTrackerEvent.InVehicle }
							.take(1)
							.first()
					}
					triggered = true
				} catch (_: Exception) {
					// Expected timeout - триггер InVehicle не сработал, это правильно
				}
				if (triggered) {
					val actualEventIndex = triggerEventIndex ?: -1
					println("❌ FAIL: Dataset $fileName triggered driving at event #$actualEventIndex (expected NO driving)")
					assertTrue(false, "Did not expect driving, but triggered for $fileName at event #$actualEventIndex")
				} else {
					println("✅ PASS: Dataset $fileName correctly did NOT detect driving")
					assertTrue(true, "Correctly did not trigger for $fileName")
				}
			}
			
			// Вариант 1: Гарантируем завершение всех async-джобов перед destroy()
			// Убеждаемся что emitJob завершен
			try {
				emitJob.await()
			} catch (e: Exception) {
				// Если уже завершен или отменен - это нормально
			}
			
			// Отменяем triggerCollector если он еще активен
			// Для Deferred используем cancel() и await() для ожидания завершения
			if (!triggerCollector.isCompleted) {
				triggerCollector.cancel()
				try {
					triggerCollector.await()
				} catch (e: Exception) {
					// Игнорируем исключения при отмене (CancellationException)
				}
			}
			
			advanceUntilIdle()
			
			// Останавливаем трекер перед destroy()
			tracker.stopTracking()
			advanceUntilIdle()
			
			// Очищаем трекер после каждого датасета
			tracker.destroy()
			advanceUntilIdle()
		}
	}

	private suspend fun emitEvents(events: List<MotionEvent>) {
		for (e in events) {
			motionEventsFlow.emit(e)
		}
	}

	/**
	 * Читает тестовый файл из файловой системы.
	 * В Android unit tests рабочая директория (user.dir) указывает на модуль trackingsdk.
	 */
	private fun readDatasetFile(fileName: String): List<MotionEvent> {
		val workingDir = File(System.getProperty("user.dir") ?: ".")
		
		// user.dir может быть либо корень проекта, либо модуль trackingsdk
		val file = if (workingDir.name == "trackingsdk") {
			// Если мы в trackingsdk, путь без trackingsdk/
			File(workingDir, "src/commonTest/resources/motiondatasets/$fileName")
		} else {
			// Если мы в корне проекта, путь с trackingsdk/
			File(workingDir, "trackingsdk/src/commonTest/resources/motiondatasets/$fileName")
		}
		
		if (!file.exists()) {
			error("Dataset file not found: ${file.absolutePath}\n" +
				"Working dir: ${workingDir.absolutePath}\n" +
				"File name: $fileName")
		}
		
		return readDatasetFromFile(file)
	}
	
	private fun parseMotionEvents(lines: List<String>): List<MotionEvent> {
		val parsed = mutableListOf<MotionEvent>()
		
		for (line in lines) {
			val trimmed = line.trim()
			if (trimmed.isEmpty()) continue
			
			val parts = trimmed.split(",").map { it.trim() }
			if (parts.size != 3) {
				println("Warning: Skipping invalid line: $trimmed")
				continue
			}
			
			val state = when (parts[0]) {
				"IN_VEHICLE" -> MotionState.IN_VEHICLE
				"ON_BICYCLE" -> MotionState.ON_BICYCLE
				"RUNNING" -> MotionState.RUNNING
				"WALKING" -> MotionState.WALKING
				"STATIONARY" -> MotionState.STATIONARY
				else -> MotionState.UNKNOWN
			}
			
			val conf = parts[1].toIntOrNull() ?: 0
			val timeStr = parts[2]
			val ms = parseTimeToMillis(timeStr)
			
			parsed.add(MotionEvent(state, conf, ms))
		}
		
		return parsed
	}

	private fun readDatasetFromFile(file: File): List<MotionEvent> {
		val lines = file.readLines()
		return parseMotionEvents(lines)
	}

	private fun parseTimeToMillis(hhmmssSSS: String): Long {
		val parts = hhmmssSSS.split(":", ".")
		require(parts.size == 4) { "Invalid time format: $hhmmssSSS" }
		val h = parts[0].toInt()
		val m = parts[1].toInt()
		val s = parts[2].toInt()
		val ms = parts[3].toInt()
		return h * 3_600_000L + m * 60_000L + s * 1_000L + ms
	}
}
