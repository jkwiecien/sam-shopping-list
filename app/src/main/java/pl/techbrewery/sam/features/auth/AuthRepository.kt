package pl.techbrewery.sam.features.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import io.github.aakira.napier.Napier
import pl.techbrewery.sam.R

const val AUTH_LOG_TAG = "SAM-auth"

class AuthRepository(
    private val context: Context
) {
    private val credentialManager = CredentialManager.create(context)
    private var firebaseAuth = Firebase.auth

    fun isSignedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    suspend fun signIn() {
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.google_web_client_id))
            .setAutoSelectEnabled(true)
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(
            request = request,
            context = context,
        )
        handleSignIn(result)
    }

    private fun handleSignIn(result: GetCredentialResponse) {
        // Handle the successfully returned credential.
        val credential = result.credential

        when (credential) {
            // GoogleIdToken credential
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential =
                        GoogleIdTokenCredential.createFrom(credential.data)
                    // Sign in to Firebase with using the token
                    firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
                } else {
                    // Catch any unrecognized custom credential type here.
                    throw IllegalArgumentException("Failed to recognize credentials. Credential type: ${credential.type}")
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Napier.d(tag = AUTH_LOG_TAG) { "signInWithCredential:success" }
                    val user = firebaseAuth.currentUser
                } else {
                    // If sign in fails, display a message to the user
                    throw FirebaseAuthException(
                        "firebaseAuthWithGoogle",
                        "Failed to sign in to Firebase"
                    )
                }
            }
    }
}