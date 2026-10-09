package com.hoaxdetector.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import com.hoaxdetector.app.data.local.UserSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val sessionManager: UserSessionManager
) : ViewModel() {

    private val _isLoginTab = MutableStateFlow(true)
    val isLoginTab: StateFlow<Boolean> = _isLoginTab.asStateFlow()

    val emailInput = MutableStateFlow("")
    val passwordInput = MutableStateFlow("")
    val nameInput = MutableStateFlow("")
    val confirmPasswordInput = MutableStateFlow("")

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun setTab(isLogin: Boolean) {
        _isLoginTab.value = isLogin
        _errorMessage.value = null
    }

    fun login(onSuccess: () -> Unit) {
        val email = emailInput.value.trim()
        val password = passwordInput.value.trim()

        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Email dan kata sandi harus diisi"
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _errorMessage.value = "Format email tidak valid"
            return
        }

        _errorMessage.value = null
        val extractedName = email.substringBefore("@").replaceFirstChar { it.uppercase() }
        sessionManager.login(name = extractedName, email = email)
        onSuccess()
    }

    fun signUp(onSuccess: () -> Unit) {
        val name = nameInput.value.trim()
        val email = emailInput.value.trim()
        val password = passwordInput.value.trim()
        val confirm = confirmPasswordInput.value.trim()

        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Semua kolom pendaftaran wajib diisi"
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _errorMessage.value = "Format email tidak valid"
            return
        }

        if (password.length < 6) {
            _errorMessage.value = "Kata sandi minimal 6 karakter"
            return
        }

        if (password != confirm) {
            _errorMessage.value = "Konfirmasi kata sandi tidak cocok"
            return
        }

        _errorMessage.value = null
        sessionManager.login(name = name, email = email)
        onSuccess()
    }

    fun continueAsGuest(onSuccess: () -> Unit) {
        sessionManager.loginAsGuest()
        onSuccess()
    }
}
