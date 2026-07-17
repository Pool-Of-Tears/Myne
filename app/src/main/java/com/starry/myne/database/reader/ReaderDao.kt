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
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReaderDao {

    // --- Progress related methods (moved from ProgressDao) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(readerProgress: ReaderProgress)

    @Query("DELETE FROM reader_table WHERE library_item_id = :libraryItemId")
    fun delete(libraryItemId: Int)

    @Update
    fun update(readerProgress: ReaderProgress)

    @Query("SELECT * FROM reader_table WHERE library_item_id = :libraryItemId")
    fun getReaderData(libraryItemId: Int): ReaderProgress?

    @Query("SELECT * FROM reader_table")
    fun getAllReaderItems(): List<ReaderProgress>

    @Query("SELECT * FROM reader_table WHERE library_item_id = :libraryItemId")
    fun getReaderDataAsFlow(libraryItemId: Int): Flow<ReaderProgress?>

    // --- Bookmark related methods ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: ReaderBookmark)

    @Query("DELETE FROM bookmarks_table WHERE id = :id")
    suspend fun deleteBookmark(id: Int)

    @Query("SELECT * FROM bookmarks_table WHERE book_id = :bookId ORDER BY created_at DESC")
    fun getBookmarksForBook(bookId: Int): Flow<List<ReaderBookmark>>
}
