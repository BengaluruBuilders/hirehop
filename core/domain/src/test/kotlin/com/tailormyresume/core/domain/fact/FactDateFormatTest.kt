package com.tailormyresume.core.domain.fact

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FactDateFormatTest {
    @Test
    fun blankTextIsReadable() {
        assertThat(FactDateFormat.isReadable("")).isTrue()
    }

    @Test
    fun whitespaceOnlyTextIsReadable() {
        assertThat(FactDateFormat.isReadable("   ")).isTrue()
    }

    @Test
    fun shortMonthAndYearIsReadable() {
        assertThat(FactDateFormat.isReadable("Aug 2024")).isTrue()
    }

    @Test
    fun fullMonthAndYearIsReadable() {
        assertThat(FactDateFormat.isReadable("August 2024")).isTrue()
    }

    @Test
    fun dayMonthAndYearIsReadable() {
        assertThat(FactDateFormat.isReadable("12 Aug 2024")).isTrue()
    }

    @Test
    fun bareYearIsReadable() {
        assertThat(FactDateFormat.isReadable("2024")).isTrue()
    }

    @Test
    fun yearAndMonthIsReadable() {
        assertThat(FactDateFormat.isReadable("2024-08")).isTrue()
    }

    @Test
    fun yearAndMonthWithSlashIsReadable() {
        assertThat(FactDateFormat.isReadable("2024/08")).isTrue()
    }

    @Test
    fun presentIsReadable() {
        assertThat(FactDateFormat.isReadable("Present")).isTrue()
    }

    @Test
    fun lowercasePresentIsReadable() {
        assertThat(FactDateFormat.isReadable("present")).isTrue()
    }

    @Test
    fun currentIsReadable() {
        assertThat(FactDateFormat.isReadable("Current")).isTrue()
    }

    @Test
    fun nowIsReadable() {
        assertThat(FactDateFormat.isReadable("Now")).isTrue()
    }

    @Test
    fun ongoingIsReadable() {
        assertThat(FactDateFormat.isReadable("Ongoing")).isTrue()
    }

    @Test
    fun twoYearsRunTogetherIsNotReadable() {
        assertThat(FactDateFormat.isReadable("Aug 20242023")).isFalse()
    }

    @Test
    fun yearWithTrailingDigitsIsNotReadable() {
        assertThat(FactDateFormat.isReadable("Aug 20242")).isFalse()
    }

    @Test
    fun monthThirteenIsNotReadable() {
        assertThat(FactDateFormat.isReadable("2025-13")).isFalse()
    }

    @Test
    fun freeTextSeasonIsNotReadable() {
        assertThat(FactDateFormat.isReadable("Summer internship")).isFalse()
    }

    @Test
    fun lettersOnlyIsNotReadable() {
        assertThat(FactDateFormat.isReadable("abc")).isFalse()
    }

    @Test
    fun sixDigitRunIsNotReadable() {
        assertThat(FactDateFormat.isReadable("20242023")).isFalse()
    }

    @Test
    fun appProduced_tillDate_isReadable() {
        assertThat(FactDateFormat.isReadable("Till date")).isTrue()
    }

    @Test
    fun appProduced_toDate_isReadable() {
        assertThat(FactDateFormat.isReadable("To date")).isTrue()
    }

    @Test
    fun appProduced_lowercaseTillDate_isReadable() {
        assertThat(FactDateFormat.isReadable("till date")).isTrue()
    }

    @Test
    fun appProduced_twoDigitYear_isReadable() {
        assertThat(FactDateFormat.isReadable("23")).isTrue()
    }

    @Test
    fun appProduced_curlyApostropheMonthYear_isReadable() {
        assertThat(FactDateFormat.isReadable("Jul’2024")).isTrue()
    }

    @Test
    fun appProduced_monthYearWithoutSpace_isReadable() {
        assertThat(FactDateFormat.isReadable("Jul2024")).isTrue()
    }

    @Test
    fun appProduced_monthSlashYear_isReadable() {
        assertThat(FactDateFormat.isReadable("06/2021")).isTrue()
    }

    @Test
    fun twoDigitStartDate_isNotReadable() {
        assertThat(FactDateFormat.isReadable("12", isEnd = false)).isFalse()
    }

    @Test
    fun monthYearStartDate_isReadable() {
        assertThat(FactDateFormat.isReadable("Aug 2024", isEnd = false)).isTrue()
    }

    @Test
    fun twoDigitEndDate_isReadable() {
        assertThat(FactDateFormat.isReadable("23", isEnd = true)).isTrue()
    }
}
