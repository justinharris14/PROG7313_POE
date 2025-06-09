package com.example.bybetterbudget.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

object FirestoreHelper {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }


    fun getUid(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User not signed in")
    }


    suspend fun saveDocument(
        collectionName: String,
        data: Map<String, Any>,
        docId: String? = null
    ) {
        val uid = getUid()
        val docRef = if (docId != null) {
            firestore.collection("users")
                .document(uid)
                .collection(collectionName)
                .document(docId)
        } else {
            firestore.collection("users")
                .document(uid)
                .collection(collectionName)
                .document()
        }
        docRef.set(data, SetOptions.merge()).await()
    }
}
