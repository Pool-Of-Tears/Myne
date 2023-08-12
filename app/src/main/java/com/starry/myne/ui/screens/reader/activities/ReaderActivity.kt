/*
Copyright 2022 - 2023 Stɑrry Shivɑm

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.starry.myne.ui.screens.reader.activities

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.OutlinedButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.starry.myne.R
import com.starry.myne.databinding.ActivityReaderBinding
import com.starry.myne.ui.screens.reader.adapters.ReaderClickListener
import com.starry.myne.ui.screens.reader.adapters.ReaderRVAdapter
import com.starry.myne.ui.screens.reader.viewmodels.ReaderFont
import com.starry.myne.ui.screens.reader.viewmodels.ReaderViewModel
import com.starry.myne.ui.screens.settings.viewmodels.SettingsViewModel
import com.starry.myne.ui.screens.settings.viewmodels.ThemeMode
import com.starry.myne.ui.theme.MyneTheme
import com.starry.myne.ui.theme.figeronaFont
import com.starry.myne.utils.toToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

@AndroidEntryPoint
@ExperimentalMaterial3Api
@ExperimentalMaterialApi
class ReaderActivity : AppCompatActivity(), ReaderClickListener {

    enum class TextScaleButtonType { INCREASE, DECREASE }

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
        when (settingsViewModel.getThemeValue()) {
            ThemeMode.Auto.ordinal -> settingsViewModel.setTheme(ThemeMode.Auto)
            ThemeMode.Dark.ordinal -> settingsViewModel.setTheme(ThemeMode.Dark)
            ThemeMode.Light.ordinal -> settingsViewModel.setTheme(ThemeMode.Light)
        }
        settingsViewModel.setMaterialYou(settingsViewModel.getMaterialYouValue())

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
            intent.data!!.path?.let {
                viewModel.loadEpubBookExternal(it, onLoaded = { res ->
                    adapter.allChapters = res.epubBook!!.chapters
                })
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
                val progressChIndex = layoutManager.findLastVisibleItemPosition()
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
        })

        // Set UI contents.
        setContent {
            MyneTheme(settingsViewModel = settingsViewModel) {
                TransparentSystemBars()
                val textSizeValue = remember { mutableStateOf(viewModel.getFontSize()) }
                val readerFontFamily = remember { mutableStateOf(viewModel.getFontFamily()) }
                ReaderScreen(
                    viewModel = viewModel,
                    textSizeValue = textSizeValue,
                    readerFontFamily = readerFontFamily,
                    recyclerViewManager = layoutManager,
                    readerContent = { AndroidView(factory = { binding.root }) }
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        val layoutManager = (binding.readerRecyclerView.layoutManager as LinearLayoutManager)
        val currentPosition = layoutManager.findLastVisibleItemPosition()
        val currentOffset = layoutManager.findViewByPosition(currentPosition)!!.top
        mRVPositionAndOffset = Pair(currentPosition, currentOffset)
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

    @Composable
    private fun ReaderScreen(
        viewModel: ReaderViewModel,
        textSizeValue: MutableState<Int>,
        readerFontFamily: MutableState<ReaderFont>,
        recyclerViewManager: LinearLayoutManager,
        readerContent: @Composable (paddingValues: PaddingValues) -> Unit
    ) {
        // Hide reader menu on back press.
        BackHandler(viewModel.state.showReaderMenu) {
            viewModel.hideReaderInfo()
        }

        // Font style chooser dialog.
        val showFontDialog = remember { mutableStateOf(false) }
        val radioOptions = ReaderFont.getAllFonts().map { it.name }
        val (selectedOption, onOptionSelected) = remember {
            mutableStateOf(viewModel.getFontFamily())
        }

        if (showFontDialog.value) {
            AlertDialog(onDismissRequest = {
                showFontDialog.value = false
            }, title = {
                Text(
                    text = stringResource(id = R.string.reader_font_style_chooer),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }, text = {
                Column(
                    modifier = Modifier.selectableGroup(),
                    verticalArrangement = Arrangement.Center,
                ) {
                    radioOptions.forEach { text ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .selectable(
                                    selected = (text == selectedOption.name),
                                    onClick = { onOptionSelected(ReaderFont.getFontByName(text)) },
                                    role = Role.RadioButton,
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = (text == selectedOption.name),
                                onClick = null,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary,
                                    unselectedColor = MaterialTheme.colorScheme.inversePrimary,
                                    disabledSelectedColor = Color.Black,
                                    disabledUnselectedColor = Color.Black
                                ),
                            )
                            Text(
                                text = text,
                                modifier = Modifier.padding(start = 16.dp),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = figeronaFont
                            )
                        }
                    }
                }
            }, confirmButton = {
                TextButton(onClick = {
                    showFontDialog.value = false
                    viewModel.setFontFamily(selectedOption)
                    readerFontFamily.value = selectedOption
                }) {
                    Text(stringResource(id = R.string.theme_dialog_apply_button))
                }
            }, dismissButton = {
                TextButton(onClick = {
                    showFontDialog.value = false
                }) {
                    Text(stringResource(id = R.string.cancel))
                }
            })
        }

        val showChaptersDialog = remember { mutableStateOf(false) }
        if (showChaptersDialog.value) {
            AlertDialog(onDismissRequest = {
                showChaptersDialog.value = false
            }, content = {
                Column(
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    LazyColumn(content = {
                        viewModel.state.epubBook?.chapters?.size?.let {
                            items(it) { index ->
                                val chapter = viewModel.state.epubBook!!.chapters[index]
                                TextButton(
                                    onClick = {
                                        recyclerViewManager.scrollToPositionWithOffset(index, 0)
                                        showChaptersDialog.value = false
                                        viewModel.hideReaderInfo()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        text = chapter.title,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Start
                                    )
                                }
                            }
                        }
                    })
                }
            })
        }

        val snackBarHostState = remember { SnackbarHostState() }
        val visibleChapterIdx = visibleChapterFlow.collectAsState().value
        val chapter = viewModel.state.epubBook?.chapters?.get(visibleChapterIdx)

        Scaffold(
            snackbarHost = { SnackbarHost(snackBarHostState) },
            topBar = {
                AnimatedVisibility(
                    visible = viewModel.state.showReaderMenu,
                    enter = expandVertically(initialHeight = { 0 }, expandFrom = Alignment.Top)
                            + fadeIn(),
                    exit = shrinkVertically(targetHeight = { 0 }, shrinkTowards = Alignment.Top)
                            + fadeOut(),
                ) {
                    Surface(color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)) {
                        Column(modifier = Modifier.displayCutoutPadding()) {
                            TopAppBar(
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                                        2.dp
                                    ),
                                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                                        2.dp
                                    ),
                                ),
                                title = {
                                    viewModel.state.epubBook?.let {
                                        Text(
                                            text = it.title,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.animateContentSize(),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                },
                                actions = {
                                    IconButton(onClick = { showChaptersDialog.value = true }) {
                                        Icon(
                                            Icons.Filled.Menu, null,
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            )
                            Column(
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                                    .padding(horizontal = 16.dp),
                            ) {
                                chapter?.title?.let {
                                    Text(
                                        text = it,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            Divider()
                        }
                    }
                }
            },
            bottomBar = {
                AnimatedVisibility(
                    visible = viewModel.state.showReaderMenu,
                    enter = expandVertically(initialHeight = { 0 }) + fadeIn(),
                    exit = shrinkVertically(targetHeight = { 0 }) + fadeOut(),
                ) {
                    BottomSheetContents(
                        viewModel = viewModel,
                        textSizeValue = textSizeValue,
                        readerFontFamily = readerFontFamily,
                        showFontDialog = showFontDialog,
                        snackBarHostState = snackBarHostState
                    )
                }
            },
            content = {
                if (viewModel.state.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 65.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    readerContent(it)
                }
            }
        )
    }

    @Composable
    private fun BottomSheetContents(
        viewModel: ReaderViewModel,
        textSizeValue: MutableState<Int>,
        readerFontFamily: MutableState<ReaderFont>,
        showFontDialog: MutableState<Boolean>,
        snackBarHostState: SnackbarHostState
    ) {
        val coroutineScope = rememberCoroutineScope()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 21.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                ReaderTextScaleButton(
                    buttonType = TextScaleButtonType.DECREASE,
                    textSizeValue = textSizeValue,
                    coroutineScope = coroutineScope,
                    snackBarHostState = snackBarHostState,
                    onTextSizeValueChanged = { viewModel.setFontSize(it) }
                )
                Spacer(modifier = Modifier.width(14.dp))
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(45.dp)
                        .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.onSurface,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clip(RoundedCornerShape(6.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.ic_reader_text_size),
                                contentDescription = null,
                                //    modifier = Modifier.size(size = 15.dp),
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = textSizeValue.value.toString(),
                                fontFamily = figeronaFont,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = 2.dp, bottom = 1.dp),
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                ReaderTextScaleButton(
                    buttonType = TextScaleButtonType.INCREASE,
                    textSizeValue = textSizeValue,
                    coroutineScope = coroutineScope,
                    snackBarHostState = snackBarHostState,
                    onTextSizeValueChanged = { viewModel.setFontSize(it) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                OutlinedButton(
                    onClick = { showFontDialog.value = true },
                    modifier = Modifier.width(325.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                    )
                ) {
                    Row {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_reader_font),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = readerFontFamily.value.name,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    @Composable
    private fun ReaderTextScaleButton(
        buttonType: TextScaleButtonType,
        textSizeValue: MutableState<Int>,
        coroutineScope: CoroutineScope,
        snackBarHostState: SnackbarHostState,
        onTextSizeValueChanged: (newValue: Int) -> Unit
    ) {
        val context = LocalContext.current
        val iconRes: Int
        val callback: () -> Unit

        when (buttonType) {
            TextScaleButtonType.DECREASE -> {
                iconRes = R.drawable.ic_reader_text_minus
                callback = {
                    if (textSizeValue.value <= 50) {
                        coroutineScope.launch {
                            snackBarHostState.showSnackbar(
                                context.getString(R.string.reader_min_font_size_reached),
                                null
                            )
                        }
                    } else {
                        coroutineScope.launch {
                            textSizeValue.value -= 10
                            onTextSizeValueChanged(textSizeValue.value)
                        }
                    }
                }
            }

            TextScaleButtonType.INCREASE -> {
                iconRes = R.drawable.ic_reader_text_plus
                callback = {
                    if (textSizeValue.value >= 200) {
                        coroutineScope.launch {
                            snackBarHostState.showSnackbar(
                                context.getString(R.string.reader_max_font_size_reached),
                                null
                            )
                        }
                    } else {
                        coroutineScope.launch {
                            textSizeValue.value += 10
                            onTextSizeValueChanged(textSizeValue.value)
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .width(100.dp)
                .height(45.dp)
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.onSurface,
                    shape = RoundedCornerShape(6.dp)
                )
                .clip(RoundedCornerShape(6.dp))
                .clickable { callback() }, contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = iconRes),
                contentDescription = stringResource(id = R.string.back_button_desc),
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(14.dp)
            )
        }
    }

    @Composable
    private fun TransparentSystemBars(alpha: Float = 0f) {
        val systemUiController = rememberSystemUiController()
        val useDarkIcons = settingsViewModel.getCurrentTheme() == ThemeMode.Light
        val baseColor = MaterialTheme.colorScheme.primary
        val color = remember(alpha, baseColor) { baseColor.copy(alpha = alpha) }
        SideEffect {
            systemUiController.setSystemBarsColor(
                color = color,
                darkIcons = useDarkIcons
            )
        }
    }
}