package pl.techbrewery.sam.features.stores.editor

import pl.techbrewery.sam.kmp.database.entity.Store

internal class StoreNameChanged(val name: String)
internal object SaveStorePressed
internal class StoreDepartmentMoved(val from: Int, val to: Int)