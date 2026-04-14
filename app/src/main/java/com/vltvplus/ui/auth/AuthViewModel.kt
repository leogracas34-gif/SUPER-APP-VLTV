package com.vltvplus.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vltvplus.data.repository.VLTVRepository
import com.vltvplus.utils.PreferenceManager
import com.vltvplus.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Estado separado para não misturar idle/loading/success/error
sealed class LoginUiState {
    object Idle    : LoginUiState()   // tela limpa, botão habilitado
    object Loading : LoginUiState()   // spinner girando, botão desabilitado
    object Success : LoginUiState()   // navegar para MainActivity
    data class Error(val message: String) : LoginUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: VLTVRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    // ← Começa em Idle (não Loading!)
    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState

    fun isLoggedIn(): Boolean = preferenceManager.isLoggedIn()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginUiState.Loading

            val result = repository.login(username, password)

            _loginState.value = when (result) {
                is Resource.Success -> LoginUiState.Success
                is Resource.Error   -> LoginUiState.Error(result.message)
                else                -> LoginUiState.Error("Erro desconhecido")
            }
        }
    }

    fun logout() {
        viewModelScope.launch { repository.logout() }
    }
}
