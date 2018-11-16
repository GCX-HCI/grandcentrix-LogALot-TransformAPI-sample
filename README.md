# Using the Transform API for byte code manipulation

# Introduction

The [Transform API](http://tools.android.com/tech-docs/new-build-system/transform-api) is part of the Android build system since version 1.5 and allows you to hook into the build to modify code and Java resources.

Here I use it to emit logs for methods/functions and access to fields annotated with a custom annotation.

# Resources

- http://tools.android.com/tech-docs/new-build-system/transform-api
- http://www.javassist.org

# How the Transform API generally works

First you need to register your transformation to have it executed during the build.

The only problem I encountered was the fact that the examples I was able to find are written in Groovy and use older versions of the Transform API.
It took me some time to realize that `project.android.registerTransform(..)` in Groovy needs to be `(project.extensions.findByName("android") as BaseExtension).registerTransform(..)` in Kotlin.

The transform needs to be registered during the apply phase and the transform is executed for every build variant. (But you can access the currently building variant during transformation.)

The transformation is a subclass of `com.android.build.api.transform.Transform`. You need to implement a couple of functions to make the build know about your transformation. e.g. `getName`, `getScopes` and so on.

The heavy lifting happens in `transform`.

You get an instance of `TransformInvocation` which offers everything you need for your actual transform.

Besides a context (the variant, a LoggingManager etc.) you get the inputs to your transform and an output provider you need to use to know where to put your transformed files.

It's worth to note that you might not only get directories containing class files but for some scopes you will likely get JARs - if you want to modify them you have to take that into account.

If your transformation can handle incremental transforms you can query for the changed files that need to be re-processed.

The important thing is that even if you don't want to modify a file in your requested scopes you need to copy it over - otherwise it won't make it into your final APK.

How exactly you process the individual files is completely up to you. If you want to modify bytecode there are plenty of libraries to make your life easier.
(e.g. ASM, BCEL, ByteBuddy are popular)

But the most convenient library in my opinion is Javassist - it is quite high level and can even process a subset of Java source. The only downside is you need all the dependencies of the classes you want to process on Javassist's own classpath.

# Things to note about this implementation

First you can configure for which variants you want the transform to get applied. Because this check can only be done in the transform itself we need to copy the requested files even if we don't modify them.

As said above there is a custom annotation. It lives in it's own module and you need a dependency to that in order to use it.

Additionally there is a runtime which is called whenever an actual log happens. I could have used `android.util.Log` or anything else directly but a dedicated runtime gives more freedom for the implementation.
The runtime needs to be added as a dependency for at least every variant you enable the transformation for.

The actual byte code manipulation is pretty straight forward thanks to the high level Javassist API.

While logging a method invocation is done by just adding a `before` block to the method itself it's a bit trickier to do that for field access:
Since there is no code run when accessing a field I instrument every read and write of the annotated fields in the methods which actually perform that operation.

The transform only touches files from the `PROJECT` scope. Not from external dependencies or sub modules. That's not a limitation of the Transform API but a design decision.

Since Javassist needs all the referenced classes of a class you want to modify on it's own classpath we need the scopes EXTERNAL_LIBRARIES and SUB_PROJECTS.
We could have added them to the requested scopes but in that case we need to copy them in order to get them into the final APK. That would certainly work.

However I do it differently: I request the EXTERNAL_LIBRARIES and SUB_PROJECTS scopes as referenced scopes.
This way we get them into the transform as referenced input but don't have to copy them.

To make things easier for now the transform is not incremental.
Enabling incremental transformation would be a bit tricky for this transform since in that case you could add the annotation to a public field and that means we have to process also the untouched classes since they could contain a field access to such a field.

One important hint to safe you a lot of trouble: For some reasons Javassist often sees old versions of the runtime which is a problem if you add methods or change method signatures!
The solution is: Kill the Gradle daemon (gradlew --stop) after changing the runtime. (Or don't built via IDE and always use --no-daemon command line switch).

There are tests for the plugin and the transform itself. Since we are using buildSrc the tests consequently also live in buildSrc. Downside of this is that importing the project into the IDE will fail if there are failing tests.

The tests are low level unit tests using MockK and Kluent but don't use TestKit. They are really unit tests not functional tests.

Most challenging part in testing this is to test the actual byte code modifications.

# License

```
Copyright 2018 grandcentrix GmbH

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
