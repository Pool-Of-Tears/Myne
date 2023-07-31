package com.starry.myne.ui.screens.reader.activities

import android.os.Bundle
import android.util.Log
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.OutlinedButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
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
        const val EXTERNAL_BOOK_FILEPATH = "reader_book_filepath"
        const val INVALID = -100000
    }

    private lateinit var binding: ActivityReaderBinding
    lateinit var settingsViewModel: SettingsViewModel
    private val viewModel: ReaderViewModel by viewModels()

    private var isExternalBook by Delegates.notNull<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReaderBinding.inflate(layoutInflater)

        // Set app theme.
        settingsViewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
        when (settingsViewModel.getThemeValue()) {
            ThemeMode.Auto.ordinal -> settingsViewModel.setTheme(ThemeMode.Auto)
            ThemeMode.Dark.ordinal -> settingsViewModel.setTheme(ThemeMode.Dark)
            ThemeMode.Light.ordinal -> settingsViewModel.setTheme(ThemeMode.Light)
        }
        settingsViewModel.setMaterialYou(settingsViewModel.getMaterialYouValue())

        // Fullscreen mode that ignores any cutout, notch etc.
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.displayCutout())
        controller.hide(WindowInsetsCompat.Type.systemBars())

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
        val bookId = intent.extras?.getInt(EXTRA_BOOK_ID, INVALID)!!
        val chapterIndex = intent.extras?.getInt(EXTRA_CHAPTER_IDX, INVALID)!!
        val externalBookFilePath = intent.extras?.getString(EXTERNAL_BOOK_FILEPATH, null)

        // Internal book
        if (bookId != INVALID) {
            isExternalBook = false
            // Load epub book from given id and set chapters as items in
            // reader's recycler view adapter.
            viewModel.loadEpubBook(bookId = bookId, onLoaded = {
                adapter.allChapters = it.epubBook!!.chapters
                // if there is saved progress for this book, then scroll to
                // last page at exact position were used had left.

                println("reader data ${it.readerData}")
                println("reader chapterIdx $chapterIndex")

                if (it.readerData != null && chapterIndex == INVALID) {
                    Log.d("READER_ACT", "Resume called")
                    layoutManager.scrollToPositionWithOffset(
                        it.readerData.lastChapterIndex,
                        it.readerData.lastChapterOffset
                    )
                }
            })
            // if user clicked on specific chapter, then scroll to
            // that chapter directly.
            if (chapterIndex != INVALID) {
                layoutManager.scrollToPosition(chapterIndex)
            }

            // External book.
        } else if (externalBookFilePath != null) {
            // TODO: External book
            isExternalBook = true
        } else {
            //TODO: Error
        }

        // Listener for updating reading progress in database.
        binding.readerRecyclerView.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!isExternalBook) {
                    val progressChIndex = layoutManager.findLastVisibleItemPosition()
                    val progressChOffset = layoutManager.findViewByPosition(progressChIndex)!!.top
                    viewModel.updateReaderProgress(
                        bookId = bookId,
                        chapterIndex = progressChIndex,
                        chapterOffset = progressChOffset
                    )

                    Log.d("READER_ACT", "Listener called")
                    Log.d("READER_ACT", "Chapter IDX; $progressChIndex")
                    Log.d("READER_ACT", "Chapter OFFSET; $progressChOffset")
                }
            }
        })


        setContent {
            MyneTheme(settingsViewModel = settingsViewModel) {
                TransparentSystemBars()

                val textSizeValue = remember { mutableStateOf(viewModel.getFontSize()) }
                val readerFontFamily = remember { mutableStateOf(viewModel.getFontFamily()) }

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
                    ReaderScreen(
                        viewModel = viewModel,
                        textSizeValue = textSizeValue,
                        readerFontFamily = readerFontFamily,
                        readerContent = { AndroidView(factory = { binding.root }) }
                    )
                }
            }
        }
    }


    override fun onReaderClick() {
        viewModel.showReaderInfo()
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

    @Composable
    private fun ReaderScreen(
        viewModel: ReaderViewModel,
        textSizeValue: MutableState<Int>,
        readerFontFamily: MutableState<ReaderFont>,
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

        val snackBarHostState = remember { SnackbarHostState() }
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
                    Surface(color = MaterialTheme.colorScheme.primary) {
                        Column(modifier = Modifier.displayCutoutPadding()) {
                            TopAppBar(
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    scrolledContainerColor = MaterialTheme.colorScheme.primary,
                                ),
                                title = {
                                    Text(
                                        text = "Hello Hello",
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.animateContentSize()
                                    )
                                },
                                actions = {
                                    IconButton(onClick = {}) {
                                        Icon(Icons.Filled.Face, null)
                                    }
                                }
                            )
                            Column(
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                                    .padding(horizontal = 16.dp),
                            ) {
                                Text(
                                    text = "Chapter 45/87",
                                    style = MaterialTheme.typography.labelMedium,
                                )
                                Text(
                                    text = "Progress 100%",
                                    style = MaterialTheme.typography.labelMedium,
                                )
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
            content = readerContent
        )
    }

    @Composable
    fun BottomSheetContents(
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
                        backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                            2.dp
                        )
                    )
                ) {
                    Row {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_reader_font),
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = readerFontFamily.value.name)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    @Composable
    fun ReaderTextScaleButton(
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
}