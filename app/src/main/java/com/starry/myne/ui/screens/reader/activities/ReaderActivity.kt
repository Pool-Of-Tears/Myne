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

package com.starry.myne.ui.screens.reader.activities

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.starry.myne.R
import com.starry.myne.databinding.ActivityReaderBinding
import com.starry.myne.ui.screens.reader.adapters.ReaderClickListener
import com.starry.myne.ui.screens.reader.adapters.ReaderRVAdapter
import com.starry.myne.ui.screens.reader.composables.ReaderScreen
import com.starry.myne.ui.screens.reader.composables.TransparentSystemBars
import com.starry.myne.ui.screens.reader.viewmodels.ReaderViewModel
import com.starry.myne.ui.screens.settings.viewmodels.SettingsViewModel
import com.starry.myne.ui.theme.MyneTheme
import com.starry.myne.utils.toToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.FileInputStream
import kotlin.properties.Delegates

@AndroidEntryPoint
@ExperimentalMaterial3Api
@ExperimentalMaterialApi
class ReaderActivity : AppCompatActivity(), ReaderClickListener {

    companion object {
        const val EXTRA_BOOK_ID = "reader_book_id"
        const val EXTRA_CHAPTER_IDX = "reader_chapter_index"
        private const val DEFAULT_NONE = -100000

    }

    private lateinit var binding: ActivityReaderBinding
    lateinit var settingsViewModel: SettingsViewModel
    private val viewModel: ReaderViewModel by viewModels()

    // Weather book was opened from external epub file.
    private var isExternalBook by Delegates.notNull<Boolean>()

    // Flow which stores currently visible chapter index.
    private val visibleChapterFlow = MutableStateFlow(0)

    // Store recycler view position and offset when activity
    // gets paused, so we can restore it on orientation changes.
    private var mRVPositionAndOffset: Pair<Int, Int>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReaderBinding.inflate(layoutInflater)

        // Fullscreen mode that ignores any cutout, notch etc.
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.displayCutout())
        controller.hide(WindowInsetsCompat.Type.systemBars())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        // Set app theme.
        settingsViewModel = ViewModelProvider(this)[SettingsViewModel::class.java]

        // Setup reader's recycler view.
        val adapter = ReaderRVAdapter(
            activity = this,
            viewModel = viewModel,
            clickListener = this
        )
        binding.readerRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.readerRecyclerView.adapter = adapter
        val layoutManager = (binding.readerRecyclerView.layoutManager as LinearLayoutManager)

        // Fetch data from intent.
        val bookId = intent.extras?.getInt(EXTRA_BOOK_ID, DEFAULT_NONE)
        val chapterIndex = intent.extras?.getInt(EXTRA_CHAPTER_IDX, DEFAULT_NONE)
        isExternalBook = intent.type == "application/epub+zip"

        // Internal book
        if (bookId != null && bookId != DEFAULT_NONE) {
            // Load epub book from given id and set chapters as items in
            // reader's recycler view adapter.
            viewModel.loadEpubBook(bookId = bookId, onLoaded = {
                adapter.allChapters = it.epubBook!!.chapters
                // if there is saved progress for this book, then scroll to
                // last page at exact position were used had left.
                if (it.readerData != null && chapterIndex == DEFAULT_NONE) {
                    layoutManager.scrollToPositionWithOffset(
                        it.readerData.lastChapterIndex,
                        it.readerData.lastChapterOffset
                    )
                }
            })
            // if user clicked on specific chapter, then scroll to
            // that chapter directly.
            if (chapterIndex != null && chapterIndex != DEFAULT_NONE) {
                layoutManager.scrollToPosition(chapterIndex)
            }

            // External book.
        } else if (isExternalBook) {
            intent?.data?.let {
                contentResolver.openInputStream(it)?.let { ips ->
                    viewModel.loadEpubBookExternal(ips as FileInputStream, onLoaded = { epubBook ->
                        adapter.allChapters = epubBook.chapters
                        ips.close()
                    })
                }
            }
        } else {
            getString(R.string.error).toToast(this, Toast.LENGTH_LONG)
            finish()
        }

        // Listener for updating reading progress in database.
        binding.readerRecyclerView.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                // fetch last visible chapter position and offset.
                val progressChIndex = layoutManager.findFirstVisibleItemPosition()
                val progressChOffset = layoutManager.findViewByPosition(progressChIndex)!!.top
                // Emit currently visible chapter.
                visibleChapterFlow.value = progressChIndex
                // If book was not opened from external epub file, update the
                // reading progress into the database.
                if (!isExternalBook) {
                    viewModel.updateReaderProgress(
                        bookId = bookId!!,
                        chapterIndex = progressChIndex,
                        chapterOffset = progressChOffset
                    )
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
                // Calculate the scroll percentage for the currently visible chapter
                // and store its value in view model.
                viewModel.setItemScrolledPercent(
                    calculateItemScrollPercentage(recyclerView, firstVisiblePosition)
                )
            }
        })

        // Set UI contents.
        setContent {
            MyneTheme(settingsViewModel = settingsViewModel) {
                TransparentSystemBars(settingsViewModel = settingsViewModel)
                ReaderScreen(
                    viewModel = viewModel,
                    recyclerViewManager = layoutManager,
                    visibleChapterFlow = visibleChapterFlow,
                    readerContent = { AndroidView(factory = { binding.root }) }
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        val layoutManager = (binding.readerRecyclerView.layoutManager as LinearLayoutManager)
        val currentPosition = layoutManager.findLastVisibleItemPosition()
        layoutManager.findViewByPosition(currentPosition)?.let {
            mRVPositionAndOffset = Pair(currentPosition, it.top)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Handler(Looper.getMainLooper()).postDelayed({
            if (mRVPositionAndOffset != null) {
                val layoutManager =
                    (binding.readerRecyclerView.layoutManager as LinearLayoutManager)
                layoutManager.scrollToPositionWithOffset(
                    mRVPositionAndOffset!!.first,
                    mRVPositionAndOffset!!.second
                )
            }
        }, 50)
    }

    override fun onReaderClick() {
        if (!viewModel.state.showReaderMenu) {
            viewModel.showReaderInfo()
        } else {
            viewModel.hideReaderInfo()
        }
    }

    /**
     * Calculate the scroll percentage for a specific item in a RecyclerView.
     *
     * @param recyclerView The RecyclerView containing the item
     * @param itemPosition The position of the item in the RecyclerView
     * @return The scroll percentage for the item, or -1 if the item is not visible
     */
    fun calculateItemScrollPercentage(recyclerView: RecyclerView, itemPosition: Int): Float {
        val layoutManager = recyclerView.layoutManager!!
        // Find the view corresponding to the item position
        val itemView = layoutManager.findViewByPosition(itemPosition) ?: return -1f
        // Calculate the vertical offset of the item relative to the RecyclerView
        val itemTop = itemView.top.toFloat()
        val itemBottom = itemView.bottom.toFloat()
        // Calculate the visible height of the RecyclerView
        val recyclerViewHeight = recyclerView.height.toFloat()
        // Calculate the height of the item
        val itemHeight = itemView.height.toFloat()
        // Calculate the scroll percentage for the item
        return if (itemTop >= recyclerViewHeight || itemBottom <= 0f) {
            1f // Item is completely scrolled out of view
        } else {
            // Calculate the visible portion of the item
            val visiblePortion = if (itemTop < 0) {
                itemBottom
            } else {
                recyclerViewHeight - itemTop
            }
            // Calculate the scroll percentage based on the visible portion
            ((1 - visiblePortion / itemHeight)).coerceIn(0f, 1f)
        }
    }


}