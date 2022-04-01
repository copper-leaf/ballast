---
---

# {{ page.title }}

Ballast can be used from SwiftUI, but it requires a bit of boilerplate to be added to your iOS Swift code. The ViewModel
implementation needed for iOS is `IosViewModel`.

Start by creating a Swift file in your iOS project to hold some Swift classes that wraps the Ballst ViewModel and 
converts its StateFlow into a Combine Publisher. You really don't need to understand what's in this file, you'll only 
need to create it once.

```swift
// Ballast+.swift

import Combine
import Foundation
import shared

// Originally created by Nicklas Jensen (@Nillerr on GitHub) and published under MIT license in the following Article
// and GitHub Gist:
// - https://betterprogramming.pub/using-kotlin-flow-in-swift-3e7b53f559b6
// - https://gist.github.com/Nillerr/dc437c02485da661b4f285cee01069a1
//
// Some changes were made to conform the types in this file to those in the Ballast Core library.

typealias OnEach<Output> = (Output) -> Void
typealias OnCollect<Output> = (@escaping OnEach<Output>) -> Ballast_coreCloseable
typealias OnCollect1<T1, Output> = (T1, @escaping OnEach<Output>) -> Ballast_coreCloseable

/**
 Creates a `Publisher` that collects output from a flow wrapper function emitting values from an underlying
 instance of `Flow<T>`.
 */
func collect<T1, Output>(_ onCollect: @escaping OnCollect1<T1, Output>, with arg1: T1) -> Publishers.Flow<Output, Error> {
    return Publishers.Flow { onCollect(arg1, $0) }
}

/**
 Wraps a Ballast `Closeable` in a Combine `Subscription`
 */
class SharedCancellableSubscription: Subscription {
    private var isCancelled: Bool = false

    var cancellable: Ballast_coreCloseable? {
        didSet {
            if isCancelled {
                cancellable?.close()
            }
        }
    }

    func request(_ demand: Subscribers.Demand) {
        // Not supported
    }

    func cancel() {
        isCancelled = true
        cancellable?.close()
    }
}

extension Publishers {
    class Flow<Output, Failure: Error>: Publisher {
        private let onCollect: OnCollect<Output>

        init(onCollect: @escaping OnCollect<Output>) {
            self.onCollect = onCollect
        }

        func receive<S>(subscriber: S) where S: Subscriber, Failure == S.Failure, Output == S.Input {
            let subscription = SharedCancellableSubscription()
            subscriber.receive(subscription: subscription)

            let cancellable = onCollect({ input in _ = subscriber.receive(input) })

            subscription.cancellable = cancellable
        }
    }
}

public class BallastObservable<Inputs: AnyObject, Events: AnyObject, State: AnyObject>: ObservableObject {
    private var subscriptions = Set<AnyCancellable>()
    private let vm: Ballast_coreIosViewModel<Inputs, Events, State>
    @Published public private(set) var state: State

    init(vm: Ballast_coreIosViewModel<Inputs, Events, State>) {
        self.vm = vm
        self.state = self.vm.initialState
    }

    func trySend(_ input: Inputs) {
        self.vm.trySend(element: input)
    }
    
    func start(eventHandler: Ballast_coreEventHandler) {
        collect(self.vm.onEachState, with: eventHandler)
                .sink(
                    receiveCompletion: { completion in  },
                    receiveValue: { state in self.state = state }
                )
                .store(in: &subscriptions)
    }

    func clearSubscriptions() {
        subscriptions.forEach { cancellable in cancellable.cancel() }
        subscriptions.removeAll()
    }
}
```

Then, from any SwiftUI View, you can observe one of your `IosViewModels` by wrapping it in `BallastObservable`. One-time 
initialization should be placed in the View's `init()` block, which corresponds to Android's `onViewCreated()`. Note 
that Kotlin's Swift name translation will convert the nested class names like `ExampleContract.Inputs.Initialize` to
drop the second `.`, looking like `ExampleContract.InputsInitialize` in Swift.

_Disclaimer: I am not an iOS developer, so this may be the entirely wrong way to go about things. I have gotten this to
work in a POC application, but a full production iOS app built with Ballast will likely look different. If you figure 
out a good pattern, I invite you to update these docs to demonstrate that pattern!_

```swift
import SwiftUI
import shared

struct ExampleView: View {

    @ObservedObject var vm = BallastObservable<
        ExampleContract.Inputs,
        ExampleContract.Events,
        ExampleContract.State>(
            vm: ExampleViewModel(),
            eventHandler: ExampleEventHandler()
    )

    var body: some View {
        VStack { 
            // ...
        }
        .onAppear {
            vm.start(eventHandler: ExampleEventHandler())
            vm.trySend(ExampleContract.InputsInitialize())
        }
        .onDisappear { vm.clearSubscriptions() }
    }
}
```
