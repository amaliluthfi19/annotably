package com.amali.annotably.data.repository

import com.amali.annotably.data.network.ApiService
import javax.inject.Inject

/**
 * Example repository demonstrating how to use the network setup with Hilt DI
 * 
 * Usage example:
 * ```
 * @HiltViewModel
 * class YourViewModel @Inject constructor(
 *     private val repository: ExampleRepository
 * ) : ViewModel() {
 *     
 *     fun fetchData() {
 *         viewModelScope.launch {
 *             when (val result = repository.getData()) {
 *                 is NetworkResult.Success -> {
 *                     // Handle success
 *                     val data = result.data
 *                 }
 *                 is NetworkResult.Error -> {
 *                     // Handle error
 *                     val errorMessage = result.message
 *                 }
 *                 is NetworkResult.Loading -> {
 *                     // Show loading state
 *                 }
 *             }
 *         }
 *     }
 * }
 * ```
 */
class ExampleRepository @Inject constructor(
    private val apiService: ApiService
) : BaseRepository() {
    
    // Example method - uncomment and modify when you have actual API endpoints
    /*
    suspend fun getData(): NetworkResult<YourDataType> {
        return safeApiCall {
            apiService.yourApiMethod()
        }
    }
    */
}
