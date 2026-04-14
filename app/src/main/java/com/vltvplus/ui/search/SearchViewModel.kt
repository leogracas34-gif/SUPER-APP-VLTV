package com.vltvplus.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vltvplus.data.database.entities.ChannelEntity
import com.vltvplus.data.database.entities.MovieEntity
import com.vltvplus.data.database.entities.SeriesEntity
import com.vltvplus.data.repository.VLTVRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchResults(
    val movies: List<MovieEntity> = emptyList(),
    val series: List<SeriesEntity> = emptyList(),
    val channels: List<ChannelEntity> = emptyList()
) {
    val totalCount get() = movies.size + series.size + channels.size
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: VLTVRepository
) : ViewModel() {

    private val _searchResults = MutableStateFlow(SearchResults())
    val searchResults: StateFlow<SearchResults> = _searchResults

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var searchType = "all"

    fun setSearchType(type: String) {
        searchType = type
    }

    fun search(query: String) {
        if (query.length < 2) {
            _searchResults.value = SearchResults()
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            val movies = if (searchType == "all" || searchType == "movie")
                repository.searchMovies(query) else emptyList()
            val series = if (searchType == "all" || searchType == "series")
                repository.searchSeries(query) else emptyList()
            val channels = if (searchType == "all" || searchType == "live")
                repository.searchChannels(query) else emptyList()

            _searchResults.value = SearchResults(movies, series, channels)
            _isLoading.value = false
        }
    }
}
