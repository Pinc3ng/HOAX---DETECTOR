package com.hoaxdetector.app.ui.screens.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoaxdetector.app.data.local.entity.CheckHistoryEntity
import com.hoaxdetector.app.data.repository.FactCheckRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val repository: FactCheckRepository
) : ViewModel() {

    private val _historyItem = MutableStateFlow<CheckHistoryEntity?>(null)
    val historyItem: StateFlow<CheckHistoryEntity?> = _historyItem.asStateFlow()

    fun loadHistory(id: Long) {
        viewModelScope.launch {
            _historyItem.value = repository.getHistoryById(id)
        }
    }

    fun deleteCurrentHistory(id: Long, onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteHistory(id)
            onDeleted()
        }
    }
}
