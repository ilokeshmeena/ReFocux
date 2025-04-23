package com.rexvit.refocux.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.rexvit.refocux.data.model.UserStats
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseService @Inject constructor() {
    val auth: FirebaseAuth = Firebase.auth
    val firestore: FirebaseFirestore = Firebase.firestore

    // Sync user data to Firestore
    suspend fun syncUserData(userId: String, stats: UserStats) {
        firestore.collection("users").document(userId)
            .set(stats)
            .addOnSuccessListener { /* Success */ }
            .addOnFailureListener { /* Handle error */ }
    }

    // Get user data from Firestore
    suspend fun getUserData(userId: String): UserStats? {
        return try {
            val document = firestore.collection("users").document(userId).get().await()
            document.toObject(UserStats::class.java)
        } catch (e: Exception) {
            null
        }
    }
}