---
---

# {{ page.title }}

Ballast Test gives you a DSL you can include in any Kotlin testing framework to setup sequences of inputs and assert the
results of their processing. It is currently a work-in-progress and is likely to be changed significantly before 
Ballast's 1.0.0 release.

# Usage

After [including the dependency](#Installation) into your test sourceSet, you can run `viewModelTest()`, which gives you
a DSL for setting up specific scenarios and asserting what happened during the execution of those scenarios. 
`viewModelTest()` is a suspending function, so it will need to be run within `runBlocking` in your tests.

You do not need to provide a ViewModel implementation for these tests. A feature of Ballast is that the chosen ViewModel
base class is just a wrapper around the actual processor, and the test framework defines its own ViewModel class to run
the scenarios in. Instead, you just need to provide the other components you would normally pass to your ViewModel 
configuration, and then proceed setting your testing suite.

`viewModelTest()` defines an entire test suite for a single Ballast ViewModel, which contains many scenarios with 
`scenario("human-readbale scenario description")`. Most properties can be configured within the `viewModelTest { }` 
block which will get applied to all scenarios, but each `scenario { }` can set their own values, which will override 
those set for the suite.

In each `scenario { }` block, `running { }` is the scenario script that will be run. Inputs are sent for processing 
using the unary `+` operator, which will either send the Input and wait for it to be completed, or unary `-` which will 
send the Input and immediately continue the script without waiting for it to complete. You'd typically want to use `+`
unless you are explicitly wanting to test the cancellation behavior or something else that relies upon multiple Inputs
being sent before the first has finished processing.

`resultsIn { }` will be called after the scenario has run to completion (or timed out), and will give a `TestResults` 
which contains all the values and their statues that were seen during the test scenario. You can use your favorite 
assertion library to make any assertions on any results within that object.

```kotlin
@Test
fun testExampleViewModel() = runBlocking<Unit> {
    viewModelTest(
        inputHandler = ExampleInputHandler(),
        eventHandler = ExampleEventHandler(),
        filter = null,
    ) {
        defaultInitialState { State() }
        
        scenario("update string value only") {
            running {
                +Inputs.UpdateStringValue("one")
            }
            resultsIn {
                assertEquals("one", latestState.stringValue)
                assertEquals(0, latestState.intValue)
            }
        }

        scenario("increment int value only") {
            running {
                +Inputs.Increment
                +Inputs.Increment
            }
            resultsIn {
                assertEquals(2, latestState.intValue)
            }
        }
    }
}
```

# Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM or Android projects
dependencies {
    testImplementation("io.github.copper-leaf:ballast-test:{{site.version}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonTest by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-test:{{site.version}}")
            }
        }
    }
}
```
