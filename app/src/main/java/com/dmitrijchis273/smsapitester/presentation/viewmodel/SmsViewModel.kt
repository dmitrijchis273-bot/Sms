package com.dmitrijchis273.smsapitester.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dmitrijchis273.smsapitester.data.models.SmsRequest
import com.dmitrijchis273.smsapitester.data.repository.SmsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SmsUiState(
    val isLoading: Boolean = false,
    val phoneNumber: String = "",
    val message: String = "",
    val isSuccess: Boolean = false,
    val isError: Boolean = false,
    val history: List<SmsRequest> = emptyList()
)

@HiltViewModel
class SmsViewModel @Inject constructor(
    private val repository: SmsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SmsUiState())
    val uiState: StateFlow<SmsUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            repository.getHistoryFlow().collect { requests ->
                _uiState.value = _uiState.value.copy(history = requests)
            }
        }
    }

    fun onPhoneNumberChange(phone: String) {
        _uiState.value = _uiState.value.copy(
            phoneNumber = phone,
            message = "",
            isSuccess = false,
            isError = false
        )
    }

    fun sendSms() {
        val phoneNumber = _uiState.value.phoneNumber.trim()

        if (phoneNumber.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                message = "Введите номер телефона",
                isError = true
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = repository.sendSmsCode(phoneNumber)
            result.onSuccess { message ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = message,
                    isSuccess = true,
                    isError = false,
                    phoneNumber = ""
                )
            }

            result.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Ошибка: ${error.message}",
                    isSuccess = false,
                    isError = true
                )
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun resetMessage() {
        _uiState.value = _uiState.value.copy(
            message = "",
            isSuccess = false,
            isError = false
        )
    }
}
