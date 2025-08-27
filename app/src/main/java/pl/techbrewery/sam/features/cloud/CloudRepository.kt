package pl.techbrewery.sam.features.cloud

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import pl.techbrewery.sam.kmp.database.entity.SingleItem
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CloudRepository {
    companion object {
        private const val SINGLE_ITEMS_COLLECTION = "single_items"
    }

    private val firestore = Firebase.firestore
    private val user get() = Firebase.auth.currentUser!!

    fun saveSingleItem(singleItem: SingleItem) {


        firestore.collection(SINGLE_ITEMS_COLLECTION)
            .document(singleItem.id)
            .set(singleItem, SetOptions.merge())
            .addOnSuccessListener { Timber.d("DocumentSnapshot successfully written!") }
            .addOnFailureListener { e -> Timber.w(e, "Error writing document") }
    }

    suspend fun getSingleItem(cloudId: String) = suspendCoroutine { continuation ->
        firestore.collection(SINGLE_ITEMS_COLLECTION)
            .document(cloudId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val d = documentSnapshot.data
            }
            .addOnFailureListener { continuation.resumeWithException(it) }
    }
}

