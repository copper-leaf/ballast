package com.copperleaf.ballast.imageloader

import androidx.compose.ui.graphics.toComposeImageBitmap
import com.caseyjbrooks.arkham.config.ArkhamConfig
import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import kotlinx.coroutines.delay
import org.jetbrains.skia.Image
import java.io.ByteArrayOutputStream
import java.net.URL
import javax.imageio.ImageIO

class ImageLoaderInputHandler(
    private val config: com.caseyjbrooks.arkham.config.ArkhamConfig,
) : InputHandler<
    ImageLoaderContract.Inputs,
    ImageLoaderContract.Events,
    ImageLoaderContract.State> {
    override suspend fun InputHandlerScope<
        ImageLoaderContract.Inputs,
        ImageLoaderContract.Events,
        ImageLoaderContract.State>.handleInput(
        input: ImageLoaderContract.Inputs
    ) = when (input) {
        is ImageLoaderContract.Inputs.ImageEnteredView -> {
            val currrentState = updateStateAndGet {
                val updatedImagesInView = buildMap {
                    putAll(it.imagesInView)
                    this[input.image] = ((this[input.image] ?: 0) + 1)
                }

                it.copy(imagesInView = updatedImagesInView)
            }

            if(currrentState.imagesInView[input.image] == 1) {
                // this was the first time requesting this image, load it now in the background
                sideJob(input.image.url) {
                    val loadedImage = runCatching {
                        val connection = URL(input.image.url).openConnection()
                        connection.connect()

                        ByteArrayOutputStream()
                            .also { ImageIO.write(ImageIO.read(connection.inputStream), "png", it) }
                            .toByteArray()
                            .let { Image.makeFromEncoded(it).toComposeImageBitmap() }
                    }

                    delay(1000)

                    postInput(
                        ImageLoaderContract.Inputs.ImageLoaded(input.image, loadedImage)
                    )
                }
            }

            Unit
        }
        is ImageLoaderContract.Inputs.ImageLeftView -> {
            updateState {
                val updatedImagesInView = buildMap {
                    putAll(it.imagesInView)
                    this[input.image] = ((this[input.image] ?: 0) - 1)
                    this[input.image]?.let { count: Int ->
                        if(count <= 0) {
                            remove(input.image)
                        }
                    }

                    Unit
                }

                it.copy(imagesInView = updatedImagesInView)
            }
        }
        is ImageLoaderContract.Inputs.ImageLoaded -> {
            updateState {
                val currentMap = buildMap {
                    putAll(it.imageCache)
                    if (this.size > config.imageCacheSize) {
                        val urlsInView = it.imagesInView.map { it.key.url }
                        val itemsToRemove = it.imageCache.toList().filter { it.second.description.url !in urlsInView }

                        itemsToRemove.forEach { itemToRemove ->
                            remove(itemToRemove.first)
                        }
                    }
                }

                it.copy(
                    imageCache = currentMap.toMap() + (input.image.url to CachedImage(
                        description = input.image,
                        image = input.imageResult,
                    ))
                )
            }
        }
    }
}
