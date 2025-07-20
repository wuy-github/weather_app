package com.example.weatherassistant.data.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

fun saveFCMTokenAndLocationToFirestore(token: String, lat: Double, lon: Double) {
    // Step 1: Determine database:
    val db = FirebaseFirestore.getInstance()
    // Step 2: Get User If=d
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous_user"
    // Step 3: Determine User reference in the Firebase store:
    val userRef = db.collection("users").document(userId)
    // Step 4: Construct datas to become a specific type:
    val userData = mapOf(
        "fcmToken" to token,
        "latitude" to lat,
        "longitude" to lon,
        "lastUpdated" to System.currentTimeMillis()
    )

    // After we knew what reference of our data storage, we'll come to pass and save our new data into it at Step 5:
    // Step 5: Pass and save data:
    val mergeOptions = SetOptions.merge()
    userRef.set(userData, mergeOptions)
        .addOnSuccessListener {
            Log.d("Firestore", "💕💕💕 FCM Token and location (merged) saved successfully for $userId")
        }
        .addOnFailureListener { error ->
            Log.e("Firestore", "Error saving FCM Token and location (merged)", error)
        }
}
