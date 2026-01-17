package com.amali.annotably.data.repository

import com.amali.annotably.data.network.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

/**
 * Base repository class with common network call handling
 */
abstract class BaseRepository {
    
    /**
     * Safe API call wrapper that handles exceptions and returns NetworkResult
     */
    suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): NetworkResult<T> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiCall()
                if (response.isSuccessful) {
                    response.body()?.let {
                        NetworkResult.Success(it)
                    } ?: NetworkResult.Error("Empty response body")
                } else {
                    NetworkResult.Error("Error: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                NetworkResult.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
}
