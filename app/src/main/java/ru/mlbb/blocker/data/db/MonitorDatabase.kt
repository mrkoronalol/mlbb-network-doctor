package ru.mlbb.blocker.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        EndpointEntity::class,
        BlockedAttemptEntity::class,
        PingSampleEntity::class,
        MatchSessionEntity::class,
        RegionIpEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class MonitorDatabase : RoomDatabase() {
    abstract fun monitorDao(): MonitorDao
    abstract fun regionIpDao(): RegionIpDao

    companion object {
        @Volatile
        private var instance: MonitorDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `region_ip_ranges` (" +
                        "`key` TEXT NOT NULL, `region` TEXT NOT NULL, `cidr` TEXT NOT NULL, " +
                        "`label` TEXT NOT NULL, PRIMARY KEY(`key`))",
                )
            }
        }

        fun get(context: Context): MonitorDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    MonitorDatabase::class.java,
                    "mlbb-monitor.db",
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
