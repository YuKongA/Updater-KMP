# Build logic

This included build follows [Gradle's recommendations for structuring builds](https://docs.gradle.org/current/userguide/best_practices_structuring_builds.html#favor_composite_builds).
The main settings include it with `includeBuild("build-logic")`. It contains only project plugins, so it does not need early inclusion in `pluginManagement`.

| Subproject | Responsibility |
| --- | --- |
| `common` | Application/SDK/JVM constants and a lazy Git version provider |
| `kotlin-conventions` | KMP toolchain, shared library targets, serialization |
| `android-conventions` | Android library and application defaults |
| `compose-conventions` | Compose and Compose compiler plugins |
| `versioning` | Cacheable Kotlin/Xcode version generation |

Modules explicitly apply the conventions they need. Dependencies and plugin versions come from the shared `../gradle/libs.versions.toml` catalog. Repository declarations stay in each build's settings; the main build also permits Kotlin's temporary Node/Yarn distribution repositories. There is no cross-project configuration through `allprojects`, `subprojects`, or `afterEvaluate`.

The root project's `apply false` plugin declarations share external plugin classes across modules. Keep these declarations: loading Kotlin independently for different convention combinations breaks Kotlin/Native's shared build services.

`updater.multiplatform-library` supplies the existing desktop, iOS, macOS, JS, and Wasm targets. The data and domain modules add their CLI-only Windows/Linux targets, and platform launchers configure their own targets. Native binary options, linker flags, Android shrinking, and desktop packaging remain in their original modules.

The shared module declares the Koin BOM alongside its versionless Koin dependencies. Kotlin's NPM aggregation needs those constraints locally; relying on the data module's transitive BOM prevents JS/Wasm distribution tasks from resolving Koin Compose.

Web launchers explicitly declare `terser-webpack-plugin` as an NPM development dependency, with its version in the shared catalog. Their existing custom minification settings stay in `webpack.config.d/config.js`.

Parallel execution, configuration caching, and build caching are enabled for both builds. Main-build incremental compilation and Native optimizations stay in the root `gradle.properties`. Generated sources use a task output provider so Gradle infers compilation dependencies. Kotlin version sources and Xcode's `Generated.xcconfig` are both declared outputs, allowing missing files to be restored from the build cache. Git's commit count is obtained through `providers.exec`, so configuration-cache reuse checks version changes.

Application projects retain their physical `app/<name>` directories and use flat logical paths (`:android`, `:shared`, `:desktop`, `:js`, `:wasmJs`, `:macos`, `:cli`). This avoids creating an empty `:app` project. Use these paths in task commands and `projects.shared` in dependencies. CI, Xcode, and IDE run configurations use the same paths. The shared Compose resource package is explicitly pinned to `updater.app.shared.generated.resources` to preserve the existing resource API.

If an existing checkout reports changed Yarn lock files during Web packaging, refresh the generated local locks with `./gradlew kotlinUpgradeYarnLock kotlinWasmUpgradeYarnLock`. Keep lock mismatch checks enabled.

Run from the repository root:

```shell
# Build and validate the included build independently; also run version/cache regression tests.
./gradlew -p build-logic check --parallel --configuration-cache --configuration-cache-problems=fail

# Check the project structure and build several targets in parallel.
./gradlew projects
./gradlew :data:desktopTest :android:assembleDebug :desktop:desktopJar :js:compileKotlinJs :wasmJs:compileKotlinWasmJs --parallel --configuration-cache --configuration-cache-problems=fail

# Repeating a command should reuse the configuration cache and up-to-date task outputs.
./gradlew :data:generateVersionInfo --parallel --configuration-cache --configuration-cache-problems=fail
```

Apple packaging requires macOS/Xcode; Windows packaging requires Windows. The versioning tests check configuration-cache reuse, Git version changes, missing xcconfig restoration, and cache restoration in a different checkout.
