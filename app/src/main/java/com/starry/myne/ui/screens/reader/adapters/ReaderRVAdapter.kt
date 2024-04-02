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

package com.starry.myne.ui.screens.reader.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.starry.myne.R
import com.starry.myne.epub.BookTextMapper
import com.starry.myne.epub.models.EpubChapter
import com.starry.myne.ui.screens.reader.activities.ReaderActivity
import com.starry.myne.ui.screens.reader.viewmodels.ReaderViewModel
import com.starry.myne.ui.theme.MyneTheme
import com.starry.myne.ui.theme.pacificoFont
import com.starry.myne.utils.noRippleClickable

@ExperimentalMaterialApi
@ExperimentalMaterial3Api
class ReaderRVAdapter(
    private val activity: ReaderActivity,
    private val viewModel: ReaderViewModel,
    private val clickListener: ReaderClickListener
) :
    RecyclerView.Adapter<ReaderRVAdapter.ReaderComposeViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<EpubChapter>() {
        override fun areItemsTheSame(oldItem: EpubChapter, newItem: EpubChapter): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: EpubChapter, newItem: EpubChapter): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    var allChapters: List<EpubChapter>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    inner class ReaderComposeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val composeView: ComposeView = view.findViewById(R.id.ReaderRVItem)

        fun bind(position: Int, onClick: () -> Unit) {
            val chapter = allChapters[position]
            composeView.setContent {
                MyneTheme(settingsViewModel = activity.settingsViewModel) {
                    SelectionContainer {
                        println("ChapterRVItem: $chapter")
                        // SelectionContainer is used to enable text selection
                        // in the text displayed in the Text composable
                        // This is required to enable text selection in the reader screen
                        // as the Text composable does not support text selection by default
                        ChapterRVItem(chapter = chapter, viewModel = viewModel, onClick = onClick)
                    }
                }

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReaderComposeViewHolder {
        return ReaderComposeViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.reader_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return allChapters.size
    }

    override fun onBindViewHolder(holder: ReaderComposeViewHolder, position: Int) {
        holder.bind(position, onClick = { clickListener.onReaderClick() })
    }

    override fun getItemId(position: Int): Long {
        return allChapters[position].hashCode().toLong()
    }
}

@Composable
private fun ChapterRVItem(
    chapter: EpubChapter,
    viewModel: ReaderViewModel,
    onClick: () -> Unit
) {
    val epubBook = viewModel.state.epubBook
    val paragraphs = remember { chunkText(chapter.body) }
    val targetFontSize = (viewModel.fontSize.value / 10) * 1.8f
    val fontSize by animateFloatAsState(
        targetValue = targetFontSize,
        animationSpec = tween(durationMillis = 300),
        label = "fontSize"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .noRippleClickable { onClick() }
    ) {
        Text(
            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 10.dp),
            text = chapter.title,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            fontFamily = pacificoFont,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.88f)
        )
        Spacer(modifier = Modifier.height(12.dp))

        paragraphs.forEach { para ->
            val imgEntry = BookTextMapper.ImgEntry.fromXMLString(para)
            when {
                imgEntry == null -> {
                    Text(
                        text = para,
                        fontSize = fontSize.sp,
                        lineHeight = 1.3.em,
                        fontFamily = viewModel.readerFont.value.fontFamily,
                        modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 8.dp),
                    )
                }

                else -> {
                    val image = epubBook?.images?.find { it.absPath == imgEntry.path }
                    image?.let {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(image.image)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 6.dp)
                        )
                    }
                }
            }

        }

        HorizontalDivider(
            thickness = 2.dp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
        )
    }
}

private fun chunkText(text: String): List<String> {
    return text.splitToSequence("\n\n")
        .filter { it.isNotBlank() }
        .toList()
}