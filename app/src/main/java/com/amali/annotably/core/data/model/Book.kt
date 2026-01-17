package com.amali.annotably.data.model

import com.google.gson.annotations.SerializedName

/**
 * Book model containing essential book information
 *
 * Image URL formats:
 * - Author image: https://covers.openlibrary.org/a/olid/{author_key}-{size}.jpg
 * - Cover image: https://covers.openlibrary.org/b/id/{cover_i}-{size}.jpg
 *
 * Size options: S (small), M (medium), L (large)
 */
data class Book(
        @SerializedName("title") val title: String?,
        @SerializedName("first_publish_year") val firstPublish: Int?,
        @SerializedName("author_name") val authorName: List<String>?,
        @SerializedName("author_key") val authorKey: List<String>?,
        @SerializedName("cover_i") val coverId: Int?,
        @SerializedName("key") val key: String?
) {
    /**
     * Get author image URL
     *
     * @param size Image size: "S" (small), "M" (medium), or "L" (large). Default is "M"
     * @return Author image URL or null if author_key is not available
     */
    fun getAuthorImageUrl(size: String = "M"): String? {
        return authorKey?.firstOrNull()?.let { key ->
            "https://covers.openlibrary.org/a/olid/$key-$size.jpg"
        }
    }

    /**
     * Get cover image URL
     *
     * @param size Image size: "S" (small), "M" (medium), or "L" (large). Default is "L"
     * @return Cover image URL or null if cover_i is not available
     */
    fun getCoverImageUrl(size: String = "L"): String? {
        return coverId?.let { id -> "https://covers.openlibrary.org/b/id/$id-$size.jpg" }
    }

    /**
     * Get the primary author name
     * @return First author name or null
     */
    fun getPrimaryAuthor(): String? = authorName?.firstOrNull()

    /**
     * Get formatted author names as a single string
     * @return Comma-separated author names or "Unknown Author"
     */
    fun getFormattedAuthors(): String {
        return authorName?.joinToString(", ") ?: "Unknown Author"
    }

    /**
     * Get a unique key for this book to identify it
     * @return A unique string identifier for this book
     */
    fun getUniqueKey(): String {
        val titlePart = title?.trim()?.lowercase() ?: ""
        val authorPart = getPrimaryAuthor()?.trim()?.lowercase() ?: ""
        val coverPart = coverId?.toString() ?: ""
        return "$titlePart|$authorPart|$coverPart"
    }
}
