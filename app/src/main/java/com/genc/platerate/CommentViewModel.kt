package com.genc.platerate


import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class CommentViewModel : ViewModel() {
    val commentsList = mutableStateListOf<CommentItem>()
    val userComments = mutableStateListOf<CommentItem>()
    val plateComments = mutableStateListOf<CommentItem>()
    val previousSearches = mutableStateListOf<String>()

    // Fetch all comments
    fun fetchAllComments(currentUser: FirebaseUser?) {
        val db = Firebase.firestore
        currentUser?.let { user ->
            db.collection("comments")
                .orderBy("timestamp")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        println("Error fetching comments: ${e.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        commentsList.clear()
                        for (document in snapshot.documents.reversed()) {
                            val commentItem = parseComment(document.id, document.data, user)
                            commentsList.add(commentItem)
                        }
                    }
                }
        }
    }

    // Fetch comments for a specific plate
    fun fetchPlateComments(plateNumber: String, currentUser: FirebaseUser?) {
        val db = Firebase.firestore
        db.collection("comments")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)  // En yeni önce
            .whereEqualTo("normalizedPlate", plateNumber.uppercase())
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    println("Error fetching plate comments: ${e.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    plateComments.clear()
                    for (document in snapshot.documents.reversed()) {
                        val commentItem = parseComment(document.id, document.data, currentUser)
                        plateComments.add(commentItem)
                    }
                }
            }
    }

    fun fetchUserComments(currentUser: FirebaseUser?) {
        val db = Firebase.firestore
        currentUser?.let { user ->
            db.collection("comments")
                .whereEqualTo("userId", user.uid)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        println("Error fetching user comments: ${e.message}")
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        println("ProfileScreen: Fetching updated comments...") // DEBUG

                        userComments.clear() // Listeyi temizle
                        for (document in snapshot.documents) {
                            val commentItem = parseComment(document.id, document.data, user)
                            userComments.add(commentItem)
                        }

                        println("ProfileScreen: Güncellenmiş yorum sayısı: ${userComments.size}") // DEBUG
                    }
                }
        }
    }

    fun fetchSearchHistory(userId: String, onResult: (List<String>) -> Unit) {
        val db = Firebase.firestore
        val userRef = db.collection("profiles").document(userId)

        userRef.get().addOnSuccessListener { document ->
            val searchHistory = document.get("searchHistory") as? List<String> ?: emptyList()
            onResult(searchHistory)
        }.addOnFailureListener { e ->
            println("Arama geçmişi çekilirken hata: ${e.message}")
            onResult(emptyList()) // Hata durumunda boş liste döndür
        }
    }

    fun removeSearchFromHistory(userId: String, plateNumber: String) {
        val db = Firebase.firestore
        val userRef = db.collection("profiles").document(userId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val searchHistory = snapshot.get("searchHistory") as? MutableList<String> ?: mutableListOf()

            searchHistory.remove(plateNumber) // Sil

            transaction.update(userRef, "searchHistory", searchHistory)
        }.addOnSuccessListener {
            println("Arama geçmişinden silindi: $plateNumber")
        }.addOnFailureListener { e ->
            println("Arama geçmişinden silinirken hata: ${e.message}")
        }
    }







    private fun parseComment(commentId: String, data: Map<String, Any>?, user: FirebaseUser?): CommentItem {
        val licensePlate = data?.get("licensePlate")?.toString() ?: ""
        val comment = data?.get("comment")?.toString() ?: ""
        val likes = (data?.get("likesCount") as? Long)?.toInt() ?: 0
        val dislikes = (data?.get("dislikesCount") as? Long)?.toInt() ?: 0
        val likedBy = data?.get("likedBy") as? List<String> ?: emptyList()
        val dislikedBy = data?.get("dislikedBy") as? List<String> ?: emptyList()
        val bookmarkedBy = data?.get("bookmarkedBy") as? List<String> ?: emptyList()
        val plateBookmarkedBy = data?.get("plateBookmarkedBy") as?List<String> ?: emptyList()
        val userId = data?.get("userId")?.toString() ?: ""

        return CommentItem(
            commentId = commentId,
            licensePlate = licensePlate,
            comment = comment,
            likesCount = likes,
            dislikesCount = dislikes,
            likedBy = likedBy,
            dislikedBy = dislikedBy,
            isLiked = mutableStateOf(user?.uid in likedBy),
            isDisliked = mutableStateOf(user?.uid in dislikedBy),
            isBookmarked = mutableStateOf(user?.uid in bookmarkedBy),
            isPlateBookmarked = mutableStateOf(user?.uid in plateBookmarkedBy),
            userId=userId
        )
    }

    fun updateCommentState(
        commentId: String,
        currentUserId: String,
        isLiked: Boolean? = null,
        isDisliked: Boolean? = null,
        isBookmarked: Boolean? = null,
        isPlateBookmarked: Boolean? = null
    ) {
        val db = Firebase.firestore
        val commentRef = db.collection("comments").document(commentId)




        db.runTransaction { transaction ->
            val snapshot = transaction.get(commentRef)
            val likedBy = snapshot.get("likedBy") as? MutableList<String> ?: mutableListOf()
            val dislikedBy = snapshot.get("dislikedBy") as? MutableList<String> ?: mutableListOf()
            val bookmarkedBy = snapshot.get("bookmarkedBy") as? MutableList<String> ?: mutableListOf()
            val plateBookmarkedBy = snapshot.get("plateBookmarkedBy") as? MutableList<String> ?: mutableListOf()

            if (isLiked != null) {
                if (isLiked) {
                    if (!likedBy.contains(currentUserId)) {
                        likedBy.add(currentUserId)
                    }
                    dislikedBy.remove(currentUserId)
                } else {
                    likedBy.remove(currentUserId)
                }
                transaction.update(commentRef, "likedBy", likedBy)
                transaction.update(commentRef, "likesCount", likedBy.size)
            }

            if (isDisliked != null) {
                if (isDisliked) {
                    if (!dislikedBy.contains(currentUserId)) {
                        dislikedBy.add(currentUserId)
                    }
                    likedBy.remove(currentUserId)
                } else {
                    dislikedBy.remove(currentUserId)
                }
                transaction.update(commentRef, "dislikedBy", dislikedBy)
                transaction.update(commentRef, "dislikesCount", dislikedBy.size)
            }

            if (isBookmarked != null) {
                if (isBookmarked) {
                    if (!bookmarkedBy.contains(currentUserId)) {
                        bookmarkedBy.add(currentUserId)
                    }
                } else {
                    bookmarkedBy.remove(currentUserId)
                }
                transaction.update(commentRef, "bookmarkedBy", bookmarkedBy)
            }

            if (isPlateBookmarked != null){
                if (isPlateBookmarked){
                    if (!plateBookmarkedBy.contains(currentUserId)){
                        plateBookmarkedBy.add(currentUserId)
                    }
                }else{
                    plateBookmarkedBy.remove(currentUserId)
                }
                transaction.update(commentRef,"plateBookmarkedBy", plateBookmarkedBy)
            }





            null
        }.addOnSuccessListener {
            // Transaction success handling if needed
            println("Transaction success!")
        }.addOnFailureListener { e ->
            // Transaction failure handling
            println("Transaction failure: ${e.message}")
        }
    }

    fun handlePlateBookmark(plateNumber: String, currentUserId: String, isBookmarked: Boolean) {
        val db = Firebase.firestore
        val userRef = db.collection("profiles").document(currentUserId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)

            // Retrieve the bookmarkedPlates field, or initialize it as an empty list
            val bookmarkedPlates = snapshot.get("bookmarkedPlates") as? MutableList<String> ?: mutableListOf()

            if (isBookmarked) {
                // Add plate if it is not already in the list
                if (!bookmarkedPlates.contains(plateNumber)) {
                    bookmarkedPlates.add(plateNumber)
                }
            } else {
                // Remove plate if it is already in the list
                bookmarkedPlates.remove(plateNumber)
            }

            // Update the profile document with the new bookmarkedPlates list
            transaction.update(userRef, "bookmarkedPlates", bookmarkedPlates)

        }.addOnSuccessListener {
            // Transaction succeeded
            println("Plate bookmark transaction success!")
        }.addOnFailureListener { e ->
            // Transaction failed
            println("Plate bookmark transaction failure: ${e.message}")
        }
    }

    fun deleteComment(commentId: String, currentUserId: String, onDeleteSuccess: () -> Unit, onDeleteFailure: (String) -> Unit) {
        val db = Firebase.firestore
        val commentRef = db.collection("comments").document(commentId)

        commentRef.get().addOnSuccessListener { document ->
            if (document.exists() && document.getString("userId") == currentUserId) {
                commentRef.delete().addOnSuccessListener {
                    onDeleteSuccess()
                }.addOnFailureListener { e ->
                    onDeleteFailure(e.message ?: "Silme işlemi başarısız.")
                }
            } else {
                onDeleteFailure("Bu yorumu silme yetkiniz yok.")
            }
        }.addOnFailureListener { e ->
            onDeleteFailure(e.message ?: "Yorumu kontrol ederken hata oluştu.")
        }
    }


    fun isPlateBookmarked(plateNumber: String, currentUserId: String, onResult: (Boolean) -> Unit) {
        val db = Firebase.firestore
        val userRef = db.collection("profiles").document(currentUserId)

        userRef.get().addOnSuccessListener { document ->
            val bookmarkedPlates = document.get("bookmarkedPlates") as? List<String> ?: emptyList()
            onResult(bookmarkedPlates.contains(plateNumber))
        }.addOnFailureListener { e ->
            println("Error checking bookmarked plates: ${e.message}")
            onResult(false)
        }
    }
    fun ensureUserProfileExists(currentUserId: String, onProfileExists: () -> Unit) {
        val db = Firebase.firestore
        val userRef = db.collection("profiles").document(currentUserId)

        userRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                // Create a new profile document with an empty bookmarkedPlates list
                val newUserProfile = hashMapOf(
                    "bookmarkedPlates" to mutableListOf<String>()
                )
                userRef.set(newUserProfile).addOnSuccessListener {
                    // Profile created successfully
                    onProfileExists()
                }.addOnFailureListener { e ->
                    println("Error creating user profile: ${e.message}")
                }
            } else {
                // Profile already exists
                onProfileExists()
            }
        }.addOnFailureListener { e ->
            println("Error checking user profile: ${e.message}")
        }
    }
    fun getBookmarkedPlates(userId: String, onResult: (List<String>) -> Unit) {
        val db = Firebase.firestore
        val userRef = db.collection("profiles").document(userId)

        userRef.get().addOnSuccessListener { document ->
            val plates = document.get("bookmarkedPlates") as? List<String> ?: emptyList()
            onResult(plates)
        }.addOnFailureListener { e ->
            println("Error fetching bookmarked plates: ${e.message}")
            onResult(emptyList())
        }
    }
    fun getParsedComment(commentId: String, data: Map<String, Any>?, user: FirebaseUser?): CommentItem {
        return parseComment(commentId, data, user)
    }

    fun addSearchToHistory(userId: String, plateNumber: String) {
        val db = Firebase.firestore
        val userRef = db.collection("profiles").document(userId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val searchHistory = snapshot.get("searchHistory") as? MutableList<String> ?: mutableListOf()

            searchHistory.remove(plateNumber)
            searchHistory.add(0, plateNumber)

            if (searchHistory.size > 10) {
                searchHistory.removeLast()
            }

            transaction.update(userRef, "searchHistory", searchHistory)
        }.addOnSuccessListener {
            println("Arama geçmişine eklendi: $plateNumber")
        }.addOnFailureListener { e ->
            println("Arama geçmişine eklenirken hata: ${e.message}")
        }
    }




}
