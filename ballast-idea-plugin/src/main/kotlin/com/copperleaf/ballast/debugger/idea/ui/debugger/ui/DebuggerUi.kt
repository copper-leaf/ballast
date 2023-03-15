package com.copperleaf.ballast.debugger.idea.ui.debugger.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.copperleaf.ballast.debugger.idea.theme.IdeaPluginTheme
import com.copperleaf.ballast.debugger.idea.ui.debugger.injector.DebuggerToolWindowInjector
import com.copperleaf.ballast.debugger.idea.ui.debugger.router.DebuggerRoute
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.DebuggerPrimaryToolbar
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.DebuggerScaffold
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.EventDetails
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.EventDetailsToolbar
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.EventsList
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.EventsListToolbar
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.InputDetails
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.InputDetailsToolbar
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.InputsList
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.InputsListToolbar
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.InterceptorDetails
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.InterceptorDetailsToolbar
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.InterceptorsList
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.InterceptorsListToolbar
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.ProvideTime
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.SideJobDetails
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.SideJobDetailsToolbar
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.SideJobsList
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.SideJobsListToolbar
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.SpecialRouterToolbar
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.StateDetails
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.StateDetailsToolbar
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.StatesList
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.StatesListToolbar
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.ViewModelTabStrip
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.rememberConnectionCurrentDestination
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.rememberConnectionsList
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.rememberSelectedConnection
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.rememberSelectedViewModel
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.rememberSelectedViewModelEvent
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.rememberSelectedViewModelInput
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.rememberSelectedViewModelInterceptor
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.rememberSelectedViewModelSideJob
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.rememberSelectedViewModelStateSnapshot
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.rememberViewModelEventsList
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.rememberViewModelInputsList
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.rememberViewModelInterceptorList
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.rememberViewModelList
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.rememberViewModelSideJobsList
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.rememberViewModelStatesList
import com.copperleaf.ballast.debugger.idea.ui.debugger.vm.DebuggerUiContract
import com.copperleaf.ballast.navigation.routing.Destination
import com.copperleaf.ballast.navigation.routing.renderCurrentDestination

object DebuggerUi {

    @Composable
    fun Content(injector: DebuggerToolWindowInjector) {
        val debuggerUiViewModel = remember(injector) { injector.debuggerUiViewModel }
        val debuggerUiState by debuggerUiViewModel.observeStates().collectAsState()

        IdeaPluginTheme(injector.project, debuggerUiState.cachedSettings) {
            ProvideTime {
                Content(
                    debuggerUiState,
                    debuggerUiViewModel::trySend,
                )
            }
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
                val connectionsList by rememberConnectionsList(uiState.serverState)
                val connection by rememberSelectedConnection(connectionsList)
                val viewModelList by rememberViewModelList(connection)
                val currentAppDestination by rememberConnectionCurrentDestination(connection, uiState.cachedSettings)

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
                val connectionsList by rememberConnectionsList(uiState.serverState)
                val connection by rememberSelectedConnection(connectionsList)
                val viewModelList by rememberViewModelList(connection)
                val viewModel by rememberSelectedViewModel(connection)
                val currentAppDestination by rememberConnectionCurrentDestination(connection, uiState.cachedSettings)
                val statesList by rememberViewModelStatesList(viewModel, uiState.searchText)

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
                val connectionsList by rememberConnectionsList(uiState.serverState)
                val connection by rememberSelectedConnection(connectionsList)
                val viewModelList by rememberViewModelList(connection)
                val viewModel by rememberSelectedViewModel(connection)
                val currentAppDestination by rememberConnectionCurrentDestination(connection, uiState.cachedSettings)
                val statesList by rememberViewModelStatesList(viewModel, uiState.searchText)
                val selectedState by rememberSelectedViewModelStateSnapshot(viewModel)

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
                val connectionsList by rememberConnectionsList(uiState.serverState)
                val connection by rememberSelectedConnection(connectionsList)
                val viewModelList by rememberViewModelList(connection)
                val viewModel by rememberSelectedViewModel(connection)
                val currentAppDestination by rememberConnectionCurrentDestination(connection, uiState.cachedSettings)
                val inputsList by rememberViewModelInputsList(viewModel, uiState.searchText)

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
                val connectionsList by rememberConnectionsList(uiState.serverState)
                val connection by rememberSelectedConnection(connectionsList)
                val viewModelList by rememberViewModelList(connection)
                val viewModel by rememberSelectedViewModel(connection)
                val currentAppDestination by rememberConnectionCurrentDestination(connection, uiState.cachedSettings)
                val inputsList by rememberViewModelInputsList(viewModel, uiState.searchText)
                val selectedInput by rememberSelectedViewModelInput(viewModel)

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
                val connectionsList by rememberConnectionsList(uiState.serverState)
                val connection by rememberSelectedConnection(connectionsList)
                val viewModelList by rememberViewModelList(connection)
                val viewModel by rememberSelectedViewModel(connection)
                val currentAppDestination by rememberConnectionCurrentDestination(connection, uiState.cachedSettings)
                val eventsList by rememberViewModelEventsList(viewModel, uiState.searchText)

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
                val connectionsList by rememberConnectionsList(uiState.serverState)
                val connection by rememberSelectedConnection(connectionsList)
                val viewModelList by rememberViewModelList(connection)
                val viewModel by rememberSelectedViewModel(connection)
                val currentAppDestination by rememberConnectionCurrentDestination(connection, uiState.cachedSettings)
                val eventsList by rememberViewModelEventsList(viewModel, uiState.searchText)
                val selectedEvent by rememberSelectedViewModelEvent(viewModel)

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
                val connectionsList by rememberConnectionsList(uiState.serverState)
                val connection by rememberSelectedConnection(connectionsList)
                val viewModelList by rememberViewModelList(connection)
                val viewModel by rememberSelectedViewModel(connection)
                val currentAppDestination by rememberConnectionCurrentDestination(connection, uiState.cachedSettings)
                val sideJobsList by rememberViewModelSideJobsList(viewModel, uiState.searchText)

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
                val connectionsList by rememberConnectionsList(uiState.serverState)
                val connection by rememberSelectedConnection(connectionsList)
                val viewModelList by rememberViewModelList(connection)
                val viewModel by rememberSelectedViewModel(connection)
                val currentAppDestination by rememberConnectionCurrentDestination(connection, uiState.cachedSettings)
                val sideJobsList by rememberViewModelSideJobsList(viewModel, uiState.searchText)
                val selectedSideJob by rememberSelectedViewModelSideJob(viewModel)

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
                val connectionsList by rememberConnectionsList(uiState.serverState)
                val connection by rememberSelectedConnection(connectionsList)
                val viewModelList by rememberViewModelList(connection)
                val viewModel by rememberSelectedViewModel(connection)
                val currentAppDestination by rememberConnectionCurrentDestination(connection, uiState.cachedSettings)
                val interceptorList by rememberViewModelInterceptorList(viewModel, uiState.searchText)

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
                    mainContentLeft = { InterceptorsList(connection, viewModel, interceptorList, null, postInput) },
                    contentLeftToolbar = { InterceptorsListToolbar(connection, viewModel, interceptorList, postInput) },
                    secondaryContent = { SpecialRouterToolbar(currentAppDestination, postInput) },
                )
            }

            DebuggerRoute.ViewModelInterceptorDetails -> {
                val connectionsList by rememberConnectionsList(uiState.serverState)
                val connection by rememberSelectedConnection(connectionsList)
                val viewModelList by rememberViewModelList(connection)
                val viewModel by rememberSelectedViewModel(connection)
                val currentAppDestination by rememberConnectionCurrentDestination(connection, uiState.cachedSettings)
                val interceptorList by rememberViewModelInterceptorList(viewModel, uiState.searchText)
                val selectedInterceptor by rememberSelectedViewModelInterceptor(viewModel)

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
                    mainContentLeft = { InterceptorsList(connection, viewModel, interceptorList, selectedInterceptor, postInput) },
                    contentLeftToolbar = { InterceptorsListToolbar(connection, viewModel, interceptorList, postInput) },
                    mainContentRight = { InterceptorDetails(selectedInterceptor, postInput) },
                    contentRightToolbar = {
                        InterceptorDetailsToolbar(
                            connection,
                            viewModel,
                            selectedInterceptor,
                            postInput
                        )
                    },
                    secondaryContent = { SpecialRouterToolbar(currentAppDestination, postInput) },
                )
            }

            DebuggerRoute.ViewModelLogs -> {
                val connectionsList by rememberConnectionsList(uiState.serverState)
                val connection by rememberSelectedConnection(connectionsList)
                val viewModelList by rememberViewModelList(connection)
                val viewModel by rememberSelectedViewModel(connection)
                val currentAppDestination by rememberConnectionCurrentDestination(connection, uiState.cachedSettings)

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
                val connectionsList by rememberConnectionsList(uiState.serverState)
                val connection by rememberSelectedConnection(connectionsList)
                val viewModelList by rememberViewModelList(connection)
                val viewModel by rememberSelectedViewModel(connection)
                val currentAppDestination by rememberConnectionCurrentDestination(connection, uiState.cachedSettings)

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
