package pl.techbrewery.sam.kmp.database

import androidx.room.RoomDatabase

expect fun getDatabaseBuilder(): RoomDatabase.Builder<KmpDatabase>