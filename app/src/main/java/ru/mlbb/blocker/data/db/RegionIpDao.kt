package ru.mlbb.blocker.data.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

/** Известный диапазон IP матч-серверов региона (этап 4). */
@Entity(tableName = "region_ip_ranges")
data class RegionIpEntity(
    @PrimaryKey val key: String,
    val region: String,
    val cidr: String,
    val label: String,
)

@Dao
interface RegionIpDao {

    @Query("SELECT * FROM region_ip_ranges ORDER BY region, cidr")
    suspend fun all(): List<RegionIpEntity>

    @Query("SELECT cidr FROM region_ip_ranges WHERE region = :region ORDER BY cidr")
    suspend fun cidrs(region: String): List<String>

    @Query("SELECT COUNT(*) FROM region_ip_ranges")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(items: List<RegionIpEntity>)
}
