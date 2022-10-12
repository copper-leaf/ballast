package com.copperleaf.ballast.examples.sync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.copperleaf.ballast.examples.MainApplication

class SyncFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeView(requireContext())
            .apply {
                setContent {
                    val injector = remember { MainApplication.getInstance().injector }
                    SyncComposeUi.Content { injector.counterViewModel(null, it) }
                }
            }
    }
}
