package com.copperleaf.ballast.examples.ui.storefront

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.Checkbox
import androidx.compose.material.Chip
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.copperleaf.ballast.examples.injector.ComposeDesktopInjector
import com.copperleaf.ballast.examples.ui.storefront.models.CoffeeProduct
import com.copperleaf.ballast.examples.ui.storefront.models.CoffeeProductColumn
import com.copperleaf.ballast.examples.ui.storefront.models.ColumnSort
import eu.wewox.lazytable.LazyTable
import eu.wewox.lazytable.LazyTableItem
import eu.wewox.lazytable.lazyTableDimensions
import eu.wewox.lazytable.lazyTablePinConfiguration

object StorefrontUi {

    @Composable
    fun Content(injector: ComposeDesktopInjector) {
        val viewModelCoroutineScope = rememberCoroutineScope()
        val vm = remember(viewModelCoroutineScope) {
            injector.storefrontViewModel(viewModelCoroutineScope)
        }
        val uiState by vm.observeStates().collectAsState()

        Content(uiState) { vm.trySend(it) }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    public fun Content(
        uiState: StorefrontContract.State,
        postInput: (StorefrontContract.Inputs) -> Unit,
    ) {
        Column(Modifier.fillMaxSize()) {
            TopAppBar(title = { Text("Undo/Redo") })
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                Column(Modifier.width(240.dp).fillMaxHeight()) {
                    if (uiState.loading) {
                        CircularProgressIndicator()
                    }

                    var searchText by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = {
                            searchText = it
                            postInput(StorefrontContract.Inputs.UpdateSearchQuery(it))
                        },
                        label = { Text("Search for coffees") }
                    )

                    ListItem(
                        modifier = Modifier
                            .toggleable(
                                value = uiState.filterInStock,
                                onValueChange = { postInput(StorefrontContract.Inputs.ToggleFilterInStock) }
                            ),
                        icon = {
                            Checkbox(
                                checked = uiState.filterInStock,
                                onCheckedChange = null
                            )
                        },
                        text = { Text("Show In Stock Only") }
                    )

                    OutlinedTextField(
                        value = uiState.priceRange.start.takeIf { it != 0u }?.toString() ?: "",
                        onValueChange = {
                            it.toUIntOrNull()?.let { intValue ->
                                postInput(StorefrontContract.Inputs.UpdatePriceRangeMin(intValue))
                            }
                        },
                        label = { Text("min price") }
                    )
                    OutlinedTextField(
                        value = uiState.priceRange.endInclusive.takeIf { it != UInt.MAX_VALUE }?.toString() ?: "",
                        onValueChange = {
                            it.toUIntOrNull()?.let { intValue ->
                                postInput(StorefrontContract.Inputs.UpdatePriceRangeMax(intValue))
                            }
                        },
                        label = { Text("max price") }
                    )

                    Text("Filter By Rating")
                    Row {
                        (1u..5u).map { ratingButtonValue ->
                            Column {
                                Text("$ratingButtonValue stars")
                                RadioButton(
                                    selected = uiState.filterByRating != null && uiState.filterByRating >= ratingButtonValue,
                                    onClick = {
                                        postInput(StorefrontContract.Inputs.UpdateRating(ratingButtonValue))
                                    },
                                )
                            }
                        }
                    }

                    Text("Filter By Tag")
                    FlowRow {
                        uiState.filterByTags.forEach { tag ->
                            Chip(
                                onClick = { postInput(StorefrontContract.Inputs.ToggleTag(tag)) },
                                content = { Text(tag) },
                            )
                        }
                    }

                    Text("${uiState.filteredCoffeeProducts.size} / ${uiState.totalNumberOfProducts} products visible")
                }
                Column(Modifier.weight(1f).fillMaxHeight()) {
                    if (uiState.filteredCoffeeProducts.isEmpty()) {
                        Text("No products")
                    } else {
                        val headerRow: List<Triple<Int, CoffeeProductColumn, CoffeeProduct?>> = CoffeeProductColumn
                            .entries
                            .map { column ->
                                Triple(0, column, null)
                            }

                        val coffeeProductRows: List<Triple<Int, CoffeeProductColumn, CoffeeProduct?>> = uiState
                            .filteredCoffeeProducts
                            .flatMapIndexed { rowIndex, product ->
                                CoffeeProductColumn
                                    .entries
                                    .map { column ->
                                        Triple(rowIndex + 1, column, product)
                                    }
                            }

                        val cells: List<Triple<Int, CoffeeProductColumn, CoffeeProduct?>> = headerRow + coffeeProductRows

                        LazyTable(
                            modifier = Modifier.fillMaxSize(),
                            dimensions = lazyTableDimensions(
                                columnSize = {
                                    when(CoffeeProductColumn.entries[it]) {
                                        CoffeeProductColumn.Name -> 180.dp
                                        CoffeeProductColumn.Description -> 240.dp
                                        CoffeeProductColumn.Tags -> 300.dp
                                        CoffeeProductColumn.Quantity -> 96.dp
                                        CoffeeProductColumn.Cost -> 96.dp
                                        CoffeeProductColumn.Rating -> 96.dp
                                        CoffeeProductColumn.NumberOfReviews -> 160.dp
                                    }
                                },
                                rowSize = {
                                    if(it == 0) {
                                        32.dp
                                    } else {
                                        96.dp
                                    }
                                },
                            ),
                            pinConfiguration = lazyTablePinConfiguration(columns = 1, rows = 1)
                        ) {
                            items(
                                items = cells,
                                layoutInfo = { (rowIndex, column, _) ->
                                    LazyTableItem(
                                        column = column.ordinal,
                                        row = rowIndex,
                                    )
                                }
                            ) { (_, column, product) ->
                                if (product != null) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .background(MaterialTheme.colors.surface)
                                            .border(Dp.Hairline, MaterialTheme.colors.onSurface)
                                    ) {
                                        when (column) {
                                            CoffeeProductColumn.Name -> {
                                                Text(product.name + "")
                                            }

                                            CoffeeProductColumn.Description -> {
                                                Text(product.description + "")
                                            }

                                            CoffeeProductColumn.Tags -> {
                                                FlowRow {
                                                    product.tags.forEach { tag ->
                                                        Chip(
                                                            onClick = { postInput(StorefrontContract.Inputs.ToggleTag(tag)) },
                                                            content = { Text(tag) },
                                                        )
                                                    }
                                                }
                                            }

                                            CoffeeProductColumn.Quantity -> {
                                                Text("${product.quantity}", maxLines = 1)
                                            }

                                            CoffeeProductColumn.Cost -> {
                                                Text("$${product.cost}.00", maxLines = 1)
                                            }

                                            CoffeeProductColumn.Rating -> {
                                                Text("${product.rating} / 5", maxLines = 1)
                                            }

                                            CoffeeProductColumn.NumberOfReviews -> {
                                                Text("${product.numberOfReviews}", maxLines = 1)
                                            }
                                        }
                                    }
                                } else {
                                    Row(
                                        horizontalArrangement = Arrangement.Start,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .background(MaterialTheme.colors.surface)
                                            .border(Dp.Hairline, MaterialTheme.colors.onSurface)
                                            .clickable { postInput(StorefrontContract.Inputs.ToggleColumnSort(column)) }
                                            .padding(8.dp)
                                    ) {
                                        Text(column.name)

                                        if(column.canSort) {
                                            val sortColumnIndex = uiState.sortResultsBy.indexOfFirst { it.column == column }
                                            val sortColumnCurrentDirection = uiState.sortResultsBy.getOrNull(sortColumnIndex)?.sortDirection
                                            val icon = when (sortColumnCurrentDirection) {
                                                null -> null
                                                ColumnSort.Direction.Ascending -> Icons.Default.ArrowDropDown
                                                ColumnSort.Direction.Descending -> Icons.Default.ArrowDropUp
                                            }

                                            if(icon != null) {
                                                Icon(icon, "")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
