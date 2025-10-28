package com.shiplocate.trackingsdk.utils

import com.shiplocate.trackingsdk.LatLng
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LocationUtilsGeographicCenterTest {

    @Test
    fun `getGeographicCenter should return correct center for Moscow area`() {
        // Arrange - координаты вокруг Москвы
        val coords = listOf(
            LatLng(55.7558, 37.6176, 10), // Центр Москвы
            LatLng(55.7658, 37.6276, 10), // Северо-восток
            LatLng(55.7458, 37.6076, 10), // Юго-запад
            LatLng(55.7558, 37.6076, 10)  // Запад
        )

        // Act
        val center = LocationUtils.getGeographicCenter(coords)

        // Assert
        // Центр должен быть примерно в центре квадрата
        assertTrue(center.latitude > 55.7458 && center.latitude < 55.7658)
        assertTrue(center.longitude > 37.6076 && center.longitude < 37.6276)
        assertEquals(10, center.error)
    }

    @Test
    fun `getGeographicCenter should handle coordinates around equator`() {
        // Arrange - координаты вокруг экватора
        val coords = listOf(
            LatLng(0.0, 0.0, 5),   // Экватор, нулевой меридиан
            LatLng(0.0, 1.0, 5),   // Экватор, 1° восточнее
            LatLng(1.0, 0.0, 5),   // 1° севернее, нулевой меридиан
            LatLng(1.0, 1.0, 5)    // 1° севернее, 1° восточнее
        )

        // Act
        val center = LocationUtils.getGeographicCenter(coords)

        // Assert
        // Центр должен быть в точке (0.5, 0.5)
        assertEquals(0.5, center.latitude, 0.0001)
        assertEquals(0.5, center.longitude, 0.0001)
        assertEquals(5, center.error)
    }

    @Test
    fun `getGeographicCenter should handle coordinates crossing 180th meridian`() {
        // Arrange - координаты пересекающие 180-й меридиан
        val coords = listOf(
            LatLng(0.0, 179.0, 8),  // Западнее 180°
            LatLng(0.0, -179.0, 8), // Восточнее 180° (это то же самое что 181°)
            LatLng(1.0, 179.0, 8),
            LatLng(1.0, -179.0, 8)
        )

        // Act
        val center = LocationUtils.getGeographicCenter(coords)

        // Assert
        // Центр должен быть в точке (0.5, 180.0) или около того
        assertEquals(0.5, center.latitude, 0.0001)
        // Долгота может быть либо 180.0, либо -180.0 (это одно и то же)
        assertTrue(center.longitude == 180.0 || center.longitude == -180.0)
        assertEquals(8, center.error)
    }

    @Test
    fun `getGeographicCenter should handle coordinates around North Pole`() {
        // Arrange - координаты вокруг Северного полюса
        val coords = listOf(
            LatLng(89.0, 0.0, 12),   // Близко к полюсу
            LatLng(89.0, 90.0, 12),  // Близко к полюсу, 90° восточнее
            LatLng(89.0, 180.0, 12), // Близко к полюсу, 180°
            LatLng(89.0, 270.0, 12)  // Близко к полюсу, 270°
        )

        // Act
        val center = LocationUtils.getGeographicCenter(coords)

        // Assert
        // Центр должен быть очень близко к полюсу
        assertTrue(center.latitude > 88.0)
        assertEquals(12, center.error)
    }

    @Test
    fun `getGeographicCenter should handle coordinates around South Pole`() {
        // Arrange - координаты вокруг Южного полюса
        val coords = listOf(
            LatLng(-89.0, 0.0, 7),   // Близко к полюсу
            LatLng(-89.0, 90.0, 7),  // Близко к полюсу, 90° восточнее
            LatLng(-89.0, 180.0, 7), // Близко к полюсу, 180°
            LatLng(-89.0, 270.0, 7)  // Близко к полюсу, 270°
        )

        // Act
        val center = LocationUtils.getGeographicCenter(coords)

        // Assert
        // Центр должен быть очень близко к полюсу
        assertTrue(center.latitude < -88.0)
        assertEquals(7, center.error)
    }

    @Test
    fun `getGeographicCenter should calculate correct average error`() {
        // Arrange
        val coords = listOf(
            LatLng(0.0, 0.0, 5),
            LatLng(0.0, 1.0, 15),
            LatLng(1.0, 0.0, 25),
            LatLng(1.0, 1.0, 35)
        )

        // Act
        val center = LocationUtils.getGeographicCenter(coords)

        // Assert
        // Средняя погрешность: (5 + 15 + 25 + 35) / 4 = 20
        assertEquals(20, center.error)
    }
}
