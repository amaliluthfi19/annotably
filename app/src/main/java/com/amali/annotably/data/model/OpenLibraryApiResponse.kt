
import com.amali.annotably.data.model.Book
import com.google.gson.annotations.SerializedName

/**
 * Response model for OpenLibrary Search API
 * 
 * API Documentation: https://openlibrary.org/dev/docs/api/search
 * 
 * Example usage:
 * ```
 * interface ApiService {
 *     @GET("search.json")
 *     suspend fun searchBooks(@Query("q") query: String): Response<OpenLibraryApiResponse>
 * }
 * ```
 */
data class OpenLibraryApiResponse(
    @SerializedName("numFound")
    val numFound: Int,
    
    @SerializedName("start")
    val start: Int,
    
    @SerializedName("numFoundExact")
    val numFoundExact: Boolean,
    
    @SerializedName("num_found")
    val numFoundAlt: Int,
    
    @SerializedName("documentation_url")
    val documentationUrl: String,
    
    @SerializedName("q")
    val query: String,
    
    @SerializedName("offset")
    val offset: Int?,
    
    @SerializedName("docs")
    val docs: List<Book>
)
