package pl.techbrewery.sam.features.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.techbrewery.sam.features.auth.AUTH_LOG_TAG
import pl.techbrewery.sam.features.auth.AuthRepository
import pl.techbrewery.sam.features.auth.GoogleSignInPressed
import pl.techbrewery.sam.features.auth.ToggleAuthModal
import pl.techbrewery.sam.shared.BaseViewModel

class NavigationViewModel(
    private val auth: AuthRepository
) : BaseViewModel() {

    var showAuthModal: Boolean by mutableStateOf(false)
        private set

    private fun signIn() {
        showAuthModal = false
        viewModelScope.launch(Dispatchers.Main + CoroutineExceptionHandler { _, error ->
            Napier.e(error, AUTH_LOG_TAG) {error.message ?: "Error while signing in"}
        }) {
            auth.signIn()
        }
    }

    override fun onAction(action: Any) {
        when (action) {
            is ToggleAuthModal -> showAuthModal = action.showModal
            is GoogleSignInPressed -> signIn()
        }
    }

}