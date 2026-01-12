package com.example.movilog.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.movilog.data.model.Movie

// 1. Tell Room this is a Database, which entities (tables) to include,
// and the version number (start at 1).
@Database(entities = [Movie::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // 2. Connect the "Remote Control" (DAO) we made earlier.
    abstract fun movieDao(): MovieDao

    // 3. This part is like a "Security Guard" (Singleton).
    // It makes sure we only ever create ONE database to save phone memory.
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // If the database exists, return it. If not, create it.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "movie_database" // This is the actual name of the file on the phone
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}