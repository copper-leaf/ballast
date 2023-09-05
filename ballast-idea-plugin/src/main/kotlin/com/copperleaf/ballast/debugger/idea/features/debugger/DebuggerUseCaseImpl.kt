package com.copperleaf.ballast.debugger.idea.features.debugger

import com.copperleaf.ballast.debugger.idea.features.debugger.repository.DebuggerUseCase
import com.copperleaf.ballast.debugger.idea.repository.RepositoryViewModel
import com.copperleaf.ballast.debugger.idea.settings.DebuggerUiSettings
import com.copperleaf.ballast.debugger.idea.settings.GeneralSettings
import com.copperleaf.ballast.debugger.server.BallastDebuggerServerSettings
import com.copperleaf.ballast.repository.cache.Cached
import com.copperleaf.ballast.repository.cache.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

public class DebuggerUseCaseImpl(
    private val viewModel: RepositoryViewModel
) : DebuggerUseCase {

    override fun observeGeneralSettings(): Flow<Cached<GeneralSettings>> {
        return viewModel.observeStates().map { state ->
            state.settings.map { settings ->
                settings
            }
        }
    }

    override fun observeBallastDebuggerServerSettings(): Flow<Cached<BallastDebuggerServerSettings>> {
        return viewModel.observeStates().map { state ->
            state.settings.map { settings ->
                settings
            }
        }
    }

    override fun observeDebuggerUiSettings(): Flow<Cached<DebuggerUiSettings>> {
        return viewModel.observeStates().map { state ->
            state.settings.map { settings ->
                settings
            }
        }
    }
}
