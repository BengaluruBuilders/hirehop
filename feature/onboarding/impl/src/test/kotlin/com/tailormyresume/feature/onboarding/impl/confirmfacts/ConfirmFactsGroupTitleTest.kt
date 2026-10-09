package com.tailormyresume.feature.onboarding.impl.confirmfacts

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.feature.onboarding.impl.R
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConfirmFactsGroupTitleTest {

    private val resources = ApplicationProvider.getApplicationContext<android.content.Context>().resources

    @Test
    fun groupTitle_isPluralCorrect() {
        val one = resources.getQuantityString(R.plurals.feature_onboarding_impl_confirm_facts_confirmed_group_title, 1, 1)
        val many = resources.getQuantityString(R.plurals.feature_onboarding_impl_confirm_facts_confirmed_group_title, 3, 3)

        assertThat(one).isEqualTo("1 fact confirmed")
        assertThat(many).isEqualTo("3 facts confirmed")
    }
}
