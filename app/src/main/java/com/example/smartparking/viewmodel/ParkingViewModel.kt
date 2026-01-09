package com.example.smartparking.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartparking.api.ApiClient
import com.example.smartparking.model.ParkingLot
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ParkingUiState {
    data object Loading : ParkingUiState()
    data class Success(val parkingLots: List<ParkingLot>) : ParkingUiState()
    data class Error(val message: String) : ParkingUiState()
}

class ParkingViewModel : ViewModel() {
    private val ui_state = MutableStateFlow<ParkingUiState>(ParkingUiState.Loading)
    val uiState: StateFlow<ParkingUiState> = ui_state.asStateFlow()

    init {
        startAutoRefresh()
    }

    private fun startAutoRefresh() {
        viewModelScope.launch {
            // Load initial data
            loadParkingData()
            // Then refresh every 5 seconds
            while (true) {
                delay(5000)
                loadParkingData()
            }
        }
    }

    private suspend fun loadParkingData() {
        try {
            // Fetch lots - they already contain parkingSlots nested inside
            val lots = ApiClient.parkingApi.getParkingLots()

            ui_state.value = ParkingUiState.Success(lots)
        } catch (e: Exception) {
            val errorMessage = when {
                e is java.net.ConnectException || e.message?.contains("failed to connect", ignoreCase = true) == true ->
                    "Unable to connect to server.\nPlease check:\n• Server is running\n• Network connection\n• Server IP address"
                e is java.net.SocketTimeoutException || e.message?.contains("timeout", ignoreCase = true) == true ->
                    "Connection timeout.\nServer is taking too long to respond."
                e is java.net.UnknownHostException ->
                    "Cannot reach server.\nPlease check your network connection."
                else ->
                    "Unable to load parking data.\nPlease ensure server is running."
            }
            ui_state.value = ParkingUiState.Error(errorMessage)
        }
    }
}
