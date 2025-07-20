package pl.techbrewery.sam.shared

import androidx.lifecycle.ViewModel

abstract class BaseViewModel: ViewModel() {
    open fun onAction(action: Any) {
        // Default implementation does nothing
        // Subclasses can override this method to handle actions
    }
}