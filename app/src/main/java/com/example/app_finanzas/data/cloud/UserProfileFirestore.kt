package com.example.app_finanzas.data.cloud

import com.example.app_finanzas.data.user.UserProfile
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

private const val USERS_COLLECTION = "users"

class UserProfileFirestore(private val firestore: FirebaseFirestore) {

    suspend fun upsertProfile(profile: UserProfile, provider: String) {
        val documentRef = firestore.collection(USERS_COLLECTION).document(profile.uid)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(documentRef)
            val data = mutableMapOf<String, Any?>(
                "email" to profile.email,
                "name" to profile.name,
                "provider" to provider,
                "lastLoginAt" to FieldValue.serverTimestamp()
            )
            if (!snapshot.exists()) {
                data["createdAt"] = FieldValue.serverTimestamp()
            }
            transaction.set(documentRef, data, SetOptions.merge())
        }.await()
    }
}
