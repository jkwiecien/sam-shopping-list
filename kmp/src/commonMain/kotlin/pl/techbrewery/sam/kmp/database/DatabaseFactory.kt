package pl.techbrewery.sam.kmp.database

import androidx.room.RoomDatabase
import kotlinx.coroutines.Dispatchers

fun getRoomDatabase(builder: RoomDatabase.Builder<KmpDatabase>): KmpDatabase {
    return builder
        // Add migrations here if you have them
        // .addMigrations(MIGRATION_1_2)
        .fallbackToDestructiveMigrationOnDowngrade() //fixme For development, handle migrations properly in production
        .setQueryCoroutineContext(Dispatchers.IO) // Set coroutine context for queries
        .build()
}