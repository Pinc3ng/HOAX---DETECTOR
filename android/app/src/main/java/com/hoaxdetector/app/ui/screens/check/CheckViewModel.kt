package com.hoaxdetector.app.ui.screens.check

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoaxdetector.app.data.repository.FactCheckRepository
import com.hoaxdetector.app.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CheckState {
    object Idle : CheckState()
    object Loading : CheckState()
    data class Success(val historyId: Long) : CheckState()
    data class Error(val message: String) : CheckState()
}

@HiltViewModel
class CheckViewModel @Inject constructor(
    private val repository: FactCheckRepository
) : ViewModel() {

    private val _checkState = MutableStateFlow<CheckState>(CheckState.Idle)
    val checkState: StateFlow<CheckState> = _checkState.asStateFlow()

    var textInput = MutableStateFlow("")
    var urlInput = MutableStateFlow("")
    var selectedTab = MutableStateFlow(0) // 0 = Text, 1 = URL

    fun checkText() {
        val text = textInput.value.trim()
        if (text.isBlank()) return

        viewModelScope.launch {
            _checkState.value = CheckState.Loading
            when (val result = repository.checkText(text)) {
                is Result.Success -> {
                    // Get the latest history item ID
                    val historyId = getLatestHistoryId()
                    _checkState.value = CheckState.Success(historyId)
                }
                is Result.Error -> {
                    _checkState.value = CheckState.Error(result.message)
                }
                Result.Loading -> {}
            }
        }
    }

    fun checkUrl() {
        val url = urlInput.value.trim()
        if (url.isBlank()) return

        viewModelScope.launch {
            _checkState.value = CheckState.Loading
            when (val result = repository.checkUrl(url)) {
                is Result.Success -> {
                    val historyId = getLatestHistoryId()
                    _checkState.value = CheckState.Success(historyId)
                }
                is Result.Error -> {
                    _checkState.value = CheckState.Error(result.message)
                }
                Result.Loading -> {}
            }
        }
    }

    private suspend fun getLatestHistoryId(): Long {
        // Get the first item from history (most recent)
        return repository.getAllHistory().first().firstOrNull()?.id ?: 0L
    }

    fun resetState() {
        _checkState.value = CheckState.Idle
    }

    fun clearText() {
        textInput.value = ""
    }

    fun clearUrl() {
        urlInput.value = ""
    }
}
