package com.hirehop.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.hirehop.core.database.model.PopulatedProfile
import com.hirehop.core.database.model.ProfileEntity
import com.hirehop.core.database.model.ProfileEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ProfileDao {
    @Transaction
    @Query("SELECT * FROM profile WHERE id = ${ProfileEntity.SINGLETON_ID}")
    abstract fun observePopulatedProfile(): Flow<PopulatedProfile?>

    @Upsert
    abstract suspend fun upsertProfile(profile: ProfileEntity)

    @Upsert
    abstract suspend fun upsertEntries(entries: List<ProfileEntryEntity>)

    @Query("DELETE FROM profile_entries WHERE profileId = ${ProfileEntity.SINGLETON_ID}")
    abstract suspend fun deleteEntries()

    @Query("DELETE FROM profile")
    abstract suspend fun deleteProfile()

    @Transaction
    open suspend fun replaceProfile(profile: ProfileEntity, entries: List<ProfileEntryEntity>) {
        upsertProfile(profile)
        deleteEntries()
        upsertEntries(entries)
    }
}
