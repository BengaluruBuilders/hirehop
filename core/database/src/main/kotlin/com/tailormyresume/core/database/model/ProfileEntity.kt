package com.tailormyresume.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey
    val id: Int = SINGLETON_ID,
    val fullName: String,
    val email: String,
    val phone: String,
    val headline: String,
    val skills: List<String>,
    @ColumnInfo(defaultValue = "'[]'")
    val userStatedSkills: List<String> = emptyList(),
) {
    companion object {
        const val SINGLETON_ID = 1
    }
}
