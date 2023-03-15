package com.copperleaf.ballast.debugger.idea.repository

import com.copperleaf.ballast.core.BasicViewModel

typealias RepositoryViewModel = BasicViewModel<
        RepositoryContract.Inputs,
        RepositoryContract.Events,
        RepositoryContract.State>
