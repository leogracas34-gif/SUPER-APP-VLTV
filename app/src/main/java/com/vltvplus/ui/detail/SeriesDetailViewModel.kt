package com.vltvplus.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vltvplus.data.database.entities.*
import com.vltvplus.data.repository.VLTVRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SeriesDetailViewModel @Inject constructor(
    private val repository: VLTVRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _series = MutableStateFlow<SeriesEntity?>(null)
    val series: StateFlow<SeriesEntity?> = _series

    private val _selectedSeason = MutableStateFlow(1)
    private val _allEpisodes = MutableStateFlow<List<EpisodeEntity>>(emptyList())

    val episodes: StateFlow<List<EpisodeEntity>> = combine(_allEpisodes, _selectedSeason) { eps, season ->
        eps.filter { it.season == season }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isFavorite: StateFlow<Boolean> = _series
        .filterNotNull()
        .flatMapLatest { s -> repository.isFavorite(s.seriesId.toString()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val continueEpisode: StateFlow<WatchProgressEntity?> = _series
        .filterNotNull()
        .flatMapLatest { s ->
            repository.getRecentProgress().map { list ->
                list.firstOrNull { it.seriesId == s.seriesId }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun loadSeries(seriesId: Int) {
        viewModelScope.launch {
            var series = repository.getSeriesById(seriesId) ?: return@launch
            series = repository.enrichSeriesWithTmdb(series)
            _series.value = series

            // Load & cache episodes
            repository.loadAndCacheEpisodes(seriesId)
            repository.getEpisodesBySeriesId(seriesId).collect { eps ->
                _allEpisodes.value = eps
            }
        }
    }

    fun selectSeason(season: Int) {
        _selectedSeason.value = season
    }

    fun getFirstEpisode(): EpisodeEntity? = _allEpisodes.value
        .sortedWith(compareBy({ it.season }, { it.episodeNum }))
        .firstOrNull()

    fun getEpisodeById(episodeId: String): EpisodeEntity? =
        _allEpisodes.value.firstOrNull { it.episodeId == episodeId }

    fun toggleFavorite() {
        viewModelScope.launch {
            val s = _series.value ?: return@launch
            val id = s.seriesId.toString()
            if (isFavorite.value) {
                repository.removeFavorite(id)
            } else {
                repository.addFavorite(FavoriteEntity(
                    contentId = id,
                    contentType = "series",
                    title = s.name,
                    thumbnailUrl = s.posterPath ?: s.cover,
                    categoryId = s.categoryId
                ))
            }
        }
    }
}
