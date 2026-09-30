package com.hirehop.core.data.model

import com.hirehop.core.database.json.EvidenceBulletDto
import com.hirehop.core.database.model.PopulatedProfile
import com.hirehop.core.database.model.ProfileEntity
import com.hirehop.core.database.model.ProfileEntryEntity
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.EvidenceBullet
import com.hirehop.core.model.ProfileEntry

fun EvidenceBullet.asDto() = EvidenceBulletDto(id = id, text = text)

fun EvidenceBulletDto.asExternalModel() = EvidenceBullet(id = id, text = text)

fun CandidateProfile.asEntity() = ProfileEntity(
    fullName = fullName,
    email = email,
    phone = phone,
    headline = headline,
    skills = skills,
)

fun ProfileEntry.asEntity(position: Int) = ProfileEntryEntity(
    id = id,
    position = position,
    category = category,
    title = title,
    organization = organization,
    startDate = startDate,
    endDate = endDate,
    bullets = bullets.map(EvidenceBullet::asDto),
    source = source,
    isConfirmed = isConfirmed,
)

fun CandidateProfile.asEntryEntities(): List<ProfileEntryEntity> =
    entries.mapIndexed { index, entry -> entry.asEntity(position = index) }

fun ProfileEntryEntity.asExternalModel() = ProfileEntry(
    id = id,
    category = category,
    title = title,
    organization = organization,
    startDate = startDate,
    endDate = endDate,
    bullets = bullets.map(EvidenceBulletDto::asExternalModel),
    source = source,
    isConfirmed = isConfirmed,
)

fun PopulatedProfile.asExternalModel() = CandidateProfile(
    fullName = profile.fullName,
    email = profile.email,
    phone = profile.phone,
    headline = profile.headline,
    skills = profile.skills,
    entries = entries
        .sortedBy(ProfileEntryEntity::position)
        .map(ProfileEntryEntity::asExternalModel),
)
