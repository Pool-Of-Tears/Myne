package com.starry.myne.utils

import com.starry.myne.api.models.Author
import java.util.*

object Utils {
    fun getAuthorsAsString(authors: List<Author>): String {
        return if (authors.isNotEmpty()) {
            var result: String
            if (authors.size > 1) {
                result = authors.first().name
                authors.slice(1 until authors.size).forEach { author ->
                    if (author.name != "N/A")
                        result += ", ${author.name}"
                }
            } else {
                result = authors.first().name
            }
            result
        } else {
            "Unknown Author"
        }
    }

    fun getLanguagesAsString(languages: List<String>): String {
        var result = ""
        languages.forEachIndexed { index, lang ->
            val loc = Locale(lang)
            if (index == 0) {
                result = loc.displayLanguage
            } else {
                result += ", ${loc.displayLanguage}"
            }
        }
        return result
    }

    fun getSubjectsAsString(subjects: List<String>, limit: Int): String {
        val allSubjects = ArrayList<String>()
        // strip "--" from subjects.
        subjects.forEach { subject ->
            if (subject.contains("--")) {
                allSubjects.addAll(subject.split("--"))
            } else {
                allSubjects.add(subject)
            }
        }
        val truncatedSubs: List<String> = if (allSubjects.size > limit) {
            allSubjects.toSet().toList().subList(0, limit)
        } else {
            allSubjects.toSet().toList()
        }
        return truncatedSubs.joinToString(separator = ", ") {
            return@joinToString it.trim()
        }
    }
}