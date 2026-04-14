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

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: VLTVRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<Unit>>(Resource.Loading)
    val loginState: StateFlow<Resource<Unit>> = _loginState

    private val _dnsStatus = MutableStateFlow<String>("Procurando servidor...")
    val dnsStatus: StateFlow<String> = _dnsStatus

    fun isLoggedIn(): Boolean = preferenceManager.isLoggedIn()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading
            _dnsStatus.value = "Testando servidores..."

            val result = repository.login(username, password)
            when (result) {
                is Resource.Success -> {
                    _dnsStatus.value = "Conectado!"
                    _loginState.value = Resource.Success(Unit)
                }
                is Resource.Error -> {
                    _dnsStatus.value = result.message
                    _loginState.value = result
                }
                else -> {}
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
}
