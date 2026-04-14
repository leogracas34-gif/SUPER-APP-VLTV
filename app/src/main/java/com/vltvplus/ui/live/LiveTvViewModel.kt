package com.vltvplus.ui.live

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vltvplus.data.database.entities.CategoryEntity
import com.vltvplus.data.database.entities.ChannelEntity
import com.vltvplus.data.database.entities.EpgCacheEntity
import com.vltvplus.data.repository.VLTVRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class LiveTvViewModel @Inject constructor(
    private val repository: VLTVRepository
) : ViewModel() {

    private val _selectedCategoryId = MutableStateFlow<String?>(null)

    val categories: StateFlow<List<CategoryEntity>> = repository.getLiveCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val channels: StateFlow<List<ChannelEntity>> = _selectedCategoryId
        .flatMapLatest { catId ->
            if (catId == null) repository.getAllChannels()
            else repository.getChannelsByCategory(catId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _epgList = MutableStateFlow<List<EpgCacheEntity>>(emptyList())
    val epgList: StateFlow<List<EpgCacheEntity>> = _epgList

    private val _currentProgram = MutableStateFlow<EpgCacheEntity?>(null)
    val currentProgram: StateFlow<EpgCacheEntity?> = _currentProgram

    fun selectCategory(categoryId: String?) {
        _selectedCategoryId.value = categoryId
    }

    fun loadEpg(streamId: Int, channelId: String) {
        viewModelScope.launch {
            repository.loadEpg(streamId, channelId)
            _epgList.value = repository.getUpcomingEpg(channelId)
            _currentProgram.value = repository.getCurrentProgram(channelId)
        }
    }

    fun startMiniPreview(streamId: Int) {
        // Mini preview URL generation for the mini player surface
        val url = repository.buildLiveUrl(streamId)
        _miniPreviewUrl.value = url
    }

    private val _miniPreviewUrl = MutableStateFlow<String?>(null)
    val miniPreviewUrl: StateFlow<String?> = _miniPreviewUrl

    fun stopMiniPreview() {
        _miniPreviewUrl.value = null
    }

    fun formatTime(startTs: Long?, stopTs: Long?): String {
        if (startTs == null || stopTs == null) return ""
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val start = sdf.format(Date(startTs * 1000))
        val end = sdf.format(Date(stopTs * 1000))
        return "$start - $end"
    }
}
