package com.example.memosapp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.memosapp.model.*
import com.example.memosapp.repository.MemosRepository
import com.example.memosapp.repository.NetworkResult
import kotlinx.coroutines.launch

class MemoViewModel(private val repository: MemosRepository) : BaseViewModel() {

    // LiveData for the list of memos
    val memosList = MutableLiveData<List<Memo>?>()
    // LiveData for a single selected memo (e.g., for detail view)
    val selectedMemo = MutableLiveData<Memo?>()
    // LiveData for API configuration status
    val apiConfig = MutableLiveData<ApiConfig?>() // To store/retrieve API URL and Token
    val currentUser = MutableLiveData<User?>()

    // --- API Configuration --- 
    fun saveApiConfig(apiUrl: String, apiToken: String, userId: String?) {
        val config = ApiConfig(apiUrl, apiToken, userId)
        // Here, you would typically save this to SharedPreferences or a local database
        // For this example, we'll just hold it in LiveData
        apiConfig.postValue(config)
    }

    fun loadApiConfig() {
        // Here, you would load from SharedPreferences or local DB
        // For now, assume it's null or set externally if previously saved
        // Example: val loadedConfig = sharedPrefs.getApiConfig()
        // apiConfig.postValue(loadedConfig)
    }

    fun checkAuthAndFetchUser(apiUrl: String, token: String) {
        viewModelScope.launch {
            // Ensure API URL ends with a slash if not already
            val correctedApiUrl = if (apiUrl.endsWith("/")) apiUrl else "$apiUrl/"
            saveApiConfig(correctedApiUrl, token, null) // Temporarily save, userId will be updated
            
            isLoading.postValue(true)
            when (val result = repository.checkAuthStatus(token)) {
                is NetworkResult.Success -> {
                    currentUser.postValue(result.data)
                    // Update ApiConfig with the fetched userId
                    saveApiConfig(correctedApiUrl, token, result.data.id)
                    errorMessage.postValue(null)
                    // Optionally trigger fetching memos or other initial data load here
                }
                is NetworkResult.Error -> {
                    currentUser.postValue(null)
                    errorMessage.postValue(result.message ?: "Authentication failed")
                }
                is NetworkResult.Loading -> { /* isLoading is already true */ }
            }
            isLoading.postValue(false)
        }
    }

    // --- Memos Operations --- 
    fun fetchMemos(limit: Int? = null, offset: Int? = null, contentSearch: String? = null) {
        val currentApiConfig = apiConfig.value
        val user = currentUser.value
        if (currentApiConfig == null || currentApiConfig.apiToken.isBlank() || user == null) {
            errorMessage.postValue("API URL, Token, or User ID is not configured.")
            return
        }

        var filterString = "creator == 'users/${user.id}'"
        if (!contentSearch.isNullOrBlank()) {
            filterString += " && content_search == ['$contentSearch']"
        }
        // Add visibility filter if needed, e.g., " && visibilities == ['PUBLIC', 'PROTECTED']"

        viewModelScope.launch {
            val result = repository.getMemos(currentApiConfig.apiToken, null, limit, offset, filterString)
            handleNetworkResult(memosList, result)
        }
    }

    fun createMemo(content: String, visibility: String = "PUBLIC", resourceIdList: List<String>? = null) {
        val currentApiConfig = apiConfig.value
        if (currentApiConfig == null || currentApiConfig.apiToken.isBlank()) {
            errorMessage.postValue("API URL or Token is not configured.")
            return
        }
        val payload = CreateMemoPayload(content = content, visibility = visibility, resourceIdList = resourceIdList)
        viewModelScope.launch {
            val result = repository.createMemo(currentApiConfig.apiToken, payload)
            handleNetworkResult(result, onSuccess = {
                // Optionally, refresh the memos list or add the new memo directly
                fetchMemos() // Refresh list after creation
            })
        }
    }

    fun fetchMemoDetails(memoUid: String) {
        val currentApiConfig = apiConfig.value
        if (currentApiConfig == null || currentApiConfig.apiToken.isBlank()) {
            errorMessage.postValue("API URL or Token is not configured.")
            return
        }
        viewModelScope.launch {
            val result = repository.getMemoByUid(currentApiConfig.apiToken, memoUid)
            handleNetworkResult(selectedMemo, result)
        }
    }

    fun updateMemo(memoName: String, content: String?, visibility: String?, pinned: Boolean?) {
        val currentApiConfig = apiConfig.value
        if (currentApiConfig == null || currentApiConfig.apiToken.isBlank()) {
            errorMessage.postValue("API URL or Token is not configured.")
            return
        }
        val payload = UpdateMemoPayload(content = content, visibility = visibility, pinned = pinned)
        viewModelScope.launch {
            val result = repository.updateMemo(currentApiConfig.apiToken, memoName, payload)
            handleNetworkResult(result, onSuccess = {
                // Refresh list or update the specific memo in the list
                fetchMemos()
            })
        }
    }

    fun deleteMemo(memoName: String) {
        val currentApiConfig = apiConfig.value
        if (currentApiConfig == null || currentApiConfig.apiToken.isBlank()) {
            errorMessage.postValue("API URL or Token is not configured.")
            return
        }
        viewModelScope.launch {
            val result = repository.deleteMemo(currentApiConfig.apiToken, memoName)
            handleNetworkResult(result, onSuccess = {
                // Refresh list after deletion
                fetchMemos()
            })
        }
    }
    
    // --- Resource Operations ---
    val uploadedResource = MutableLiveData<MemoResource?>()
    fun uploadResource(base64Content: String, filename: String, mimeType: String, visibility: String = "PUBLIC") {
        val currentApiConfig = apiConfig.value
        if (currentApiConfig == null || currentApiConfig.apiToken.isBlank()) {
            errorMessage.postValue("API URL or Token is not configured.")
            return
        }
        val payload = UploadResourcePayload(content = base64Content, filename = filename, type = mimeType, visibility = visibility)
        viewModelScope.launch {
            val result = repository.uploadResource(currentApiConfig.apiToken, payload)
            handleNetworkResult(uploadedResource, result)
        }
    }

    // --- Tags Operations ---
    val tags = MutableLiveData<Map<String, Int>?>()
    fun fetchTags() {
        val currentApiConfig = apiConfig.value
        val user = currentUser.value
        if (currentApiConfig == null || currentApiConfig.apiToken.isBlank() || user == null) {
            errorMessage.postValue("API URL, Token, or User ID is not configured for fetching tags.")
            return
        }
        val filterString = "creator == 'users/${user.id}'"
        viewModelScope.launch {
            val result = repository.getTags(currentApiConfig.apiToken, filterString)
            handleNetworkResult(tags, result) { response ->
                // The API returns tagAmounts directly in the TagsResponse model
            }
        }
    }
}

