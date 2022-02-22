---
---

# Compare to Redux

Ballast and Redux both implement the same MVI/Flux design pattern, but in slightly different ways, and each are a
product of the programming language they were built upon. For the most-part, all the main features of Redux have a 
corresponding feature in Ballast, but they may look quite different or work differently under-the-hood. But 
understanding how the terms match up may help experienced Redux developers pick up the concepts of Ballast more quickly.

One particular difference in how one should approach Redux vs Ballast is in how the model is constructed. In Redux, one 
assumes that the _actions_ are the most important part of the model, and a State is derived which contains the results 
of processing the Actions. Ballast assumes the opposite, that it's the State that is most important, and the programmer
derives Actions which can modify the State.

This page describes how some of the most common Redux features correspond to Ballast.

## Actions --> Inputs

The objects that the UI generates, which hold all the data necessary to tell the framework how to process itself. These 
objects do nothing in themselves, but are handed off to the library to be processed later.

- Redux: Actions
- Ballast: Inputs

## Action Creators --> Sealed Input Classes

The functions that define a contract for how to create an Action/Input. These ensure only well-formed data is actually 
sent into the library for processing.

- Redux: Action Creators are the functions that define the types of "actions" sent to the Store. They typically include
  a `type` and a `payload`, which are used to pass data into the innards of the Store. These functions define the 
  "contract" for what properties are sent in the payload (via function parameters), and it internally sets the type, so
  the resulting objects have a standard "schema". It's up to the programmer to actually call these functions to ensure
  the resulting objects are well-formed.
- Ballast: With Kotlin we have the benefit of type-safety, so we define discrete classes to be the different types of 
  "actions" or "inputs". If that class needs no other parameters other than its class type, it should be defined as an 
  `object`. Otherwise, it should be a `data class` where its parameters are all immutable `val`s. The type system of 
  Kotlin itself ensures that all Input objects are well-formed.

## Store --> State

A repository of data that is stored and updated by the library user. The library has its own rules and mechanisms for
ensuring any changes to that data are handled safely, to avoid race conditions or conflicting updates.

- Redux: Redux uses its own mechanism for managing and updating its Store. Store updates are handled safely by only 
  allowing Reducers to operate on a single field at a time without actually manipulating the store itself. Redux takes 
  the changes reqeusted by Reducers, and applies the changes itself.
- Ballast: Ballast uses a Kotlin `StateFlow` for holding onto the State, and uses atomic update functions for allowing
  the InputHandler to directly make changes to the State. Unlike Redux, Ballast allows one to update multiple fields 
  with a single update, and even allows one Input to update the State multiple times. Ballast adds other restrictions 
  around State updates to protect against potential race conditions. 

## Dispatcher --> InputHandler

The logic for how a single Action/Input produces multiple changes to the Store/State.

- Redux: The Redux Dispatcher is responsible for delegating each Action to all the Reducers that care about it. All 
  Reducers process each Action independently, so if you want to make multiple changes to the Store, you need multiple
  Reducers. One Action uses multiple Reducers which each make 1 change.
- Ballast: Ballast inverts the logic of Redux here. Rather than having multiple reducers for each Action, Ballast takes
  a more imperative approach, and allows multiple changes to take place as the result of one Input. Usually, the logic
  for making changes to the State are related to each other, and this allows that logic to be grouped together according
  to the Input, rather than according to the backing fields.

## Reducers --> InputHandlerScope

The features that encapsulate the limitations for how the programmer makes changes to the Store/State.

- Redux: A Reducer takes an Action and the previous value of a Store, and generates the updated value of a Store. In 
  Redux, there may be several Stores and several Reducers, each of which may operate on one or more different types of 
  actions. The term "reducer" is accurate in functional-programming terms, in that it is essentially the same operation
  as `List<T>.reduce { }`, but I personally find it quite nebulous and difficult to explain to someone who is not 
  already familiar with Redux or FP.
- Ballast: Ballast uses a slightly more flexible pattern for processing inputs, which is exposed through the 
  `InputHandlerScope`. More accurately, a single condition of the `when` statement within the `InputHandler` translates
  most closely with the concept of a Reducer, except that the condition only handles 1 input type and performs multiple
  updates, instead of considering multiple Inputs to make 1 update. Handling an Input with a single 
  `updateState { it.copy(...) }` statement mimics the behavior of a Redux Reducer, but Ballast allows the programmer to 
  follow that up with more State changes and other features, in a more imperative manner which is easier to understand 
  and requires less boilerplate for someone not familiar with the MVI pattern.

## Async Data Fetching

Most applications will require some kind of asynchronous fetching to take place. Redux places these concerns in a 
separate library, while Ballast is built on top of Kotlin coroutines and are a first-class citizen.

- Redux: Uses "thunk" middleware for async logic. A single "action" that will eventually change the state multiple times
  will need several "action creators" for each field it tries to update, and another to run the async code.
- Ballast: Ballast is built entirely upon Kotlin Coroutines, using concepts like `Channels` and `Flows` to take care of 
  all the hard parts of asynchronous programming. It allows a single Input to make multiple changes, by calling 
  `updateState` multiple times, even waiting for async coroutine code to finish within the single input handler. This 
  makes the business logic related to handling inputs with async code more clear, and also makes for a simpler library
  core which is built upon trusted features of Kotlin. Observing reactive data sources in Ballast looks more like a 
  standard async update from Redux, dispatching multiple Inputs over time, but simple use-cases are handled safely by 
  ensuring only 1 Input is executing at a time.
