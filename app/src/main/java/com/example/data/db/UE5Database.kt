package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [LevelEntity::class], version = 1, exportSchema = false)
abstract class UE5Database : RoomDatabase() {
    abstract fun levelDao(): LevelDao

    companion object {
        @Volatile
        private var INSTANCE: UE5Database? = null

        fun getInstance(context: Context): UE5Database {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UE5Database::class.java,
                    "ue5_editor.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
