package com.copperleaf.ballast.debugger.idea.features.debugger.repository

import com.copperleaf.ballast.debugger.idea.settings.DebuggerUiSettings
import com.copperleaf.ballast.debugger.idea.settings.GeneralSettings
import com.copperleaf.ballast.debugger.server.BallastDebuggerServerSettings
import com.copperleaf.ballast.repository.cache.Cached
import kotlinx.coroutines.flow.Flow

public interface DebuggerUseCase {
    public fun observeGeneralSettings(): Flow<Cached<GeneralSettings>>
    public fun observeBallastDebuggerServerSettings(): Flow<Cached<BallastDebuggerServerSettings>>
    public fun observeDebuggerUiSettings(): Flow<Cached<DebuggerUiSettings>>
}
