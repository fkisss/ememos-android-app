package com.example.memosapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.memosapp.repository.NetworkResult
import kotlinx.coroutines.launch

// Base ViewModel to handle common LiveData for API call states
open class BaseViewModel : ViewModel() {
    // LiveData for error messages
    val errorMessage = MutableLiveData<String?>()
    // LiveData for loading state
    val isLoading = MutableLiveData<Boolean>(false)

    protected fun <T> handleNetworkResult(
        liveData: MutableLiveData<T?>,
        result: NetworkResult<T>,
        onSuccess: ((T) -> Unit)? = null // Optional success callback for more complex logic
    ) {
        when (result) {
            is NetworkResult.Loading -> {
                isLoading.postValue(true)
            }
            is NetworkResult.Success -> {
                isLoading.postValue(false)
                liveData.postValue(result.data)
                errorMessage.postValue(null) // Clear any previous error
                onSuccess?.invoke(result.data)
            }
            is NetworkResult.Error -> {
                isLoading.postValue(false)
                errorMessage.postValue(result.message ?: "An unknown error occurred")
                liveData.postValue(null) // Clear data on error
            }
        }
    }
    
    // Overloaded for cases where we don't need to post to a specific LiveData for the result itself,
    // but still want to handle loading and error states (e.g., for actions like delete)
    protected fun <T> handleNetworkResult(
        result: NetworkResult<T>,
        onSuccess: ((T) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        when (result) {
            is NetworkResult.Loading -> {
                isLoading.postValue(true)
            }
            is NetworkResult.Success -> {
                isLoading.postValue(false)
                errorMessage.postValue(null)
                onSuccess?.invoke(result.data)
            }
            is NetworkResult.Error -> {
                isLoading.postValue(false)
                val errorMsg = result.message ?: "An unknown error occurred"
                errorMessage.postValue(errorMsg)
                onError?.invoke(errorMsg)
            }
        }
    }
}

