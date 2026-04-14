package com.vltvplus.ui.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vltvplus.data.database.entities.CategoryEntity
import com.vltvplus.data.database.entities.MovieEntity
import com.vltvplus.data.repository.VLTVRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val repository: VLTVRepository
) : ViewModel() {

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    val categories: StateFlow<List<CategoryEntity>> = repository.getMovieCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val movies: StateFlow<List<MovieEntity>> = _selectedCategoryId
        .flatMapLatest { catId ->
            _isLoading.value = true
            val flow = if (catId == null) repository.getAllMovies()
            else repository.getMoviesByCategory(catId)
            flow.onEach { _isLoading.value = false }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectCategory(categoryId: String?) {
        _selectedCategoryId.value = categoryId
    }
}
