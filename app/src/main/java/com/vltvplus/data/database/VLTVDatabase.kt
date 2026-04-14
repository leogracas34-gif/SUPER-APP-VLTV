package com.vltvplus.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.vltvplus.data.database.dao.*
import com.vltvplus.data.database.entities.*

@Database(
    entities = [
        ChannelEntity::class,
        MovieEntity::class,
        SeriesEntity::class,
        EpisodeEntity::class,
        CategoryEntity::class,
        WatchProgressEntity::class,
        FavoriteEntity::class,
        UserSessionEntity::class,
        EpgCacheEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class VLTVDatabase : RoomDatabase() {

    abstract fun channelDao(): ChannelDao
    abstract fun movieDao(): MovieDao
    abstract fun seriesDao(): SeriesDao
    abstract fun episodeDao(): EpisodeDao
    abstract fun categoryDao(): CategoryDao
    abstract fun watchProgressDao(): WatchProgressDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun userSessionDao(): UserSessionDao
    abstract fun epgCacheDao(): EpgCacheDao

    companion object {
        const val DATABASE_NAME = "vltv_database"

        fun create(context: Context): VLTVDatabase {
            return Room.databaseBuilder(
                context,
                VLTVDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .setJournalMode(JournalMode.WAL) // Write-Ahead Logging for performance
                .build()
        }
    }
}
