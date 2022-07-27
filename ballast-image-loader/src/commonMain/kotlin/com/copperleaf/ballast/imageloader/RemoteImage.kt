package com.copperleaf.ballast.imageloader

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import com.caseyjbrooks.arkham.di.ArkhamInjector

val LocalImageLoader = staticCompositionLocalOf<ImageLoaderViewModel> { error("ImageLoaderViewModel not provided") }

@Composable
fun ImageLoader(
    injector: com.caseyjbrooks.arkham.di.ArkhamInjector,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val imageLoaderViewModel = remember(coroutineScope) { injector.imageLoaderViewModel(coroutineScope) }

    CompositionLocalProvider(LocalImageLoader providesDefault imageLoaderViewModel) {
        content()
    }
}

@Composable
fun RemoteImage(
    url: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val vm = LocalImageLoader.current
    val vmState by vm.observeStates().collectAsState()
    RemoteImage(
        url = url,
        contentDescription = contentDescription,
        modifier = modifier,
        state = vmState,
        postInput = { vm.trySend(it) },
    )
}

@Composable
fun RemoteImage(
    url: String,
    contentDescription: String,
    modifier: Modifier = Modifier,

    state: ImageLoaderContract.State,
    postInput: (ImageLoaderContract.Inputs) -> Unit,
) {
    DisposableEffect(url, contentDescription) {
        val description = ImageDescription(
            url = url,
            contentDescription = contentDescription,
        )

        postInput(ImageLoaderContract.Inputs.ImageEnteredView(description))
        onDispose { postInput(ImageLoaderContract.Inputs.ImageLeftView(description)) }
    }

    Box(modifier) {
        val image = state.imageCache[url]
        if (image == null) {
            CircularProgressIndicator()
        } else {
            image.image.fold(
                onSuccess = { loadedImage -> Image(loadedImage, contentDescription, Modifier.fillMaxSize()) },
                onFailure = { Text("error loading image", color = MaterialTheme.colors.error) }
            )
        }
    }
}
