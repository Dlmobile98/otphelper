import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("com.android.application") version "8.4.1" apply false
  id("com.android.library") version "8.4.1" apply false
  id("org.jetbrains.kotlin.android") version "1.9.24" apply false
  id("com.google.dagger.hilt.android") version "2.50" apply false
  id("com.google.devtools.ksp") version "1.9.24-1.0.20" apply false
}

// https://chrisbanes.me/posts/composable-metrics/
// Usage:
// gradlew assembleRelease -PenableComposeCompilerReports=true
// metrics output: ./app/build/compose_metrics
// to force rerun tasks when data is stale:
// gradlew assembleRelease -PenableComposeCompilerReports=true --rerun-tasks
subprojects {
  tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
      if (project.findProperty("enableComposeCompilerReports") == "true") {
        freeCompilerArgs +=
            listOf(
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" +
                    project.buildDir.absolutePath +
                    "/compose_metrics")
        freeCompilerArgs +=
            listOf(
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" +
                    project.buildDir.absolutePath +
                    "/compose_metrics")
      }
    }
  }
}
