package com.shiplocate.trackingsdk.utils

import com.shiplocate.trackingsdk.utils.models.LatLng
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LocationUtilsTest {

    @Test
    fun `getGeographicCenter should return center for two points`() {
        // Arrange
        val coords = listOf(
            LatLng(0.0, 0.0, 10), // Экватор, нулевой меридиан
            LatLng(0.0, 1.0, 10)  // Экватор, 1 градус восточнее
        )

        // Act
        val center = LocationUtils.getGeographicCenter(coords)

        // Assert
        assertEquals(0.0, center.latitude, 0.0001)
        assertEquals(0.5, center.longitude, 0.0001)
        assertEquals(10, center.error)
    }

    @Test
    fun `getGeographicCenter should return center for three points forming triangle`() {
        // Arrange - треугольник на экваторе
        val coords = listOf(
            LatLng(0.0, 0.0, 5),   // Западная точка
            LatLng(0.0, 2.0, 5),   // Восточная точка
            LatLng(1.0, 1.0, 5)    // Северная точка
        )

        // Act
        val center = LocationUtils.getGeographicCenter(coords)

        // Assert
        // Центр должен быть примерно в центре треугольника
        assertTrue(center.latitude > 0.0 && center.latitude < 1.0)
        assertTrue(center.longitude > 0.0 && center.longitude < 2.0)
        assertEquals(5, center.error)
    }

    @Test
    fun `getGeographicCenter should return center for four points forming square`() {
        // Arrange - квадрат 1x1 градус
        val coords = listOf(
            LatLng(0.0, 0.0, 3),   // Юго-запад
            LatLng(0.0, 1.0, 3),   // Юго-восток
            LatLng(1.0, 0.0, 3),   // Северо-запад
            LatLng(1.0, 1.0, 3)    // Северо-восток
        )

        // Act
        val center = LocationUtils.getGeographicCenter(coords)

        // Assert
        // Центр квадрата должен быть в точке (0.5, 0.5)
        assertEquals(0.5, center.latitude, 0.0001)
        assertEquals(0.5, center.longitude, 0.0001)
        assertEquals(3, center.error)
    }

    @Test
    fun `getGeographicCenter should throw exception for empty list`() {
        // Arrange
        val coords = emptyList<LatLng>()

        // Act & Assert
        assertFailsWith<IllegalStateException> {
            LocationUtils.getGeographicCenter(coords)
        }
    }

    @Test
    fun `getGeographicCenter should return same point for single coordinate`() {
        // Arrange
        val coords = listOf(LatLng(55.7558, 37.6176, 15)) // Москва

        // Act
        val center = LocationUtils.getGeographicCenter(coords)

        // Assert
        assertEquals(55.7558, center.latitude, 0.0001)
        assertEquals(37.6176, center.longitude, 0.0001)
        assertEquals(15, center.error)
    }

    @Test
    fun `getGeographicCenter should calculate average error correctly`() {
        // Arrange
        val coords = listOf(
            LatLng(0.0, 0.0, 10),
            LatLng(0.0, 1.0, 20),
            LatLng(1.0, 0.0, 30)
        )

        // Act
        val center = LocationUtils.getGeographicCenter(coords)

        // Assert
        assertEquals(20, center.error) // (10 + 20 + 30) / 3 = 20
    }

    @Test
    fun `calculateDistance should return correct distance for same points`() {
        // Arrange
        val coord = LatLng(55.7558, 37.6176, 10) // Москва

        // Act
        val distance = LocationUtils.calculateDistance(coord, coord)

        // Assert
        assertEquals(0.0, distance, 0.1)
    }

    @Test
    fun `calculateDistance should return correct distance for known points`() {
        // Arrange
        val moscow = LatLng(55.7558, 37.6176, 10)
        val spb = LatLng(59.9311, 30.3609, 10) // Санкт-Петербург

        // Act
        val distance = LocationUtils.calculateDistance(moscow, spb)

        // Assert
        // Расстояние между Москвой и СПб примерно 635 км
        assertTrue(distance > 630000 && distance < 640000)
    }

    @Test
    fun `isInRadius should return true for same points`() {
        // Arrange
        val coord = LatLng(55.7558, 37.6176, 10)

        // Act
        val isInRadius = LocationUtils.isInRadius(coord, coord, 100)

        // Assert
        assertTrue(isInRadius)
    }

    @Test
    fun `isInRadius should return false for distant points`() {
        // Arrange
        val moscow = LatLng(55.7558, 37.6176, 10)
        val spb = LatLng(59.9311, 30.3609, 10)

        // Act
        val isInRadius = LocationUtils.isInRadius(moscow, spb, 1000) // 1 км радиус

        // Assert
        assertTrue(!isInRadius)
    }

    @Test
    fun `areAllInRadius should return true for all points in radius`() {
        // Arrange
        val center = LatLng(0.0, 0.0, 5)
        val coords = listOf(
            LatLng(0.001, 0.001, 5), // ~100м от центра
            LatLng(-0.001, 0.001, 5),
            LatLng(0.001, -0.001, 5)
        )

        // Act
        val allInRadius = LocationUtils.areAllInRadius(center, coords, 200) // 200м радиус

        // Assert
        assertTrue(allInRadius)
    }

    @Test
    fun `areAllInRadius should return false if any point is outside radius`() {
        // Arrange
        val center = LatLng(0.0, 0.0, 5)
        val coords = listOf(
            LatLng(0.001, 0.001, 5), // ~100м от центра
            LatLng(0.01, 0.01, 5)    // ~1.4км от центра
        )

        // Act
        val allInRadius = LocationUtils.areAllInRadius(center, coords, 200) // 200м радиус

        // Assert
        assertTrue(!allInRadius)
    }
}
