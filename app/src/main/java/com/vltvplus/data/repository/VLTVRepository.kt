package com.vltvplus.data.repository

import com.vltvplus.BuildConfig
import com.vltvplus.data.api.DnsManager
import com.vltvplus.data.api.DnsResult
import com.vltvplus.data.api.TmdbApiService
import com.vltvplus.data.api.XtreamApiService
import com.vltvplus.data.database.dao.*
import com.vltvplus.data.database.entities.*
import com.vltvplus.data.models.*
import com.vltvplus.utils.PreferenceManager
import com.vltvplus.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

@Singleton
class VLTVRepository @Inject constructor(
    private val xtreamApi: XtreamApiService,
    private val tmdbApi: TmdbApiService,
    private val dnsManager: DnsManager,
    private val channelDao: ChannelDao,
    private val movieDao: MovieDao,
    private val seriesDao: SeriesDao,
    private val episodeDao: EpisodeDao,
    private val categoryDao: CategoryDao,
    private val watchProgressDao: WatchProgressDao,
    private val favoriteDao: FavoriteDao,
    private val userSessionDao: UserSessionDao,
    private val epgCacheDao: EpgCacheDao,
    private val preferenceManager: PreferenceManager
) {
    companion object {
        const val TMDB_API_KEY = BuildConfig.TMDB_API_KEY
        const val TMDB_IMAGE_W500 = "https://image.tmdb.org/t/p/w500"
        const val TMDB_IMAGE_ORIGINAL = "https://image.tmdb.org/t/p/original"
        const val SYNC_INTERVAL_MS = 3 * 60 * 60 * 1000L // 3 horas
        private const val TAG = "VLTVRepository"
    }

    // ===== AUTHENTICATION =====

    suspend fun login(username: String, password: String): Resource<UserInfo> =
        withContext(Dispatchers.IO) {
            try {
                // Aumentamos a tolerância para servidores lentos no DnsManager
                val dnsResult = dnsManager.findFastestDnsForCredentials(username, password)
                when (dnsResult) {
                    is DnsResult.Error -> Resource.Error("Servidor não respondeu a tempo. Tente novamente.")
                    is DnsResult.Success -> {
                        val authUrl = dnsManager.buildAuthUrl(dnsResult.dns, username, password)
                        val response = xtreamApi.authenticate(authUrl)
                        if (response.isSuccessful && response.body() != null) {
                            val body = response.body()!!
                            val userInfo = body.userInfo
                            if (userInfo?.status == "Active" || userInfo?.status == "active") {
                                preferenceManager.setCredentials(username, password)
                                preferenceManager.setActiveDns(dnsResult.dns)
                                preferenceManager.setBaseUrl(dnsResult.baseUrl)

                                userSessionDao.upsert(
                                    UserSessionEntity(
                                        username = username,
                                        password = password,
                                        activeDns = dnsResult.dns,
                                        baseUrl = dnsResult.baseUrl,
                                        status = userInfo.status,
                                        expDate = userInfo.expDate,
                                        maxConnections = userInfo.maxConnections?.toIntOrNull(),
                                        allowedFormats = userInfo.allowedFormats?.joinToString(",")
                                    )
                                )
                                Resource.Success(userInfo)
                            } else {
                                Resource.Error("Conta inativa ou expirada.")
                            }
                        } else {
                            Resource.Error("Credenciais inválidas ou erro no servidor.")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Login error", e)
                Resource.Error("Erro de conexão: Verifique se o servidor está online.")
            }
        }

    suspend fun logout() = withContext(Dispatchers.IO) {
        try {
            preferenceManager.clearSession()
            userSessionDao.clear()
        } catch (e: Exception) {
            Log.e(TAG, "Logout error", e)
        }
    }

    // ===== SYNC =====

    suspend fun syncAll(forceRefresh: Boolean = false): Resource<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val username = preferenceManager.getUsername() ?: return@withContext Resource.Error("Não autenticado")
                val password = preferenceManager.getPassword() ?: return@withContext Resource.Error("Não autenticado")
                val dns = preferenceManager.getActiveDns() ?: return@withContext Resource.Error("Sem DNS ativo")

                val lastSync = preferenceManager.getLastSync()
                val now = System.currentTimeMillis()
                
                if (!forceRefresh && (now - lastSync) < SYNC_INTERVAL_MS && movieDao.count() > 0) {
                    return@withContext Resource.Success(Unit)
                }

                coroutineScope {
                    // Proteção individual: Se um falhar (ex: VOD vazio), o app NÃO FECHA.
                    val liveJob = async { 
                        try { syncLiveChannels(dns, username, password) } 
                        catch (e: Exception) { Log.e(TAG, "Live sync fail", e) } 
                    }
                    val moviesJob = async { 
                        try { syncMovies(dns, username, password) } 
                        catch (e: Exception) { Log.e(TAG, "Movies sync fail", e) } 
                    }
                    val seriesJob = async { 
                        try { syncSeries(dns, username, password) } 
                        catch (e: Exception) { Log.e(TAG, "Series sync fail", e) } 
                    }
                    
                    liveJob.await()
                    moviesJob.await()
                    seriesJob.await()
                }

                preferenceManager.setLastSync(now)
                Resource.Success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "SyncAll fatal error", e)
                // Retornamos sucesso mesmo com erro parcial para permitir que o usuário use o que já foi baixado
                Resource.Success(Unit) 
            }
        }

    private suspend fun syncLiveChannels(dns: String, username: String, password: String) {
        val categoriesUrl = "$dns:${DnsManager.DEFAULT_PORT}/player_api.php?username=$username&password=$password&action=get_live_categories"
        val streamsUrl = "$dns:${DnsManager.DEFAULT_PORT}/player_api.php?username=$username&password=$password&action=get_live_streams"

        val categoriesResp = xtreamApi.getLiveCategories(categoriesUrl)
        if (categoriesResp.isSuccessful) {
            val categories = categoriesResp.body() ?: return
            val catEntities = categories.map { CategoryEntity(it.categoryId, it.categoryName, "live") }
            
            categoryDao.deleteByType("live")
            categoryDao.insertAll(catEntities)

            val catMap = categories.associate { it.categoryId to it.categoryName }
            val streamsResp = xtreamApi.getLiveStreams(streamsUrl)

            if (streamsResp.isSuccessful) {
                val streams = streamsResp.body() ?: return
                val channelEntities = streams.map { s ->
                    ChannelEntity(
                        streamId = s.streamId ?: 0,
                        name = s.name ?: "",
                        streamIcon = s.streamIcon,
                        epgChannelId = s.epgChannelId,
                        categoryId = s.categoryId,
                        categoryName = catMap[s.categoryId],
                        num = s.num ?: 0,
                        tvArchive = s.tvArchive ?: 0
                    )
                }
                channelDao.deleteAll()
                channelDao.insertAll(channelEntities)
            }
        }
    }

    private suspend fun syncMovies(dns: String, username: String, password: String) {
        val categoriesUrl = "$dns:${DnsManager.DEFAULT_PORT}/player_api.php?username=$username&password=$password&action=get_vod_categories"
        val streamsUrl = "$dns:${DnsManager.DEFAULT_PORT}/player_api.php?username=$username&password=$password&action=get_vod_streams"

        val categoriesResp = xtreamApi.getVodCategories(categoriesUrl)
        if (categoriesResp.isSuccessful) {
            val categories = categoriesResp.body() ?: return
            val catEntities = categories.map { CategoryEntity(it.categoryId, it.categoryName, "movie") }
            
            categoryDao.deleteByType("movie")
            categoryDao.insertAll(catEntities)
            
            val catMap = categories.associate { it.categoryId to it.categoryName }
            val streamsResp = xtreamApi.getVodStreams(streamsUrl)

            if (streamsResp.isSuccessful) {
                val streams = streamsResp.body() ?: return
                val entities = streams.map { s ->
                    MovieEntity(
                        streamId = s.streamId ?: 0,
                        name = s.name ?: "",
                        streamIcon = s.streamIcon,
                        categoryId = s.categoryId,
                        categoryName = catMap[s.categoryId],
                        rating = s.rating?.toDoubleOrNull(),
                        containerExtension = s.containerExtension,
                        addedDate = s.added,
                        num = s.num ?: 0,
                        tmdbId = null, posterPath = null, backdropPath = null,
                        overview = null, releaseDate = null, genres = null,
                        voteAverage = null, tagline = null, runtime = null,
                        trailerKey = null, cast = null, director = null
                    )
                }
                movieDao.deleteAll()
                movieDao.insertAll(entities)
            }
        }
    }

    private suspend fun syncSeries(dns: String, username: String, password: String) {
        val categoriesUrl = "$dns:${DnsManager.DEFAULT_PORT}/player_api.php?username=$username&password=$password&action=get_series_categories"
        val streamsUrl = "$dns:${DnsManager.DEFAULT_PORT}/player_api.php?username=$username&password=$password&action=get_series"

        val categoriesResp = xtreamApi.getSeriesCategories(categoriesUrl)
        if (categoriesResp.isSuccessful) {
            val categories = categoriesResp.body() ?: return
            val catEntities = categories.map { CategoryEntity(it.categoryId, it.categoryName, "series") }
            
            categoryDao.deleteByType("series")
            categoryDao.insertAll(catEntities)
            
            val catMap = categories.associate { it.categoryId to it.categoryName }
            val streamsResp = xtreamApi.getSeriesList(streamsUrl)

            if (streamsResp.isSuccessful) {
                val streams = streamsResp.body() ?: return
                val entities = streams.map { s ->
                    SeriesEntity(
                        seriesId = s.seriesId ?: 0,
                        name = s.name ?: "",
                        cover = s.cover,
                        categoryId = s.categoryId,
                        categoryName = catMap[s.categoryId],
                        rating = s.rating?.toDoubleOrNull(),
                        plot = s.plot,
                        cast = s.cast,
                        director = s.director,
                        genre = s.genre,
                        releaseDate = s.releaseDate,
                        episodeRunTime = s.episodeRunTime,
                        youtubeTrailer = s.youtubeTrailer,
                        backdropPath = s.backdropPath?.firstOrNull(),
                        tmdbId = null, posterPath = null, tmdbBackdropPath = null,
                        overview = null, voteAverage = null,
                        numberOfSeasons = null, numberOfEpisodes = null, trailerKey = null
                    )
                }
                seriesDao.deleteAll()
                seriesDao.insertAll(entities)
            }
        }
    }

    // ===== TMDB ENRICHMENT =====

    suspend fun enrichMovieWithTmdb(movie: MovieEntity): MovieEntity = withContext(Dispatchers.IO) {
        if (movie.tmdbId != null) return@withContext movie
        try {
            val searchResp = tmdbApi.searchMovie(TMDB_API_KEY, movie.name)
            val tmdbMovie = searchResp.body()?.results?.firstOrNull() ?: return@withContext movie
            val detailResp = tmdbApi.getMovieDetail(tmdbMovie.id, TMDB_API_KEY)
            val detail = detailResp.body() ?: return@withContext movie

            val trailer = detail.videos?.results?.firstOrNull { it.type == "Trailer" && it.site == "YouTube" }
            val director = detail.credits?.crew?.firstOrNull { it.job == "Director" }?.name
            val cast = detail.credits?.cast?.take(5)?.joinToString(", ") { it.name ?: "" }

            val enriched = movie.copy(
                tmdbId = detail.id,
                posterPath = detail.posterPath?.let { "$TMDB_IMAGE_W500$it" },
                backdropPath = detail.backdropPath?.let { "$TMDB_IMAGE_ORIGINAL$it" },
                overview = detail.overview,
                releaseDate = detail.releaseDate,
                genres = detail.genres?.joinToString(", ") { it.name ?: "" },
                voteAverage = detail.voteAverage,
                tagline = detail.tagline,
                runtime = detail.runtime,
                trailerKey = trailer?.key,
                cast = cast,
                director = director
            )
            movieDao.update(enriched)
            enriched
        } catch (e: Exception) {
            Log.e(TAG, "TMDB Movie Enrichment error", e)
            movie
        }
    }

    suspend fun enrichSeriesWithTmdb(series: SeriesEntity): SeriesEntity = withContext(Dispatchers.IO) {
        if (series.tmdbId != null) return@withContext series
        try {
            val searchResp = tmdbApi.searchTv(TMDB_API_KEY, series.name)
            val tmdbShow = searchResp.body()?.results?.firstOrNull() ?: return@withContext series
            val detailResp = tmdbApi.getTvDetail(tmdbShow.id, TMDB_API_KEY)
            val detail = detailResp.body() ?: return@withContext series

            val trailer = detail.videos?.results?.firstOrNull { it.type == "Trailer" && it.site == "YouTube" }

            val enriched = series.copy(
                tmdbId = detail.id,
                posterPath = detail.posterPath?.let { "$TMDB_IMAGE_W500$it" },
                tmdbBackdropPath = detail.backdropPath?.let { "$TMDB_IMAGE_ORIGINAL$it" },
                overview = detail.overview,
                voteAverage = detail.voteAverage,
                numberOfSeasons = detail.numberOfSeasons,
                numberOfEpisodes = detail.numberOfEpisodes,
                trailerKey = trailer?.key
            )
            seriesDao.update(enriched)
            enriched
        } catch (e: Exception) {
            Log.e(TAG, "TMDB Series Enrichment error", e)
            series
        }
    }

    // ===== DATA ACCESS =====

    fun getAllChannels(): Flow<List<ChannelEntity>> = channelDao.getAllChannels()
    fun getChannelsByCategory(categoryId: String): Flow<List<ChannelEntity>> = channelDao.getChannelsByCategory(categoryId)
    suspend fun searchChannels(query: String) = channelDao.searchChannels(query)
    suspend fun getChannelById(id: Int) = channelDao.getChannelById(id)

    fun getAllMovies(): Flow<List<MovieEntity>> = movieDao.getAllMovies()
    fun getMoviesByCategory(categoryId: String): Flow<List<MovieEntity>> = movieDao.getMoviesByCategory(categoryId)
    suspend fun searchMovies(query: String) = movieDao.searchMovies(query)
    suspend fun getMovieById(id: Int) = movieDao.getMovieById(id)
    fun getRecentMovies() = movieDao.getRecentMovies()
    fun getTopRatedMovies() = movieDao.getTopRatedMovies()

    fun getAllSeries(): Flow<List<SeriesEntity>> = seriesDao.getAllSeries()
    fun getSeriesByCategory(categoryId: String): Flow<List<SeriesEntity>> = seriesDao.getSeriesByCategory(categoryId)
    suspend fun searchSeries(query: String) = seriesDao.searchSeries(query)
    suspend fun getSeriesById(id: Int) = seriesDao.getSeriesById(id)
    fun getRecentSeries() = seriesDao.getRecentSeries()

    fun getLiveCategories(): Flow<List<CategoryEntity>> = categoryDao.getCategoriesByType("live")
    fun getMovieCategories(): Flow<List<CategoryEntity>> = categoryDao.getCategoriesByType("movie")
    fun getSeriesCategories(): Flow<List<CategoryEntity>> = categoryDao.getCategoriesByType("series")

    fun getEpisodesBySeriesId(seriesId: Int): Flow<List<EpisodeEntity>> = episodeDao.getEpisodesBySeriesId(seriesId)
    suspend fun getEpisodesBySeason(seriesId: Int, season: Int) = episodeDao.getEpisodesBySeason(seriesId, season)

    suspend fun loadAndCacheEpisodes(seriesId: Int) = withContext(Dispatchers.IO) {
        try {
            val username = preferenceManager.getUsername() ?: return@withContext
            val password = preferenceManager.getPassword() ?: return@withContext
            val dns = preferenceManager.getActiveDns() ?: return@withContext
            val url = "$dns:${DnsManager.DEFAULT_PORT}/player_api.php?username=$username&password=$password&action=get_series_info&series_id=$seriesId"
            val resp = xtreamApi.getSeriesInfo(url)
            resp.body()?.episodes?.forEach { (seasonNum, episodes) ->
                val entities = episodes.map { ep ->
                    EpisodeEntity(
                        episodeId = ep.id ?: "${seriesId}_${ep.season}_${ep.episodeNum}",
                        seriesId = seriesId,
                        episodeNum = ep.episodeNum ?: 0,
                        title = ep.title,
                        containerExtension = ep.containerExtension,
                        plot = ep.info?.plot,
                        durationSecs = ep.info?.durationSecs,
                        duration = ep.info?.duration,
                        movieImage = ep.info?.movieImage,
                        rating = ep.info?.rating,
                        season = ep.season ?: seasonNum.toIntOrNull() ?: 1,
                        releaseDate = ep.info?.releaseDate,
                        coverBig = ep.info?.coverBig,
                        tmdbId = ep.info?.tmdbId,
                        stillPath = null, tmdbOverview = null, tmdbRuntime = null
                    )
                }
                episodeDao.insertAll(entities)
            }
        } catch (e: Exception) { 
            Log.e(TAG, "Episodes load fail for series $seriesId", e) 
        }
    }

    fun getRecentProgress() = watchProgressDao.getRecentProgress()
    suspend fun getProgress(contentId: String) = watchProgressDao.getProgressById(contentId)
    suspend fun saveProgress(entity: WatchProgressEntity) = watchProgressDao.upsert(entity)
    suspend fun updateProgress(contentId: String, position: Long) = watchProgressDao.updatePosition(contentId, position)

    fun getAllFavorites() = favoriteDao.getAllFavorites()
    fun isFavorite(contentId: String) = favoriteDao.isFavorite(contentId)
    suspend fun addFavorite(entity: FavoriteEntity) = favoriteDao.insert(entity)
    suspend fun removeFavorite(contentId: String) = favoriteDao.delete(contentId)

    suspend fun getSession() = userSessionDao.getSession()
    fun getSessionFlow() = userSessionDao.getSessionFlow()

    fun buildLiveUrl(streamId: Int): String {
        val dns = preferenceManager.getActiveDns() ?: ""
        val username = preferenceManager.getUsername() ?: ""
        val password = preferenceManager.getPassword() ?: ""
        return dnsManager.buildStreamUrl(dns, username, password, streamId.toString())
    }

    fun buildMovieUrl(streamId: Int, ext: String = "mp4"): String {
        val dns = preferenceManager.getActiveDns() ?: ""
        val username = preferenceManager.getUsername() ?: ""
        val password = preferenceManager.getPassword() ?: ""
        return dnsManager.buildVodUrl(dns, username, password, streamId.toString(), ext)
    }

    fun buildEpisodeUrl(episodeId: String, ext: String = "mp4"): String {
        val dns = preferenceManager.getActiveDns() ?: ""
        val username = preferenceManager.getUsername() ?: ""
        val password = preferenceManager.getPassword() ?: ""
        return dnsManager.buildSeriesUrl(dns, username, password, episodeId, ext)
    }

    suspend fun getUpcomingEpg(channelId: String) = epgCacheDao.getUpcomingEpg(channelId)
    suspend fun getCurrentProgram(channelId: String) = epgCacheDao.getCurrentProgram(channelId)

    suspend fun loadEpg(streamId: Int, channelId: String) = withContext(Dispatchers.IO) {
        try {
            val username = preferenceManager.getUsername() ?: return@withContext
            val password = preferenceManager.getPassword() ?: return@withContext
            val dns = preferenceManager.getActiveDns() ?: return@withContext
            val url = "$dns:${DnsManager.DEFAULT_PORT}/player_api.php?username=$username&password=$password&action=get_short_epg&stream_id=$streamId&limit=5"
            val resp = xtreamApi.getShortEpg(url)
            resp.body()?.epgListings?.let { listings ->
                val entities = listings.map { epg ->
                    EpgCacheEntity(
                        id = epg.id ?: "${channelId}_${epg.startTimestamp}",
                        channelId = channelId,
                        title = epg.decodedTitle(),
                        description = epg.decodedDescription(),
                        startTimestamp = epg.startTimestamp,
                        stopTimestamp = epg.stopTimestamp,
                        nowPlaying = epg.nowPlaying == 1
                    )
                }
                epgCacheDao.insertAll(entities)
            }
        } catch (e: Exception) { 
            Log.e(TAG, "EPG load fail for stream $streamId", e) 
        }
    }
}
