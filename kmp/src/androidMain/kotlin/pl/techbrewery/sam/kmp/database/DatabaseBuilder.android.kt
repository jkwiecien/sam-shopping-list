package pl.techbrewery.sam.kmp.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

private lateinit var appContext: Context

actual fun getDatabaseBuilder(): RoomDatabase.Builder<KmpDatabase> {
    val appContext: Context = appContext
    val dbFile = appContext.getDatabasePath("sam_room.db")
    return Room.databaseBuilder<KmpDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
        .setDriver(BundledSQLiteDriver())
        .fallbackToDestructiveMigration(true)
}

fun initKmpModule(context: Context) {
    appContext = context
}