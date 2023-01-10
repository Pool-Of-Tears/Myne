<div align="center">
<img src="https://te.legra.ph/file/8234db16f1e9418b73005.png" align="center" style="width: 100%" />
<h1>Myne: Ebook downloader</h1>
</div>

<p align="center">
  <a href="https://www.android.com"><img src="https://forthebadge.com/images/badges/built-for-android.svg"></a>
  <a href="https://www.github.com/starry69"><img src="https://forthebadge.com/images/badges/built-with-love.svg"/></a>
</p>

------

**Myne** is a [FOSS](https://en.m.wikipedia.org/wiki/Free_and_open-source_software) Android application to download ebooks from the [Project GutenBerg](https://gutenberg.org), it uses [GutenDex](https://gutendex.com) API to fetch metadata of ebooks in the backend.

*The name of the app is inspired from the main character of an anime called [Ascendance of a Bookworm](https://myanimelist.net/anime/39468/Honzuki_no_Gekokujou__Shisho_ni_Naru_Tame_ni_wa_Shudan_wo_Erandeiraremasen)*.

**Note** The app also uses [Google Books](https://books.google.co.in/) API to fetch some extra data like book summary and pages count etc, as the GutenBerg project don't include those values in their metadata. It tries it's best to map the data received from Google books with Gutenberg's
metadata but the mapping is not 100% accurate and not all books available on GutenBerg is also available on Google books or is available but with different title, so you may find some books without summary or page count etc.

------

<h2 align="center">Screenshots</h2>

| ![](https://te.legra.ph/file/f80277d5ebb912d9f46ce.jpg) | ![](https://te.legra.ph/file/5ed804ec2ba37a412c24a.jpg) | ![](https://te.legra.ph/file/cca9c0e85c6b9be8885d7.jpg) | ![](https://te.legra.ph/file/d8178355c4d1c02f19046.jpg) |
|--------------------------------------------------------|--------------------------------------------------------|--------------------------------------------------------|--------------------------------------------------------|
| ![](https://te.legra.ph/file/f2375e1df6bda65436e82.jpg) | ![](https://te.legra.ph/file/40dcb912ed618f905330c.jpg) | ![](https://te.legra.ph/file/33f8a685536eacad0cd60.jpg) | ![](https://te.legra.ph/file/afa255c9d0c75cda0c735.jpg) |

------

<h2 align="center">Highlights</h2>

- Clean & beautiful UI based on Google's [material design three](https://m3.material.io/) guidelines.
- Browse and download over 60k free ebooks available in multiple languages and updated daily.
- Compatible with Android 7.0 and above (API 24+)
- Supports [Material You](https://www.androidpolice.com/everything-we-love-about-material-you/amp/) theming in devices running on Android 12+
- Comes in both light and dark mode.

------

<h2 align="center">Donations</h2>

If this project helped you a little bit, please consider donating a small amount to support further development and ofcourse boosting morale :)

[![Github-sponsors](https://img.shields.io/badge/sponsor-30363D?style=for-the-badge&logo=GitHub-Sponsors&logoColor=#EA4AAA)](https://github.com/sponsors/starry69)
[![Bitcoin](https://img.shields.io/badge/Bitcoin-000?style=for-the-badge&logo=bitcoin&logoColor=white)](https://www.blockchain.com/btc/address/bc1q82qh9hw5xupwlf0f3ddfud63sek53lavk6cf0k)
[![Ethereum](https://img.shields.io/badge/Ethereum-3C3C3D?style=for-the-badge&logo=Ethereum&logoColor=white)](https://www.blockchain.com/eth/address/0x9ef20ad6FBf1985e6eF6ea6337ad800Cb8126eD3)
![](https://img.shields.io/badge/starry%40airtel-UPI-red?style=for-the-badge)

------

<h2 align="center">Tech Stack</h2>

- [Kotlin](https://kotlinlang.org/) - First class and official programming language for Android development.
- [Coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html) - To improve performance and overall user experience.
- [Android Architecture Components](https://developer.android.com/topic/libraries/architecture) - Collection of libraries that help you design robust, testable, and maintainable apps.
  - [Jetpack Compose](https://developer.android.com/jetpack/compose?gclsrc=ds&gclsrc=ds) - Jetpack Compose is Android’s recommended modern toolkit for building native UI
  - [LiveData](https://developer.android.com/topic/libraries/architecture/livedata) - Data objects that notify views when the underlying database changes.
  - [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) - Stores UI-related data that isn't destroyed on UI changes.
- [OkHttp3](https://square.github.io/okhttp/) - OkHttp is an HTTP client for Android that’s efficient by default.
- [Gson](https://github.com/google/gson) - A Java serialization/deserialization library to convert Java Objects into JSON and back.
- [Coil](https://coil-kt.github.io/coil/compose/) - An image loading library for Android backed by Kotlin Coroutines.
- [Dagger-Hilt](https://dagger.dev/hilt/) For [Dependency injection (DI)](https://developer.android.com/training/dependency-injection)
- [Room database](https://developer.android.com/jetpack/androidx/releases/room) - Persistence library provides an abstraction layer over SQLite to allow for more robust database access while harnessing the full power of SQLite.

------

<h2 align="center">License</h2>

[Apache License 2.0][license] © [Stɑrry Shivɑm][github]

[license]: /LICENSE
[github]: https://github.com/starry69

```
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
```
