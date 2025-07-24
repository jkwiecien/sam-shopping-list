package pl.techbrewery.sam.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel: ViewModel() {
    protected val actionsMutableFlow = MutableSharedFlow<Any>()
    val actionsFlow = actionsMutableFlow.asSharedFlow()
    open fun onAction(action: Any) {
        // Default implementation does nothing
        // Subclasses can override this method to handle actions
    }

    fun emitSingleAction(action: Any) {
        viewModelScope.launch { actionsMutableFlow.emit(action) }
    }
}