package pl.techbrewery.sam.shared

import pl.techbrewery.sam.kmp.model.SuggestedItem

object KeyboardDonePressed
class SearchQueryChanged(val query: String)
object BottomSheetDismissRequested
class ToastRequested(val message: String)
class SuggestedItemDeletePressed(val item: SuggestedItem)
class OnItemTextFieldFocusChanged(val focused: Boolean)