package com.vltvplus.data.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "channels", indices = [Index("categoryId"), Index("streamId")])
data class ChannelEntity(
    @PrimaryKey val streamId: Int,
    val name: String,
    val streamIcon: String?,
    val epgChannelId: String?,
    val categoryId: String?,
    val categoryName: String?,
    val num: Int,
    val tvArchive: Int,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "movies", indices = [Index("categoryId"), Index("streamId"), Index("tmdbId")])
data class MovieEntity(
    @PrimaryKey val streamId: Int,
    val name: String,
    val streamIcon: String?,
    val categoryId: String?,
    val categoryName: String?,
    val rating: Double?,
    val containerExtension: String?,
    val addedDate: String?,
    val num: Int,
    // TMDB enriched
    val tmdbId: Int?,
    val posterPath: String?,
    val backdropPath: String?,
    val overview: String?,
    val releaseDate: String?,
    val genres: String?,
    val voteAverage: Double?,
    val tagline: String?,
    val runtime: Int?,
    val trailerKey: String?,
    val cast: String?,
    val director: String?,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "series", indices = [Index("categoryId"), Index("seriesId"), Index("tmdbId")])
data class SeriesEntity(
    @PrimaryKey val seriesId: Int,
    val name: String,
    val cover: String?,
    val categoryId: String?,
    val categoryName: String?,
    val rating: Double?,
    val plot: String?,
    val cast: String?,
    val director: String?,
    val genre: String?,
    val releaseDate: String?,
    val episodeRunTime: String?,
    val youtubeTrailer: String?,
    val backdropPath: String?,
    // TMDB enriched
    val tmdbId: Int?,
    val posterPath: String?,
    val tmdbBackdropPath: String?,
    val overview: String?,
    val voteAverage: Double?,
    val numberOfSeasons: Int?,
    val numberOfEpisodes: Int?,
    val trailerKey: String?,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "episodes",
    indices = [Index("seriesId"), Index("season")]
)
data class EpisodeEntity(
    @PrimaryKey val episodeId: String,
    val seriesId: Int,
    val episodeNum: Int,
    val title: String?,
    val containerExtension: String?,
    val plot: String?,
    val durationSecs: Int?,
    val duration: String?,
    val movieImage: String?,
    val rating: Double?,
    val season: Int,
    val releaseDate: String?,
    val coverBig: String?,
    // TMDB
    val tmdbId: String?,
    val stillPath: String?,
    val tmdbOverview: String?,
    val tmdbRuntime: Int?
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val categoryId: String,
    val categoryName: String,
    val type: String // "live", "movie", "series"
)

@Entity(tableName = "watch_progress", indices = [Index("contentId")])
data class WatchProgressEntity(
    @PrimaryKey val contentId: String, // streamId or episodeId
    val contentType: String, // "movie", "series", "live"
    val position: Long,
    val duration: Long,
    val title: String?,
    val thumbnailUrl: String?,
    val episodeId: String?,
    val seasonNumber: Int?,
    val episodeNumber: Int?,
    val seriesId: Int?,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val contentId: String,
    val contentType: String, // "movie", "series", "live"
    val title: String,
    val thumbnailUrl: String?,
    val categoryId: String?,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_session")
data class UserSessionEntity(
    @PrimaryKey val id: Int = 1,
    val username: String,
    val password: String,
    val activeDns: String,
    val baseUrl: String,
    val status: String?,
    val expDate: String?,
    val maxConnections: Int?,
    val allowedFormats: String?,
    val lastSyncAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "epg_cache", indices = [Index("channelId")])
data class EpgCacheEntity(
    @PrimaryKey val id: String,
    val channelId: String,
    val title: String?,
    val description: String?,
    val startTimestamp: Long?,
    val stopTimestamp: Long?,
    val nowPlaying: Boolean,
    val cachedAt: Long = System.currentTimeMillis()
)
