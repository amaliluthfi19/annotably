package com.amali.annotably.core.data.model

import com.amali.annotably.data.model.Book
import com.google.firebase.firestore.DocumentSnapshot

data class BookPage(
        val books: List<Book>,
        val lastDocument: DocumentSnapshot?,
        val hasMore: Boolean
)
