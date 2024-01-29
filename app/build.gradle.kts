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

plugins {
    id("com.android.application")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "tech.rollw.player"
    compileSdk = 34

    defaultConfig {
        applicationId = "tech.rollw.player"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.2.R"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        externalNativeBuild {
            cmake {
                cppFlags += ""
            }
        }
        val filesAuthorityValue = "$applicationId.FileProvider"
        manifestPlaceholders["filesAuthority"] = filesAuthorityValue
        buildConfigField(
            "String",
            "FILES_AUTHORITY",
            "\"${filesAuthorityValue}\""
        )

        buildConfigField("String", "VERSION_NAME", "\"$versionName\"")
        buildConfigField("int", "VERSION_CODE", "$versionCode")
        buildConfigField("String", "APPLICATION_ID", "\"$applicationId\"")

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
        dataBinding = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.browser:browser:1.7.0")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    implementation("androidx.transition:transition-ktx:1.4.1")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation(project(":support"))

    val lifeCycleVersion = "2.7.0"
    val navigationVersion = "2.7.6"
    val roomVersion = "2.6.1"
    val workVersion = "2.9.0"
    val media2Version = "1.3.0"

    implementation("androidx.palette:palette-ktx:1.0.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifeCycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-core-ktx:$lifeCycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifeCycleVersion")
    implementation("androidx.lifecycle:lifecycle-service:$lifeCycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifeCycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:$lifeCycleVersion")


    implementation("androidx.media:media:1.7.0")
    implementation("androidx.media2:media2-session:$media2Version")


    implementation("androidx.navigation:navigation-ui-ktx:$navigationVersion")
    implementation("androidx.navigation:navigation-fragment-ktx:$navigationVersion")

    implementation("androidx.room:room-runtime:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-guava:$roomVersion")

    implementation("androidx.work:work-runtime:$workVersion")
    implementation("androidx.work:work-runtime-ktx:$workVersion")


    implementation("net.jthink:jaudiotagger:3.0.1")
    implementation("com.google.oboe:oboe:1.8.0")

    implementation("space.lingu.fiesta:fiesta-annotations:0.2.0")
    compileOnly("space.lingu.fiesta:fiesta-checker:0.2.0")

    implementation("com.github.bumptech.glide:glide:4.14.2")
    ksp("com.github.bumptech.glide:compiler:4.14.2")

    debugImplementation("com.guolindev.glance:glance:1.1.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}