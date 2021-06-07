<!---
# This file is part of the pl.wrzasq.commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

# Native runtime

Even though **JVM** is supported by **AWS Lambda**, we have small wrapper that takes care of runtime integration to
provide own custom runtime that will work in `provided.al2` environment. One reason for it is that we want to use native
images for running JVM lambdas (for performance reasons) and second is that built-in runtimes have very limited default
serialization capabilities.

In order to run a Lambda handler we need to create a runtime wrapper that takes care about all the API communication by
specifying `ObjectMapper` instance responsible for serialization. Then you can execute your "real" Lambda handler in
that wrapper - currently only stream handlers are supported. Signature of your handler needs to be (can be also a method
reference as long as the contract is the same):

```kotlin
fun handleRequest(inputStream: InputStream, outputStream: OutputStream, context: Context)
```

Example on how one to execute the runner (you can investigate signature of `NativeLambdaApi` class, but most of the
arguments are there for testing purposes):

```kotlin
import pl.wrzasq.commons.aws.runtine.NativeLambdaApi

fun main() {
    val handler = YourLogic()
    val objectMapper = ObjectMapper()
    val api = NativeLambdaApi(objectMapper)
    api.run(handler::handleRequest)
}
```
