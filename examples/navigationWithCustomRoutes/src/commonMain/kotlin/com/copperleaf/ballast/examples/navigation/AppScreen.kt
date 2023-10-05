package com.copperleaf.ballast.examples.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.copperleaf.ballast.navigation.routing.RouterContract
import com.copperleaf.ballast.navigation.routing.Tag

interface AppScreen {
    @Composable
    fun Content()
}

@Route
@InitialRoute
data object Home : AppScreen {
    @Composable
    override fun Content() {
        val localRouter = LocalRouter.current
        val goToPostList: () -> Unit = {
            localRouter.trySend(
                RouterContract.Inputs.GoToDestination(
                    PostList.navigate(null)
                )
            )
        }
        val goToPost: (postId: Int) -> Unit = { postId ->
            localRouter.trySend(
                RouterContract.Inputs.GoToDestination(
                    PostDetails.navigate(postId)
                )
            )
        }

        Column {
            Text("Home")

            Button({ goToPostList() }) {
                Text("Go To Post List")
            }

            Button({ goToPost(5) }) {
                Text("Go To Latest Post")
            }
        }
    }
}

@Route(path = "/blog")
data class PostList(
    @QueryParameter public val sort: String?
) : AppScreen {
    @Composable
    override fun Content() {
        val localRouter = LocalRouter.current
        val changeSort: (String) -> Unit = { sortDirection ->
            localRouter.trySend(
                RouterContract.Inputs.ReplaceTopDestination(
                    PostList.navigate(sortDirection)
                )
            )
        }
        val goBack: () -> Unit = {
            localRouter.trySend(RouterContract.Inputs.GoBack())
        }
        val goToPost: (postId: Int) -> Unit = { postId ->
            localRouter.trySend(
                RouterContract.Inputs.GoToDestination(
                    PostDetails.navigate(postId)
                )
            )
        }

        Column {
            Text("Post List")

            Button({ goBack() }) {
                Text("Go Back")
            }

            if (sort == "asc") {
                Button({ changeSort("desc") }) {
                    Text("Sort Descending")
                }
            } else {
                Button({ changeSort("asc") }) {
                    Text("Sort Ascending")
                }
            }

            val posts = if (sort == "asc") {
                (1..5)
            } else {
                (1..5).reversed()
            }

            posts.forEach { index ->
                Button({ goToPost(index) }) {
                    Text("Go To Post $index")
                }
            }
        }
    }

    companion object {}
}

@Route(path = "/blog/{postId}")
@With(Tag::class, "Authenticated")
data class PostDetails(
    @PathParameter val postId: Int,
) : AppScreen {
    @Composable
    override fun Content() {
        val localRouter = LocalRouter.current

        val goBack: () -> Unit = {
            localRouter.trySend(RouterContract.Inputs.GoBack())
        }

        Column {
            Text("Post $postId")

            Button({ goBack() }) {
                Text("Go Back")
            }
        }
    }

    companion object {}
}
