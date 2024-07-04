import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.internal.api.ApkVariantOutputImpl

plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("com.google.devtools.ksp")
  id("com.google.dagger.hilt.android")
  id("com.google.protobuf") version "0.9.4"
}

android {
  namespace = "io.github.jd1378.otphelper"
  compileSdk = 34

  defaultConfig {
    applicationId = "io.github.jd1378.otphelper"
    minSdk = 24
    targetSdk = 34
    versionCode = 29
    versionName = "1.15.3"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables { useSupportLibrary = true }
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
  kotlinOptions { jvmTarget = "1.8" }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }
  packagingOptions { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
  androidResources { generateLocaleConfig = true }

  applicationVariants.all(ApplicationVariantAction())
}

val protobufVersion = "3.25.3"

dependencies {
  implementation(platform("androidx.compose:compose-bom:2024.06.00"))
  androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))

  implementation("androidx.core:core-ktx:1.13.1")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
  implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.3")
  implementation("androidx.activity:activity-compose:1.9.0")
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-tooling-preview")
  implementation("androidx.compose.material3:material3")
  implementation("androidx.datastore:datastore-preferences:1.1.1")
  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.2.1")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
  androidTestImplementation("androidx.compose.ui:ui-test-junit4")
  debugImplementation("androidx.compose.ui:ui-tooling")
  debugImplementation("androidx.compose.ui:ui-test-manifest")
  // debugImplementation because LeakCanary should only run in debug builds.
  debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")
  // navigation
  implementation("androidx.navigation:navigation-compose:2.7.7")
  // hilt
  implementation("com.google.dagger:hilt-android:2.50")
  ksp("com.google.dagger:hilt-compiler:2.50")
  // hilt for navigation compose
  implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
  // app compat (for locales)
  val appcompatVersion = "1.7.0"
  implementation("androidx.appcompat:appcompat:$appcompatVersion")
  implementation("androidx.appcompat:appcompat-resources:$appcompatVersion")
  // room db
  val roomVersion = "2.6.1"
  implementation("androidx.room:room-runtime:$roomVersion")
  annotationProcessor("androidx.room:room-compiler:$roomVersion")
  ksp("androidx.room:room-compiler:$roomVersion")
  implementation("androidx.room:room-ktx:$roomVersion")
  implementation("androidx.room:room-paging:$roomVersion")
  val pagingVersion = "3.3.0"
  implementation("androidx.paging:paging-runtime-ktx:$pagingVersion")
  implementation("androidx.paging:paging-compose:$pagingVersion")

  // datastore

  implementation("androidx.datastore:datastore:1.1.1")
  implementation("com.google.protobuf:protobuf-javalite:$protobufVersion")
  implementation("com.google.protobuf:protobuf-kotlin-lite:$protobufVersion")
}

hilt { enableAggregatingTask = true }

ksp { arg("room.schemaLocation", "$projectDir/schemas") }

protobuf {
  protoc { artifact = "com.google.protobuf:protoc:$protobufVersion" }

  // Generates the java Protobuf-lite code for the Protobufs in this project. See
  // https://github.com/google/protobuf-gradle-plugin#customizing-protobuf-compilation
  // for more information.
  generateProtoTasks {
    all().forEach { task ->
      task.builtins {
        // Configures the task output type
        // Lite has smaller code size and is recommended for Android
        create("java") { option("lite") }
        create("kotlin") { option("lite") }
      }
    }
  }
}

class ApplicationVariantAction : Action<ApplicationVariant> {
  override fun execute(variant: ApplicationVariant) {
    variant.outputs.all(VariantOutputAction(variant))
  }

  class VariantOutputAction(private val variant: ApplicationVariant) : Action<BaseVariantOutput> {
    override fun execute(output: BaseVariantOutput) {
      if (output is ApkVariantOutputImpl) {
        val abi =
            output.getFilter(com.android.build.api.variant.FilterConfiguration.FilterType.ABI.name)
        val abiVersionCode =
            when (abi) {
              "armeabi-v7a" -> 1
              "arm64-v8a" -> 2
              "x86" -> 3
              "x86_64" -> 4
              else -> 0
            }
        val versionCode = variant.versionCode * 1000 + abiVersionCode
        output.versionCodeOverride = versionCode

        val flavor = variant.flavorName
        val builtType = variant.buildType.name
        val versionName = variant.versionName
        val architecture = abi ?: "-universal"

        output.outputFileName =
            "otp-helper-${flavor}-${builtType}-${versionName}-${architecture}-${versionCode}.apk"
      }
    }
  }
}
