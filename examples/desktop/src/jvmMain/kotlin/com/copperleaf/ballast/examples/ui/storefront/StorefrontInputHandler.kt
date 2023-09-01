package com.copperleaf.ballast.examples.ui.storefront

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.examples.ui.storefront.api.CoffeeProductsApi
import com.copperleaf.ballast.examples.ui.storefront.models.ColumnSort
import com.copperleaf.ballast.postInput

public class StorefrontInputHandler(
    private val api: CoffeeProductsApi,
) : InputHandler<
        StorefrontContract.Inputs,
        StorefrontContract.Events,
        StorefrontContract.State> {
    override suspend fun InputHandlerScope<
            StorefrontContract.Inputs,
            StorefrontContract.Events,
            StorefrontContract.State>.handleInput(
        input: StorefrontContract.Inputs
    ): Unit = when (input) {
        is StorefrontContract.Inputs.Initialize -> {
            updateState { it.copy(loading = true) }
            val totalNumberOfProducts = api.getTotalProductsCount()
            updateState { it.copy(loading = false, totalNumberOfProducts = totalNumberOfProducts) }

            postInput(StorefrontContract.Inputs.QueryCoffeeProducts)
        }

        is StorefrontContract.Inputs.QueryCoffeeProducts -> {
            val currentState = updateStateAndGet { it.copy(loading = true) }
            val filteredProducts = api.queryProducts(
                searchQuery = currentState.searchQuery.takeIf { it.isNotEmpty() },
                filterByTags = currentState.filterByTags.takeIf { it.isNotEmpty() },
                filterInStock = currentState.filterInStock,
                priceRange = currentState.priceRange.takeIf { it.start != 0u || it.endInclusive != UInt.MAX_VALUE },
                rating = currentState.filterByRating,
                sortResultsBy = currentState.sortResultsBy,
            )

            updateState { it.copy(loading = false, filteredCoffeeProducts = filteredProducts) }
        }

        is StorefrontContract.Inputs.UpdateSearchQuery -> {
            updateState {
                it.copy(
                    searchQuery = input.searchQuery
                )
            }
            postInput(StorefrontContract.Inputs.QueryCoffeeProducts)
        }

        is StorefrontContract.Inputs.ToggleColumnSort -> {
            updateState {
                it.copy(
                    sortResultsBy = it
                        .sortResultsBy
                        .toMutableList()
                        .apply {
                            val sortColumnIndex = indexOfFirst { it.column == input.column }
                            val sortColumnCurrentDirection = this.getOrNull(sortColumnIndex)?.sortDirection
                            when (sortColumnCurrentDirection) {
                                null -> {
                                    // no sort on this colum yet. Add it sorting ascending at the end
                                    add(ColumnSort(input.column, ColumnSort.Direction.Ascending))
                                }

                                ColumnSort.Direction.Ascending -> {
                                    // existing sort was ascending, switch it to descending
                                    this[sortColumnIndex] = ColumnSort(input.column, ColumnSort.Direction.Descending)
                                }

                                ColumnSort.Direction.Descending -> {
                                    // existing sort was descending, remove it from the sort
                                    removeAt(sortColumnIndex)
                                }
                            }
                        }
                        .toList()
                )
            }
            postInput(StorefrontContract.Inputs.QueryCoffeeProducts)
        }

        is StorefrontContract.Inputs.ToggleTag -> {
            updateState {
                it.copy(
                    filterByTags = it
                        .filterByTags
                        .toMutableList()
                        .apply {
                            if (input.tag in this) {
                                remove(input.tag)
                            } else {
                                add(input.tag)
                            }
                        }
                        .toList()
                )
            }
            postInput(StorefrontContract.Inputs.QueryCoffeeProducts)
        }

        is StorefrontContract.Inputs.ToggleFilterInStock -> {
            updateState { it.copy(filterInStock = !it.filterInStock) }
            postInput(StorefrontContract.Inputs.QueryCoffeeProducts)
        }

        is StorefrontContract.Inputs.UpdatePriceRangeMin -> {
            updateState { it.copy(priceRange = input.minPrice..it.priceRange.endInclusive) }
            postInput(StorefrontContract.Inputs.QueryCoffeeProducts)
        }

        is StorefrontContract.Inputs.UpdatePriceRangeMax -> {
            updateState { it.copy(priceRange = it.priceRange.start..input.maxPrice) }
            postInput(StorefrontContract.Inputs.QueryCoffeeProducts)
        }

        is StorefrontContract.Inputs.UpdateRating -> {
            updateState { it.copy(filterByRating = input.rating) }
            postInput(StorefrontContract.Inputs.QueryCoffeeProducts)
        }
    }
}
