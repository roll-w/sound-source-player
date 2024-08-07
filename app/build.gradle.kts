/*
 * Copyright (C) 2024 RollW
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.android.build.api.dsl.ApplicationProductFlavor
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.util.capitalizeDecapitalize.capitalizeAsciiOnly
import java.util.*

plugins {
    id("com.android.application")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.kapt")
}

val dimensionAbi = "abi"

android {
    namespace = "tech.rollw.player"
    compileSdk = 34

    defaultConfig {
        applicationId = "tech.rollw.player"
        minSdk = 24
        targetSdk = 34
        versionCode = 23
        versionName = "0.1.5-beta06"

        val filesAuthorityValue = "$applicationId.FileProvider"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        externalNativeBuild {
            cmake {
                cppFlags += ""
                arguments += "-DANDROID_STL=c++_shared"
            }
        }
        manifestPlaceholders += mapOf("filesAuthority" to filesAuthorityValue)

        buildConfigField(
            "String",
            "FILES_AUTHORITY",
            "\"${filesAuthorityValue}\""
        )
        buildConfigField("String", "VERSION_NAME", "\"$versionName\"")
        buildConfigField("int", "VERSION_CODE", "$versionCode")
        buildConfigField("String", "APPLICATION_ID", "\"$applicationId\"")

        vectorDrawables {
            useSupportLibrary = true
        }

        signingConfigs {
            val storeFile = project.rootProject.file("Keystore/key.jks")
            val properties = Properties()
            properties.load(project.rootProject.file("local.properties").inputStream())

            val storePassword = properties.getProperty("KEY_STORE_PASSWORD")
            val keyAlias = properties.getProperty("KEY_ALIAS")
            val keyPassword = properties.getProperty("KEY_PASSWORD")

            create("default") {
                this.storeFile = storeFile
                this.storePassword = storePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
                this.enableV2Signing = true
                this.enableV3Signing = true
                this.enableV4Signing = true
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isDebuggable = false
            signingConfig = signingConfigs["default"]
        }
        debug {
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
            isDebuggable = true
        }
    }
    flavorDimensions += dimensionAbi
    productFlavors {
        fun createByAbi(
            name: String,
            abis: List<String>
        ): ApplicationProductFlavor {
            return this.create(name) {
                buildConfigField("String", "ABI", "\"$name\"")
                dimension = dimensionAbi
                ndk {
                    abiFilters += abis
                }
                externalNativeBuild {
                    cmake {
                        abiFilters += abis
                    }
                }
            }
        }
        createByAbi("arm64-v8a", listOf("arm64-v8a"))
        createByAbi("armeabi-v7a", listOf("armeabi-v7a"))

        createByAbi("x86", listOf("x86"))
        createByAbi("x86_64", listOf("x86_64"))
        createByAbi(
            "universal", listOf(
                "arm64-v8a", "armeabi-v7a",
                "x86", "x86_64"
            )
        )
    }
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
            freeCompilerArgs.addAll(
                listOf(
                    "-opt-in=androidx.compose.foundation.layout.ExperimentalLayoutApi",
                    "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
                    "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi",
                    "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
                    "-opt-in=androidx.compose.animation.ExperimentalAnimationApi",
                )
            )
        }
    }
    buildFeatures {
        buildConfig = true
        compose = true
        prefab = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(":support"))
    implementation(project(":ui"))
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(libs.bundles.androidx.appcompat)
    implementation(libs.bundles.androidx.core)

    implementation(libs.material)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.palette.ktx)

    implementation(libs.bundles.androidx.compose)
    implementation(libs.bundles.accompanist)
    implementation(libs.bundles.androidx.work)
    implementation(libs.bundles.androidx.lifecycle)
    implementation(libs.bundles.androidx.media3)
    implementation(libs.bundles.androidx.navigation)
    implementation(libs.bundles.androidx.room)

    ksp(libs.androidx.room.compiler)

    implementation(libs.oboe)

    implementation(libs.fiesta.annotations)
    compileOnly(libs.fiesta.checker)

    implementation(libs.coil.compose)

    debugImplementation("com.guolindev.glance:glance:1.1.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.bundles.androidx.test)

    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}