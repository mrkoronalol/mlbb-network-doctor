package ru.mlbb.blocker.data

import android.content.Context
import ru.mlbb.blocker.data.db.MonitorDatabase
import ru.mlbb.blocker.data.db.RegionIpEntity

/**
 * Таблица диапазонов IP по регионам (этап 4).
 *
 * Источники: офлайн GeoIP + community-списки + ручной ввод. Дефолты покрывают
 * известные матч-серверы ДВ/Новосибирска/Москвы. Профиль «ДВ» никогда не
 * задерживает ДВ и логин-пул.
 */
object RegionIpRepository {

    const val REGION_DV = "DV"
    const val REGION_NSK = "NSK"
    const val REGION_MSK = "MSK"
    const val REGION_OTHER = "OTHER"

    val DEFAULTS: List<RegionIpEntity> = listOf(
        RegionIpEntity("DV:92.223.80.0/24", REGION_DV, "92.223.80.0/24", "Хабаровск"),
        RegionIpEntity("NSK:146.185.199.0/24", REGION_NSK, "146.185.199.0/24", "Новосибирск"),
        RegionIpEntity("OTHER:92.223.91.0/24", REGION_OTHER, "92.223.91.0/24", "Екатеринбург"),
        RegionIpEntity("OTHER:146.185.197.0/24", REGION_OTHER, "146.185.197.0/24", "Ростов-на-Дону"),
        RegionIpEntity("MSK:154.85.91.0/24", REGION_MSK, "154.85.91.0/24", "Москва (логин/матч)"),
        RegionIpEntity("MSK:66.253.15.0/24", REGION_MSK, "66.253.15.0/24", "Москва, UDP-пробы"),
        RegionIpEntity("MSK:81.177.97.0/24", REGION_MSK, "81.177.97.0/24", "Москва, UDP-проба"),
    )

    @Volatile
    private var cached: List<RegionIpEntity> = DEFAULTS

    /** Загружает диапазоны в память; при пустой таблице записывает дефолты. */
    suspend fun refresh(context: Context) {
        val dao = MonitorDatabase.get(context).regionIpDao()
        if (dao.count() == 0) {
            dao.upsert(DEFAULTS)
        }
        cached = dao.all().ifEmpty { DEFAULTS }
    }

    fun cidrsFor(region: String): List<String> =
        cached.filter { it.region == region }.map { it.cidr }.ifEmpty {
            DEFAULTS.filter { it.region == region }.map { it.cidr }
        }

    fun allCached(): List<RegionIpEntity> = cached.toList()

    /** Цели шейпера: нежелательные регионы, кроме ДВ и логин-пула (Москва). */
    fun delayTargetCidrs(): List<String> =
        cidrsFor(REGION_NSK) + cidrsFor(REGION_OTHER)
}
