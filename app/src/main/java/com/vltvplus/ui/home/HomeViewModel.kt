package com.vltvplus.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vltvplus.data.database.entities.MovieEntity
import com.vltvplus.data.database.entities.SeriesEntity
import com.vltvplus.data.database.entities.WatchProgressEntity
import com.vltvplus.data.repository.VLTVRepository
import com.vltvplus.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: VLTVRepository
) : ViewModel() {

    private val _syncState = MutableStateFlow<Resource<Unit>>(Resource.Loading)
    val syncState: StateFlow<Resource<Unit>> = _syncState

    val recentMovies: StateFlow<List<MovieEntity>> = repository.getRecentMovies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topRatedMovies: StateFlow<List<MovieEntity>> = repository.getTopRatedMovies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentSeries: StateFlow<List<SeriesEntity>> = repository.getRecentSeries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val continueWatching: StateFlow<List<WatchProgressEntity>> = repository.getRecentProgress()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun syncContent(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _syncState.value = Resource.Loading
            val result = repository.syncAll(forceRefresh)
            _syncState.value = result
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
}
