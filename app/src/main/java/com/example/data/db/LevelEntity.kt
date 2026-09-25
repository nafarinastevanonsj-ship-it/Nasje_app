package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "levels")
data class LevelEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val lastModified: Long,
    val actorsJson: String,
    val blueprintJson: String = "",
    val materialJson: String = ""
)
