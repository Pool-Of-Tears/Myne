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

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ReaderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(readerItem: ReaderItem)

    @Query("DELETE FROM reader_table WHERE book_id = :bookId")
    fun delete(bookId: Int)

    @Query(
        "UPDATE reader_table SET "
                + "last_chapter_index = :lastChapterIndex,"
                + "last_chapter_offset = :lastChapterOffset"
                + " WHERE  book_id = :bookId"
    )
    fun update(bookId: Int, lastChapterIndex: Int, lastChapterOffset: Int)

    @Query("SELECT * FROM reader_table WHERE book_id = :bookId")
    fun getReaderItem(bookId: Int): ReaderItem?
}