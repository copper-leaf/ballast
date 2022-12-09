package com.copperleaf.ballast.navigation

import com.copperleaf.ballast.core.AndroidViewModel
import com.copperleaf.ballast.navigation.routing.RouterContract

public typealias AndroidRouter<T> = AndroidViewModel<
    RouterContract.Inputs<T>,
    RouterContract.Events<T>,
    RouterContract.State<T>>
