<div align="center">
  <a href=""><img width="200" height="200" src="https://github.com/Pool-Of-Tears/Myne/blob/main/app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png"></a>
  <h2>Myne: Download & Read eBooks</h2>
</div>

<p align="center">
  <a href="https://www.android.com"><img src="https://forthebadge.com/images/badges/built-for-android.svg"></a>
  <a href="https://www.github.com/starry-shivam"><img src="https://forthebadge.com/images/badges/built-with-love.svg"/></a>
</p>

<p align="center">
  <a href="https://t.me/PotApps"><img src="https://img.shields.io/badge/Telegram-PotApps-green?style=flat&logo=telegram"/></a>
  <a href="https://github.com/Pool-Of-Tears/Myne/releases"><img src="https://img.shields.io/github/downloads/Pool-Of-Tears/Myne/total?label=Downloads&logo=github"></a>
  <img alt="GitHub" src="https://img.shields.io/github/license/Pool-Of-Tears/Myne">
  <img alt="GitHub code size in bytes" src="https://img.shields.io/github/languages/code-size/Pool-Of-Tears/Myne">
  <a href="https://www.repostatus.org/#active"><img src="https://www.repostatus.org/badges/latest/active.svg" alt="Project Status: Active – The project has reached a stable, usable state and is being actively developed." /></a>
  <img alt="build-workflow"src="https://github.com/Pool-Of-Tears/Myne/actions/workflows/android.yml/badge.svg">
</p>

------

**Myne** is a [FOSS](https://en.m.wikipedia.org/wiki/Free_and_open-source_software) Android application for downloading and reading ebooks from [Project Gutenberg](https://gutenberg.org). It uses the [GutenDex](https://github.com/garethbjohnson/gutendex) API to fetch metadata for ebooks in the backend. Additionally, it functions as an EPUB reader, allowing you to easily import and immerse yourself in your favorite EPUB books!

*The name of the app is inspired from the main character of an anime called [Ascendance of a Bookworm](https://myanimelist.net/anime/39468/Honzuki_no_Gekokujou__Shisho_ni_Naru_Tame_ni_wa_Shudan_wo_Erandeiraremasen)*.


>[!Note]
>
>The app also utilizes the Google Books API to retrieve additional data such as synopsis and page count. As the Gutenberg project does not include these values in their metadata. While the app attempts to map the data received from Google Books with Gutenberg's metadata, the mapping is not always 100% accurate. Additionally, not all books available on Gutenberg are accessible on Google Books, or they may be available under different titles. As a result, you may find some books without synopsis or page count, etc.

------

<h2 align="center">Screenshots</h2>

| ![](https://graph.org/file/a86b19969bbba506fcdfa.png) | ![](https://graph.org/file/7d1964fb46fd1f4ebf98c.png) | ![](https://graph.org/file/14bc72fa57c42deaf03bf.png) |
|-------------------------------------------------------|-------------------------------------------------------|-------------------------------------------------------|
| ![](https://graph.org/file/3b2b55557ef23d96213ba.png) | ![](https://graph.org/file/0278019a5078d6f8a6155.png) | ![](https://graph.org/file/c44d5574399783320be10.png) |

------

<h2 align="center">Highlights</h2>

- Clean & beautiful UI based on Google's [material design three](https://m3.material.io/) guidelines.
- Browse and download over 70k free ebooks available in multiple languages and updated daily.
- Comes with inbuilt ebook reader while also having an option to use third-party ebook readers.
- Compatible with Android 8.0 and above (API 26+)
- Supports [Material You](https://www.androidpolice.com/everything-we-love-about-material-you/amp/) theming in devices running on Android 12+
- Comes in both light and dark mode.
- MAD: UI and logic written with pure Kotlin. Single activity, no fragments, only composable destinations.

------

<h2 align="center">Downloads</h2>

<div align="center">
<a href="https://play.google.com/store/apps/details?id=com.starry.myne"><img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png" height="65"></a>
<a href='https://f-droid.org/packages/com.starry.myne/'><img alt='Get it on F-Droid' src='https://fdroid.gitlab.io/artwork/badge/get-it-on.png' height='65'/></a>
<a href='https://apt.izzysoft.de/fdroid/index/apk/com.starry.myne'><img alt='Get it on IzzyOnDroid' src='https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png' height='65'/></a>
<a href="https://github.com/Pool-Of-Tears/Myne/releases/latest"><img alt="Get it on GitHub" src="https://github.com/machiav3lli/oandbackupx/blob/034b226cea5c1b30eb4f6a6f313e4dadcbb0ece4/badge_github.png" height="65"></a>
</div>

------

<h2 align="center">Donations</h2>

Myne doesn't contain any ads and doesn't sell your data.
The development of the app is financed by individual user contributions, such as you purchasing the app via Google Play or becoming a sponsor on Github ❤️

Become a [Sponser](https://github.com/sponsors/starry-shivam) on Github | Purchase it on [Google Play](https://play.google.com/store/apps/details?id=com.starry.myne)

------

<h2 align="center">Contributions</h2>

Contributions are welcome! 

>[!Note]
>
>For submitting bug reports, feature requests, questions, or any other ideas to improve, please read [CONTRIBUTING.md](/CONTRIBUTING.md) for instructions and guidelines first.

------

<h2 align="center">Translations</h2>

If you want to make the app available in your language, you're welcome to create a pull request with your translation file. The base string resources can be found under:
```
/app/src/main/res/values/strings.xml
```
It is easiest to make a translation using the Android Studio XML editor, but you can always use your favorite XML text editor instead. Check out this guide to learn more about translation strings from [Helpshift](https://developers.helpshift.com/android/i18n/) for Android.

------

<h2 align="center">Tech Stack</h2>

- [Kotlin](https://kotlinlang.org/) - First class and official programming language for Android development.
- [Coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html) - To improve performance by doing I/O tasks out of main thread asynchronously.
- [Flow](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-flow/) - A cold asynchronous data stream that sequentially emits values and completes normally or with an exception.
- [Android Architecture Components](https://developer.android.com/topic/libraries/architecture) - Collection of libraries that help you design robust, testable, and maintainable apps.
  - [Jetpack Compose](https://developer.android.com/jetpack/compose?gclsrc=ds&gclsrc=ds) - Jetpack Compose is Android’s recommended modern toolkit for building native UI
  - [LiveData](https://developer.android.com/topic/libraries/architecture/livedata) - Data objects that notify views when the underlying database changes.
  - [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) - Stores UI-related data that isn't destroyed on UI changes.
- [OkHttp3](https://square.github.io/okhttp/) - OkHttp is an HTTP client for Android that’s efficient by default.
- [Kotlinx.serialization](https://kotlinlang.org/docs/serialization.html) - Provides sets of libraries for various serialization formats – JSON, CBOR, protocol buffers, and others. 
- [Jsoup](https://jsoup.org) - Jsoup is a Java library for working with HTML. It provides a convenient API for extracting and manipulating data, using the HTML5 DOM methods and CSS selectors.
- [Coil](https://coil-kt.github.io/coil/compose/) - An image loading library for Android backed by Kotlin Coroutines.
- [Dagger-Hilt](https://dagger.dev/hilt/) For [Dependency injection (DI)](https://developer.android.com/training/dependency-injection)
- [Room database](https://developer.android.com/jetpack/androidx/releases/room) - Persistence library provides an abstraction layer over SQLite to allow for more robust database access while harnessing the full power of SQLite.
- [Lottie](https://airbnb.design/lottie) - Lottie is an Android, iOS and React Native library that renders After Effects animations in real time.

------

<h2 align="center">Star History</h2>

[![Star History Chart](https://api.star-history.com/svg?repos=Pool-Of-Tears/Myne&type=Timeline)](https://star-history.com/#Pool-Of-Tears/Myne&Timeline)

------

<h2 align="center">License</h2>

[Apache License 2.0][license] © [Stɑrry Shivɑm][github]

[license]: /LICENSE
[github]: https://github.com/starry-shivam

```
Copyright (c) [2022 - Present] Stɑrry Shivɑm

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```
