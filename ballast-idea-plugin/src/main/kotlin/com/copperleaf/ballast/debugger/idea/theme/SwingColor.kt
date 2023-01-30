package com.copperleaf.ballast.debugger.idea.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.LafManagerListener
import com.intellij.openapi.application.ApplicationManager
import javax.swing.UIManager
import java.awt.Color as AWTColor

internal class ThemeChangeListener(
    val updateColors: () -> Unit
) : LafManagerListener {
    override fun lookAndFeelChanged(source: LafManager) {
        updateColors()
    }
}

interface SwingColor {
    val background: Color
}

@Composable
fun SwingColor(): SwingColor {
    val swingColor = remember { SwingColorImpl() }

    val messageBus = remember {
        ApplicationManager.getApplication().messageBus.connect()
    }

    remember(messageBus) {
        messageBus.subscribe(
            LafManagerListener.TOPIC,
            ThemeChangeListener(swingColor::updateCurrentColors)
        )
    }

    DisposableEffect(messageBus) {
        onDispose {
            messageBus.disconnect()
        }
    }

    return swingColor
}

private class SwingColorImpl : SwingColor {
    private val _backgroundState: MutableState<Color> = mutableStateOf(getBackgroundColor)

    override val background: Color get() = _backgroundState.value

    private val getBackgroundColor get() = getColor(BACKGROUND_KEY)

    fun updateCurrentColors() {
        _backgroundState.value = getBackgroundColor
    }

    private val AWTColor.asComposeColor: Color get() = Color(red, green, blue, alpha)
    private fun getColor(key: String): Color = UIManager.getColor(key).asComposeColor

    companion object {
        private const val BACKGROUND_KEY = "Panel.background"
    }
}
