package ru.mlbb.blocker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import ru.mlbb.blocker.data.RegionIpRepository

class RegionIpRepositoryTest {

    @Test
    fun khabarovskMatchPoolIsFarEast() {
        assertEquals(RegionIpRepository.REGION_DV, RegionIpRepository.regionOf("5.188.3.161"))
        assertEquals(RegionIpRepository.REGION_DV, RegionIpRepository.regionOf("92.223.80.21"))
    }

    @Test
    fun novosibirskIsNotMoscowDespiteGeoIp() {
        // DB-IP ошибочно относит 146.185.199.0/24 к Москве — встроенная таблица важнее.
        assertEquals(RegionIpRepository.REGION_NSK, RegionIpRepository.regionOf("146.185.199.90"))
    }

    @Test
    fun moscowPoolIsMoscow() {
        assertEquals(RegionIpRepository.REGION_MSK, RegionIpRepository.regionOf("154.85.91.155"))
        assertEquals(RegionIpRepository.REGION_MSK, RegionIpRepository.regionOf("66.253.15.85"))
    }

    @Test
    fun unknownIpHasNoRegion() {
        assertNull(RegionIpRepository.regionOf("203.0.113.10"))
    }

    @Test
    fun delayTargetsExcludeFarEastAndMoscow() {
        val targets = RegionIpRepository.delayTargetCidrs()
        assertEquals(
            listOf("146.185.199.0/24", "92.223.91.0/24", "146.185.197.0/24"),
            targets,
        )
    }
}
