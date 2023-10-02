package com.pratikk.jetpackpdf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavHost
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pratikk.jetpackpdf.horizontalSamples.HorizontalPdfViewer
import com.pratikk.jetpackpdf.horizontalSamples.HorizontalPdfViewerLocal
import com.pratikk.jetpackpdf.ui.theme.JetpackPDFTheme
import com.pratikk.jetpackpdf.verticalSamples.VerticalPdfViewer
import com.pratikk.jetpackpdf.verticalSamples.VerticalPdfViewerLocal
import com.pratikk.jetpdfvue.state.VueFileType
import com.pratikk.jetpdfvue.state.VueResourceType
import com.pratikk.jetpdfvue.state.rememberHorizontalVueReaderState
import com.pratikk.jetpdfvue.state.rememberVerticalVueReaderState
import com.pratikk.jetpdfvue.util.toFile


class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JetpackPDFTheme {
                val navController = rememberNavController()
                // A surface container using the 'background' color from the theme
                Scaffold(topBar = {
                    Column {
                        TopAppBar(title = { Text(text = "JetPDFVue Sample") })
                        Divider()
                    }
                }
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavHost(
                            navController = navController, startDestination = "Home"){
                            composable(route = "Home"){
                                HomeOptions(
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    onSelection = {isVertical, sampleResourceType ->
                                        if(!isVertical){
                                            navController.navigate("Horizontal/$sampleResourceType")
                                        }else{
                                            navController.navigate("Vertical/$sampleResourceType")
                                        }
                                    }
                                )
                            }
                            composable(route = "Horizontal/{type}",
                                arguments = listOf(
                                    navArgument("type"){
                                        type = NavType.IntType
                                    }
                                )
                            ){
                                val context = LocalContext.current
                                val type = it.arguments?.getInt("type")!!
                                when(type){
                                    1 -> {
                                        //Local
                                        HorizontalPdfViewerLocal()
                                    }
                                    2 -> {
                                        //Asset as local
                                        val localImage = rememberHorizontalVueReaderState(
                                            resource = VueResourceType.Local(
                                                uri = context.resources.openRawResource(
                                                    R.raw.demo
                                                ).toFile(".jpg").toUri(),
                                                fileType = VueFileType.IMAGE
                                            ),
                                        )
                                        /**
                                        Other file types
                                        val localPdf = rememberHorizontalVueReaderState(
                                        resource = VueResourceType.Local(
                                        uri = context.resources.openRawResource(
                                        R.raw.lorem_ipsum
                                        ).toFile(".pdf").toUri(),
                                        fileType = VueFileType.PDF
                                        )
                                        )
                                        val localBase64 = rememberHorizontalVueReaderState(
                                        resource = VueResourceType.Local(
                                        uri = context.resources.openRawResource(
                                        R.raw.lorem_ipsum_base64
                                        ).toFile(".txt").toUri(),
                                        fileType = VueFileType.BASE64
                                        )
                                        )
                                         */
                                        HorizontalPdfViewer(horizontalVueReaderState = localImage)
                                    }
                                    3 -> {
                                        //Remote
                                        val remoteImageLink = listOf(
                                            "https://images.pexels.com/photos/943907/pexels-photo-943907.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
                                            "https://images.freeimages.com/images/large-previews/7f3/path-1441068.jpg"
                                        )

                                        val remotePdf =
                                            rememberHorizontalVueReaderState(
                                                resource = VueResourceType.Remote(
                                                    "https://drive.google.com/uc?export=download&id=1DSA7cmFzqCtTsHhlB0xdYJ6UweuC8IOz",
                                                    fileType = VueFileType.PDF
                                                )
                                            )
                                        /**
                                         * Other File Types
                                        val remoteImage =
                                        rememberHorizontalVueReaderState(
                                        resource = VueResourceType.Remote(
                                        remoteImageLink[0],
                                        fileType = VueFileType.IMAGE
                                        )
                                        )

                                        val remoteBase64 =
                                        rememberHorizontalVueReaderState(
                                        resource = VueResourceType.Remote(
                                        "https://drive.google.com/uc?export=download&id=1-mmdJ2K2x3MDgTqmFd8sMpW3zIFyNYY-",
                                        fileType = VueFileType.BASE64
                                        )
                                        )
                                         */
                                        HorizontalPdfViewer(horizontalVueReaderState = remotePdf)
                                    }
                                    4 -> {
                                        //Asset
                                        val assetPdf = rememberHorizontalVueReaderState(
                                            resource = VueResourceType.Asset(assetId = R.raw.lorem_ipsum, fileType = VueFileType.PDF)
                                        )
                                        /**
                                         * Other File Types
                                        val assetImage = rememberHorizontalVueReaderState(
                                        resource = VueResourceType.Asset(assetId = R.raw.demo, fileType = VueFileType.IMAGE)
                                        )

                                        val assetBase64 = rememberHorizontalVueReaderState(
                                        resource = VueResourceType.Asset(
                                        assetId = R.raw.lorem_ipsum_base64,
                                        fileType = VueFileType.BASE64
                                        )
                                        )
                                         */
                                        HorizontalPdfViewer(horizontalVueReaderState = assetPdf)
                                    }
                                }
                            }
                            composable(route = "Vertical/{type}",
                                arguments = listOf(
                                    navArgument("type"){
                                        type = NavType.IntType
                                    }
                                )){
                                val context = LocalContext.current
                                val type = it.arguments?.getInt("type")!!
                                when(type){
                                    1 -> {
                                        //Local
                                        VerticalPdfViewerLocal()
                                    }
                                    2 -> {
                                        //Asset as local
                                        val localImage = rememberVerticalVueReaderState(
                                            resource = VueResourceType.Local(
                                                uri = context.resources.openRawResource(
                                                    R.raw.demo
                                                ).toFile(".jpg").toUri(),
                                                fileType = VueFileType.IMAGE
                                            ),
                                        )
                                        /**
                                        Other file types
                                        val localPdf = rememberVerticalVueReaderState(
                                        resource = VueResourceType.Local(
                                        uri = context.resources.openRawResource(
                                        R.raw.lorem_ipsum
                                        ).toFile(".pdf").toUri(),
                                        fileType = VueFileType.PDF
                                        )
                                        )
                                        val localBase64 = rememberVerticalVueReaderState(
                                        resource = VueResourceType.Local(
                                        uri = context.resources.openRawResource(
                                        R.raw.lorem_ipsum_base64
                                        ).toFile(".txt").toUri(),
                                        fileType = VueFileType.BASE64
                                        )
                                        )
                                         */
                                        VerticalPdfViewer(verticalVueReaderState = localImage)
                                    }
                                    3 -> {
                                        //Remote
                                        val remoteImageLink = listOf(
                                            "https://images.pexels.com/photos/943907/pexels-photo-943907.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
                                            "https://images.freeimages.com/images/large-previews/7f3/path-1441068.jpg"
                                        )

                                        val remotePdf =
                                            rememberVerticalVueReaderState(
                                                resource = VueResourceType.Remote(
                                                    "https://drive.google.com/uc?export=download&id=1DSA7cmFzqCtTsHhlB0xdYJ6UweuC8IOz",
                                                    fileType = VueFileType.PDF
                                                )
                                            )
                                        /**
                                         * Other File Types
                                        val remoteImage =
                                        rememberVerticalVueReaderState(
                                        resource = VueResourceType.Remote(
                                        remoteImageLink[0],
                                        fileType = VueFileType.IMAGE
                                        )
                                        )

                                        val remoteBase64 =
                                        rememberVerticalVueReaderState(
                                        resource = VueResourceType.Remote(
                                        "https://drive.google.com/uc?export=download&id=1-mmdJ2K2x3MDgTqmFd8sMpW3zIFyNYY-",
                                        fileType = VueFileType.BASE64
                                        )
                                        )
                                         */
                                        VerticalPdfViewer(verticalVueReaderState = remotePdf)
                                    }
                                    4 -> {
                                        //Asset
                                        val assetPdf = rememberVerticalVueReaderState(
                                            resource = VueResourceType.Asset(assetId = R.raw.lorem_ipsum, fileType = VueFileType.PDF)
                                        )
                                        /**
                                         * Other File Types
                                        val assetImage = rememberVerticalVueReaderState(
                                        resource = VueResourceType.Asset(assetId = R.raw.demo, fileType = VueFileType.IMAGE)
                                        )

                                        val assetBase64 = rememberVerticalVueReaderState(
                                        resource = VueResourceType.Asset(
                                        assetId = R.raw.lorem_ipsum_base64,
                                        fileType = VueFileType.BASE64
                                        )
                                        )
                                         */
                                        VerticalPdfViewer(verticalVueReaderState = assetPdf)
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun HomeOptions(modifier: Modifier = Modifier,
                onSelection:(isVertical:Boolean, sampleResourceType:Int) -> Unit) {
    var isVertical by rememberSaveable {
        mutableStateOf(false)
    }
    Column(modifier = modifier) {
        Text(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            text = "Sample Source",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge)
        Row(modifier = Modifier
            .clickable {
                onSelection(isVertical, 1)
            }
            .fillMaxWidth()
            .padding(horizontal = 16.dp,vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.weight(1f),
                text = "Local",style = MaterialTheme.typography.bodyLarge)
        }
        Divider(Modifier.padding(start = 14.dp))
        Row(modifier = Modifier
            .clickable {
                onSelection(isVertical, 2)
            }
            .fillMaxWidth()
            .padding(horizontal = 16.dp,vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.weight(1f),
                text = "Asset as local ",style = MaterialTheme.typography.bodyLarge)
        }
        Divider(Modifier.padding(start = 14.dp))
        Row(modifier = Modifier
            .clickable {
                onSelection(isVertical, 3)
            }
            .fillMaxWidth()
            .padding(horizontal = 16.dp,vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.weight(1f),
                text = "Remote",style = MaterialTheme.typography.bodyLarge)
        }
        Divider(Modifier.padding(start = 14.dp))
        Row(modifier = Modifier
            .clickable {
                onSelection(isVertical, 4)
            }
            .fillMaxWidth()
            .padding(horizontal = 16.dp,vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.weight(1f),
                text = "Asset",style = MaterialTheme.typography.bodyLarge)
        }
        Divider(Modifier.padding(start = 14.dp))
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.weight(1f),
                text = "Horizontal View",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center)
            Switch(checked = isVertical, onCheckedChange = {
                isVertical = it
            })
            Text(
                modifier = Modifier.weight(1f),
                text = "Vertical View",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center)
        }
    }
}

