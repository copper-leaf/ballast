---
---

# {{ page.title }}

Ballast can be used from SwiftUI, but it requires a bit of boilerplate to be added to your iOS Swift code. The ViewModel
implementation needed for iOS is `IosViewModel`.

> The following instructions for integrating Ballast into Swift are largely taken from Touchlab's wonderful 
> [KaMPKit project][1]. The KaMPKit repo has been forked, and its Repository and ViewModel layers replaced with Ballast 
> in the [copper-leaf fork][2] to show example usage of Ballast in iOS, rather than the custom equivalents used in the
> standard project.

## Initial Setup (one-time)

Ballast can only be used in iOS with the new Kotlin/Native memory model. Start by making sure your project targets
the new memory model with [these instructions][5]. You will also need to make sure you declare an explicit dependency on
`kotlinx-coroutines-core` version `1.6.0` or greater, because Ballast is compiled against coroutines 1.5.3, currently.

```kotlin
val commonMain by getting {
    dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    }
}
```

Next, you'll need to create a Swift file in your iOS project to hold some Swift classes that wraps the Ballast ViewModel
and converts its StateFlow into a Combine Publisher. You really don't need to understand what's in this file, you'll 
only need to create it once. Copy [this file][3] from the Ballast KaMPKit repo to your iOS Swift sources to add the 
necessary boilerplate which connects Kotlin's Flows and Ballast's ViewModels to Swift's Combine framework, so that it 
can be accessed properly from SwiftUI.

Finally, you will also need to configure your Gradle scripts to [export the Ballast dependencies][4] that need to be 
used from Swift code. You will need to export `ballast-core`, and probably `ballast-repository` if you're using that 
module. The dependencies you export will also need to be declared as an `api` dependency, not `implementation`.

```kotlin
kotlin {
    ios()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api("io.github.copper-leaf:ballast-core:{{site.version}}")
                api("io.github.copper-leaf:ballast-repository:{{site.version}}")
                implementation("io.github.copper-leaf:ballast-saved-state:{{site.version}}")
            }
        }
    }

    cocoapods {
        framework {
            isStatic = false // SwiftUI preview requires dynamic framework
            export("io.github.copper-leaf:ballast-core:{{site.version}}")
            export("io.github.copper-leaf:ballast-repository:{{site.version}}")
        }
    }
}
```

## Using Ballast from SwiftUI

Then, from any SwiftUI View, you can observe one of your `IosViewModels` by wrapping it in `BallastObservable`. You'll 
need to manually connect the `BallastObservable` to the SwiftUI View's lifecycle by calling 
`.activate()`/`.deactivate()` on the View's `.onAppear { }`/`.onDisappear { }` callbacks. One-time initialization should
also be placed in `.onAppear()`.

Just like with Jetpack Compose, you should have a separate `*Content` View that has no direct knowledge of the Ballast
ViewModel. You'll pass in the observable's `vmState` and a callback function for `postInput` from the screen that 
contains the ViewModel and manages its lifecycle. The `*Content` View, then, only needs to be responsible for displaying
its content from the non-null `vmState` value, and passing Inputs through `postInput` to be processed by the Ballst 
ViewModel. Note that Kotlin's Swift name translation will convert the nested class names like 
`ExampleContract.Inputs.Initialize` to drop the second `.` (looking like `ExampleContract.InputsInitialize` when created
in Swift), and will also require you to provide labels for the parameters for all Inputs.

```swift
import Combine
import SwiftUI
import shared

struct ExampleScreen: View {

    @ObservedObject var vm = BallastObservable<
        ExampleContract.Inputs,
        ExampleContract.Events,
        ExampleContract.State>(
            viewModelFactory: { ExampleViewModel() }, // create directly or pass it in via DI
            eventHandlerFactory: { ExampleEventHandler() } // optional, create directly or pass it in via DI
    )

    var body: some View {
        ExampleContent(
            vmState: observableModel.vmState,
            postInput: observableModel.postInput
        )
        .onAppear(perform: {
            observableModel.activate()
            observableModel.postInput(ExampleContract.InputsInitialize())
        })
        .onDisappear(perform: {
            observableModel.deactivate()
        })
    }
}

struct ExampleContent: View {
    var vmState: ExampleContract.State
    var postInput: (ExampleContract.Inputs) -> Void

    var body: some View {
        // ...
    }
}
```

Since the syntax for appear/disappear will be so common in a Ballast MVI project, the [CombineAdapters.swift file][3] 
includes a `.withViewModel` View extension to reduce the boilerplate a bit

```swift
var body: some View {
    ExampleContent(
        vmState: observableModel.vmState,
        postInput: observableModel.postInput
    )
    .withViewModel(observableModel) {
        observableModel.activate()
        observableModel.postInput(ExampleContract.InputsInitialize())
    }
}
```

[1]: https://github.com/touchlab/KaMPKit
[2]: https://github.com/copper-leaf/KaMPKit-ballast
[3]: https://github.com/copper-leaf/KaMPKit-ballast/blob/main/ios/KaMPKitiOS/CombineAdapters.swift
[4]: https://kotlinlang.org/docs/multiplatform-build-native-binaries.html#export-dependencies-to-binaries
[5]: https://github.com/JetBrains/kotlin/blob/master/kotlin-native/NEW_MM.md#enable-the-new-mm
