package com.hoaxdetector.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoaxdetector.app.data.local.entity.CheckHistoryEntity
import com.hoaxdetector.app.data.repository.FactCheckRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class HomeStats(
    val total: Int = 0,
    val hoaxCount: Int = 0,
    val validCount: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: FactCheckRepository
) : ViewModel() {

    val stats: StateFlow<HomeStats> = combine(
        repository.getTotalCount(),
        repository.getHoaxCount(),
        repository.getValidCount()
    ) { total, hoax, valid ->
        HomeStats(total = total, hoaxCount = hoax, validCount = valid)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeStats(total = 0, hoaxCount = 0, validCount = 0)
    )

    val recentHistory: StateFlow<List<CheckHistoryEntity>> = repository.getAllHistory()
        .map { list -> list.take(2) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
