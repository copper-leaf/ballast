package com.copperleaf.ballast.examples.util

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.copperleaf.ballast.examples.R

class ExamplesListFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeView(requireContext())
            .apply {
                setContent {
                    Column {
                        val navController = findNavController()

                        ListItem(Modifier.clickable { navController.navigate(R.id.action_examplesListFragment_to_counterFragment) }) { Text("Counter") }
                        ListItem(Modifier.clickable { navController.navigate(R.id.action_examplesListFragment_to_bggFragment) }) { Text("BGG") }
                        ListItem(Modifier.clickable { navController.navigate(R.id.action_examplesListFragment_to_scorekeeperFragment) }) { Text("Scorekeeper") }
                        ListItem(Modifier.clickable { navController.navigate(R.id.action_examplesListFragment_to_kitchenSinkFragment) }) { Text("Kitchen Sink") }
                    }
                }
            }
    }
}
