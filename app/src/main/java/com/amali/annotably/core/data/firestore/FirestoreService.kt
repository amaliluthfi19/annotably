package com.amali.annotably.core.data.firestore

import android.util.Log
import com.amali.annotably.core.data.model.PaginatedResults
import com.amali.annotably.data.network.NetworkResult
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Service for CRUD operations with Firestore Provides safe error handling and comprehensive logging
 */
@Singleton
class FirestoreService @Inject constructor(private val firestore: FirebaseFirestore) {
    companion object {
        private const val TAG = "FirestoreService"
    }

    /**
     * Create - Add a new document to a collection
     *
     * @param collection The collection name
     * @param payload The data to add as HashMap
     * @return NetworkResult containing the document ID on success
     */
    suspend fun create(collection: String, payload: HashMap<String, Any>): NetworkResult<String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Creating document in collection: $collection")
                Log.d(TAG, "Payload: $payload")

                val documentReference = firestore.collection(collection).add(payload).await()
                val documentId = documentReference.id

                Log.i(
                        TAG,
                        "Successfully created document with ID: $documentId in collection: $collection"
                )
                NetworkResult.Success(documentId)
            } catch (e: Exception) {
                val errorMessage =
                        "Failed to create document in collection $collection: ${e.message}"
                Log.e(TAG, errorMessage, e)
                NetworkResult.Error(errorMessage)
            }
        }
    }

    /**
     * Create - Add a new document with a specific ID
     *
     * @param collection The collection name
     * @param documentId The document ID to use
     * @param payload The data to add as HashMap
     * @return NetworkResult containing the document ID on success
     */
    suspend fun createWithId(
            collection: String,
            documentId: String,
            payload: HashMap<String, Any>
    ): NetworkResult<String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Creating document with ID: $documentId in collection: $collection")
                Log.d(TAG, "Payload: $payload")

                firestore.collection(collection).document(documentId).set(payload).await()

                Log.i(
                        TAG,
                        "Successfully created document with ID: $documentId in collection: $collection"
                )
                NetworkResult.Success(documentId)
            } catch (e: Exception) {
                val errorMessage =
                        "Failed to create document with ID $documentId in collection $collection: ${e.message}"
                Log.e(TAG, errorMessage, e)
                NetworkResult.Error(errorMessage)
            }
        }
    }

    /**
     * Read - Get all documents from a collection
     *
     * @param collection The collection name
     * @return NetworkResult containing list of documents as HashMap
     */
    suspend fun getAll(collection: String): NetworkResult<List<HashMap<String, Any>>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching all documents from collection: $collection")

                val querySnapshot = firestore.collection(collection).get().await()
                val documents =
                        querySnapshot.documents.mapNotNull { document ->
                            try {
                                val data = document.data as? HashMap<String, Any> ?: hashMapOf()
                                data["id"] = document.id
                                data
                            } catch (e: Exception) {
                                Log.w(TAG, "Error parsing document ${document.id}: ${e.message}")
                                null
                            }
                        }

                Log.i(
                        TAG,
                        "Successfully fetched ${documents.size} documents from collection: $collection"
                )
                NetworkResult.Success(documents)
            } catch (e: Exception) {
                val errorMessage =
                        "Failed to fetch documents from collection $collection: ${e.message}"
                Log.e(TAG, errorMessage, e)
                NetworkResult.Error(errorMessage)
            }
        }
    }

    suspend fun getPaginated(collection: String, lasDocument: DocumentSnapshot?, queryBuilder: (Query) -> Query, limit: Int): NetworkResult<PaginatedResults> {
        return withContext(Dispatchers.IO) {
            try {
                var query: Query = firestore.collection(collection)
               queryBuilder.let { query = it(query) }
                query = query.limit(limit.toLong())
                lasDocument?.let { query = query.startAfter(it) }

                val querySnapshot = query.get().await()

                val documents = querySnapshot.documents.mapNotNull { document ->
                    try {
                        val data = document.data as? HashMap<String, Any> ?: hashMapOf()
                        data["id"] = document.id
                        data
                    }catch (e: Exception) {
                        Log.e(TAG, "Error parsing Document $e")
                        null
                    }
                }

                NetworkResult.Success(
                    PaginatedResults(
                        documents,
                        querySnapshot.documents.lastOrNull(),
                        documents.size == limit
                    )
                )

            }catch (e: Exception) {
                Log.e(TAG, "Error get books $e")
                NetworkResult.Error(e.message ?: "Something went wrong")
            }
        }
    }

    /**
     * Read - Get a single document by ID
     *
     * @param collection The collection name
     * @param documentId The document ID
     * @return NetworkResult containing the document as HashMap
     */
    suspend fun getById(
            collection: String,
            documentId: String
    ): NetworkResult<HashMap<String, Any>?> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching document with ID: $documentId from collection: $collection")

                val documentSnapshot =
                        firestore.collection(collection).document(documentId).get().await()

                if (documentSnapshot.exists()) {
                    val data = documentSnapshot.data as? HashMap<String, Any> ?: hashMapOf()
                    data["id"] = documentSnapshot.id

                    Log.i(
                            TAG,
                            "Successfully fetched document with ID: $documentId from collection: $collection"
                    )
                    NetworkResult.Success(data)
                } else {
                    val errorMessage =
                            "Document with ID $documentId not found in collection $collection"
                    Log.w(TAG, errorMessage)
                    NetworkResult.Error(errorMessage)
                }
            } catch (e: Exception) {
                val errorMessage =
                        "Failed to fetch document with ID $documentId from collection $collection: ${e.message}"
                Log.e(TAG, errorMessage, e)
                NetworkResult.Error(errorMessage)
            }
        }
    }

    /**
     * Read - Get documents by query
     *
     * @param collection The collection name
     * @param queryBuilder Lambda to build the query
     * @return NetworkResult containing list of documents as HashMap
     */
    suspend fun getByQuery(
            collection: String,
            queryBuilder: (Query) -> Query
    ): NetworkResult<List<HashMap<String, Any>>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching documents by query from collection: $collection")

                val baseQuery = firestore.collection(collection)
                val query = queryBuilder(baseQuery)
                val querySnapshot = query.get().await()

                val documents =
                        querySnapshot.documents.mapNotNull { document ->
                            try {
                                val data = document.data as? HashMap<String, Any> ?: hashMapOf()
                                data["id"] = document.id
                                data
                            } catch (e: Exception) {
                                Log.w(TAG, "Error parsing document ${document.id}: ${e.message}")
                                null
                            }
                        }

                Log.i(
                        TAG,
                        "Successfully fetched ${documents.size} documents by query from collection: $collection"
                )
                NetworkResult.Success(documents)
            } catch (e: Exception) {
                val errorMessage =
                        "Failed to fetch documents by query from collection $collection: ${e.message}"
                Log.e(TAG, errorMessage, e)
                NetworkResult.Error(errorMessage)
            }
        }
    }

    /**
     * Update - Update a document
     *
     * @param collection The collection name
     * @param documentId The document ID
     * @param updates The fields to update as HashMap
     * @return NetworkResult indicating success or failure
     */
    suspend fun update(
            collection: String,
            documentId: String,
            updates: HashMap<String, Any>
    ): NetworkResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Updating document with ID: $documentId in collection: $collection")
                Log.d(TAG, "Updates: $updates")

                firestore.collection(collection).document(documentId).update(updates).await()

                Log.i(
                        TAG,
                        "Successfully updated document with ID: $documentId in collection: $collection"
                )
                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                val errorMessage =
                        "Failed to update document with ID $documentId in collection $collection: ${e.message}"
                Log.e(TAG, errorMessage, e)
                NetworkResult.Error(errorMessage)
            }
        }
    }

    /**
     * Update - Set a document (overwrites entire document)
     *
     * @param collection The collection name
     * @param documentId The document ID
     * @param payload The complete document data as HashMap
     * @param merge If true, merges with existing data; if false, overwrites entire document
     * @return NetworkResult indicating success or failure
     */
    suspend fun set(
            collection: String,
            documentId: String,
            payload: HashMap<String, Any>,
            merge: Boolean = false
    ): NetworkResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(
                        TAG,
                        "Setting document with ID: $documentId in collection: $collection (merge: $merge)"
                )
                Log.d(TAG, "Payload: $payload")

                val documentReference = firestore.collection(collection).document(documentId)

                if (merge) {
                    documentReference
                            .set(payload, com.google.firebase.firestore.SetOptions.merge())
                            .await()
                } else {
                    documentReference.set(payload).await()
                }

                Log.i(
                        TAG,
                        "Successfully set document with ID: $documentId in collection: $collection"
                )
                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                val errorMessage =
                        "Failed to set document with ID $documentId in collection $collection: ${e.message}"
                Log.e(TAG, errorMessage, e)
                NetworkResult.Error(errorMessage)
            }
        }
    }

    /**
     * Delete - Delete a document
     *
     * @param collection The collection name
     * @param documentId The document ID
     * @return NetworkResult indicating success or failure
     */
    suspend fun delete(collection: String, documentId: String): NetworkResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Deleting document with ID: $documentId from collection: $collection")

                firestore.collection(collection).document(documentId).delete().await()

                Log.i(
                        TAG,
                        "Successfully deleted document with ID: $documentId from collection: $collection"
                )
                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                val errorMessage =
                        "Failed to delete document with ID $documentId from collection $collection: ${e.message}"
                Log.e(TAG, errorMessage, e)
                NetworkResult.Error(errorMessage)
            }
        }
    }

    /**
     * Check if a document exists
     *
     * @param collection The collection name
     * @param documentId The document ID
     * @return NetworkResult containing boolean indicating existence
     */
    suspend fun exists(collection: String, documentId: String): NetworkResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(
                        TAG,
                        "Checking if document with ID: $documentId exists in collection: $collection"
                )

                val documentSnapshot =
                        firestore.collection(collection).document(documentId).get().await()

                val exists = documentSnapshot.exists()
                Log.d(
                        TAG,
                        "Document with ID: $documentId ${if (exists) "exists" else "does not exist"} in collection: $collection"
                )
                NetworkResult.Success(exists)
            } catch (e: Exception) {
                val errorMessage =
                        "Failed to check existence of document with ID $documentId in collection $collection: ${e.message}"
                Log.e(TAG, errorMessage, e)
                NetworkResult.Error(errorMessage)
            }
        }
    }

    /**
     * Get document reference for advanced operations
     *
     * @param collection The collection name
     * @param documentId The document ID
     * @return DocumentReference
     */
    fun getDocumentReference(collection: String, documentId: String): DocumentReference {
        return firestore.collection(collection).document(documentId)
    }

    /**
     * Get collection reference for advanced operations
     *
     * @param collection The collection name
     * @return Query (collection reference)
     */
    fun getCollectionReference(collection: String): Query {
        return firestore.collection(collection)
    }
}
