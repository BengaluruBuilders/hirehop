package com.tailormyresume.core.data.model

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.database.model.PopulatedProfile
import com.tailormyresume.core.database.model.ProfileEntity
import com.tailormyresume.core.model.CandidateProfile
import org.junit.Test

class ProfileMappersTest {

    @Test
    fun profileRoundTripsThroughEntities() {
        val populated = PopulatedProfile(
            profile = testProfile.asEntity(),
            entries = testProfile.asEntryEntities(),
        )

        assertThat(populated.asExternalModel()).isEqualTo(testProfile)
    }

    @Test
    fun entryEntitiesKeepListOrderAsPosition() {
        val positions = testProfile.asEntryEntities().map { it.position }

        assertThat(positions).containsExactly(0, 1, 2).inOrder()
    }

    @Test
    fun externalModelRestoresOrderFromShuffledEntities() {
        val populated = PopulatedProfile(
            profile = testProfile.asEntity(),
            entries = testProfile.asEntryEntities().reversed(),
        )

        assertThat(populated.asExternalModel().entries).isEqualTo(testEntries)
    }

    @Test
    fun profileWithoutEntriesRoundTrips() {
        val profile = CandidateProfile(
            fullName = "Empty",
            email = "",
            phone = "",
            headline = "",
            skills = emptyList(),
            entries = emptyList(),
        )
        val populated = PopulatedProfile(profile.asEntity(), profile.asEntryEntities())

        assertThat(populated.asExternalModel()).isEqualTo(profile)
    }

    @Test
    fun profileEntityUsesSingletonId() {
        assertThat(testProfile.asEntity().id).isEqualTo(ProfileEntity.SINGLETON_ID)
    }

    @Test
    fun entryEntitiesPointToSingletonProfile() {
        val profileIds = testProfile.asEntryEntities().map { it.profileId }.distinct()

        assertThat(profileIds).containsExactly(ProfileEntity.SINGLETON_ID)
    }

    @Test
    fun evidenceBulletRoundTrips() {
        val bullet = testEntries.first().bullets.first()

        assertThat(bullet.asDto().asExternalModel()).isEqualTo(bullet)
    }
}
