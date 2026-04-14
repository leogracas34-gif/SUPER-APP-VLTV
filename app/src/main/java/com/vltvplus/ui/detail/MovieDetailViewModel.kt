package com.vltvplus.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.vltvplus.data.database.entities.FavoriteEntity
import com.vltvplus.data.database.entities.MovieEntity
import com.vltvplus.data.repository.VLTVRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    private val repository: VLTVRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _movie = MutableStateFlow<MovieEntity?>(null)
    val movie: StateFlow<MovieEntity?> = _movie

    private val _similarMovies = MutableStateFlow<List<MovieEntity>>(emptyList())
    val similarMovies: StateFlow<List<MovieEntity>> = _similarMovies

    val isFavorite: StateFlow<Boolean> = _movie
        .filterNotNull()
        .flatMapLatest { m -> repository.isFavorite(m.streamId.toString()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun loadMovie(streamId: Int) {
        viewModelScope.launch {
            var movie = repository.getMovieById(streamId) ?: return@launch
            // Enrich with TMDB if needed
            movie = repository.enrichMovieWithTmdb(movie)
            _movie.value = movie

            // Load similar from same category
            if (!movie.categoryId.isNullOrEmpty()) {
                repository.getMoviesByCategory(movie.categoryId)
                    .collect { list ->
                        _similarMovies.value = list.filter { it.streamId != streamId }.take(20)
                    }
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val m = _movie.value ?: return@launch
            val id = m.streamId.toString()
            if (isFavorite.value) {
                repository.removeFavorite(id)
            } else {
                repository.addFavorite(FavoriteEntity(
                    contentId = id,
                    contentType = "movie",
                    title = m.name,
                    thumbnailUrl = m.posterPath ?: m.streamIcon,
                    categoryId = m.categoryId
                ))
            }
        }
    }
}
