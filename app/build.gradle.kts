import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

// 安全加载 local.properties，若文件不存在则返回空的 Properties 对象
val localProperties = Properties().apply {
    val localFile = rootProject.file("local.properties")
    if (localFile.exists()) {
        localFile.inputStream().use { load(it) }
    }
}

android {
    signingConfigs {
        // 本地开发配置（仅当 local.properties 中存在有效路径且文件存在时才创建）
        val localStoreFile = localProperties.getProperty("signing.storeFile")
        if (!localStoreFile.isNullOrBlank()) {
            val storeFileObj = file(localStoreFile)
            if (storeFileObj.exists()) {
                create("shiaho") {
                    storeFile = storeFileObj
                    storePassword = localProperties.getProperty("signing.storePassword") ?: ""
                    keyAlias = localProperties.getProperty("signing.keyAlias") ?: ""
                    keyPassword = localProperties.getProperty("signing.keyPassword") ?: ""
                }
            } else {
                println("⚠️ 警告：本地签名文件不存在，跳过 shiaho 配置：${storeFileObj.absolutePath}")
            }
        }
    
        // CI/CD 发布配置（强制要求 STORE_FILE_PATH 不为空）
        create("release") {
            val storeFilePath = (project.findProperty("STORE_FILE_PATH") as? String)
                ?.takeIf { it.isNotBlank() }
                ?: error("❌ STORE_FILE_PATH 未设置或为空，请在命令行通过 -PSTORE_FILE_PATH 指定有效路径")
    
            // 使用局部变量避免智能转换问题
            val f = file(storeFilePath)
            if (!f.exists()) {
                error("❌ 签名文件不存在：${f.absolutePath}")
            }
    
            storeFile = f
            storePassword = project.findProperty("STORE_PASSWORD") as? String ?: ""
            keyAlias = project.findProperty("KEY_ALIAS") as? String ?: ""
            keyPassword = project.findProperty("KEY_PASSWORD") as? String ?: ""
        }
    }
    
    namespace = "com.webtoapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.webtoapp"
        minSdk = 23
        targetSdk = 36
        versionCode = 32
        versionName = "1.9.5"

        vectorDrawables {
            useSupportLibrary = true
        }
        
        // NDK 配置
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
        
        // CMake 配置
        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17"
                arguments += "-DANDROID_STL=c++_shared"
            }
        }
    }
    
    // 外部 Native 构建配置
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release") 
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    // 禁用 ABI splits
    splits {
        abi {
            isEnable = false
        }
    }
    
    androidResources {
        // 允许打包以 "." 开头的 assets 文件（如 .pypackages）
        ignoreAssetsPattern = ""
    }
    
    bundle {
        language {
            enableSplit = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        // 确保 native 库不被压缩
        jniLibs {
            useLegacyPackaging = true
            // 排除 GeckoView 原生库
            excludes += "**/libxul.so"
            excludes += "**/libmozglue.so"
            excludes += "**/libgeckoffi.so"
            excludes += "**/libmozavutil.so"
            excludes += "**/libmozavcodec.so"
        }
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.documentfile:documentfile:1.0.1")
    
    // Material Design 3
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.activity:activity-compose:1.8.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("dev.chrisbanes.haze:haze:0.7.1")

    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.5")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("io.coil-kt:coil-video:2.5.0")
    implementation("io.coil-kt:coil-gif:2.5.0")

    // Gson for JSON parsing
    implementation("com.google.code.gson:gson:2.10.1")

    // OkHttp for networking
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // Koin for dependency injection
    implementation("io.insert-koin:koin-android:3.5.3")
    implementation("io.insert-koin:koin-androidx-compose:3.5.3")

    // WebKit for advanced WebView features
    implementation("androidx.webkit:webkit:1.9.0")

    // DataStore for preferences
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Security - EncryptedSharedPreferences for secure token storage
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // Apache Commons Compress for tar.gz/xz extraction (Linux environment)
    implementation("org.apache.commons:commons-compress:1.26.0")
    implementation("org.tukaani:xz:1.9")
    
    // APK 签名库（支持 v1/v2/v3 签名）
    implementation("com.android.tools.build:apksig:8.3.0")
    
    // GeckoView (Firefox 内核) — Java/Kotlin API 编译进 dex，原生 .so 排除（按需下载）
    implementation("org.mozilla.geckoview:geckoview-arm64-v8a:137.0.20250414091429")
    
    // ZXing 二维码生成和扫描
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    
    // Vico 图表库（高级数据看板）
    implementation("com.patrykandpatrick.vico:compose-m3:2.0.0-beta.3")
    
    // Google Play Billing (Pro/Ultra 订阅)
    implementation("com.android.billingclient:billing-ktx:7.0.0")
    
    // Google Sign-In (Credential Manager + Web OAuth fallback)
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation("androidx.browser:browser:1.8.0") // Chrome Custom Tab for OAuth fallback
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("com.google.truth:truth:1.1.5")
    testImplementation("org.robolectric:robolectric:4.12.2")
    testImplementation("androidx.test:core:1.5.0")
    
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

// ==================== PHP Binary Setup ====================
// Android 15+ enforces SELinux execute_no_trans denial for binaries in app data dir.
// Bundling PHP as a "native library" in jniLibs/ ensures it's extracted to nativeLibraryDir
// which has apk_data_file SELinux context (execute_no_trans allowed).
//
// Run: ./gradlew downloadPhpBinary
// This only needs to run once (the binary is gitignored).

tasks.register("downloadPhpBinary") {
    description = "Downloads pre-built PHP binary for Android arm64 and bundles it as native library"
    group = "setup"
    
    val phpVersion = "8.4"
    val jniLibsDir = file("src/main/jniLibs/arm64-v8a")
    val outputFile = File(jniLibsDir, "libphp.so")
    
    onlyIf { !outputFile.exists() }
    
    doLast {
        jniLibsDir.mkdirs()
        val url = "https://github.com/pmmp/PHP-Binaries/releases/download/pm5-php-${phpVersion}-latest/PHP-${phpVersion}-Android-arm64-PM5.tar.gz"
        val tempDir = File(project.layout.buildDirectory.asFile.get(), "tmp/php-download")
        tempDir.mkdirs()
        val tarFile = File(tempDir, "php.tar.gz")
        
        println("Downloading PHP $phpVersion for Android arm64...")
        
        exec { 
            commandLine("curl", "-L", "-f", "-o", tarFile.absolutePath, url) 
        }
        
        println("Extracting PHP binary...")
        
        exec { 
            commandLine("tar", "-xzf", tarFile.absolutePath, "-C", tempDir.absolutePath) 
        }
        
        // pmmp tar.gz structure: bin/php or just php
        val extracted = File(tempDir, "bin/php").takeIf { it.exists() }
            ?: tempDir.walkTopDown().firstOrNull { it.name == "php" && it.isFile }
            ?: throw GradleException("PHP binary not found in archive")
        
        extracted.copyTo(outputFile, overwrite = true)
        outputFile.setExecutable(true)
        tempDir.deleteRecursively()
        
        println("PHP binary installed: ${outputFile.relativeTo(rootDir)}")
    }
}
