package com.copperleaf.ballast.examples.ui

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.copperleaf.ballast.examples.MainApplication
import com.copperleaf.android.R
import com.copperleaf.ballast.navigation.routing.RouterContract

@Suppress("UNUSED_PARAMETER", "UNCHECKED_CAST", "DEPRECATION")
class FloatingFragment : DialogFragment(R.layout.dialog_fragment_content) {

    private val router by lazy { MainApplication.getInstance().injector.router() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = requireArguments()
        val contentFragmentClass = args.getSerializable(KEY_CONTENT_FRAGMENT_CLASS)!! as Class<out Fragment>
        val navigationArgs = args.getBundle(KEY_NAVIGATION_ARGS)!!

        childFragmentManager
            .beginTransaction()
            .add(R.id.child_fragment_host, contentFragmentClass, navigationArgs)
            .commit()
    }

    override fun onCancel(dialog: DialogInterface) {
        // when the user manually dismisses the dialog (back button or touch on scrim), we need to notify the router so
        // it can update the backstack appropriately
        router.trySend(RouterContract.Inputs.GoBack())
    }

    companion object {
        public const val KEY_CONTENT_FRAGMENT_CLASS = "KEY_CONTENT_FRAGMENT_CLASS"
        public const val KEY_NAVIGATION_ARGS = "KEY_NAVIGATION_ARGS"

        fun create(
            contentFragmentClass: Class<out Fragment>,
            navigationArgs: Bundle,
        ): FloatingFragment {
            return FloatingFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(KEY_CONTENT_FRAGMENT_CLASS, contentFragmentClass)
                    putBundle(KEY_NAVIGATION_ARGS, navigationArgs)
                }
            }
        }
    }
}
