package com.tailormyresume.core.domain.offline

import com.tailormyresume.core.model.EditType

internal data class Rewrite(val text: String, val editTypes: List<EditType>)

internal class BulletRewriter(private val jobKeywords: Set<String>) {
    fun rewrite(source: String): Rewrite {
        val editTypes = mutableListOf<EditType>()
        var text = source
        text = apply(text, EditType.REWORD, editTypes) { AliasRewriter.rewrite(it, jobKeywords) }
        text = apply(text, EditType.SHORTEN, editTypes, FillerRemover::shorten)
        text = apply(text, EditType.EMPHASISE, editTypes) { ClauseEmphasiser.emphasise(it, jobKeywords) }
        return Rewrite(text, editTypes)
    }

    private fun apply(
        text: String,
        editType: EditType,
        editTypes: MutableList<EditType>,
        transform: (String) -> String,
    ): String {
        val result = transform(text)
        if (result.withoutExtraSpace() == text.withoutExtraSpace()) return text
        editTypes += editType
        return result
    }

    private fun String.withoutExtraSpace(): String = trim().replace(WHITESPACE, " ")

    private companion object {
        val WHITESPACE = Regex("\\s+")
    }
}
