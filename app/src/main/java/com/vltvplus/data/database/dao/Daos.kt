package com.vltvplus.data.database.dao

import androidx.room.*
import com.vltvplus.data.database.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels ORDER BY num ASC")
    fun getAllChannels(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE categoryId = :categoryId ORDER BY num ASC")
    fun getChannelsByCategory(categoryId: String): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE name LIKE '%' || :query || '%' ORDER BY num ASC")
    suspend fun searchChannels(query: String): List<ChannelEntity>

    @Query("SELECT * FROM channels WHERE streamId = :streamId LIMIT 1")
    suspend fun getChannelById(streamId: Int): ChannelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(channels: List<ChannelEntity>)

    @Query("DELETE FROM channels")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM channels")
    suspend fun count(): Int
}

@Dao
interface MovieDao {
    @Query("SELECT * FROM movies ORDER BY num ASC")
    fun getAllMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE categoryId = :categoryId ORDER BY num ASC")
    fun getMoviesByCategory(categoryId: String): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE name LIKE '%' || :query || '%' OR overview LIKE '%' || :query || '%' ORDER BY voteAverage DESC")
    suspend fun searchMovies(query: String): List<MovieEntity>

    @Query("SELECT * FROM movies WHERE streamId = :streamId LIMIT 1")
    suspend fun getMovieById(streamId: Int): MovieEntity?

    @Query("SELECT * FROM movies ORDER BY addedDate DESC LIMIT :limit")
    fun getRecentMovies(limit: Int = 20): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies ORDER BY voteAverage DESC LIMIT :limit")
    fun getTopRatedMovies(limit: Int = 20): Flow<List<MovieEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(movies: List<MovieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movie: MovieEntity)

    @Update
    suspend fun update(movie: MovieEntity)

    @Query("DELETE FROM movies")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM movies")
    suspend fun count(): Int
}

@Dao
interface SeriesDao {
    @Query("SELECT * FROM series ORDER BY name ASC")
    fun getAllSeries(): Flow<List<SeriesEntity>>

    @Query("SELECT * FROM series WHERE categoryId = :categoryId ORDER BY name ASC")
    fun getSeriesByCategory(categoryId: String): Flow<List<SeriesEntity>>

    @Query("SELECT * FROM series WHERE name LIKE '%' || :query || '%' OR overview LIKE '%' || :query || '%' ORDER BY voteAverage DESC")
    suspend fun searchSeries(query: String): List<SeriesEntity>

    @Query("SELECT * FROM series WHERE seriesId = :seriesId LIMIT 1")
    suspend fun getSeriesById(seriesId: Int): SeriesEntity?

    @Query("SELECT * FROM series ORDER BY addedAt DESC LIMIT :limit")
    fun getRecentSeries(limit: Int = 20): Flow<List<SeriesEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(series: List<SeriesEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(series: SeriesEntity)

    @Update
    suspend fun update(series: SeriesEntity)

    @Query("DELETE FROM series")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM series")
    suspend fun count(): Int
}

@Dao
interface EpisodeDao {
    @Query("SELECT * FROM episodes WHERE seriesId = :seriesId ORDER BY season ASC, episodeNum ASC")
    fun getEpisodesBySeriesId(seriesId: Int): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes WHERE seriesId = :seriesId AND season = :season ORDER BY episodeNum ASC")
    suspend fun getEpisodesBySeason(seriesId: Int, season: Int): List<EpisodeEntity>

    @Query("SELECT * FROM episodes WHERE episodeId = :episodeId LIMIT 1")
    suspend fun getEpisodeById(episodeId: String): EpisodeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(episodes: List<EpisodeEntity>)

    @Query("DELETE FROM episodes WHERE seriesId = :seriesId")
    suspend fun deleteBySeriesId(seriesId: Int)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE type = :type ORDER BY categoryName ASC")
    fun getCategoriesByType(type: String): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories WHERE type = :type")
    suspend fun deleteByType(type: String)
}

@Dao
interface WatchProgressDao {
    @Query("SELECT * FROM watch_progress ORDER BY updatedAt DESC LIMIT :limit")
    fun getRecentProgress(limit: Int = 20): Flow<List<WatchProgressEntity>>

    @Query("SELECT * FROM watch_progress WHERE contentId = :contentId LIMIT 1")
    suspend fun getProgressById(contentId: String): WatchProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: WatchProgressEntity)

    @Query("DELETE FROM watch_progress WHERE contentId = :contentId")
    suspend fun delete(contentId: String)

    @Query("UPDATE watch_progress SET position = :position, updatedAt = :updatedAt WHERE contentId = :contentId")
    suspend fun updatePosition(contentId: String, position: Long, updatedAt: Long = System.currentTimeMillis())
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE contentType = :type ORDER BY addedAt DESC")
    fun getFavoritesByType(type: String): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE contentId = :contentId)")
    fun isFavorite(contentId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE contentId = :contentId")
    suspend fun delete(contentId: String)
}

@Dao
interface UserSessionDao {
    @Query("SELECT * FROM user_session WHERE id = 1 LIMIT 1")
    suspend fun getSession(): UserSessionEntity?

    @Query("SELECT * FROM user_session WHERE id = 1 LIMIT 1")
    fun getSessionFlow(): Flow<UserSessionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(session: UserSessionEntity)

    @Query("DELETE FROM user_session")
    suspend fun clear()
}

@Dao
interface EpgCacheDao {
    @Query("SELECT * FROM epg_cache WHERE channelId = :channelId AND stopTimestamp > :now ORDER BY startTimestamp ASC LIMIT :limit")
    suspend fun getUpcomingEpg(channelId: String, now: Long = System.currentTimeMillis() / 1000, limit: Int = 5): List<EpgCacheEntity>

    @Query("SELECT * FROM epg_cache WHERE channelId = :channelId AND nowPlaying = 1 LIMIT 1")
    suspend fun getCurrentProgram(channelId: String): EpgCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(epg: List<EpgCacheEntity>)

    @Query("DELETE FROM epg_cache WHERE cachedAt < :threshold")
    suspend fun deleteOld(threshold: Long = System.currentTimeMillis() - 86_400_000)
}
