package com.amali.annotably.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * @deprecated This object is deprecated in favor of Hilt dependency injection.
 * Use NetworkModule in the di package instead.
 * See DEPENDENCY_INJECTION.md for migration guide.
 */
@Deprecated(
    message = "Use Hilt dependency injection instead. See NetworkModule in di package.",
    replaceWith = ReplaceWith(
        expression = "NetworkModule",
        imports = ["com.amali.annotably.di.NetworkModule"]
    )
)
object RetrofitClient {
    
    // TODO: Replace with your actual base URL
    private const val BASE_URL = "https://api.example.com/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    fun <T> createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }
}
