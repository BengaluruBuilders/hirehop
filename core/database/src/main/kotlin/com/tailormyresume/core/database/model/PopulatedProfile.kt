package com.tailormyresume.core.database.model

import androidx.room.Embedded
import androidx.room.Relation

data class PopulatedProfile(
    @Embedded
    val profile: ProfileEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "profileId",
        entity = ProfileEntryEntity::class,
    )
    val entries: List<ProfileEntryEntity>,
)
