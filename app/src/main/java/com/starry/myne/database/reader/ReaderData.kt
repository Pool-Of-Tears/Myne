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


package com.starry.myne.database.reader

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reader_table")
data class ReaderData(
    @ColumnInfo(name = "library_item_id") val libraryItemId: Int,
    @ColumnInfo(name = "last_chapter_index") val lastChapterIndex: Int,
    @ColumnInfo(name = "last_chapter_offset") val lastChapterOffset: Int
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    fun getProgressPercent(totalChapters: Int) =
        String.format("%.2f", ((lastChapterIndex + 1).toFloat() / totalChapters.toFloat()) * 100f)
}