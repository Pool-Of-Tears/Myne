package com.starry.myne.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface LibraryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(libraryItem: LibraryItem)

    @Delete
    fun delete(libraryItem: LibraryItem)

    @Query("SELECT * FROM book_library ORDER BY id ASC")
    fun getAllItems(): LiveData<List<LibraryItem>>
}