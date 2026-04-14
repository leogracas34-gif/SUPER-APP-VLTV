package com.vltvplus.ui.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vltvplus.data.database.entities.EpisodeEntity
import com.vltvplus.data.database.entities.WatchProgressEntity
import com.vltvplus.data.repository.VLTVRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val repository: VLTVRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _streamUrl = MutableStateFlow<String?>(null)
    val streamUrl: StateFlow<String?> = _streamUrl

    private val _nextEpisodeEvent = MutableSharedFlow<EpisodeEntity?>(replay = 0)
    val nextEpisodeEvent: SharedFlow<EpisodeEntity?> = _nextEpisodeEvent

    private var type: String = "live"
    private var streamId: Int = -1
    private var episodeId: String? = null
    var seriesId: Int = -1
    private var season: Int = 1
    private var episodeNum: Int = 1
    private var ext: String = "ts"
    private var contentTitle: String = ""

    private var allEpisodes: List<EpisodeEntity> = emptyList()
    private var currentEpisodeIndex: Int = -1

    fun setup(type: String, streamId: Int, episodeId: String?, ext: String,
              seriesId: Int, season: Int, episodeNum: Int) {
        this.type = type
        this.streamId = streamId
        this.episodeId = episodeId
        this.ext = ext
        this.seriesId = seriesId
        this.season = season
        this.episodeNum = episodeNum

        viewModelScope.launch {
            val url = when (type) {
                PlayerActivity.TYPE_LIVE -> repository.buildLiveUrl(streamId)
                PlayerActivity.TYPE_MOVIE -> repository.buildMovieUrl(streamId, ext)
                PlayerActivity.TYPE_EPISODE -> repository.buildEpisodeUrl(episodeId ?: "", ext)
                else -> null
            }
            _streamUrl.value = url

            // Pre-load episode list for next/prev navigation
            if (type == PlayerActivity.TYPE_EPISODE && seriesId != -1) {
                repository.getEpisodesBySeriesId(seriesId).collect { eps ->
                    allEpisodes = eps.sortedWith(compareBy({ it.season }, { it.episodeNum }))
                    currentEpisodeIndex = allEpisodes.indexOfFirst { it.episodeId == episodeId }
                }
            }
        }
    }

    fun hasNextEpisode(): Boolean = type == PlayerActivity.TYPE_EPISODE &&
            currentEpisodeIndex >= 0 && currentEpisodeIndex < allEpisodes.size - 1

    fun hasPrevEpisode(): Boolean = type == PlayerActivity.TYPE_EPISODE &&
            currentEpisodeIndex > 0

    fun playNextEpisode() {
        if (!hasNextEpisode()) return
        val next = allEpisodes[currentEpisodeIndex + 1]
        viewModelScope.launch { _nextEpisodeEvent.emit(next) }
    }

    fun playPrevEpisode() {
        if (!hasPrevEpisode()) return
        val prev = allEpisodes[currentEpisodeIndex - 1]
        viewModelScope.launch { _nextEpisodeEvent.emit(prev) }
    }

    fun onPlaybackEnded() {
        if (type == PlayerActivity.TYPE_EPISODE) {
            playNextEpisode()
        }
    }

    fun saveProgress(position: Long, duration: Long) {
        if (position <= 0) return
        viewModelScope.launch {
            val contentId = when (type) {
                PlayerActivity.TYPE_MOVIE -> streamId.toString()
                PlayerActivity.TYPE_EPISODE -> episodeId ?: return@launch
                else -> return@launch
            }
            repository.saveProgress(
                WatchProgressEntity(
                    contentId = contentId,
                    contentType = type,
                    position = position,
                    duration = duration,
                    episodeId = episodeId,
                    seasonNumber = if (type == PlayerActivity.TYPE_EPISODE) season else null,
                    episodeNumber = if (type == PlayerActivity.TYPE_EPISODE) episodeNum else null,
                    seriesId = if (type == PlayerActivity.TYPE_EPISODE) seriesId else null
                )
            )
        }
    }
}
