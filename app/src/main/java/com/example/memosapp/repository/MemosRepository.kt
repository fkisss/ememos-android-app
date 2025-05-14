package com.example.memosapp.repository

import com.example.memosapp.model.*
import com.example.memosapp.network.MemosApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

// Result wrapper to handle API call states (Loading, Success, Error)
sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(val exception: Exception, val message: String? = null) : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>() // Optional: for UI to show loading state
}

class MemosRepository(private val memosApiService: MemosApiService) {

    // Helper function to safely make API calls
    private suspend fun <T : Any> safeApiCall(call: suspend () -> Response<T>): NetworkResult<T> {
        return try {
            val response = call.invoke()
            if (response.isSuccessful) {
                response.body()?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Error(Exception("Response body is null"))
            } else {
                NetworkResult.Error(Exception("API Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            NetworkResult.Error(e, e.message)
        }
    }

    suspend fun checkAuthStatus(token: String): NetworkResult<User> = withContext(Dispatchers.IO) {
        safeApiCall { memosApiService.checkAuthStatus("Bearer $token") }
    }

    suspend fun getMemos(
        token: String,
        creatorId: String?,
        limit: Int?,
        offset: Int?,
        filter: String?
    ): NetworkResult<MemosResponse> = withContext(Dispatchers.IO) {
        // Here you could add logic to first check a local cache (e.g., Room DB)
        // For now, we directly fetch from network
        safeApiCall { memosApiService.getMemos("Bearer $token", creatorId, limit, offset, filter) }
        // After fetching, you could save the result to the local cache
    }

    suspend fun createMemo(token: String, payload: CreateMemoPayload): NetworkResult<Memo> = withContext(Dispatchers.IO) {
        safeApiCall { memosApiService.createMemo("Bearer $token", payload) }
        // After successful creation, you might want to update the local cache
    }

    suspend fun getMemoByUid(token: String, memoUid: String): NetworkResult<Memo> = withContext(Dispatchers.IO) {
        safeApiCall { memosApiService.getMemoByUid("Bearer $token", memoUid) }
    }

    suspend fun updateMemo(token: String, memoName: String, payload: UpdateMemoPayload): NetworkResult<Memo> = withContext(Dispatchers.IO) {
        safeApiCall { memosApiService.updateMemo("Bearer $token", memoName, payload) }
        // After successful update, update local cache
    }

    suspend fun deleteMemo(token: String, memoName: String): NetworkResult<Unit> = withContext(Dispatchers.IO) {
        safeApiCall { memosApiService.deleteMemo("Bearer $token", memoName) }
        // After successful deletion, remove from local cache
    }

    suspend fun uploadResource(token: String, payload: UploadResourcePayload): NetworkResult<MemoResource> = withContext(Dispatchers.IO) {
        safeApiCall { memosApiService.uploadResource("Bearer $token", payload) }
    }

    suspend fun getTags(token: String, filter: String): NetworkResult<TagsResponse> = withContext(Dispatchers.IO) {
        safeApiCall { memosApiService.getTags("Bearer $token", filter) }
    }
    
    // TODO: Add methods for interacting with a local database (Room) if caching is implemented.
    // For example:
    // suspend fun getMemosFromDb(): List<Memo> 
    // suspend fun insertMemosToDb(memos: List<Memo>)
}

