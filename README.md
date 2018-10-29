# Using the Transform API for byte code manipulation

# Introduction

The [Transform API](http://tools.android.com/tech-docs/new-build-system/transform-api) is part of the Android build system since version 1.5 and allows you to hook into the build to modify code and resources.

Here I use it to emit logs for methods/functions and fields annotated with a custom annotation.

# Resources

- http://tools.android.com/tech-docs/new-build-system/transform-api
- https://vincent.bernat.ch/en/blog/2016-android-build-time-patch
- https://afterecho.uk/blog/create-a-standalone-gradle-plugin-for-android-part-4-the-transform-api.html
- http://www.javassist.org

# How it generally works

First you need to register your Transformation to have it executing during the build.
The only problem I encountered was the fact that the examples I was able to find are written in Groovy and use older versions of the Transform API.
It took me some time to realize that `project.android.registerTransform(..)` in Groovy needs to be ```(target.extensions.findByName("android") as BaseExtension).registerTransform(..)``` in Kotlin.

The transform needs to be registered during the apply phase and the transform is executed for every build variant. (But you can

The transformation is a subclass of `com.android.build.api.transform.Transform`. You need to implement a couple of functions to make the build know about your plugin. e.g. `getName`, `getScopes` and so on.

The heavy lifting happens in `transform`.

You get an instance of `TransformInvocation` which offers everything you need for your actual transform.

Beside a context (e.g. the variant, a logger etc.) you get the inputs to your transform and an output provider you need to use to know where to put your transformed files.

It's worth to note that you might not only get class files but for some scopes you will likely get JARs - if you want to modify them you have to take that into account.

If your transformation can handle incremental transforms you can query for the changed files.

The important thing is that even if you don't want to modify a file you need to copy it over - otherwise it won't make it into your final APK.

How exactly you process the individual files is completely up to you. If you want to modify bytecode there are plenty of libraries to make your life easier.

The most convenient library in my opinion is Javassist - it quite high level and can even process a subset of Java source. The only downside is you need all the dependencies of the classes you want to process on Javassists own classpath.

# Things to note about this implementation

As said above there is a custom annotation. It lives in it's own module and you need a dependency to that in order to use it.

Additionally there is a runtime which is called whenever an actual log happens. I could have used `android.util.Log` or anything else but a dedicated runtime gives more freedom for the implementation.
The runtime is added by the plugin so you don't have to add this dependency yourself.

The actual byte code manipulation is pretty straight forward thanks to the high level Javassist API.

While logging a method invocation is done by adding a `before` block to the method itself it's a bit trickier to do that for field access:
Since there is no code run when accessing a field I instrument every read and write of the annotated fields in the methods which actually perform that operation.

The transform only touches files from the `PROJECT` scope. Not from external dependencies or sub modules.

To make things easier for now the tranform is not incremental.