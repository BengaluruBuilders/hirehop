package com.tailormyresume.core.database

import android.content.ContentValues
import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ProfileMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), TmrDatabase::class.java)

    @Test
    fun migration1To2KeepsTheProfileRowAndStartsWithNoUserStatedSkills() {
        helper.createDatabase(DB_NAME, 1).apply {
            insert(
                "profile",
                android.database.sqlite.SQLiteDatabase.CONFLICT_FAIL,
                ContentValues().apply {
                    put("id", 1)
                    put("fullName", "Priya")
                    put("email", "p@example.com")
                    put("phone", "1")
                    put("headline", "h")
                    put("skills", "[\"SQL\",\"Excel\"]")
                },
            )
            close()
        }

        val migrated = helper.runMigrationsAndValidate(DB_NAME, 2, true, MIGRATION_1_2)

        migrated.query("SELECT fullName, skills, userStatedSkills FROM profile").use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getString(0)).isEqualTo("Priya")
            assertThat(cursor.getString(1)).isEqualTo("[\"SQL\",\"Excel\"]")
            assertThat(cursor.getString(2)).isEqualTo("[]")
        }
    }

    private companion object {
        const val DB_NAME = "migration-test"
    }
}
