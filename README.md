# JetPDFVue [![](https://jitpack.io/v/pratikksahu/JetPDFVue.svg)](https://jitpack.io/#pratikksahu/JetPDFVue)
# App Description
JetPDFVue is a PDF library **Create,Modify,View** written in Jetpack Compose. This was created using [PDFRenderer](https://developer.android.com/reference/android/graphics/pdf/PdfRenderer) and [PDFDocument](https://developer.android.com/reference/android/graphics/pdf/PdfDocument). The library supports both Horizontal and Vertical viewing.

# Examples
1. Horizontal [Example 1](app/src/main/java/com/pratikk/jetpackpdf/horizontalSamples/HorizontalSampleA.kt) [Example 2](app/src/main/java/com/pratikk/jetpackpdf/horizontalSamples/HorizontalSampleB.kt)
2. Vertical [Example 1](app/src/main/java/com/pratikk/jetpackpdf/verticalSamples/VerticalSampleA.kt)

Remote sources might not work sometimes because files are hosted on google drive
# Features

- **Multiple Data Sources:** JetPDFVue supports various data sources out of the box, including Base64, URL, Uri, Image, and Custom.
- **Interactive Viewer:** Features like pinch-to-zoom and panning make it easy to interact with PDFs.
- **Page Rotation:** Rotate pages with ease.
- **PDF Manipulation:** Add PDF pages or images to an existing PDF.
- **Share PDF:** Share your PDF documents seamlessly.
- **State Persistence:** JetPDFVue remembers the UI state across compositions, eliminating the need for a ViewModel.
- **Custom Slider:** A customizable slider for navigating through pages.
- **Comprehensive State Indicator:** Gain insights into the PDF's loading and importing state.
- **Efficient Memory Management:** JetPDFVue incorporates cache support for efficient memory usage.
- **Useful Extensions:** Simplify common tasks with extension functions, such as image rotation and compression.

   - `File.rotateImageIfNeeded()`: Rotate images to portrait orientation during import.
   - `File.compressImageToThreshold(threshold: Int)`: Compress images during import.
   - `InputStream.toFile(extension: String)`: Convert any input stream to a file.

# Get Started
**Step 1.** Add `INTERNET` permissions on your AndroidManifest.xml

```xml
<uses-permission android:name="android.permission.INTERNET" />
```
**Step 2.** Add the JitPack maven repository 

```gradle
    maven { url "https://jitpack.io"  }
```
**Step 3.** Add the dependency

```gradle
dependencies {
        implementation("com.github.pratikksahu:JetPDFVue:1.0.3")
}
```
**Step 4.** You can use the library by creating the state in a Composable 
#### This is for horizontal viewing

```kotlin
val horizontalVueReaderState = rememberHorizontalVueReaderState(
        resource = VueResourceType.Base64(
            context.assets.open("lorem_ipsum_base64.txt").let { inputStream ->
                inputStream.toFile(extension = ".txt")
            }
        ),
        cache = 3 // By default 0
    )
// .toFile is an util extension function to convert cany input stream to a file
```
#### This is for vertical viewing
```kotlin
val verticalVueReaderState = rememberVerticalVueReaderState(
        resource = VueResourceType.Base64(
            context.assets.open("lorem_ipsum_base64.txt").let { inputStream ->
                inputStream.toFile(extension = ".txt")
            }
        ),
        cache = 3 // By default 0
    )
// .toFile is an util extension function to convert cany input stream to a file
```
**Step 5.** Invoke load() method to initalize source
```kotlin
LaunchedEffect(Unit) {
            horizontalVueReaderState.load( // or verticalVueReaderState.load()
                context = context,
                coroutineScope = scope,
                containerSize = containerSize, // Used to create a canvas for bitmap
                isPortrait = true, //Use LocalConfiguration to determine orientation
                customResource = null // Requires when using Custom as Resource type
            )
        }
```
**Step 6.** Observe the reader state 
```kotlin
val vueLoadState = horizontalVueReaderState.vueLoadState
        when(vueLoadState){
            is VueLoadState.DocumentError -> {
                /**
                 * Handle Error by using
                 * vueLoadState.getErrorMessage
                 * */
            }
            VueLoadState.DocumentImporting -> {
                /**
                 * Indicates when image/pdf is being imported
                 * This is also the state when the image is done importing but is being processed
                 * */
            }
            VueLoadState.DocumentLoaded -> {
                /**
                 * This is the state where either
                 * HorizontalPdfViewer(horizontalVueReaderState = horizontalVueReaderState)
                 * or
                 * VerticalPdfViewer(verticalVueReaderState = verticalVueReaderState)
                 * Is used to display pdf
                 * */
            }
            VueLoadState.DocumentLoading -> {
                /**
                 * Indicates when image/pdf is loaded initially
                 * This is also the state when resource type is custom
                 * Use horizontalVueReaderState.loadPercent to get progress (Does not work with Custom resource)
                 * */
            }
            VueLoadState.NoDocument -> {
                /**
                 * This is the state where you want to create a new document
                 * Here, show UI for ex, button to launch the import intent
                 * */
            }
        }
```
**Step 7.** HorizontalVueReader and VerticalVueReader Should be used only when in `VueLoadState.DocumentLoaded` State
```kotlin
is VueLoadState.DocumentLoaded -> {
                HorizontalVueReader(
                    modifier = Modifier, // Modifier for pager
                    contentModifier = Modifier, // Modifier for Individual page
                    horizontalVueReaderState = horizontalVueReaderState
                )
            }
```
# [Resource Type](JetPDFVue/src/main/java/com/pratikk/jetpdfvue/state/VueResourceType.kt)
- `Remote`
    1. Base64
    2. PDF
    3. Image
- `Local`
    1. Base64
    2. PDF
    3. Asset
- `Custom`
    Any network request or transformation can be done in this scope
    ```kotlin
    LaunchedEffect(key1 = Unit, block = {
                horizontalVueReaderState.load(
                    context = context, 
                    coroutineScope = this,
                    containerSize = containerSize,
                    isPortrait = true,
                    customResource = { // This lambda will be invoked when using Custom resource type
                        networkCall() // Should return a file
                    })
            })
    ```

# Feature Implementations

### 1.  Page rotation
The rotation is being done on bitmap level but you don't have to worry about that Just use `horizontalVueReaderState.rotate(angle)`

### 2.  Custom Slider
Zoom gesture and swipe gesture does not seem to be working together but there is a custom slider to move between pages.

```kotlin
VueHorizontalSlider(
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 10.dp)
                    .fillMaxWidth()
                    .height(40.dp),
                horizontalVueReaderState = horizontalVueReaderState
            )
```
Helper function is also available if you choose not to use the slider
```kotlin
scope.launch {horizontalVueReaderState.prevPage()}
and
scope.launch {horizontalVueReaderState.nextPage()}
```


### License
```xml
   Copyright [2023] [Pratik Sahu]

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
