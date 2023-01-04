package com.copperleaf.ballast.debugger.idea.ui.debugger

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.copperleaf.ballast.debugger.idea.ui.debugger.injector.DebuggerToolWindowInjector
import com.copperleaf.ballast.debugger.idea.ui.debugger.router.DebuggerRoute
import com.copperleaf.ballast.debugger.idea.ui.debugger.widgets.LocalTimer
import com.copperleaf.ballast.debugger.idea.ui.debugger.widgets.currentTimeAsState
import com.copperleaf.ballast.debugger.idea.ui.debugger.widgets.*
import com.copperleaf.ballast.navigation.routing.Destination
import com.copperleaf.ballast.navigation.routing.renderCurrentDestination

object DebuggerToolWindowUi {

    @Composable
    fun Content(injector: DebuggerToolWindowInjector) {
        val debuggerUiViewModel = remember(injector) { injector.debuggerUiViewModel }
        val debuggerUiState by debuggerUiViewModel.observeStates().collectAsState()

        val time by currentTimeAsState()

        CompositionLocalProvider(LocalTimer provides time) {
            Content(
                debuggerUiState,
                debuggerUiViewModel::trySend,
            )
        }
    }

    @Composable
    fun Content(
        uiState: DebuggerUiContract.State,
        postInput: (DebuggerUiContract.Inputs) -> Unit,
    ) {
        uiState.backstack.renderCurrentDestination(
            route = { currentRoute ->
                RouteContent(currentRoute, uiState, postInput)
            },
            notFound = { },
        )
    }

    @Composable
    fun Destination.Match<DebuggerRoute>.RouteContent(
        currentRoute: DebuggerRoute,
        uiState: DebuggerUiContract.State,
        postInput: (DebuggerUiContract.Inputs) -> Unit,
    ) {
        when (currentRoute) {
            DebuggerRoute.Connection -> {
                val connectionsList = rememberConnectionsList(uiState.serverState)
                val connection = rememberSelectedConnection(connectionsList)
                val viewModelList = rememberViewModelList(connection)
                val currentAppDestination = rememberConnectionCurrentDestination(connection, uiState.uiSettings)

                DebuggerScaffold(
                    primaryToolbar = {
                        DebuggerPrimaryToolbar(
                            currentRoute,
                            connectionsList,
                            connection,
                            viewModelList,
                            null,
                            uiState.searchText,
                            postInput,
                        )
                    },
                    secondaryContent = { SpecialRouterToolbar(currentAppDestination, postInput) },
                )
            }

            DebuggerRoute.ViewModelStates -> {
                val connectionsList = rememberConnectionsList(uiState.serverState)
                val connection = rememberSelectedConnection(connectionsList)
                val viewModelList = rememberViewModelList(connection)
                val viewModel = rememberSelectedViewModel(connection)
                val currentAppDestination = rememberConnectionCurrentDestination(connection, uiState.uiSettings)
                val statesList = rememberViewModelStatesList(viewModel, uiState.searchText)

                DebuggerScaffold(
                    primaryToolbar = {
                        DebuggerPrimaryToolbar(
                            currentRoute,
                            connectionsList,
                            connection,
                            viewModelList,
                            viewModel,
                            uiState.searchText,
                            postInput,
                        )
                    },
                    tabs = { ViewModelTabStrip(connection, viewModel, postInput) },
                    mainContentLeft = { StatesList(connection, viewModel, statesList, null, postInput) },
                    contentLeftToolbar = { StatesListToolbar(connection, viewModel, statesList, postInput) },
                    secondaryContent = { SpecialRouterToolbar(currentAppDestination, postInput) },
                )
            }

            DebuggerRoute.ViewModelStateDetails -> {
                val connectionsList = rememberConnectionsList(uiState.serverState)
                val connection = rememberSelectedConnection(connectionsList)
                val viewModelList = rememberViewModelList(connection)
                val viewModel = rememberSelectedViewModel(connection)
                val currentAppDestination = rememberConnectionCurrentDestination(connection, uiState.uiSettings)
                val statesList = rememberViewModelStatesList(viewModel, uiState.searchText)
                val selectedState = rememberSelectedViewModelStateSnapshot(viewModel)

                DebuggerScaffold(
                    primaryToolbar = {
                        DebuggerPrimaryToolbar(
                            currentRoute,
                            connectionsList,
                            connection,
                            viewModelList,
                            viewModel,
                            uiState.searchText,
                            postInput,
                        )
                    },
                    tabs = { ViewModelTabStrip(connection, viewModel, postInput) },
                    mainContentLeft = { StatesList(connection, viewModel, statesList, selectedState, postInput) },
                    contentLeftToolbar = { StatesListToolbar(connection, viewModel, statesList, postInput) },
                    mainContentRight = { StateDetails(selectedState, postInput) },
                    contentRightToolbar = { StateDetailsToolbar(connection, viewModel, selectedState, postInput) },
                    secondaryContent = { SpecialRouterToolbar(currentAppDestination, postInput) },
                )
            }

            DebuggerRoute.ViewModelInputs -> {
                val connectionsList = rememberConnectionsList(uiState.serverState)
                val connection = rememberSelectedConnection(connectionsList)
                val viewModelList = rememberViewModelList(connection)
                val viewModel = rememberSelectedViewModel(connection)
                val currentAppDestination = rememberConnectionCurrentDestination(connection, uiState.uiSettings)
                val inputsList = rememberViewModelInputsList(viewModel, uiState.searchText)

                DebuggerScaffold(
                    primaryToolbar = {
                        DebuggerPrimaryToolbar(
                            currentRoute,
                            connectionsList,
                            connection,
                            viewModelList,
                            viewModel,
                            uiState.searchText,
                            postInput,
                        )
                    },
                    tabs = { ViewModelTabStrip(connection, viewModel, postInput) },
                    mainContentLeft = { InputsList(connection, viewModel, inputsList, null, postInput) },
                    contentLeftToolbar = { InputsListToolbar(connection, viewModel, inputsList, postInput) },
                    secondaryContent = { SpecialRouterToolbar(currentAppDestination, postInput) },
                )
            }

            DebuggerRoute.ViewModelInputDetails -> {
                val connectionsList = rememberConnectionsList(uiState.serverState)
                val connection = rememberSelectedConnection(connectionsList)
                val viewModelList = rememberViewModelList(connection)
                val viewModel = rememberSelectedViewModel(connection)
                val currentAppDestination = rememberConnectionCurrentDestination(connection, uiState.uiSettings)
                val inputsList = rememberViewModelInputsList(viewModel, uiState.searchText)
                val selectedInput = rememberSelectedViewModelInput(viewModel)

                DebuggerScaffold(
                    primaryToolbar = {
                        DebuggerPrimaryToolbar(
                            currentRoute,
                            connectionsList,
                            connection,
                            viewModelList,
                            viewModel,
                            uiState.searchText,
                            postInput,
                        )
                    },
                    tabs = { ViewModelTabStrip(connection, viewModel, postInput) },
                    mainContentLeft = { InputsList(connection, viewModel, inputsList, selectedInput, postInput) },
                    contentLeftToolbar = { InputsListToolbar(connection, viewModel, inputsList, postInput) },
                    mainContentRight = { InputDetails(selectedInput, postInput) },
                    contentRightToolbar = { InputDetailsToolbar(connection, viewModel, selectedInput, postInput) },
                    secondaryContent = { SpecialRouterToolbar(currentAppDestination, postInput) },
                )
            }

            DebuggerRoute.ViewModelEvents -> {
                val connectionsList = rememberConnectionsList(uiState.serverState)
                val connection = rememberSelectedConnection(connectionsList)
                val viewModelList = rememberViewModelList(connection)
                val viewModel = rememberSelectedViewModel(connection)
                val currentAppDestination = rememberConnectionCurrentDestination(connection, uiState.uiSettings)
                val eventsList = rememberViewModelEventsList(viewModel, uiState.searchText)

                DebuggerScaffold(
                    primaryToolbar = {
                        DebuggerPrimaryToolbar(
                            currentRoute,
                            connectionsList,
                            connection,
                            viewModelList,
                            viewModel,
                            uiState.searchText,
                            postInput,
                        )
                    },
                    tabs = { ViewModelTabStrip(connection, viewModel, postInput) },
                    mainContentLeft = { EventsList(connection, viewModel, eventsList, null, postInput) },
                    contentLeftToolbar = { EventsListToolbar(connection, viewModel, eventsList, postInput) },
                    secondaryContent = { SpecialRouterToolbar(currentAppDestination, postInput) },
                )
            }

            DebuggerRoute.ViewModelEventDetails -> {
                val connectionsList = rememberConnectionsList(uiState.serverState)
                val connection = rememberSelectedConnection(connectionsList)
                val viewModelList = rememberViewModelList(connection)
                val viewModel = rememberSelectedViewModel(connection)
                val currentAppDestination = rememberConnectionCurrentDestination(connection, uiState.uiSettings)
                val eventsList = rememberViewModelEventsList(viewModel, uiState.searchText)
                val selectedEvent = rememberSelectedViewModelEvent(viewModel)

                DebuggerScaffold(
                    primaryToolbar = {
                        DebuggerPrimaryToolbar(
                            currentRoute,
                            connectionsList,
                            connection,
                            viewModelList,
                            viewModel,
                            uiState.searchText,
                            postInput,
                        )
                    },
                    tabs = { ViewModelTabStrip(connection, viewModel, postInput) },
                    mainContentLeft = { EventsList(connection, viewModel, eventsList, selectedEvent, postInput) },
                    contentLeftToolbar = { EventsListToolbar(connection, viewModel, eventsList, postInput) },
                    mainContentRight = { EventDetails(selectedEvent, postInput) },
                    contentRightToolbar = { EventDetailsToolbar(connection, viewModel, selectedEvent, postInput) },
                    secondaryContent = { SpecialRouterToolbar(currentAppDestination, postInput) },
                )
            }

            DebuggerRoute.ViewModelSideJobs -> {
                val connectionsList = rememberConnectionsList(uiState.serverState)
                val connection = rememberSelectedConnection(connectionsList)
                val viewModelList = rememberViewModelList(connection)
                val viewModel = rememberSelectedViewModel(connection)
                val currentAppDestination = rememberConnectionCurrentDestination(connection, uiState.uiSettings)
                val sideJobsList = rememberViewModelSideJobsList(viewModel, uiState.searchText)

                DebuggerScaffold(
                    primaryToolbar = {
                        DebuggerPrimaryToolbar(
                            currentRoute,
                            connectionsList,
                            connection,
                            viewModelList,
                            viewModel,
                            uiState.searchText,
                            postInput,
                        )
                    },
                    tabs = { ViewModelTabStrip(connection, viewModel, postInput) },
                    mainContentLeft = { SideJobsList(connection, viewModel, sideJobsList, null, postInput) },
                    contentLeftToolbar = { SideJobsListToolbar(connection, viewModel, sideJobsList, postInput) },
                    secondaryContent = { SpecialRouterToolbar(currentAppDestination, postInput) },
                )
            }

            DebuggerRoute.ViewModelSideJobDetails -> {
                val connectionsList = rememberConnectionsList(uiState.serverState)
                val connection = rememberSelectedConnection(connectionsList)
                val viewModelList = rememberViewModelList(connection)
                val viewModel = rememberSelectedViewModel(connection)
                val currentAppDestination = rememberConnectionCurrentDestination(connection, uiState.uiSettings)
                val sideJobsList = rememberViewModelSideJobsList(viewModel, uiState.searchText)
                val selectedSideJob = rememberSelectedViewModelSideJob(viewModel)

                DebuggerScaffold(
                    primaryToolbar = {
                        DebuggerPrimaryToolbar(
                            currentRoute,
                            connectionsList,
                            connection,
                            viewModelList,
                            viewModel,
                            uiState.searchText,
                            postInput,
                        )
                    },
                    tabs = { ViewModelTabStrip(connection, viewModel, postInput) },
                    mainContentLeft = { SideJobsList(connection, viewModel, sideJobsList, selectedSideJob, postInput) },
                    contentLeftToolbar = { SideJobsListToolbar(connection, viewModel, sideJobsList, postInput) },
                    mainContentRight = { SideJobDetails(selectedSideJob, postInput) },
                    contentRightToolbar = { SideJobDetailsToolbar(connection, viewModel, selectedSideJob, postInput) },
                    secondaryContent = { SpecialRouterToolbar(currentAppDestination, postInput) },
                )
            }

            DebuggerRoute.ViewModelInterceptors -> {
                val connectionsList = rememberConnectionsList(uiState.serverState)
                val connection = rememberSelectedConnection(connectionsList)
                val viewModelList = rememberViewModelList(connection)
                val viewModel = rememberSelectedViewModel(connection)
                val currentAppDestination = rememberConnectionCurrentDestination(connection, uiState.uiSettings)
                val eventsList = rememberViewModelEventsList(viewModel, uiState.searchText)

                DebuggerScaffold(
                    primaryToolbar = {
                        DebuggerPrimaryToolbar(
                            currentRoute,
                            connectionsList,
                            connection,
                            viewModelList,
                            viewModel,
                            uiState.searchText,
                            postInput,
                        )
                    },
                    tabs = { ViewModelTabStrip(connection, viewModel, postInput) },
                    mainContentLeft = { InterceptorsList(connection, viewModel, eventsList, null, postInput) },
                    contentLeftToolbar = { InterceptorsListToolbar(connection, viewModel, eventsList, postInput) },
                    secondaryContent = { SpecialRouterToolbar(currentAppDestination, postInput) },
                )
            }

            DebuggerRoute.ViewModelInterceptorDetails -> {
                val connectionsList = rememberConnectionsList(uiState.serverState)
                val connection = rememberSelectedConnection(connectionsList)
                val viewModelList = rememberViewModelList(connection)
                val viewModel = rememberSelectedViewModel(connection)
                val currentAppDestination = rememberConnectionCurrentDestination(connection, uiState.uiSettings)
                val eventsList = rememberViewModelEventsList(viewModel, uiState.searchText)
                val selectedEvent = rememberSelectedViewModelEvent(viewModel)

                DebuggerScaffold(
                    primaryToolbar = {
                        DebuggerPrimaryToolbar(
                            currentRoute,
                            connectionsList,
                            connection,
                            viewModelList,
                            viewModel,
                            uiState.searchText,
                            postInput,
                        )
                    },
                    tabs = { ViewModelTabStrip(connection, viewModel, postInput) },
                    mainContentLeft = { InterceptorsList(connection, viewModel, eventsList, selectedEvent, postInput) },
                    contentLeftToolbar = { InterceptorsListToolbar(connection, viewModel, eventsList, postInput) },
                    mainContentRight = { InterceptorDetails(selectedEvent, postInput) },
                    contentRightToolbar = {
                        InterceptorDetailsToolbar(
                            connection,
                            viewModel,
                            selectedEvent,
                            postInput
                        )
                    },
                    secondaryContent = { SpecialRouterToolbar(currentAppDestination, postInput) },
                )
            }

            DebuggerRoute.ViewModelLogs -> {
                val connectionsList = rememberConnectionsList(uiState.serverState)
                val connection = rememberSelectedConnection(connectionsList)
                val viewModelList = rememberViewModelList(connection)
                val viewModel = rememberSelectedViewModel(connection)
                val currentAppDestination = rememberConnectionCurrentDestination(connection, uiState.uiSettings)

                DebuggerScaffold(
                    primaryToolbar = {
                        DebuggerPrimaryToolbar(
                            currentRoute,
                            connectionsList,
                            connection,
                            viewModelList,
                            viewModel,
                            uiState.searchText,
                            postInput,
                        )
                    },
                    tabs = { ViewModelTabStrip(connection, viewModel, postInput) },
                    secondaryContent = { SpecialRouterToolbar(currentAppDestination, postInput) },
                )
            }

            DebuggerRoute.ViewModelTimeline -> {
                val connectionsList = rememberConnectionsList(uiState.serverState)
                val connection = rememberSelectedConnection(connectionsList)
                val viewModelList = rememberViewModelList(connection)
                val viewModel = rememberSelectedViewModel(connection)
                val currentAppDestination = rememberConnectionCurrentDestination(connection, uiState.uiSettings)

                DebuggerScaffold(
                    primaryToolbar = {
                        DebuggerPrimaryToolbar(
                            currentRoute,
                            connectionsList,
                            connection,
                            viewModelList,
                            viewModel,
                            uiState.searchText,
                            postInput,
                        )
                    },
                    tabs = { ViewModelTabStrip(connection, viewModel, postInput) },
                    secondaryContent = { SpecialRouterToolbar(currentAppDestination, postInput) },
                )
            }
        }
    }
}
