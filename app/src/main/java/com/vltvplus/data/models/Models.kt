package com.vltvplus.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

// ===== XTREAM API MODELS =====

data class XtreamAuthResponse(
    @SerializedName("user_info") val userInfo: UserInfo?,
    @SerializedName("server_info") val serverInfo: ServerInfo?
)

data class UserInfo(
    @SerializedName("username") val username: String?,
    @SerializedName("password") val password: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("exp_date") val expDate: String?,
    @SerializedName("is_trial") val isTrial: String?,
    @SerializedName("active_cons") val activeCons: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("max_connections") val maxConnections: String?,
    @SerializedName("allowed_output_formats") val allowedFormats: List<String>?
)

data class ServerInfo(
    @SerializedName("url") val url: String?,
    @SerializedName("port") val port: String?,
    @SerializedName("https_port") val httpsPort: String?,
    @SerializedName("server_protocol") val serverProtocol: String?,
    @SerializedName("rtmp_port") val rtmpPort: String?,
    @SerializedName("timezone") val timezone: String?,
    @SerializedName("timestamp_now") val timestampNow: Long?,
    @SerializedName("time_now") val timeNow: String?
)

data class Category(
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("category_name") val categoryName: String,
    @SerializedName("parent_id") val parentId: Int = 0
)

@Parcelize
data class LiveStream(
    @SerializedName("num") val num: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("stream_type") val streamType: String?,
    @SerializedName("stream_id") val streamId: Int?,
    @SerializedName("stream_icon") val streamIcon: String?,
    @SerializedName("epg_channel_id") val epgChannelId: String?,
    @SerializedName("added") val added: String?,
    @SerializedName("category_id") val categoryId: String?,
    @SerializedName("custom_sid") val customSid: String?,
    @SerializedName("tv_archive") val tvArchive: Int?,
    @SerializedName("direct_source") val directSource: String?,
    @SerializedName("tv_archive_duration") val tvArchiveDuration: Int?
) : Parcelable

@Parcelize
data class VodStream(
    @SerializedName("num") val num: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("stream_type") val streamType: String?,
    @SerializedName("stream_id") val streamId: Int?,
    @SerializedName("stream_icon") val streamIcon: String?,
    @SerializedName("rating") val rating: String?,
    @SerializedName("rating_5based") val rating5based: Double?,
    @SerializedName("added") val added: String?,
    @SerializedName("category_id") val categoryId: String?,
    @SerializedName("container_extension") val containerExtension: String?,
    @SerializedName("custom_sid") val customSid: String?,
    @SerializedName("direct_source") val directSource: String?,
    // TMDB enriched
    var tmdbId: Int? = null,
    var posterPath: String? = null,
    var backdropPath: String? = null,
    var overview: String? = null,
    var releaseDate: String? = null,
    var genres: String? = null,
    var voteAverage: Double? = null
) : Parcelable

data class VodInfo(
    @SerializedName("info") val info: VodInfoDetail?,
    @SerializedName("movie_data") val movieData: VodStream?
)

data class VodInfoDetail(
    @SerializedName("kinopoisk_url") val kinopoiskUrl: String?,
    @SerializedName("tmdb_id") val tmdbId: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("o_name") val originalName: String?,
    @SerializedName("cover_big") val coverBig: String?,
    @SerializedName("movie_image") val movieImage: String?,
    @SerializedName("releasedate") val releaseDate: String?,
    @SerializedName("episode_run_time") val episodeRunTime: String?,
    @SerializedName("youtube_trailer") val youtubeTrailer: String?,
    @SerializedName("director") val director: String?,
    @SerializedName("actors") val actors: String?,
    @SerializedName("cast") val cast: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("plot") val plot: String?,
    @SerializedName("age") val age: String?,
    @SerializedName("mpaa_rating") val mpaaRating: String?,
    @SerializedName("rating_count_kinopoisk") val ratingCountKinopoisk: Int?,
    @SerializedName("country") val country: String?,
    @SerializedName("genre") val genre: String?,
    @SerializedName("backdrop_path") val backdropPath: List<String>?,
    @SerializedName("duration_secs") val durationSecs: Int?,
    @SerializedName("duration") val duration: String?,
    @SerializedName("video") val video: Map<String, Any>?,
    @SerializedName("audio") val audio: Map<String, Any>?,
    @SerializedName("bitrate") val bitrate: Int?,
    @SerializedName("rating") val rating: Double?
)

@Parcelize
data class SeriesStream(
    @SerializedName("num") val num: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("series_id") val seriesId: Int?,
    @SerializedName("cover") val cover: String?,
    @SerializedName("plot") val plot: String?,
    @SerializedName("cast") val cast: String?,
    @SerializedName("director") val director: String?,
    @SerializedName("genre") val genre: String?,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("last_modified") val lastModified: String?,
    @SerializedName("rating") val rating: String?,
    @SerializedName("rating_5based") val rating5based: Double?,
    @SerializedName("backdrop_path") val backdropPath: List<String>?,
    @SerializedName("youtube_trailer") val youtubeTrailer: String?,
    @SerializedName("episode_run_time") val episodeRunTime: String?,
    @SerializedName("category_id") val categoryId: String?,
    // TMDB enriched
    var tmdbId: Int? = null,
    var posterPath: String? = null,
    var tmdbBackdropPath: String? = null,
    var overview: String? = null,
    var voteAverage: Double? = null
) : Parcelable

data class SeriesInfo(
    @SerializedName("info") val info: SeriesInfoDetail?,
    @SerializedName("episodes") val episodes: Map<String, List<Episode>>?,
    @SerializedName("seasons") val seasons: List<SeasonInfo>?
)

data class SeriesInfoDetail(
    @SerializedName("name") val name: String?,
    @SerializedName("cover") val cover: String?,
    @SerializedName("plot") val plot: String?,
    @SerializedName("cast") val cast: String?,
    @SerializedName("director") val director: String?,
    @SerializedName("genre") val genre: String?,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("last_modified") val lastModified: String?,
    @SerializedName("rating") val rating: String?,
    @SerializedName("rating_5based") val rating5based: Double?,
    @SerializedName("backdrop_path") val backdropPath: List<String>?,
    @SerializedName("youtube_trailer") val youtubeTrailer: String?,
    @SerializedName("episode_run_time") val episodeRunTime: String?,
    @SerializedName("category_id") val categoryId: String?,
    @SerializedName("tmdb_id") val tmdbId: String?
)

@Parcelize
data class Episode(
    @SerializedName("id") val id: String?,
    @SerializedName("episode_num") val episodeNum: Int?,
    @SerializedName("title") val title: String?,
    @SerializedName("container_extension") val containerExtension: String?,
    @SerializedName("info") val info: EpisodeInfo?,
    @SerializedName("custom_sid") val customSid: String?,
    @SerializedName("added") val added: String?,
    @SerializedName("season") val season: Int?,
    @SerializedName("direct_source") val directSource: String?
) : Parcelable

@Parcelize
data class EpisodeInfo(
    @SerializedName("tmdb_id") val tmdbId: String?,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("plot") val plot: String?,
    @SerializedName("duration_secs") val durationSecs: Int?,
    @SerializedName("duration") val duration: String?,
    @SerializedName("movie_image") val movieImage: String?,
    @SerializedName("bitrate") val bitrate: Int?,
    @SerializedName("rating") val rating: Double?,
    @SerializedName("season") val season: Int?,
    @SerializedName("cover_big") val coverBig: String?
) : Parcelable

data class SeasonInfo(
    @SerializedName("air_date") val airDate: String?,
    @SerializedName("episode_count") val episodeCount: Int?,
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("overview") val overview: String?,
    @SerializedName("season_number") val seasonNumber: Int?,
    @SerializedName("cover") val cover: String?,
    @SerializedName("cover_tmdb") val coverTmdb: String?
)

// EPG Models
data class EpgResponse(
    @SerializedName("epg_listings") val epgListings: List<EpgListing>?
)

data class EpgListing(
    @SerializedName("id") val id: String?,
    @SerializedName("epg_id") val epgId: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("lang") val lang: String?,
    @SerializedName("start") val start: String?,
    @SerializedName("end") val end: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("channel_id") val channelId: String?,
    @SerializedName("start_timestamp") val startTimestamp: Long?,
    @SerializedName("stop_timestamp") val stopTimestamp: Long?,
    @SerializedName("now_playing") val nowPlaying: Int?,
    @SerializedName("has_archive") val hasArchive: Int?
) {
    fun decodedTitle(): String = try {
        String(android.util.Base64.decode(title ?: "", android.util.Base64.DEFAULT))
    } catch (e: Exception) { title ?: "" }

    fun decodedDescription(): String = try {
        String(android.util.Base64.decode(description ?: "", android.util.Base64.DEFAULT))
    } catch (e: Exception) { description ?: "" }
}

// ===== TMDB MODELS =====

data class TmdbSearchResponse<T>(
    @SerializedName("page") val page: Int,
    @SerializedName("results") val results: List<T>,
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("total_results") val totalResults: Int
)

data class TmdbMovie(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("original_title") val originalTitle: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("overview") val overview: String?,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("vote_average") val voteAverage: Double?,
    @SerializedName("genre_ids") val genreIds: List<Int>?
)

data class TmdbTvShow(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("original_name") val originalName: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("overview") val overview: String?,
    @SerializedName("first_air_date") val firstAirDate: String?,
    @SerializedName("vote_average") val voteAverage: Double?,
    @SerializedName("genre_ids") val genreIds: List<Int>?
)

data class TmdbTrending(
    @SerializedName("id") val id: Int,
    @SerializedName("media_type") val mediaType: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("overview") val overview: String?,
    @SerializedName("vote_average") val voteAverage: Double?
)

data class TmdbMovieDetail(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("original_title") val originalTitle: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("overview") val overview: String?,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("runtime") val runtime: Int?,
    @SerializedName("vote_average") val voteAverage: Double?,
    @SerializedName("genres") val genres: List<TmdbGenre>?,
    @SerializedName("production_countries") val productionCountries: List<TmdbCountry>?,
    @SerializedName("videos") val videos: TmdbVideosResponse?,
    @SerializedName("credits") val credits: TmdbCredits?,
    @SerializedName("similar") val similar: TmdbSearchResponse<TmdbMovie>?,
    @SerializedName("images") val images: TmdbImages?,
    @SerializedName("tagline") val tagline: String?,
    @SerializedName("status") val status: String?
)

data class TmdbTvDetail(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("original_name") val originalName: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("overview") val overview: String?,
    @SerializedName("first_air_date") val firstAirDate: String?,
    @SerializedName("last_air_date") val lastAirDate: String?,
    @SerializedName("number_of_seasons") val numberOfSeasons: Int?,
    @SerializedName("number_of_episodes") val numberOfEpisodes: Int?,
    @SerializedName("vote_average") val voteAverage: Double?,
    @SerializedName("genres") val genres: List<TmdbGenre>?,
    @SerializedName("videos") val videos: TmdbVideosResponse?,
    @SerializedName("credits") val credits: TmdbCredits?,
    @SerializedName("similar") val similar: TmdbSearchResponse<TmdbTvShow>?,
    @SerializedName("images") val images: TmdbImages?,
    @SerializedName("seasons") val seasons: List<TmdbSeasonSummary>?,
    @SerializedName("tagline") val tagline: String?,
    @SerializedName("status") val status: String?
)

data class TmdbSeason(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("overview") val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("season_number") val seasonNumber: Int?,
    @SerializedName("episodes") val episodes: List<TmdbEpisode>?
)

data class TmdbEpisode(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("overview") val overview: String?,
    @SerializedName("still_path") val stillPath: String?,
    @SerializedName("episode_number") val episodeNumber: Int?,
    @SerializedName("season_number") val seasonNumber: Int?,
    @SerializedName("air_date") val airDate: String?,
    @SerializedName("vote_average") val voteAverage: Double?,
    @SerializedName("runtime") val runtime: Int?
)

data class TmdbSeasonSummary(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("season_number") val seasonNumber: Int?,
    @SerializedName("episode_count") val episodeCount: Int?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("air_date") val airDate: String?
)

data class TmdbGenre(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String?
)

data class TmdbCountry(
    @SerializedName("iso_3166_1") val iso: String?,
    @SerializedName("name") val name: String?
)

data class TmdbVideosResponse(
    @SerializedName("results") val results: List<TmdbVideo>?
)

data class TmdbVideo(
    @SerializedName("id") val id: String?,
    @SerializedName("key") val key: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("site") val site: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("official") val official: Boolean?
) {
    fun getYoutubeUrl(): String? = if (site == "YouTube") "https://www.youtube.com/watch?v=$key" else null
    fun getThumbnailUrl(): String? = if (site == "YouTube") "https://img.youtube.com/vi/$key/maxresdefault.jpg" else null
}

data class TmdbCredits(
    @SerializedName("cast") val cast: List<TmdbCastMember>?,
    @SerializedName("crew") val crew: List<TmdbCrewMember>?
)

data class TmdbCastMember(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("character") val character: String?,
    @SerializedName("profile_path") val profilePath: String?,
    @SerializedName("order") val order: Int?
)

data class TmdbCrewMember(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("job") val job: String?,
    @SerializedName("department") val department: String?,
    @SerializedName("profile_path") val profilePath: String?
)

data class TmdbImages(
    @SerializedName("backdrops") val backdrops: List<TmdbImage>?,
    @SerializedName("posters") val posters: List<TmdbImage>?
)

data class TmdbImage(
    @SerializedName("file_path") val filePath: String?,
    @SerializedName("width") val width: Int?,
    @SerializedName("height") val height: Int?,
    @SerializedName("vote_average") val voteAverage: Double?
)

// ===== LOCAL MODELS =====

data class WatchProgress(
    val contentId: String,
    val contentType: String, // "movie", "series", "live"
    val position: Long,
    val duration: Long,
    val episodeId: String? = null,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null
)

enum class ContentType {
    LIVE, MOVIE, SERIES
}
