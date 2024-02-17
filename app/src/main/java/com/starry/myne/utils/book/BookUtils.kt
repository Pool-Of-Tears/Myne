/**
 * Copyright (c) [2022 - Present] Stɑrry Shivɑm
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.starry.myne.utils.book

import com.starry.myne.repo.models.Author
import java.util.Locale

object BookUtils {
    fun getAuthorsAsString(authors: List<Author>): String {
        return if (authors.isNotEmpty()) {
            var result: String
            if (authors.size > 1) {
                result = fixAuthorName(authors.first().name)
                authors.slice(1 until authors.size).forEach { author ->
                    if (author.name != "N/A")
                        result += ", ${fixAuthorName(author.name)}"
                }
            } else {
                result = fixAuthorName(authors.first().name)
            }
            result
        } else {
            "Unknown Author"
        }
    }

    /**
     * For some weird reasons, gutenberg gives name of authors in
     * reversed, where first name and last are separated by a comma
     * Eg: "Fyodor Dostoyevsky" becomes "Dostoyevsky, Fyodor", This
     * function fixes that and returns name in correct format.
     */
    private fun fixAuthorName(name: String): String {
        val reversed = name.split(",").reversed()
        return reversed.joinToString(separator = " ") {
            return@joinToString it.trim()
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