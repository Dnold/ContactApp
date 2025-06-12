package com.example.contactapp.data // This declares the package for AppDatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.contactapp.model.User // Import for User model (different package)


// UserDao is expected to be in the same package 'com.example.contactapp.data',
// so no explicit import for it is needed here.

@Database(entities = [User::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Abstract method to get the UserDao.
     * Room will generate the implementation for this.
     * The UserDao interface itself must also be in the 'com.example.contactapp.data' package
     * or be explicitly imported if it were in a different package.
     */
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "user_database" // Name for your database file
                )
                    // It's good practice to handle migrations properly rather than always
                    // falling back to destructive migration, especially for released apps.
                    .fallbackToDestructiveMigration() // Clears database on schema change if no migration path found
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}