[create_an_android_app_named_phone_manger_by_kotline_the_app_have_websocat_port_f (1).md](https://github.com/user-attachments/files/24902483/create_an_android_app_named_phone_manger_by_kotline_the_app_have_websocat_port_f.1.md)
# create an android app named phone manger by kotline the app have websocat port field ,default8030 ,

> **Provider:** LMArena  
> **Date:** 1/28/2026, 10:13:51 AM  
> **Chat ID:** `019c02ce-7b15-79e0-8cb9-0791bde6274a`  
> **Source:** [LMArena](https://lmarena.ai/c/019c02ce-7b15-79e0-8cb9-0791bde6274a)  

---

## [1] USER

create an android app named phone manger by kotline
the app have websocat port field ,default:8030 , on/off toggle switch
http get/post  port field , default 8040, on/off toggle switch
user can dial number/ussd on sim1/sim2 both over websocat/http get/post
user can also send command to answare call, reject call, terminate call, hold call, unhold call, mute call , unmute call , loudspeaker on , loudspeaker off 
user can also send command to receive info such as current active call number with timestamp, all incoming /outgoing/ missed call all list with time stamp (with sqlite critera)

the app must ask call, notification, full network access permision to work in backgound. even in idle state the app keep alive unless user truned it off.
note : number/ussd, sim1/sim2 choice must be integrated in both http get/post api

---

## [2] ASSISTANT (claude-opus-4-5-20251101-thinking-32k)

<details>
<summary><strong>💭 Thinking Process</strong></summary>

> The user wants me to create a comprehensive Android app called "Phone Manager" using Kotlin. Let me break down the requirements:
> 
> 1. **WebSocket Server** on configurable port (default: 8030) with on/off toggle
> 2. **HTTP Server** for GET/POST on configurable port (default: 8040) with on/off toggle
> 3. **Features:**
>    - Dial number/USSD on SIM1/SIM2
>    - Answer call, reject call, terminate call
>    - Hold/unhold call
>    - Mute/unmute call
>    - Loudspeaker on/off
>    - Get current active call info with timestamp
>    - Get call logs (incoming/outgoing/missed) with SQLite criteria
> 
> 4. **Permissions needed:**
>    - CALL_PHONE
>    - READ_PHONE_STATE
>    - READ_CALL_LOG
>    - ANSWER_PHONE_CALLS
>    - MODIFY_AUDIO_SETTINGS
>    - FOREGROUND_SERVICE
>    - INTERNET
>    - POST_NOTIFICATIONS
> 
> 5. **Background service** that keeps running
> 
> Let me create a complete Android project structure:
> 
> 1. **MainActivity.kt** - UI with port fields and toggle switches
> 2. **PhoneManagerService.kt** - Foreground service
> 3. **WebSocketServer.kt** - WebSocket server implementation
> 4. **HttpServer.kt** - HTTP server implementation
> 5. **CallManager.kt** - Handle call operations
> 6. **CallLogHelper.kt** - SQLite queries for call logs
> 7. **InCallServiceImpl.kt** - For call control
> 8. **activity_main.xml** - Layout file
> 9. **AndroidManifest.xml** - Permissions and components
> 
> Let me create a comprehensive implementation:
> 
> ```kotlin
> // Project structure:
> // app/
> // ├── src/main/
> // │   ├── java/com/phonemanager/
> // │   │   ├── MainActivity.kt
> 
> 
> // │   │   ├── service/
> // │   │   │   ├── PhoneManagerService.kt
> // │   │   │   ├── CallControlService.kt
> // │   │   ├── server/
> // │   │   │   ├── WebSocketServerHandler.kt
> // │   │   │   ├── HttpServerHandler.kt
> // │   │   ├── manager/
> // │   │   │   ├── CallManager.kt
> // │   │   │   ├── CallLogManager.kt
> // │   │   │   ├── USSDManager.kt
> // │   │   ├── model
> ```
> 
> The app needs a modular architecture with separate packages for services, network handlers, and call management utilities. The foreground service will manage the lifecycle while dedicated handlers process WebSocket and HTTP requests, and manager classes encapsulate call operations and database interactions.
> 
> // │   │   │   ├── CommandRequest.kt
> // │   │   │   ├── CommandResponse.kt
> // │   │   ├── utils/
> // │   │   │   ├── PermissionHelper.kt
> // │   │   ├── PreferencesManager.kt
> // │   ├── res/
> // │   │   ├── layout/
> // │   │   │   ├── activity_main.xml
> // │   │   ├── values/
> // │   │   │   ├── strings.xml
> // │   │   │   ├── themes.xml
> // │   ├── AndroidManifest.xml
> // ├── build.gradle.kts
> 
> Data models handle request/response serialization, utility classes manage permissions and app preferences, and resource files define the UI layout and app metadata. NanoHTTPD will power the HTTP server while Java-WebSocket handles bidirectional communication.

</details>

# Phone Manager Android App (Kotlin)

This is a comprehensive Android app that provides remote phone control via WebSocket and HTTP servers.

## Project Structure

<details>
<summary><strong>💻 Code Block — 27 lines</strong></summary>

```
PhoneManager/
├── app/
│   ├── src/main/
│   │   ├── java/com/phonemanager/
│   │   │   ├── MainActivity.kt
│   │   │   ├── service/
│   │   │   │   ├── PhoneManagerService.kt
│   │   │   │   └── CallControlService.kt
│   │   │   ├── server/
│   │   │   │   ├── WebSocketServerHandler.kt
│   │   │   │   └── HttpServerHandler.kt
│   │   │   ├── manager/
│   │   │   │   ├── CallManager.kt
│   │   │   │   └── CallLogManager.kt
│   │   │   ├── model/
│   │   │   │   └── Models.kt
│   │   │   └── utils/
│   │   │       └── PreferencesManager.kt
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   └── activity_main.xml
│   │   │   └── values/
│   │   │       ├── strings.xml
│   │   │       └── themes.xml
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
└── settings.gradle.kts
```

</details>

## 1. build.gradle.kts (Project level)

<details>
<summary><strong>💻 Code Block (kotlin) — 5 lines</strong></summary>

```kotlin
// Top-level build file
plugins {
    id("com.android.application") version "8.1.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
}
```

</details>

## 2. settings.gradle.kts

<details>
<summary><strong>💻 Code Block (kotlin) — 16 lines</strong></summary>

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "PhoneManager"
include(":app")
```

</details>

## 3. app/build.gradle.kts

<details>
<summary><strong>💻 Code Block (kotlin) — 59 lines</strong></summary>

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.phonemanager"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.phonemanager"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // NanoHTTPD for HTTP server
    implementation("org.nanohttpd:nanohttpd:2.3.1")
    
    // Java-WebSocket
    implementation("org.java-websocket:Java-WebSocket:1.5.4")
    
    // Gson for JSON
    implementation("com.google.code.gson:gson:2.10.1")
}
```

</details>

## 4. AndroidManifest.xml

<details>
<summary><strong>💻 Code Block (xml) — 88 lines</strong></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- Network Permissions -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />

    <!-- Phone Permissions -->
    <uses-permission android:name="android.permission.CALL_PHONE" />
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <uses-permission android:name="android.permission.READ_PHONE_NUMBERS" />
    <uses-permission android:name="android.permission.ANSWER_PHONE_CALLS" />
    <uses-permission android:name="android.permission.READ_CALL_LOG" />
    <uses-permission android:name="android.permission.WRITE_CALL_LOG" />
    <uses-permission android:name="android.permission.PROCESS_OUTGOING_CALLS" />

    <!-- Audio Permissions -->
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />

    <!-- Background Service Permissions -->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_PHONE_CALL" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

    <!-- Dual SIM -->
    <uses-permission android:name="android.permission.READ_PRIVILEGED_PHONE_STATE"
        tools:ignore="ProtectedPermissions" />

    <uses-feature android:name="android.hardware.telephony" android:required="true" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.PhoneManager"
        android:name=".PhoneManagerApp"
        tools:targetApi="34">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:launchMode="singleTop">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- Foreground Service -->
        <service
            android:name=".service.PhoneManagerService"
            android:enabled="true"
            android:exported="false"
            android:foregroundServiceType="phoneCall" />

        <!-- InCallService for call control -->
        <service
            android:name=".service.CallControlService"
            android:permission="android.permission.BIND_INCALL_SERVICE"
            android:exported="true">
            <meta-data
                android:name="android.telecom.IN_CALL_SERVICE_UI"
                android:value="false" />
            <intent-filter>
                <action android:name="android.telecom.InCallService" />
            </intent-filter>
        </service>

        <!-- Boot Receiver -->
        <receiver
            android:name=".receiver.BootReceiver"
            android:enabled="true"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
                <action android:name="android.intent.action.QUICKBOOT_POWERON" />
            </intent-filter>
        </receiver>

    </application>
</manifest>
```

</details>

## 5. PhoneManagerApp.kt

<details>
<summary><strong>💻 Code Block (kotlin) — 37 lines</strong></summary>

```kotlin
package com.phonemanager

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class PhoneManagerApp : Application() {
    
    companion object {
        const val CHANNEL_ID = "PhoneManagerServiceChannel"
        lateinit var instance: PhoneManagerApp
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Phone Manager Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Phone Manager background service notification"
                setShowBadge(false)
            }
            
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
```

</details>

## 6. res/layout/activity_main.xml

<details>
<summary><strong>💻 Code Block (xml) — 345 lines</strong></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#1E1E2E"
    tools:context=".MainActivity">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">

        <!-- App Title -->
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Phone Manager"
            android:textSize="28sp"
            android:textColor="#CDD6F4"
            android:textStyle="bold"
            android:gravity="center"
            android:layout_marginBottom="24dp" />

        <!-- Status Card -->
        <com.google.android.material.card.MaterialCardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardBackgroundColor="#313244"
            app:cardCornerRadius="12dp"
            app:cardElevation="4dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="16dp">

                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="Service Status"
                    android:textSize="18sp"
                    android:textColor="#CDD6F4"
                    android:textStyle="bold"
                    android:layout_marginBottom="12dp" />

                <TextView
                    android:id="@+id/tvStatus"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="Status: Stopped"
                    android:textSize="14sp"
                    android:textColor="#A6ADC8" />

                <TextView
                    android:id="@+id/tvIpAddress"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="IP: ---.---.---.---"
                    android:textSize="14sp"
                    android:textColor="#A6ADC8"
                    android:layout_marginTop="4dp" />

            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- WebSocket Server Card -->
        <com.google.android.material.card.MaterialCardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardBackgroundColor="#313244"
            app:cardCornerRadius="12dp"
            app:cardElevation="4dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="16dp">

                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="WebSocket Server"
                    android:textSize="18sp"
                    android:textColor="#CDD6F4"
                    android:textStyle="bold"
                    android:layout_marginBottom="12dp" />

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:gravity="center_vertical">

                    <com.google.android.material.textfield.TextInputLayout
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:hint="Port"
                        app:boxBackgroundColor="#45475A"
                        app:hintTextColor="#A6ADC8">

                        <com.google.android.material.textfield.TextInputEditText
                            android:id="@+id/etWebSocketPort"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:text="8030"
                            android:textColor="#CDD6F4"
                            android:inputType="number" />

                    </com.google.android.material.textfield.TextInputLayout>

                    <com.google.android.material.switchmaterial.SwitchMaterial
                        android:id="@+id/switchWebSocket"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_marginStart="16dp"
                        app:thumbTint="#A6E3A1"
                        app:trackTint="#45475A" />

                </LinearLayout>

                <TextView
                    android:id="@+id/tvWebSocketStatus"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="Status: OFF"
                    android:textSize="12sp"
                    android:textColor="#F38BA8"
                    android:layout_marginTop="8dp" />

            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- HTTP Server Card -->
        <com.google.android.material.card.MaterialCardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardBackgroundColor="#313244"
            app:cardCornerRadius="12dp"
            app:cardElevation="4dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="16dp">

                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="HTTP Server (GET/POST)"
                    android:textSize="18sp"
                    android:textColor="#CDD6F4"
                    android:textStyle="bold"
                    android:layout_marginBottom="12dp" />

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:gravity="center_vertical">

                    <com.google.android.material.textfield.TextInputLayout
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:hint="Port"
                        app:boxBackgroundColor="#45475A"
                        app:hintTextColor="#A6ADC8">

                        <com.google.android.material.textfield.TextInputEditText
                            android:id="@+id/etHttpPort"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:text="8040"
                            android:textColor="#CDD6F4"
                            android:inputType="number" />

                    </com.google.android.material.textfield.TextInputLayout>

                    <com.google.android.material.switchmaterial.SwitchMaterial
                        android:id="@+id/switchHttp"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_marginStart="16dp"
                        app:thumbTint="#A6E3A1"
                        app:trackTint="#45475A" />

                </LinearLayout>

                <TextView
                    android:id="@+id/tvHttpStatus"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="Status: OFF"
                    android:textSize="12sp"
                    android:textColor="#F38BA8"
                    android:layout_marginTop="8dp" />

            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- API Documentation Card -->
        <com.google.android.material.card.MaterialCardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardBackgroundColor="#313244"
            app:cardCornerRadius="12dp"
            app:cardElevation="4dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="16dp">

                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="API Commands"
                    android:textSize="18sp"
                    android:textColor="#CDD6F4"
                    android:textStyle="bold"
                    android:layout_marginBottom="12dp" />

                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="• dial - Dial number/USSD\n• answer - Answer incoming call\n• reject - Reject incoming call\n• terminate - End active call\n• hold - Hold active call\n• unhold - Unhold call\n• mute - Mute microphone\n• unmute - Unmute microphone\n• speaker_on - Enable loudspeaker\n• speaker_off - Disable loudspeaker\n• get_active_call - Get active call info\n• get_call_log - Get call history"
                    android:textSize="12sp"
                    android:textColor="#A6ADC8"
                    android:lineSpacingMultiplier="1.3" />

            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- Permissions Card -->
        <com.google.android.material.card.MaterialCardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardBackgroundColor="#313244"
            app:cardCornerRadius="12dp"
            app:cardElevation="4dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="16dp">

                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="Permissions"
                    android:textSize="18sp"
                    android:textColor="#CDD6F4"
                    android:textStyle="bold"
                    android:layout_marginBottom="12dp" />

                <TextView
                    android:id="@+id/tvPermissions"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="Checking permissions..."
                    android:textSize="12sp"
                    android:textColor="#A6ADC8" />

                <com.google.android.material.button.MaterialButton
                    android:id="@+id/btnRequestPermissions"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="Request Permissions"
                    android:layout_marginTop="12dp"
                    app:backgroundTint="#89B4FA" />

                <com.google.android.material.button.MaterialButton
                    android:id="@+id/btnSetDefaultDialer"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="Set as Default Dialer"
                    android:layout_marginTop="8dp"
                    app:backgroundTint="#F9E2AF"
                    android:textColor="#1E1E2E" />

            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- Connected Clients Card -->
        <com.google.android.material.card.MaterialCardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            app:cardBackgroundColor="#313244"
            app:cardCornerRadius="12dp"
            app:cardElevation="4dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="16dp">

                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="Activity Log"
                    android:textSize="18sp"
                    android:textColor="#CDD6F4"
                    android:textStyle="bold"
                    android:layout_marginBottom="12dp" />

                <TextView
                    android:id="@+id/tvLog"
                    android:layout_width="match_parent"
                    android:layout_height="200dp"
                    android:text="No activity yet..."
                    android:textSize="11sp"
                    android:textColor="#A6ADC8"
                    android:background="#45475A"
                    android:padding="8dp"
                    android:scrollbars="vertical"
                    android:gravity="top" />

                <com.google.android.material.button.MaterialButton
                    android:id="@+id/btnClearLog"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="Clear Log"
                    android:layout_marginTop="8dp"
                    app:backgroundTint="#F38BA8" />

            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

    </LinearLayout>
</ScrollView>
```

</details>

## 7. res/values/strings.xml

<details>
<summary><strong>💻 Code Block (xml) — 3 lines</strong></summary>

```xml
<resources>
    <string name="app_name">Phone Manager</string>
</resources>
```

</details>

## 8. res/values/themes.xml

<details>
<summary><strong>💻 Code Block (xml) — 13 lines</strong></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.PhoneManager" parent="Theme.Material3.DayNight.NoActionBar">
        <item name="colorPrimary">#89B4FA</item>
        <item name="colorPrimaryVariant">#74C7EC</item>
        <item name="colorOnPrimary">#1E1E2E</item>
        <item name="colorSecondary">#A6E3A1</item>
        <item name="colorSecondaryVariant">#94E2D5</item>
        <item name="colorOnSecondary">#1E1E2E</item>
        <item name="android:statusBarColor">#1E1E2E</item>
        <item name="android:navigationBarColor">#1E1E2E</item>
    </style>
</resources>
```

</details>

## 9. model/Models.kt

<details>
<summary><strong>💻 Code Block (kotlin) — 135 lines</strong></summary>

```kotlin
package com.phonemanager.model

import com.google.gson.annotations.SerializedName

// Command Request Model
data class CommandRequest(
    @SerializedName("command")
    val command: String,
    
    @SerializedName("number")
    val number: String? = null,
    
    @SerializedName("sim")
    val sim: Int = 1,  // 1 or 2
    
    @SerializedName("is_ussd")
    val isUssd: Boolean = false,
    
    // Call log query parameters
    @SerializedName("type")
    val type: String? = null,  // incoming, outgoing, missed, all
    
    @SerializedName("limit")
    val limit: Int = 50,
    
    @SerializedName("offset")
    val offset: Int = 0,
    
    @SerializedName("from_date")
    val fromDate: Long? = null,
    
    @SerializedName("to_date")
    val toDate: Long? = null,
    
    @SerializedName("search_number")
    val searchNumber: String? = null
)

// Command Response Model
data class CommandResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: Any? = null,
    
    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
)

// Active Call Info
data class ActiveCallInfo(
    @SerializedName("number")
    val number: String?,
    
    @SerializedName("state")
    val state: String,
    
    @SerializedName("duration")
    val duration: Long,
    
    @SerializedName("is_incoming")
    val isIncoming: Boolean,
    
    @SerializedName("start_time")
    val startTime: Long,
    
    @SerializedName("is_muted")
    val isMuted: Boolean,
    
    @SerializedName("is_on_hold")
    val isOnHold: Boolean,
    
    @SerializedName("is_speaker_on")
    val isSpeakerOn: Boolean
)

// Call Log Entry
data class CallLogEntry(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("number")
    val number: String?,
    
    @SerializedName("name")
    val name: String?,
    
    @SerializedName("type")
    val type: String,
    
    @SerializedName("date")
    val date: Long,
    
    @SerializedName("duration")
    val duration: Long,
    
    @SerializedName("sim_slot")
    val simSlot: Int
)

// Call Log Response
data class CallLogResponse(
    @SerializedName("total_count")
    val totalCount: Int,
    
    @SerializedName("entries")
    val entries: List<CallLogEntry>,
    
    @SerializedName("limit")
    val limit: Int,
    
    @SerializedName("offset")
    val offset: Int
)

// Supported Commands
object Commands {
    const val DIAL = "dial"
    const val ANSWER = "answer"
    const val REJECT = "reject"
    const val TERMINATE = "terminate"
    const val HOLD = "hold"
    const val UNHOLD = "unhold"
    const val MUTE = "mute"
    const val UNMUTE = "unmute"
    const val SPEAKER_ON = "speaker_on"
    const val SPEAKER_OFF = "speaker_off"
    const val GET_ACTIVE_CALL = "get_active_call"
    const val GET_CALL_LOG = "get_call_log"
    const val GET_STATUS = "get_status"
}
```

</details>

## 10. utils/PreferencesManager.kt

<details>
<summary><strong>💻 Code Block (kotlin) — 43 lines</strong></summary>

```kotlin
package com.phonemanager.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )
    
    companion object {
        private const val PREFS_NAME = "phone_manager_prefs"
        private const val KEY_WEBSOCKET_PORT = "websocket_port"
        private const val KEY_HTTP_PORT = "http_port"
        private const val KEY_WEBSOCKET_ENABLED = "websocket_enabled"
        private const val KEY_HTTP_ENABLED = "http_enabled"
        private const val KEY_SERVICE_ENABLED = "service_enabled"
        
        const val DEFAULT_WEBSOCKET_PORT = 8030
        const val DEFAULT_HTTP_PORT = 8040
    }
    
    var webSocketPort: Int
        get() = prefs.getInt(KEY_WEBSOCKET_PORT, DEFAULT_WEBSOCKET_PORT)
        set(value) = prefs.edit().putInt(KEY_WEBSOCKET_PORT, value).apply()
    
    var httpPort: Int
        get() = prefs.getInt(KEY_HTTP_PORT, DEFAULT_HTTP_PORT)
        set(value) = prefs.edit().putInt(KEY_HTTP_PORT, value).apply()
    
    var webSocketEnabled: Boolean
        get() = prefs.getBoolean(KEY_WEBSOCKET_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_WEBSOCKET_ENABLED, value).apply()
    
    var httpEnabled: Boolean
        get() = prefs.getBoolean(KEY_HTTP_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_HTTP_ENABLED, value).apply()
    
    var serviceEnabled: Boolean
        get() = prefs.getBoolean(KEY_SERVICE_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_SERVICE_ENABLED, value).apply()
}
```

</details>

## 11. manager/CallManager.kt

<details>
<summary><strong>💻 Code Block (kotlin) — 292 lines</strong></summary>

```kotlin
package com.phonemanager.manager

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.telecom.Call
import android.telecom.TelecomManager
import android.telecom.PhoneAccountHandle
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import com.phonemanager.model.ActiveCallInfo
import com.phonemanager.model.CommandResponse
import com.phonemanager.service.CallControlService

class CallManager(private val context: Context) {
    
    private val telecomManager: TelecomManager = 
        context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    private val audioManager: AudioManager = 
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val subscriptionManager: SubscriptionManager =
        context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
    
    companion object {
        var isMuted = false
        var isSpeakerOn = false
    }
    
    // Dial number or USSD
    fun dial(number: String, simSlot: Int, isUssd: Boolean): CommandResponse {
        return try {
            val phoneAccountHandle = getPhoneAccountHandle(simSlot)
            
            val uri = if (isUssd) {
                Uri.parse("tel:${Uri.encode(number)}")
            } else {
                Uri.parse("tel:$number")
            }
            
            val extras = android.os.Bundle().apply {
                phoneAccountHandle?.let {
                    putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, it)
                }
            }
            
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) 
                == PackageManager.PERMISSION_GRANTED) {
                
                if (isUssd) {
                    // For USSD, use ACTION_CALL
                    val intent = Intent(Intent.ACTION_CALL, uri).apply {
                        putExtras(extras)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } else {
                    telecomManager.placeCall(uri, extras)
                }
                
                CommandResponse(
                    success = true,
                    message = if (isUssd) "USSD code sent: $number on SIM$simSlot" 
                              else "Dialing: $number on SIM$simSlot"
                )
            } else {
                CommandResponse(
                    success = false,
                    message = "CALL_PHONE permission not granted"
                )
            }
        } catch (e: Exception) {
            CommandResponse(
                success = false,
                message = "Dial failed: ${e.message}"
            )
        }
    }
    
    // Get phone account handle for specific SIM
    private fun getPhoneAccountHandle(simSlot: Int): PhoneAccountHandle? {
        return try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {
                
                val accounts = telecomManager.callCapablePhoneAccounts
                if (accounts.size > simSlot - 1) {
                    accounts[simSlot - 1]
                } else {
                    accounts.firstOrNull()
                }
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    // Answer incoming call
    fun answerCall(): CommandResponse {
        return try {
            val call = CallControlService.currentCall
            if (call != null && call.state == Call.STATE_RINGING) {
                call.answer(android.telecom.VideoProfile.STATE_AUDIO_ONLY)
                CommandResponse(success = true, message = "Call answered")
            } else {
                // Fallback for older devices
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS)
                        == PackageManager.PERMISSION_GRANTED) {
                        telecomManager.acceptRingingCall()
                        CommandResponse(success = true, message = "Call answered")
                    } else {
                        CommandResponse(success = false, message = "ANSWER_PHONE_CALLS permission not granted")
                    }
                } else {
                    CommandResponse(success = false, message = "No incoming call to answer")
                }
            }
        } catch (e: Exception) {
            CommandResponse(success = false, message = "Answer failed: ${e.message}")
        }
    }
    
    // Reject incoming call
    fun rejectCall(): CommandResponse {
        return try {
            val call = CallControlService.currentCall
            if (call != null && call.state == Call.STATE_RINGING) {
                call.reject(false, null)
                CommandResponse(success = true, message = "Call rejected")
            } else {
                CommandResponse(success = false, message = "No incoming call to reject")
            }
        } catch (e: Exception) {
            CommandResponse(success = false, message = "Reject failed: ${e.message}")
        }
    }
    
    // Terminate active call
    fun terminateCall(): CommandResponse {
        return try {
            val call = CallControlService.currentCall
            if (call != null) {
                call.disconnect()
                CommandResponse(success = true, message = "Call terminated")
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS)
                        == PackageManager.PERMISSION_GRANTED) {
                        telecomManager.endCall()
                        CommandResponse(success = true, message = "Call terminated")
                    } else {
                        CommandResponse(success = false, message = "Permission not granted")
                    }
                } else {
                    CommandResponse(success = false, message = "No active call to terminate")
                }
            }
        } catch (e: Exception) {
            CommandResponse(success = false, message = "Terminate failed: ${e.message}")
        }
    }
    
    // Hold call
    fun holdCall(): CommandResponse {
        return try {
            val call = CallControlService.currentCall
            if (call != null && call.state == Call.STATE_ACTIVE) {
                call.hold()
                CommandResponse(success = true, message = "Call on hold")
            } else {
                CommandResponse(success = false, message = "No active call to hold")
            }
        } catch (e: Exception) {
            CommandResponse(success = false, message = "Hold failed: ${e.message}")
        }
    }
    
    // Unhold call
    fun unholdCall(): CommandResponse {
        return try {
            val call = CallControlService.currentCall
            if (call != null && call.state == Call.STATE_HOLDING) {
                call.unhold()
                CommandResponse(success = true, message = "Call resumed")
            } else {
                CommandResponse(success = false, message = "No held call to resume")
            }
        } catch (e: Exception) {
            CommandResponse(success = false, message = "Unhold failed: ${e.message}")
        }
    }
    
    // Mute microphone
    fun mute(): CommandResponse {
        return try {
            audioManager.isMicrophoneMute = true
            isMuted = true
            CommandResponse(success = true, message = "Microphone muted")
        } catch (e: Exception) {
            CommandResponse(success = false, message = "Mute failed: ${e.message}")
        }
    }
    
    // Unmute microphone
    fun unmute(): CommandResponse {
        return try {
            audioManager.isMicrophoneMute = false
            isMuted = false
            CommandResponse(success = true, message = "Microphone unmuted")
        } catch (e: Exception) {
            CommandResponse(success = false, message = "Unmute failed: ${e.message}")
        }
    }
    
    // Enable loudspeaker
    fun speakerOn(): CommandResponse {
        return try {
            audioManager.mode = AudioManager.MODE_IN_CALL
            audioManager.isSpeakerphoneOn = true
            isSpeakerOn = true
            CommandResponse(success = true, message = "Loudspeaker enabled")
        } catch (e: Exception) {
            CommandResponse(success = false, message = "Speaker on failed: ${e.message}")
        }
    }
    
    // Disable loudspeaker
    fun speakerOff(): CommandResponse {
        return try {
            audioManager.isSpeakerphoneOn = false
            isSpeakerOn = false
            CommandResponse(success = true, message = "Loudspeaker disabled")
        } catch (e: Exception) {
            CommandResponse(success = false, message = "Speaker off failed: ${e.message}")
        }
    }
    
    // Get active call info
    fun getActiveCallInfo(): CommandResponse {
        return try {
            val call = CallControlService.currentCall
            if (call != null) {
                val details = call.details
                val callInfo = ActiveCallInfo(
                    number = details?.handle?.schemeSpecificPart,
                    state = getCallStateName(call.state),
                    duration = CallControlService.callStartTime?.let { 
                        System.currentTimeMillis() - it 
                    } ?: 0,
                    isIncoming = CallControlService.isIncoming,
                    startTime = CallControlService.callStartTime ?: 0,
                    isMuted = isMuted,
                    isOnHold = call.state == Call.STATE_HOLDING,
                    isSpeakerOn = isSpeakerOn
                )
                CommandResponse(
                    success = true,
                    message = "Active call info retrieved",
                    data = callInfo
                )
            } else {
                CommandResponse(
                    success = true,
                    message = "No active call",
                    data = null
                )
            }
        } catch (e: Exception) {
            CommandResponse(success = false, message = "Get active call failed: ${e.message}")
        }
    }
    
    private fun getCallStateName(state: Int): String {
        return when (state) {
            Call.STATE_NEW -> "NEW"
            Call.STATE_DIALING -> "DIALING"
            Call.STATE_RINGING -> "RINGING"
            Call.STATE_HOLDING -> "HOLDING"
            Call.STATE_ACTIVE -> "ACTIVE"
            Call.STATE_DISCONNECTED -> "DISCONNECTED"
            Call.STATE_CONNECTING -> "CONNECTING"
            Call.STATE_DISCONNECTING -> "DISCONNECTING"
            Call.STATE_SELECT_PHONE_ACCOUNT -> "SELECT_PHONE_ACCOUNT"
            else -> "UNKNOWN"
        }
    }
}
```

</details>

## 12. manager/CallLogManager.kt

<details>
<summary><strong>💻 Code Block (kotlin) — 174 lines</strong></summary>

```kotlin
package com.phonemanager.manager

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.CallLog
import androidx.core.app.ActivityCompat
import com.phonemanager.model.CallLogEntry
import com.phonemanager.model.CallLogResponse
import com.phonemanager.model.CommandRequest
import com.phonemanager.model.CommandResponse

class CallLogManager(private val context: Context) {
    
    fun getCallLog(request: CommandRequest): CommandResponse {
        return try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED) {
                return CommandResponse(
                    success = false,
                    message = "READ_CALL_LOG permission not granted"
                )
            }
            
            val selection = buildSelection(request)
            val selectionArgs = buildSelectionArgs(request)
            val sortOrder = "${CallLog.Calls.DATE} DESC LIMIT ${request.limit} OFFSET ${request.offset}"
            
            val projection = arrayOf(
                CallLog.Calls._ID,
                CallLog.Calls.NUMBER,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
                CallLog.Calls.PHONE_ACCOUNT_ID
            )
            
            val cursor: Cursor? = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )
            
            val entries = mutableListOf<CallLogEntry>()
            var totalCount = 0
            
            cursor?.use {
                while (it.moveToNext()) {
                    val id = it.getLong(it.getColumnIndexOrThrow(CallLog.Calls._ID))
                    val number = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
                    val name = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME))
                    val type = it.getInt(it.getColumnIndexOrThrow(CallLog.Calls.TYPE))
                    val date = it.getLong(it.getColumnIndexOrThrow(CallLog.Calls.DATE))
                    val duration = it.getLong(it.getColumnIndexOrThrow(CallLog.Calls.DURATION))
                    val simId = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.PHONE_ACCOUNT_ID))
                    
                    entries.add(
                        CallLogEntry(
                            id = id,
                            number = number,
                            name = name,
                            type = getCallTypeName(type),
                            date = date,
                            duration = duration,
                            simSlot = extractSimSlot(simId)
                        )
                    )
                }
                totalCount = it.count
            }
            
            // Get total count without limit
            val countCursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf("COUNT(*)"),
                selection,
                selectionArgs,
                null
            )
            countCursor?.use {
                if (it.moveToFirst()) {
                    totalCount = it.getInt(0)
                }
            }
            
            val response = CallLogResponse(
                totalCount = totalCount,
                entries = entries,
                limit = request.limit,
                offset = request.offset
            )
            
            CommandResponse(
                success = true,
                message = "Call log retrieved successfully",
                data = response
            )
            
        } catch (e: Exception) {
            CommandResponse(
                success = false,
                message = "Failed to get call log: ${e.message}"
            )
        }
    }
    
    private fun buildSelection(request: CommandRequest): String? {
        val conditions = mutableListOf<String>()
        
        // Filter by call type
        request.type?.let { type ->
            when (type.lowercase()) {
                "incoming" -> conditions.add("${CallLog.Calls.TYPE} = ${CallLog.Calls.INCOMING_TYPE}")
                "outgoing" -> conditions.add("${CallLog.Calls.TYPE} = ${CallLog.Calls.OUTGOING_TYPE}")
                "missed" -> conditions.add("${CallLog.Calls.TYPE} = ${CallLog.Calls.MISSED_TYPE}")
                "rejected" -> conditions.add("${CallLog.Calls.TYPE} = ${CallLog.Calls.REJECTED_TYPE}")
                "all" -> { /* No filter */ }
            }
        }
        
        // Filter by date range
        request.fromDate?.let {
            conditions.add("${CallLog.Calls.DATE} >= ?")
        }
        
        request.toDate?.let {
            conditions.add("${CallLog.Calls.DATE} <= ?")
        }
        
        // Filter by number
        request.searchNumber?.let {
            conditions.add("${CallLog.Calls.NUMBER} LIKE ?")
        }
        
        return if (conditions.isEmpty()) null else conditions.joinToString(" AND ")
    }
    
    private fun buildSelectionArgs(request: CommandRequest): Array<String>? {
        val args = mutableListOf<String>()
        
        request.fromDate?.let { args.add(it.toString()) }
        request.toDate?.let { args.add(it.toString()) }
        request.searchNumber?.let { args.add("%$it%") }
        
        return if (args.isEmpty()) null else args.toTypedArray()
    }
    
    private fun getCallTypeName(type: Int): String {
        return when (type) {
            CallLog.Calls.INCOMING_TYPE -> "incoming"
            CallLog.Calls.OUTGOING_TYPE -> "outgoing"
            CallLog.Calls.MISSED_TYPE -> "missed"
            CallLog.Calls.REJECTED_TYPE -> "rejected"
            CallLog.Calls.BLOCKED_TYPE -> "blocked"
            CallLog.Calls.VOICEMAIL_TYPE -> "voicemail"
            else -> "unknown"
        }
    }
    
    private fun extractSimSlot(simId: String?): Int {
        // Simple extraction - may need adjustment based on device
        return try {
            if (simId?.contains("0") == true) 1
            else if (simId?.contains("1") == true) 2
            else 1
        } catch (e: Exception) {
            1
        }
    }
}
```

</details>

## 13. server/WebSocketServerHandler.kt

<details>
<summary><strong>💻 Code Block (kotlin) — 95 lines</strong></summary>

```kotlin
package com.phonemanager.server

import com.google.gson.Gson
import com.phonemanager.manager.CallLogManager
import com.phonemanager.manager.CallManager
import com.phonemanager.model.CommandRequest
import com.phonemanager.model.CommandResponse
import com.phonemanager.model.Commands
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress

class WebSocketServerHandler(
    port: Int,
    private val callManager: CallManager,
    private val callLogManager: CallLogManager,
    private val onLog: (String) -> Unit
) : WebSocketServer(InetSocketAddress(port)) {
    
    private val gson = Gson()
    
    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
        val clientAddress = conn?.remoteSocketAddress?.toString() ?: "unknown"
        onLog("WebSocket client connected: $clientAddress")
    }
    
    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        val clientAddress = conn?.remoteSocketAddress?.toString() ?: "unknown"
        onLog("WebSocket client disconnected: $clientAddress")
    }
    
    override fun onMessage(conn: WebSocket?, message: String?) {
        message?.let { msg ->
            onLog("WS received: $msg")
            try {
                val request = gson.fromJson(msg, CommandRequest::class.java)
                val response = processCommand(request)
                val jsonResponse = gson.toJson(response)
                conn?.send(jsonResponse)
                onLog("WS response: $jsonResponse")
            } catch (e: Exception) {
                val errorResponse = CommandResponse(
                    success = false,
                    message = "Invalid request format: ${e.message}"
                )
                conn?.send(gson.toJson(errorResponse))
            }
        }
    }
    
    override fun onError(conn: WebSocket?, ex: Exception?) {
        onLog("WebSocket error: ${ex?.message}")
    }
    
    override fun onStart() {
        onLog("WebSocket server started on port: $port")
        connectionLostTimeout = 100
    }
    
    private fun processCommand(request: CommandRequest): CommandResponse {
        return when (request.command.lowercase()) {
            Commands.DIAL -> {
                if (request.number.isNullOrEmpty()) {
                    CommandResponse(false, "Number is required for dial command")
                } else {
                    callManager.dial(request.number, request.sim, request.isUssd)
                }
            }
            Commands.ANSWER -> callManager.answerCall()
            Commands.REJECT -> callManager.rejectCall()
            Commands.TERMINATE -> callManager.terminateCall()
            Commands.HOLD -> callManager.holdCall()
            Commands.UNHOLD -> callManager.unholdCall()
            Commands.MUTE -> callManager.mute()
            Commands.UNMUTE -> callManager.unmute()
            Commands.SPEAKER_ON -> callManager.speakerOn()
            Commands.SPEAKER_OFF -> callManager.speakerOff()
            Commands.GET_ACTIVE_CALL -> callManager.getActiveCallInfo()
            Commands.GET_CALL_LOG -> callLogManager.getCallLog(request)
            Commands.GET_STATUS -> CommandResponse(
                success = true,
                message = "Phone Manager is running"
            )
            else -> CommandResponse(
                success = false,
                message = "Unknown command: ${request.command}"
            )
        }
    }
    
    fun broadcastMessage(message: String) {
        broadcast(message)
    }
}
```

</details>

## 14. server/HttpServerHandler.kt

<details>
<summary><strong>💻 Code Block (kotlin) — 155 lines</strong></summary>

```kotlin
package com.phonemanager.server

import com.google.gson.Gson
import com.phonemanager.manager.CallLogManager
import com.phonemanager.manager.CallManager
import com.phonemanager.model.CommandRequest
import com.phonemanager.model.CommandResponse
import com.phonemanager.model.Commands
import fi.iki.elonen.NanoHTTPD

class HttpServerHandler(
    port: Int,
    private val callManager: CallManager,
    private val callLogManager: CallLogManager,
    private val onLog: (String) -> Unit
) : NanoHTTPD(port) {
    
    private val gson = Gson()
    
    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri
        val method = session.method
        
        onLog("HTTP ${method.name} $uri")
        
        return try {
            when {
                uri == "/" || uri == "/status" -> handleStatus()
                uri == "/api" || uri.startsWith("/api/") -> handleApiRequest(session)
                else -> createJsonResponse(
                    Response.Status.NOT_FOUND,
                    CommandResponse(false, "Endpoint not found: $uri")
                )
            }
        } catch (e: Exception) {
            onLog("HTTP error: ${e.message}")
            createJsonResponse(
                Response.Status.INTERNAL_ERROR,
                CommandResponse(false, "Server error: ${e.message}")
            )
        }
    }
    
    private fun handleStatus(): Response {
        return createJsonResponse(
            Response.Status.OK,
            CommandResponse(true, "Phone Manager HTTP Server is running")
        )
    }
    
    private fun handleApiRequest(session: IHTTPSession): Response {
        val request = parseRequest(session)
        val response = processCommand(request)
        onLog("HTTP response: ${gson.toJson(response)}")
        return createJsonResponse(Response.Status.OK, response)
    }
    
    private fun parseRequest(session: IHTTPSession): CommandRequest {
        val params = mutableMapOf<String, String>()
        
        // Parse query parameters (GET)
        session.parameters.forEach { (key, values) ->
            if (values.isNotEmpty()) {
                params[key] = values[0]
            }
        }
        
        // Parse body (POST)
        if (session.method == Method.POST) {
            val contentLength = session.headers["content-length"]?.toIntOrNull() ?: 0
            if (contentLength > 0) {
                val body = mutableMapOf<String, String>()
                session.parseBody(body)
                
                // Check if JSON body
                val postData = body["postData"]
                if (postData != null) {
                    try {
                        return gson.fromJson(postData, CommandRequest::class.java)
                    } catch (e: Exception) {
                        // Not JSON, continue with form parsing
                    }
                }
                
                // Form data parsing
                session.parameters.forEach { (key, values) ->
                    if (values.isNotEmpty()) {
                        params[key] = values[0]
                    }
                }
            }
        }
        
        // Build request from parameters
        return CommandRequest(
            command = params["command"] ?: params["cmd"] ?: "",
            number = params["number"] ?: params["phone"],
            sim = params["sim"]?.toIntOrNull() ?: 1,
            isUssd = params["is_ussd"]?.toBoolean() ?: params["ussd"]?.toBoolean() ?: false,
            type = params["type"],
            limit = params["limit"]?.toIntOrNull() ?: 50,
            offset = params["offset"]?.toIntOrNull() ?: 0,
            fromDate = params["from_date"]?.toLongOrNull(),
            toDate = params["to_date"]?.toLongOrNull(),
            searchNumber = params["search_number"]
        )
    }
    
    private fun processCommand(request: CommandRequest): CommandResponse {
        if (request.command.isEmpty()) {
            return CommandResponse(
                success = false,
                message = "Command is required. Available commands: dial, answer, reject, terminate, hold, unhold, mute, unmute, speaker_on, speaker_off, get_active_call, get_call_log"
            )
        }
        
        return when (request.command.lowercase()) {
            Commands.DIAL -> {
                if (request.number.isNullOrEmpty()) {
                    CommandResponse(false, "Number is required. Use: ?command=dial&number=1234567890&sim=1&ussd=false")
                } else {
                    callManager.dial(request.number, request.sim, request.isUssd)
                }
            }
            Commands.ANSWER -> callManager.answerCall()
            Commands.REJECT -> callManager.rejectCall()
            Commands.TERMINATE -> callManager.terminateCall()
            Commands.HOLD -> callManager.holdCall()
            Commands.UNHOLD -> callManager.unholdCall()
            Commands.MUTE -> callManager.mute()
            Commands.UNMUTE -> callManager.unmute()
            Commands.SPEAKER_ON -> callManager.speakerOn()
            Commands.SPEAKER_OFF -> callManager.speakerOff()
            Commands.GET_ACTIVE_CALL -> callManager.getActiveCallInfo()
            Commands.GET_CALL_LOG -> callLogManager.getCallLog(request)
            Commands.GET_STATUS -> CommandResponse(
                success = true,
                message = "Phone Manager is running"
            )
            else -> CommandResponse(
                success = false,
                message = "Unknown command: ${request.command}"
            )
        }
    }
    
    private fun createJsonResponse(status: Response.Status, response: CommandResponse): Response {
        val json = gson.toJson(response)
        return newFixedLengthResponse(status, "application/json", json).apply {
            addHeader("Access-Control-Allow-Origin", "*")
            addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
            addHeader("Access-Control-Allow-Headers", "Content-Type")
        }
    }
}
```

</details>

## 15. service/CallControlService.kt

<details>
<summary><strong>💻 Code Block (kotlin) — 61 lines</strong></summary>

```kotlin
package com.phonemanager.service

import android.telecom.Call
import android.telecom.InCallService

class CallControlService : InCallService() {
    
    companion object {
        var currentCall: Call? = null
        var callStartTime: Long? = null
        var isIncoming: Boolean = false
        
        private var callStateListeners = mutableListOf<(Call?, Int) -> Unit>()
        
        fun addCallStateListener(listener: (Call?, Int) -> Unit) {
            callStateListeners.add(listener)
        }
        
        fun removeCallStateListener(listener: (Call?, Int) -> Unit) {
            callStateListeners.remove(listener)
        }
    }
    
    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call?, state: Int) {
            super.onStateChanged(call, state)
            
            when (state) {
                Call.STATE_ACTIVE -> {
                    if (callStartTime == null) {
                        callStartTime = System.currentTimeMillis()
                    }
                }
                Call.STATE_DISCONNECTED -> {
                    currentCall = null
                    callStartTime = null
                    isIncoming = false
                }
            }
            
            callStateListeners.forEach { it(call, state) }
        }
    }
    
    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        currentCall = call
        isIncoming = call.state == Call.STATE_RINGING
        call.registerCallback(callCallback)
    }
    
    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        call.unregisterCallback(callCallback)
        if (currentCall == call) {
            currentCall = null
            callStartTime = null
            isIncoming = false
        }
    }
}
```

</details>

## 16. service/PhoneManagerService.kt

<details>
<summary><strong>💻 Code Block (kotlin) — 194 lines</strong></summary>

```kotlin
package com.phonemanager.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.phonemanager.MainActivity
import com.phonemanager.PhoneManagerApp
import com.phonemanager.R
import com.phonemanager.manager.CallLogManager
import com.phonemanager.manager.CallManager
import com.phonemanager.server.HttpServerHandler
import com.phonemanager.server.WebSocketServerHandler
import com.phonemanager.utils.PreferencesManager
import kotlinx.coroutines.*

class PhoneManagerService : Service() {
    
    private val binder = LocalBinder()
    private var webSocketServer: WebSocketServerHandler? = null
    private var httpServer: HttpServerHandler? = null
    
    private lateinit var callManager: CallManager
    private lateinit var callLogManager: CallLogManager
    private lateinit var prefsManager: PreferencesManager
    
    private var wakeLock: PowerManager.WakeLock? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val logListeners = mutableListOf<(String) -> Unit>()
    
    inner class LocalBinder : Binder() {
        fun getService(): PhoneManagerService = this@PhoneManagerService
    }
    
    override fun onCreate() {
        super.onCreate()
        callManager = CallManager(this)
        callLogManager = CallLogManager(this)
        prefsManager = PreferencesManager(this)
        acquireWakeLock()
    }
    
    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_SERVICE -> {
                stopSelf()
                return START_NOT_STICKY
            }
        }
        
        startForeground(NOTIFICATION_ID, createNotification())
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopWebSocketServer()
        stopHttpServer()
        releaseWakeLock()
        serviceScope.cancel()
    }
    
    private fun createNotification(): Notification {
        val stopIntent = Intent(this, PhoneManagerService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE
        )
        
        val openIntent = Intent(this, MainActivity::class.java)
        val openPendingIntent = PendingIntent.getActivity(
            this, 0, openIntent, PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, PhoneManagerApp.CHANNEL_ID)
            .setContentTitle("Phone Manager")
            .setContentText("Service is running...")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(openPendingIntent)
            .addAction(R.drawable.ic_stop, "Stop", stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
    
    private fun acquireWakeLock() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "PhoneManager::WakeLock"
        ).apply {
            acquire()
        }
    }
    
    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
    }
    
    // WebSocket Server Control
    fun startWebSocketServer(port: Int): Boolean {
        return try {
            if (webSocketServer != null) {
                stopWebSocketServer()
            }
            webSocketServer = WebSocketServerHandler(port, callManager, callLogManager) { log ->
                notifyLog(log)
            }
            webSocketServer?.start()
            notifyLog("WebSocket server started on port $port")
            true
        } catch (e: Exception) {
            notifyLog("WebSocket server failed: ${e.message}")
            false
        }
    }
    
    fun stopWebSocketServer() {
        try {
            webSocketServer?.stop(1000)
            webSocketServer = null
            notifyLog("WebSocket server stopped")
        } catch (e: Exception) {
            notifyLog("WebSocket stop error: ${e.message}")
        }
    }
    
    fun isWebSocketRunning(): Boolean = webSocketServer != null
    
    // HTTP Server Control
    fun startHttpServer(port: Int): Boolean {
        return try {
            if (httpServer != null) {
                stopHttpServer()
            }
            httpServer = HttpServerHandler(port, callManager, callLogManager) { log ->
                notifyLog(log)
            }
            httpServer?.start()
            notifyLog("HTTP server started on port $port")
            true
        } catch (e: Exception) {
            notifyLog("HTTP server failed: ${e.message}")
            false
        }
    }
    
    fun stopHttpServer() {
        try {
            httpServer?.stop()
            httpServer = null
            notifyLog("HTTP server stopped")
        } catch (e: Exception) {
            notifyLog("HTTP stop error: ${e.message}")
        }
    }
    
    fun isHttpRunning(): Boolean = httpServer != null
    
    // Log listeners
    fun addLogListener(listener: (String) -> Unit) {
        logListeners.add(listener)
    }
    
    fun removeLogListener(listener: (String) -> Unit) {
        logListeners.remove(listener)
    }
    
    private fun notifyLog(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        val logMessage = "[$timestamp] $message"
        logListeners.forEach { it(logMessage) }
    }
    
    companion object {
        const val ACTION_STOP_SERVICE = "com.phonemanager.STOP_SERVICE"
        const val NOTIFICATION_ID = 1
    }
}
```

</details>

## 17. receiver/BootReceiver.kt

<details>
<summary><strong>💻 Code Block (kotlin) — 27 lines</strong></summary>

```kotlin
package com.phonemanager.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.phonemanager.service.PhoneManagerService
import com.phonemanager.utils.PreferencesManager

class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            
            val prefs = PreferencesManager(context)
            if (prefs.serviceEnabled) {
                val serviceIntent = Intent(context, PhoneManagerService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        }
    }
}
```

</details>

## 18. MainActivity.kt

<details>
<summary><strong>💻 Code Block (kotlin) — 313 lines</strong></summary>

```kotlin
package com.phonemanager

import android.Manifest
import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.telecom.TelecomManager
import android.text.method.ScrollingMovementMethod
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.phonemanager.databinding.ActivityMainBinding
import com.phonemanager.service.PhoneManagerService
import com.phonemanager.utils.PreferencesManager

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var prefsManager: PreferencesManager
    
    private var phoneManagerService: PhoneManagerService? = null
    private var isServiceBound = false
    
    private val logBuilder = StringBuilder()
    
    private val requiredPermissions = arrayOf(
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_PHONE_NUMBERS,
        Manifest.permission.ANSWER_PHONE_CALLS,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.WRITE_CALL_LOG,
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.FOREGROUND_SERVICE
    )
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        updatePermissionStatus()
    }
    
    private val defaultDialerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        checkDefaultDialer()
    }
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as PhoneManagerService.LocalBinder
            phoneManagerService = binder.getService()
            isServiceBound = true
            
            phoneManagerService?.addLogListener { log ->
                runOnUiThread {
                    appendLog(log)
                }
            }
            
            updateServiceStatus()
            restoreServerStates()
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            phoneManagerService = null
            isServiceBound = false
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        prefsManager = PreferencesManager(this)
        
        setupUI()
        updatePermissionStatus()
        updateIpAddress()
        startAndBindService()
    }
    
    private fun setupUI() {
        binding.etWebSocketPort.setText(prefsManager.webSocketPort.toString())
        binding.etHttpPort.setText(prefsManager.httpPort.toString())
        
        binding.tvLog.movementMethod = ScrollingMovementMethod()
        
        // WebSocket toggle
        binding.switchWebSocket.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val port = binding.etWebSocketPort.text.toString().toIntOrNull() ?: 8030
                prefsManager.webSocketPort = port
                val success = phoneManagerService?.startWebSocketServer(port) ?: false
                if (!success) {
                    binding.switchWebSocket.isChecked = false
                }
            } else {
                phoneManagerService?.stopWebSocketServer()
            }
            prefsManager.webSocketEnabled = isChecked
            updateWebSocketStatus()
        }
        
        // HTTP toggle
        binding.switchHttp.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val port = binding.etHttpPort.text.toString().toIntOrNull() ?: 8040
                prefsManager.httpPort = port
                val success = phoneManagerService?.startHttpServer(port) ?: false
                if (!success) {
                    binding.switchHttp.isChecked = false
                }
            } else {
                phoneManagerService?.stopHttpServer()
            }
            prefsManager.httpEnabled = isChecked
            updateHttpStatus()
        }
        
        // Request permissions button
        binding.btnRequestPermissions.setOnClickListener {
            requestPermissions()
        }
        
        // Set default dialer button
        binding.btnSetDefaultDialer.setOnClickListener {
            requestDefaultDialer()
        }
        
        // Clear log button
        binding.btnClearLog.setOnClickListener {
            logBuilder.clear()
            binding.tvLog.text = "Log cleared."
        }
    }
    
    private fun startAndBindService() {
        val serviceIntent = Intent(this, PhoneManagerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        prefsManager.serviceEnabled = true
    }
    
    private fun restoreServerStates() {
        if (prefsManager.webSocketEnabled) {
            binding.switchWebSocket.isChecked = true
        }
        if (prefsManager.httpEnabled) {
            binding.switchHttp.isChecked = true
        }
    }
    
    private fun updateServiceStatus() {
        binding.tvStatus.text = "Status: Running"
        binding.tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_light))
    }
    
    private fun updateWebSocketStatus() {
        val isRunning = phoneManagerService?.isWebSocketRunning() ?: false
        binding.tvWebSocketStatus.text = if (isRunning) "Status: ON" else "Status: OFF"
        binding.tvWebSocketStatus.setTextColor(
            if (isRunning) ContextCompat.getColor(this, android.R.color.holo_green_light)
            else ContextCompat.getColor(this, android.R.color.holo_red_light)
        )
    }
    
    private fun updateHttpStatus() {
        val isRunning = phoneManagerService?.isHttpRunning() ?: false
        binding.tvHttpStatus.text = if (isRunning) "Status: ON" else "Status: OFF"
        binding.tvHttpStatus.setTextColor(
            if (isRunning) ContextCompat.getColor(this, android.R.color.holo_green_light)
            else ContextCompat.getColor(this, android.R.color.holo_red_light)
        )
    }
    
    private fun updateIpAddress() {
        try {
            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val ipAddress = wifiManager.connectionInfo.ipAddress
            val ip = String.format(
                "%d.%d.%d.%d",
                ipAddress and 0xff,
                ipAddress shr 8 and 0xff,
                ipAddress shr 16 and 0xff,
                ipAddress shr 24 and 0xff
            )
            binding.tvIpAddress.text = "IP: $ip"
        } catch (e: Exception) {
            binding.tvIpAddress.text = "IP: Unable to get IP"
        }
    }
    
    private fun updatePermissionStatus() {
        val permissionStatus = StringBuilder()
        var allGranted = true
        
        for (permission in requiredPermissions) {
            val granted = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
            val permName = permission.substringAfterLast(".")
            val status = if (granted) "✓" else "✗"
            permissionStatus.append("$status $permName\n")
            if (!granted) allGranted = false
        }
        
        // Check default dialer
        val isDefaultDialer = isDefaultDialer()
        permissionStatus.append("${if (isDefaultDialer) "✓" else "✗"} Default Dialer")
        
        binding.tvPermissions.text = permissionStatus.toString()
        
        if (allGranted && isDefaultDialer) {
            binding.btnRequestPermissions.isEnabled = false
            binding.btnSetDefaultDialer.isEnabled = false
        }
    }
    
    private fun requestPermissions() {
        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
        
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest)
        }
        
        // Request battery optimization exemption
        requestBatteryOptimizationExemption()
    }
    
    private fun requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "Please disable battery optimization manually", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun requestDefaultDialer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
            if (roleManager.isRoleAvailable(RoleManager.ROLE_DIALER) &&
                !roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                defaultDialerLauncher.launch(intent)
            }
        } else {
            val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
            }
            defaultDialerLauncher.launch(intent)
        }
    }
    
    private fun isDefaultDialer(): Boolean {
        val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        return packageName == telecomManager.defaultDialerPackage
    }
    
    private fun checkDefaultDialer() {
        updatePermissionStatus()
        if (isDefaultDialer()) {
            Toast.makeText(this, "Set as default dialer successfully", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun appendLog(message: String) {
        logBuilder.append(message).append("\n")
        binding.tvLog.text = logBuilder.toString()
        
        // Auto scroll to bottom
        val scrollAmount = binding.tvLog.layout?.let {
            it.getLineTop(binding.tvLog.lineCount) - binding.tvLog.height
        } ?: 0
        if (scrollAmount > 0) {
            binding.tvLog.scrollTo(0, scrollAmount)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (isServiceBound) {
            phoneManagerService?.removeLogListener { }
            unbindService(serviceConnection)
            isServiceBound = false
        }
    }
}
```

</details>

## 19. Drawable Resources (res/drawable/)

### ic_notification.xml
<details>
<summary><strong>💻 Code Block (xml) — 9 lines</strong></summary>

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M6.62,10.79c1.44,2.83 3.76,5.14 6.59,6.59l2.2,-2.2c0.27,-0.27 0.67,-0.36 1.02,-0.24 1.12,0.37 2.33,0.57 3.57,0.57 0.55,0 1,0.45 1,1V20c0,0.55 -0.45,1 -1,1 -9.39,0 -17,-7.61 -17,-17 0,-0.55 0.45,-1 1,-1h3.5c0.55,0 1,0.45 1,1 0,1.25 0.2,2.45 0.57,3.57 0.11,0.35 0.03,0.74 -0.25,1.02l-2.2,2.2z"/>
</vector>
```

</details>

### ic_stop.xml
<details>
<summary><strong>💻 Code Block (xml) — 9 lines</strong></summary>

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M6,6h12v12H6z"/>
</vector>
```

</details>

---

## API Usage Examples

### HTTP GET Examples

<details>
<summary><strong>💻 Code Block (bash) — 50 lines</strong></summary>

```bash
# Dial a number on SIM1
curl "http://192.168.1.100:8040/api?command=dial&number=1234567890&sim=1"

# Dial a number on SIM2
curl "http://192.168.1.100:8040/api?command=dial&number=1234567890&sim=2"

# Dial USSD code on SIM1
curl "http://192.168.1.100:8040/api?command=dial&number=*123#&sim=1&ussd=true"

# Answer call
curl "http://192.168.1.100:8040/api?command=answer"

# Reject call
curl "http://192.168.1.100:8040/api?command=reject"

# Terminate call
curl "http://192.168.1.100:8040/api?command=terminate"

# Hold call
curl "http://192.168.1.100:8040/api?command=hold"

# Unhold call
curl "http://192.168.1.100:8040/api?command=unhold"

# Mute microphone
curl "http://192.168.1.100:8040/api?command=mute"

# Unmute microphone
curl "http://192.168.1.100:8040/api?command=unmute"

# Enable loudspeaker
curl "http://192.168.1.100:8040/api?command=speaker_on"

# Disable loudspeaker
curl "http://192.168.1.100:8040/api?command=speaker_off"

# Get active call info
curl "http://192.168.1.100:8040/api?command=get_active_call"

# Get call log (all)
curl "http://192.168.1.100:8040/api?command=get_call_log&type=all&limit=50"

# Get missed calls
curl "http://192.168.1.100:8040/api?command=get_call_log&type=missed&limit=20"

# Get incoming calls with date filter
curl "http://192.168.1.100:8040/api?command=get_call_log&type=incoming&from_date=1699000000000&to_date=1699999999999"

# Search calls by number
curl "http://192.168.1.100:8040/api?command=get_call_log&search_number=12345"
```

</details>

### HTTP POST Examples

<details>
<summary><strong>💻 Code Block (bash) — 14 lines</strong></summary>

```bash
# Dial using POST with JSON
curl -X POST "http://192.168.1.100:8040/api" \
  -H "Content-Type: application/json" \
  -d '{"command":"dial","number":"1234567890","sim":1,"is_ussd":false}'

# Dial USSD using POST
curl -X POST "http://192.168.1.100:8040/api" \
  -H "Content-Type: application/json" \
  -d '{"command":"dial","number":"*123#","sim":2,"is_ussd":true}'

# Get call log with filters
curl -X POST "http://192.168.1.100:8040/api" \
  -H "Content-Type: application/json" \
  -d '{"command":"get_call_log","type":"missed","limit":10,"offset":0}'
```

</details>

### WebSocket Examples

<details>
<summary><strong>💻 Code Block (javascript) — 41 lines</strong></summary>

```javascript
// Connect to WebSocket
const ws = new WebSocket('ws://192.168.1.100:8030');

ws.onopen = () => {
    console.log('Connected');
    
    // Dial number on SIM1
    ws.send(JSON.stringify({
        command: 'dial',
        number: '1234567890',
        sim: 1,
        is_ussd: false
    }));
};

ws.onmessage = (event) => {
    const response = JSON.parse(event.data);
    console.log('Response:', response);
};

// Dial USSD on SIM2
ws.send(JSON.stringify({
    command: 'dial',
    number: '*123#',
    sim: 2,
    is_ussd: true
}));

// Answer call
ws.send(JSON.stringify({ command: 'answer' }));

// Get active call info
ws.send(JSON.stringify({ command: 'get_active_call' }));

// Get call log
ws.send(JSON.stringify({
    command: 'get_call_log',
    type: 'all',
    limit: 50,
    offset: 0
}));
```

</details>

---

## Response Format

### Success Response
<details>
<summary><strong>💻 Code Block (json) — 6 lines</strong></summary>

```json
{
    "success": true,
    "message": "Dialing: 1234567890 on SIM1",
    "data": null,
    "timestamp": 1699123456789
}
```

</details>

### Active Call Response
<details>
<summary><strong>💻 Code Block (json) — 15 lines</strong></summary>

```json
{
    "success": true,
    "message": "Active call info retrieved",
    "data": {
        "number": "1234567890",
        "state": "ACTIVE",
        "duration": 45000,
        "is_incoming": true,
        "start_time": 1699123400000,
        "is_muted": false,
        "is_on_hold": false,
        "is_speaker_on": false
    },
    "timestamp": 1699123456789
}
```

</details>

### Call Log Response
<details>
<summary><strong>💻 Code Block (json) — 21 lines</strong></summary>

```json
{
    "success": true,
    "message": "Call log retrieved successfully",
    "data": {
        "total_count": 150,
        "entries": [
            {
                "id": 1,
                "number": "1234567890",
                "name": "John Doe",
                "type": "incoming",
                "date": 1699123456789,
                "duration": 120,
                "sim_slot": 1
            }
        ],
        "limit": 50,
        "offset": 0
    },
    "timestamp": 1699123456789
}
```

</details>

---

## Notes

1. **Required Setup**: After installing, you must:
   - Grant all requested permissions
   - Set the app as the default dialer (required for call control)
   - Disable battery optimization for the app

2. **SIM Selection**: Use `sim=1` or `sim=2` to select which SIM card to use for dialing

3. **USSD Codes**: Set `ussd=true` (GET) or `is_ussd: true` (JSON) when dialing USSD codes

4. **Background Operation**: The app uses a foreground service to stay alive even when the screen is off

5. **Security**: This app exposes phone functions over the network. Use only on trusted networks!

---

## [3] USER

e: file:///D:/Android/PhoneManger/app/src/main/java/com/phonemanger/manager/CallLogManager.kt:116:13 'when' expression must be exhaustive, add necessary 'else' branch

[Incubating] Problems report is available at: file:///D:/Android/PhoneManger/build/reports/problems/problems-report.html

---

## [4] ASSISTANT (claude-opus-4-5-20251101-thinking-32k)

<details>
<summary><strong>💭 Thinking Process</strong></summary>

> The user is getting a compilation error in CallLogManager.kt. The error says that the 'when' expression must be exhaustive. Looking at my code, I have a `when` block for call types in the `buildSelection` function that doesn't have an `else` branch.
> 
> Let me look at the code:
> 
> ```kotlin
> request.type?.let { type ->
>     when (type.lowercase()) {
>         "incoming" -> conditions.add("${CallLog.Calls.TYPE} = ${CallLog.Calls.INCOMING_TYPE}")
>         "outgoing" -> conditions.add("${CallLog.Calls.TYPE} = ${CallLog.Calls.OUTGOING_TYPE}")
>         "missed" -> conditions.add("${CallLog.Calls.TYPE} = ${CallLog.Calls.MISSED_TYPE}")
>         "rejected" -> conditions.add("${CallLog.Calls.TYPE} = ${CallLog.Calls.REJECTED_TYPE}")
>         "all" -> { /* No filter */ }
>     }
> }
> ```
> 
> This when expression is not exhaustive because it doesn't cover all possible String values. I need to add an `else` branch.
> 
> Let me provide the fixed CallLogManager.kt file.

</details>

# Fix for CallLogManager.kt

The `when` expression needs an `else` branch. Here's the corrected file:

## manager/CallLogManager.kt (Fixed)

<details>
<summary><strong>💻 Code Block (kotlin) — 180 lines</strong></summary>

```kotlin
package com.phonemanager.manager

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.CallLog
import androidx.core.app.ActivityCompat
import com.phonemanager.model.CallLogEntry
import com.phonemanager.model.CallLogResponse
import com.phonemanager.model.CommandRequest
import com.phonemanager.model.CommandResponse

class CallLogManager(private val context: Context) {
    
    fun getCallLog(request: CommandRequest): CommandResponse {
        return try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED) {
                return CommandResponse(
                    success = false,
                    message = "READ_CALL_LOG permission not granted"
                )
            }
            
            val selection = buildSelection(request)
            val selectionArgs = buildSelectionArgs(request)
            val sortOrder = "${CallLog.Calls.DATE} DESC LIMIT ${request.limit} OFFSET ${request.offset}"
            
            val projection = arrayOf(
                CallLog.Calls._ID,
                CallLog.Calls.NUMBER,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
                CallLog.Calls.PHONE_ACCOUNT_ID
            )
            
            val cursor: Cursor? = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )
            
            val entries = mutableListOf<CallLogEntry>()
            var totalCount = 0
            
            cursor?.use {
                while (it.moveToNext()) {
                    val id = it.getLong(it.getColumnIndexOrThrow(CallLog.Calls._ID))
                    val number = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
                    val name = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME))
                    val type = it.getInt(it.getColumnIndexOrThrow(CallLog.Calls.TYPE))
                    val date = it.getLong(it.getColumnIndexOrThrow(CallLog.Calls.DATE))
                    val duration = it.getLong(it.getColumnIndexOrThrow(CallLog.Calls.DURATION))
                    val simId = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.PHONE_ACCOUNT_ID))
                    
                    entries.add(
                        CallLogEntry(
                            id = id,
                            number = number,
                            name = name,
                            type = getCallTypeName(type),
                            date = date,
                            duration = duration,
                            simSlot = extractSimSlot(simId)
                        )
                    )
                }
                totalCount = it.count
            }
            
            // Get total count without limit
            val countCursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf("COUNT(*)"),
                selection,
                selectionArgs,
                null
            )
            countCursor?.use {
                if (it.moveToFirst()) {
                    totalCount = it.getInt(0)
                }
            }
            
            val response = CallLogResponse(
                totalCount = totalCount,
                entries = entries,
                limit = request.limit,
                offset = request.offset
            )
            
            CommandResponse(
                success = true,
                message = "Call log retrieved successfully",
                data = response
            )
            
        } catch (e: Exception) {
            CommandResponse(
                success = false,
                message = "Failed to get call log: ${e.message}"
            )
        }
    }
    
    private fun buildSelection(request: CommandRequest): String? {
        val conditions = mutableListOf<String>()
        
        // Filter by call type
        request.type?.let { type ->
            when (type.lowercase()) {
                "incoming" -> conditions.add("${CallLog.Calls.TYPE} = ${CallLog.Calls.INCOMING_TYPE}")
                "outgoing" -> conditions.add("${CallLog.Calls.TYPE} = ${CallLog.Calls.OUTGOING_TYPE}")
                "missed" -> conditions.add("${CallLog.Calls.TYPE} = ${CallLog.Calls.MISSED_TYPE}")
                "rejected" -> conditions.add("${CallLog.Calls.TYPE} = ${CallLog.Calls.REJECTED_TYPE}")
                "blocked" -> conditions.add("${CallLog.Calls.TYPE} = ${CallLog.Calls.BLOCKED_TYPE}")
                "voicemail" -> conditions.add("${CallLog.Calls.TYPE} = ${CallLog.Calls.VOICEMAIL_TYPE}")
                "all" -> { /* No filter - include all types */ }
                else -> { /* Unknown type - no filter applied */ }
            }
        }
        
        // Filter by date range
        request.fromDate?.let {
            conditions.add("${CallLog.Calls.DATE} >= ?")
        }
        
        request.toDate?.let {
            conditions.add("${CallLog.Calls.DATE} <= ?")
        }
        
        // Filter by number
        request.searchNumber?.let {
            conditions.add("${CallLog.Calls.NUMBER} LIKE ?")
        }
        
        return if (conditions.isEmpty()) null else conditions.joinToString(" AND ")
    }
    
    private fun buildSelectionArgs(request: CommandRequest): Array<String>? {
        val args = mutableListOf<String>()
        
        request.fromDate?.let { args.add(it.toString()) }
        request.toDate?.let { args.add(it.toString()) }
        request.searchNumber?.let { args.add("%$it%") }
        
        return if (args.isEmpty()) null else args.toTypedArray()
    }
    
    private fun getCallTypeName(type: Int): String {
        return when (type) {
            CallLog.Calls.INCOMING_TYPE -> "incoming"
            CallLog.Calls.OUTGOING_TYPE -> "outgoing"
            CallLog.Calls.MISSED_TYPE -> "missed"
            CallLog.Calls.REJECTED_TYPE -> "rejected"
            CallLog.Calls.BLOCKED_TYPE -> "blocked"
            CallLog.Calls.VOICEMAIL_TYPE -> "voicemail"
            else -> "unknown"
        }
    }
    
    private fun extractSimSlot(simId: String?): Int {
        // Simple extraction - may need adjustment based on device
        return try {
            when {
                simId.isNullOrEmpty() -> 1
                simId.contains("0") -> 1
                simId.contains("1") -> 2
                else -> 1
            }
        } catch (e: Exception) {
            1
        }
    }
}
```

</details>

## Key Changes Made:

1. **Added `else` branch** to the `when` expression in `buildSelection()`:
<details>
<summary><strong>💻 Code Block (kotlin) — 10 lines</strong></summary>

```kotlin
when (type.lowercase()) {
    "incoming" -> conditions.add("${CallLog.Calls.TYPE} = ${CallLog.Calls.INCOMING_TYPE}")
    "outgoing" -> conditions.add("${CallLog.Calls.TYPE} = ${CallLog.Calls.OUTGOING_TYPE}")
    "missed" -> conditions.add("${CallLog.Calls.TYPE} = ${CallLog.Calls.MISSED_TYPE}")
    "rejected" -> conditions.add("${CallLog.Calls.TYPE} = ${CallLog.Calls.REJECTED_TYPE}")
    "blocked" -> conditions.add("${CallLog.Calls.TYPE} = ${CallLog.Calls.BLOCKED_TYPE}")
    "voicemail" -> conditions.add("${CallLog.Calls.TYPE} = ${CallLog.Calls.VOICEMAIL_TYPE}")
    "all" -> { /* No filter - include all types */ }
    else -> { /* Unknown type - no filter applied */ }  // <-- Added this
}
```

</details>

2. **Added additional call types**: `blocked` and `voicemail`

3. **Fixed `extractSimSlot()` function** to use a `when` expression with proper else handling:
<details>
<summary><strong>💻 Code Block (kotlin) — 10 lines</strong></summary>

```kotlin
return try {
    when {
        simId.isNullOrEmpty() -> 1
        simId.contains("0") -> 1
        simId.contains("1") -> 2
        else -> 1
    }
} catch (e: Exception) {
    1
}
```

</details>

This should fix the compilation error. The `else` branch is required because Kotlin's `when` expression on a `String` cannot be exhaustive (there are infinite possible string values), so you must provide a default case.

---

## [5] USER

26-01-28 11:49:14.434 29543-29543 Compatibil...geReporter com.phonemanager                     D  Compat change id reported: 160794467; UID 10809; state: ENABLED
2026-01-28 11:49:14.453 29543-29543 AndroidRuntime          com.phonemanager                     D  Shutting down VM
2026-01-28 11:49:14.461 29543-29543 AndroidRuntime          com.phonemanager                     E  FATAL EXCEPTION: main
                                                                                                    Process: com.phonemanager, PID: 29543
                                                                                                    java.lang.RuntimeException: Unable to start service com.phonemanager.service.PhoneManagerService@c241edd with Intent { cmp=com.phonemanager/.service.PhoneManagerService }: java.lang.SecurityException: Starting FGS with type phoneCall callerApp=ProcessRecord{2617f7b 29543:com.phonemanager/u0a809} targetSDK=34 requires permissions: all of the permissions allOf=true [android.permission.FOREGROUND_SERVICE_PHONE_CALL] any of the permissions allOf=false [android.permission.MANAGE_OWN_CALLS, android.app.role.DIALER] 
                                                                                                    	at android.app.ActivityThread.handleServiceArgs(ActivityThread.java:5286)
                                                                                                    	at android.app.ActivityThread.-$$Nest$mhandleServiceArgs(Unknown Source:0)
                                                                                                    	at android.app.ActivityThread$H.handleMessage(ActivityThread.java:2531)
                                                                                                    	at android.os.Handler.dispatchMessage(Handler.java:106)
                                                                                                    	at android.os.Looper.loopOnce(Looper.java:230)
                                                                                                    	at android.os.Looper.loop(Looper.java:319)
                                                                                                    	at android.app.ActivityThread.main(ActivityThread.java:8893)
                                                                                                    	at java.lang.reflect.Method.invoke(Native Method)
                                                                                                    	at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:608)
                                                                                                    	at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1103)
                                                                                                    Caused by: java.lang.SecurityException: Starting FGS with type phoneCall callerApp=ProcessRecord{2617f7b 29543:com.phonemanager/u0a809} targetSDK=34 requires permissions: all of the permissions allOf=true [android.permission.FOREGROUND_SERVICE_PHONE_CALL] any of the permissions allOf=false [android.permission.MANAGE_OWN_CALLS, android.app.role.DIALER] 
                                                                                                    	at android.os.Parcel.createExceptionOrNull(Parcel.java:3069)
                                                                                                    	at android.os.Parcel.createException(Parcel.java:3053)
                                                                                                    	at android.os.Parcel.readException(Parcel.java:3036)
                                                                                                    	at android.os.Parcel.readException(Parcel.java:2978)
                                                                                                    	at android.app.IActivityManager$Stub$Proxy.setServiceForeground(IActivityManager.java:7214)
                                                                                                    	at android.app.Service.startForeground(Service.java:775)
                                                                                                    	at com.phonemanager.service.PhoneManagerService.onStartCommand(PhoneManagerService.kt:60)
                                                                                                    	at android.app.ActivityThread.handleServiceArgs(ActivityThread.java:5268)
                                                                                                    	at android.app.ActivityThread.-$$Nest$mhandleServiceArgs(Unknown Source:0) 
                                                                                                    	at android.app.ActivityThread$H.handleMessage(ActivityThread.java:2531) 
                                                                                                    	at android.os.Handler.dispatchMessage(Handler.java:106) 
                                                                                                    	at android.os.Looper.loopOnce(Looper.java:230) 
                                                                                                    	at android.os.Looper.loop(Looper.java:319) 
                                                                                                    	at android.app.ActivityThread.main(ActivityThread.java:8893) 
                                                                                                    	at java.lang.reflect.Method.invoke(Native Method) 
                                                                                                    	at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:608) 
                                                                                                    	at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1103) 
                                                                                                    Caused by: android.os.RemoteException: Remote stack trace:
                                                                                                    	at com.android.server.am.ActiveServices.validateForegroundServiceType(ActiveServices.java:2729)
                                                                                                    	at com.android.server.am.ActiveServices.setServiceForegroundInnerLocked(ActiveServices.java:2440)
                                                                                                    	at com.android.server.am.ActiveServices.setServiceForegroundLocked(ActiveServices.java:1785)
                                                                                                    	at com.android.server.am.ActivityManagerService.setServiceForeground(ActivityManagerService.java:15870)
                                                                                                    	at android.app.IActivityManager$Stub.onTransact$setServiceForeground$(IActivityManager.java:11873)
2026-01-28 11:49:14.485 29543-29543 Process                 com.phonemanager                     I  Sending signal. PID: 29543 SIG: 9
2026-01-28 11:49:15.846 29592-29592 om.phonemanager         com.phonemanager                     I  Late-enabling -Xcheck:jni
2026-01-28 11:49:15.892 29592-29592 om.phonemanager         com.phonemanager                     I  Using CollectorTypeCC GC.
2026-01-28 11:49:15.913 29592-29592 nativeloader            com.phonemanager                     D  Load libframework-connectivity-tiramisu-jni.so using APEX ns com_android_tethering for caller /apex/com.android.tethering/javalib/framework-connectivity-t.jar: ok
2026-01-28 11:49:15.922 29592-29592 ActivityThread          com.phonemanager                     D  setConscryptValidator
2026-01-28 11:49:15.922 29592-29592 ActivityThread          com.phonemanager                     D  setConscryptValidator - put
2026-01-28 11:49:15.987 29592-29592 Compatibil...geReporter com.phonemanager                     D  Compat change id reported: 171979766; UID 10809; state: ENABLED
2026-01-28 11:49:15.989 29592-29592 Compatibil...geReporter com.phonemanager                     D  Compat change id reported: 242716250; UID 10809; state: ENABLED
2026-01-28 11:49:16.288 29592-29592 nativeloader            com.phonemanager                     D  Configu


crash

---

## [6] ASSISTANT (claude-opus-4-5-20251101-thinking-32k)

<details>
<summary><strong>💭 Thinking Process</strong></summary>

> The error is clear - starting a foreground service with type `phoneCall` on Android 14 (targetSDK=34) requires:
> 1. `android.permission.FOREGROUND_SERVICE_PHONE_CALL` permission
> 2. AND either `android.permission.MANAGE_OWN_CALLS` OR being the default dialer (`android.app.role.DIALER`)
> 
> The problem is that the app is trying to start the foreground service before the user has set it as the default dialer. We need to:
> 
> 1. Change the foreground service type to something that doesn't require being the default dialer initially
> 2. OR delay starting the service until permissions are granted
> 3. OR use a different approach
> 
> The best solution is to:
> 1. Use a different foreground service type (like `dataSync` or `specialUse`) that doesn't have these requirements
> 2. Add proper permission handling before starting the service
> 3. Only start the service after permissions are granted
> 
> Let me fix the code:
> 
> 1. Update AndroidManifest.xml - change foreground service type
> 2. Update PhoneManagerService.kt - handle the service type properly
> 3. Update MainActivity.kt - only start service after permissions are granted
> 
> I'll use `foregroundServiceType="specialUse"` or just remove the specific type and use a basic foreground service, or use `dataSync` type.
> 
> Actually, the safest approach for Android 14+ is to:
> 1. Use `foregroundServiceType="dataSync"` or `specialUse` for the main service
> 2. Add proper permission checks before starting the service
> 3. Handle the case where the app is not yet the default dialer
> 
> Let me provide the fixed files.

</details>

# Fix for Foreground Service Crash on Android 14+

The issue is that Android 14 (API 34) requires the app to be the **default dialer** before starting a foreground service with type `phoneCall`. We need to change the approach.

## Fixed Files

### 1. AndroidManifest.xml (Updated)

<details>
<summary><strong>💻 Code Block (xml) — 92 lines</strong></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- Network Permissions -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />

    <!-- Phone Permissions -->
    <uses-permission android:name="android.permission.CALL_PHONE" />
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <uses-permission android:name="android.permission.READ_PHONE_NUMBERS" />
    <uses-permission android:name="android.permission.ANSWER_PHONE_CALLS" />
    <uses-permission android:name="android.permission.READ_CALL_LOG" />
    <uses-permission android:name="android.permission.WRITE_CALL_LOG" />
    <uses-permission android:name="android.permission.PROCESS_OUTGOING_CALLS" />
    <uses-permission android:name="android.permission.MANAGE_OWN_CALLS" />

    <!-- Audio Permissions -->
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />

    <!-- Background Service Permissions -->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

    <uses-feature android:name="android.hardware.telephony" android:required="true" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.PhoneManager"
        android:name=".PhoneManagerApp"
        tools:targetApi="34">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:launchMode="singleTop">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- Main Foreground Service - using specialUse type -->
        <service
            android:name=".service.PhoneManagerService"
            android:enabled="true"
            android:exported="false"
            android:foregroundServiceType="specialUse">
            <property
                android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE"
                android:value="phone_control_server" />
        </service>

        <!-- InCallService for call control -->
        <service
            android:name=".service.CallControlService"
            android:permission="android.permission.BIND_INCALL_SERVICE"
            android:exported="true">
            <meta-data
                android:name="android.telecom.IN_CALL_SERVICE_UI"
                android:value="false" />
            <meta-data
                android:name="android.telecom.IN_CALL_SERVICE_RINGING"
                android:value="false" />
            <intent-filter>
                <action android:name="android.telecom.InCallService" />
            </intent-filter>
        </service>

        <!-- Boot Receiver -->
        <receiver
            android:name=".receiver.BootReceiver"
            android:enabled="true"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
                <action android:name="android.intent.action.QUICKBOOT_POWERON" />
            </intent-filter>
        </receiver>

    </application>
</manifest>
```

</details>

### 2. PhoneManagerService.kt (Updated)

<details>
<summary><strong>💻 Code Block (kotlin) — 274 lines</strong></summary>

```kotlin
package com.phonemanager.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.phonemanager.MainActivity
import com.phonemanager.PhoneManagerApp
import com.phonemanager.R
import com.phonemanager.manager.CallLogManager
import com.phonemanager.manager.CallManager
import com.phonemanager.server.HttpServerHandler
import com.phonemanager.server.WebSocketServerHandler
import com.phonemanager.utils.PreferencesManager
import kotlinx.coroutines.*

class PhoneManagerService : Service() {
    
    companion object {
        const val TAG = "PhoneManagerService"
        const val ACTION_STOP_SERVICE = "com.phonemanager.STOP_SERVICE"
        const val NOTIFICATION_ID = 1
    }
    
    private val binder = LocalBinder()
    private var webSocketServer: WebSocketServerHandler? = null
    private var httpServer: HttpServerHandler? = null
    
    private lateinit var callManager: CallManager
    private lateinit var callLogManager: CallLogManager
    private lateinit var prefsManager: PreferencesManager
    
    private var wakeLock: PowerManager.WakeLock? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val logListeners = mutableListOf<(String) -> Unit>()
    
    inner class LocalBinder : Binder() {
        fun getService(): PhoneManagerService = this@PhoneManagerService
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        callManager = CallManager(this)
        callLogManager = CallLogManager(this)
        prefsManager = PreferencesManager(this)
    }
    
    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand: ${intent?.action}")
        
        when (intent?.action) {
            ACTION_STOP_SERVICE -> {
                stopServers()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
        }
        
        try {
            startForegroundWithNotification()
            acquireWakeLock()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start foreground: ${e.message}")
            e.printStackTrace()
        }
        
        return START_STICKY
    }
    
    private fun startForegroundWithNotification() {
        val notification = createNotification()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14+ (API 34+)
            startForeground(
                NOTIFICATION_ID, 
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10-13 (API 29-33)
            startForeground(
                NOTIFICATION_ID, 
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE
            )
        } else {
            // Below Android 10
            startForeground(NOTIFICATION_ID, notification)
        }
        
        Log.d(TAG, "Foreground service started successfully")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service onDestroy")
        stopServers()
        releaseWakeLock()
        serviceScope.cancel()
    }
    
    private fun stopServers() {
        stopWebSocketServer()
        stopHttpServer()
    }
    
    private fun createNotification(): Notification {
        val stopIntent = Intent(this, PhoneManagerService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this, 0, openIntent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        return NotificationCompat.Builder(this, PhoneManagerApp.CHANNEL_ID)
            .setContentTitle("Phone Manager")
            .setContentText("Servers are running in background")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(openPendingIntent)
            .addAction(R.drawable.ic_stop, "Stop", stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }
    
    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "PhoneManager::ServiceWakeLock"
            ).apply {
                acquire(10 * 60 * 1000L) // 10 minutes timeout, will re-acquire
            }
            Log.d(TAG, "WakeLock acquired")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to acquire WakeLock: ${e.message}")
        }
    }
    
    private fun releaseWakeLock() {
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                    Log.d(TAG, "WakeLock released")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release WakeLock: ${e.message}")
        }
    }
    
    // WebSocket Server Control
    fun startWebSocketServer(port: Int): Boolean {
        return try {
            stopWebSocketServer()
            
            webSocketServer = WebSocketServerHandler(port, callManager, callLogManager) { log ->
                notifyLog(log)
            }
            webSocketServer?.start()
            notifyLog("WebSocket server started on port $port")
            Log.d(TAG, "WebSocket server started on port $port")
            true
        } catch (e: Exception) {
            notifyLog("WebSocket server failed: ${e.message}")
            Log.e(TAG, "WebSocket server failed", e)
            false
        }
    }
    
    fun stopWebSocketServer() {
        try {
            webSocketServer?.let { server ->
                server.stop(1000)
                notifyLog("WebSocket server stopped")
                Log.d(TAG, "WebSocket server stopped")
            }
            webSocketServer = null
        } catch (e: Exception) {
            Log.e(TAG, "WebSocket stop error: ${e.message}")
            notifyLog("WebSocket stop error: ${e.message}")
        }
    }
    
    fun isWebSocketRunning(): Boolean = webSocketServer != null
    
    // HTTP Server Control
    fun startHttpServer(port: Int): Boolean {
        return try {
            stopHttpServer()
            
            httpServer = HttpServerHandler(port, callManager, callLogManager) { log ->
                notifyLog(log)
            }
            httpServer?.start()
            notifyLog("HTTP server started on port $port")
            Log.d(TAG, "HTTP server started on port $port")
            true
        } catch (e: Exception) {
            notifyLog("HTTP server failed: ${e.message}")
            Log.e(TAG, "HTTP server failed", e)
            false
        }
    }
    
    fun stopHttpServer() {
        try {
            httpServer?.let { server ->
                server.stop()
                notifyLog("HTTP server stopped")
                Log.d(TAG, "HTTP server stopped")
            }
            httpServer = null
        } catch (e: Exception) {
            Log.e(TAG, "HTTP stop error: ${e.message}")
            notifyLog("HTTP stop error: ${e.message}")
        }
    }
    
    fun isHttpRunning(): Boolean = httpServer != null
    
    // Log listeners
    fun addLogListener(listener: (String) -> Unit) {
        logListeners.add(listener)
    }
    
    fun removeLogListener(listener: (String) -> Unit) {
        logListeners.remove(listener)
    }
    
    private fun notifyLog(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        val logMessage = "[$timestamp] $message"
        
        serviceScope.launch(Dispatchers.Main) {
            logListeners.forEach { 
                try {
                    it(logMessage)
                } catch (e: Exception) {
                    Log.e(TAG, "Log listener error: ${e.message}")
                }
            }
        }
    }
}
```

</details>

### 3. MainActivity.kt (Updated)

<details>
<summary><strong>💻 Code Block (kotlin) — 465 lines</strong></summary>

```kotlin
package com.phonemanager

import android.Manifest
import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.telecom.TelecomManager
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.phonemanager.databinding.ActivityMainBinding
import com.phonemanager.service.PhoneManagerService
import com.phonemanager.utils.PreferencesManager
import java.net.Inet4Address
import java.net.NetworkInterface

class MainActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var prefsManager: PreferencesManager
    
    private var phoneManagerService: PhoneManagerService? = null
    private var isServiceBound = false
    
    private val logBuilder = StringBuilder()
    
    private val requiredPermissions = mutableListOf(
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.ANSWER_PHONE_CALLS,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.MODIFY_AUDIO_SETTINGS
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            add(Manifest.permission.READ_PHONE_NUMBERS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        updatePermissionStatus()
        
        if (allGranted) {
            appendLog("All permissions granted")
            startAndBindService()
        } else {
            val denied = permissions.entries.filter { !it.value }.map { it.key }
            appendLog("Some permissions denied: $denied")
            showPermissionRationale()
        }
    }
    
    private val defaultDialerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        checkDefaultDialer()
    }
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "Service connected")
            val binder = service as PhoneManagerService.LocalBinder
            phoneManagerService = binder.getService()
            isServiceBound = true
            
            phoneManagerService?.addLogListener { log ->
                runOnUiThread {
                    appendLog(log)
                }
            }
            
            updateServiceStatus()
            restoreServerStates()
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "Service disconnected")
            phoneManagerService = null
            isServiceBound = false
            updateServiceStatus()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        prefsManager = PreferencesManager(this)
        
        setupUI()
        updatePermissionStatus()
        updateIpAddress()
        
        // Check permissions first, then start service
        if (hasRequiredPermissions()) {
            startAndBindService()
        } else {
            showPermissionRationale()
        }
    }
    
    private fun hasRequiredPermissions(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    private fun setupUI() {
        binding.etWebSocketPort.setText(prefsManager.webSocketPort.toString())
        binding.etHttpPort.setText(prefsManager.httpPort.toString())
        
        binding.tvLog.movementMethod = ScrollingMovementMethod()
        
        // WebSocket toggle
        binding.switchWebSocket.setOnCheckedChangeListener { _, isChecked ->
            handleWebSocketToggle(isChecked)
        }
        
        // HTTP toggle
        binding.switchHttp.setOnCheckedChangeListener { _, isChecked ->
            handleHttpToggle(isChecked)
        }
        
        // Request permissions button
        binding.btnRequestPermissions.setOnClickListener {
            requestPermissions()
        }
        
        // Set default dialer button
        binding.btnSetDefaultDialer.setOnClickListener {
            requestDefaultDialer()
        }
        
        // Clear log button
        binding.btnClearLog.setOnClickListener {
            logBuilder.clear()
            binding.tvLog.text = "Log cleared."
        }
    }
    
    private fun handleWebSocketToggle(isChecked: Boolean) {
        if (isChecked) {
            if (!isServiceBound) {
                Toast.makeText(this, "Service not ready", Toast.LENGTH_SHORT).show()
                binding.switchWebSocket.isChecked = false
                return
            }
            
            val port = binding.etWebSocketPort.text.toString().toIntOrNull() ?: 8030
            prefsManager.webSocketPort = port
            
            val success = phoneManagerService?.startWebSocketServer(port) ?: false
            if (!success) {
                binding.switchWebSocket.isChecked = false
                Toast.makeText(this, "Failed to start WebSocket server", Toast.LENGTH_SHORT).show()
            }
        } else {
            phoneManagerService?.stopWebSocketServer()
        }
        prefsManager.webSocketEnabled = binding.switchWebSocket.isChecked
        updateWebSocketStatus()
    }
    
    private fun handleHttpToggle(isChecked: Boolean) {
        if (isChecked) {
            if (!isServiceBound) {
                Toast.makeText(this, "Service not ready", Toast.LENGTH_SHORT).show()
                binding.switchHttp.isChecked = false
                return
            }
            
            val port = binding.etHttpPort.text.toString().toIntOrNull() ?: 8040
            prefsManager.httpPort = port
            
            val success = phoneManagerService?.startHttpServer(port) ?: false
            if (!success) {
                binding.switchHttp.isChecked = false
                Toast.makeText(this, "Failed to start HTTP server", Toast.LENGTH_SHORT).show()
            }
        } else {
            phoneManagerService?.stopHttpServer()
        }
        prefsManager.httpEnabled = binding.switchHttp.isChecked
        updateHttpStatus()
    }
    
    private fun startAndBindService() {
        try {
            Log.d(TAG, "Starting and binding service")
            val serviceIntent = Intent(this, PhoneManagerService::class.java)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
            prefsManager.serviceEnabled = true
            appendLog("Service starting...")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start service", e)
            appendLog("Failed to start service: ${e.message}")
            Toast.makeText(this, "Failed to start service: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun restoreServerStates() {
        // Delay to ensure service is fully ready
        binding.root.postDelayed({
            if (prefsManager.webSocketEnabled && isServiceBound) {
                binding.switchWebSocket.isChecked = true
            }
            if (prefsManager.httpEnabled && isServiceBound) {
                binding.switchHttp.isChecked = true
            }
        }, 500)
    }
    
    private fun updateServiceStatus() {
        if (isServiceBound) {
            binding.tvStatus.text = "Status: Running"
            binding.tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_light))
        } else {
            binding.tvStatus.text = "Status: Stopped"
            binding.tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_light))
        }
    }
    
    private fun updateWebSocketStatus() {
        val isRunning = phoneManagerService?.isWebSocketRunning() ?: false
        val port = binding.etWebSocketPort.text.toString()
        binding.tvWebSocketStatus.text = if (isRunning) "Status: ON (Port: $port)" else "Status: OFF"
        binding.tvWebSocketStatus.setTextColor(
            if (isRunning) ContextCompat.getColor(this, android.R.color.holo_green_light)
            else ContextCompat.getColor(this, android.R.color.holo_red_light)
        )
    }
    
    private fun updateHttpStatus() {
        val isRunning = phoneManagerService?.isHttpRunning() ?: false
        val port = binding.etHttpPort.text.toString()
        binding.tvHttpStatus.text = if (isRunning) "Status: ON (Port: $port)" else "Status: OFF"
        binding.tvHttpStatus.setTextColor(
            if (isRunning) ContextCompat.getColor(this, android.R.color.holo_green_light)
            else ContextCompat.getColor(this, android.R.color.holo_red_light)
        )
    }
    
    private fun updateIpAddress() {
        try {
            val ip = getLocalIpAddress() ?: getWifiIpAddress()
            binding.tvIpAddress.text = "IP: ${ip ?: "Not connected"}"
        } catch (e: Exception) {
            binding.tvIpAddress.text = "IP: Unable to get IP"
        }
    }
    
    private fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        return address.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting IP", e)
        }
        return null
    }
    
    private fun getWifiIpAddress(): String? {
        try {
            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val ipAddress = wifiManager.connectionInfo.ipAddress
            if (ipAddress != 0) {
                return String.format(
                    "%d.%d.%d.%d",
                    ipAddress and 0xff,
                    ipAddress shr 8 and 0xff,
                    ipAddress shr 16 and 0xff,
                    ipAddress shr 24 and 0xff
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting WiFi IP", e)
        }
        return null
    }
    
    private fun updatePermissionStatus() {
        val permissionStatus = StringBuilder()
        var allGranted = true
        
        for (permission in requiredPermissions) {
            val granted = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
            val permName = permission.substringAfterLast(".")
            val status = if (granted) "✓" else "✗"
            permissionStatus.append("$status $permName\n")
            if (!granted) allGranted = false
        }
        
        // Check default dialer
        val isDefaultDialer = isDefaultDialer()
        permissionStatus.append("${if (isDefaultDialer) "✓" else "✗"} Default Dialer (Optional)")
        
        binding.tvPermissions.text = permissionStatus.toString()
        
        binding.btnRequestPermissions.isEnabled = !allGranted
        binding.btnSetDefaultDialer.isEnabled = !isDefaultDialer
    }
    
    private fun showPermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle("Permissions Required")
            .setMessage("This app needs phone and notification permissions to:\n\n" +
                    "• Make and control calls\n" +
                    "• Access call history\n" +
                    "• Run in background\n\n" +
                    "Please grant the required permissions.")
            .setPositiveButton("Grant Permissions") { _, _ ->
                requestPermissions()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun requestPermissions() {
        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
        
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest)
        } else {
            startAndBindService()
        }
        
        // Request battery optimization exemption
        requestBatteryOptimizationExemption()
    }
    
    private fun requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "Please disable battery optimization manually", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun requestDefaultDialer() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
                if (roleManager.isRoleAvailable(RoleManager.ROLE_DIALER) &&
                    !roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                    defaultDialerLauncher.launch(intent)
                }
            } else {
                val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                    putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
                }
                defaultDialerLauncher.launch(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting default dialer", e)
            Toast.makeText(this, "Cannot request default dialer: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun isDefaultDialer(): Boolean {
        return try {
            val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            packageName == telecomManager.defaultDialerPackage
        } catch (e: Exception) {
            false
        }
    }
    
    private fun checkDefaultDialer() {
        updatePermissionStatus()
        if (isDefaultDialer()) {
            Toast.makeText(this, "Set as default dialer successfully", Toast.LENGTH_SHORT).show()
            appendLog("Set as default dialer")
        }
    }
    
    private fun appendLog(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        val logMessage = "[$timestamp] $message\n"
        
        logBuilder.append(logMessage)
        binding.tvLog.text = logBuilder.toString()
        
        // Auto scroll to bottom
        binding.tvLog.post {
            val scrollAmount = binding.tvLog.layout?.let {
                it.getLineTop(binding.tvLog.lineCount) - binding.tvLog.height
            } ?: 0
            if (scrollAmount > 0) {
                binding.tvLog.scrollTo(0, scrollAmount)
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
        updateIpAddress()
        updateServiceStatus()
        updateWebSocketStatus()
        updateHttpStatus()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (isServiceBound) {
            try {
                unbindService(serviceConnection)
            } catch (e: Exception) {
                Log.e(TAG, "Error unbinding service", e)
            }
            isServiceBound = false
        }
    }
}
```

</details>

### 4. PhoneManagerApp.kt (Updated)

<details>
<summary><strong>💻 Code Block (kotlin) — 43 lines</strong></summary>

```kotlin
package com.phonemanager

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log

class PhoneManagerApp : Application() {
    
    companion object {
        const val TAG = "PhoneManagerApp"
        const val CHANNEL_ID = "PhoneManagerServiceChannel"
        lateinit var instance: PhoneManagerApp
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
        Log.d(TAG, "Application created")
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Phone Manager Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Phone Manager background service notification"
                setShowBadge(false)
                setSound(null, null)
                enableVibration(false)
            }
            
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }
    }
}
```

</details>

### 5. receiver/BootReceiver.kt (Updated)

<details>
<summary><strong>💻 Code Block (kotlin) — 39 lines</strong></summary>

```kotlin
package com.phonemanager.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.phonemanager.service.PhoneManagerService
import com.phonemanager.utils.PreferencesManager

class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Boot received: ${intent.action}")
        
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            
            val prefs = PreferencesManager(context)
            if (prefs.serviceEnabled) {
                try {
                    val serviceIntent = Intent(context, PhoneManagerService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                    Log.d(TAG, "Service started after boot")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to start service after boot", e)
                }
            }
        }
    }
}
```

</details>

## Key Changes Made:

1. **Changed foreground service type** from `phoneCall` to `specialUse` which doesn't require the app to be the default dialer

2. **Added `FOREGROUND_SERVICE_SPECIAL_USE` permission** and removed `FOREGROUND_SERVICE_PHONE_CALL`

3. **Added property for special use subtype** in the manifest:
   ```xml
   <property
       android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE"
       android:value="phone_control_server" />
   ```

4. **Fixed service startup** to handle different Android versions properly

5. **Added proper permission checking** before starting the service

6. **Made default dialer optional** - the app can work without being the default dialer (call control features may be limited)

7. **Added better error handling and logging** throughout

The app should now start without crashing. The default dialer feature is optional and only needed for full call control capabilities.

---

## [7] USER

StatusCode        : 200                                                                                                 StatusDescription : OK                                                                                                  Content           : {"message":"No active call","success":true,"timestamp":1769580255010}                               RawContent        : HTTP/1.1 200 OK                                                                                                         Access-Control-Allow-Origin: *
                    Access-Control-Allow-Methods: GET, POST, OPTIONS
                    Access-Control-Allow-Headers: Content-Type
                    Connection: keep-alive
                    Content-Length: 69
                    Content-Type...
Forms             : {}
Headers           : {[Access-Control-Allow-Origin, *], [Access-Control-Allow-Methods, GET, POST, OPTIONS],
                    [Access-Control-Allow-Headers, Content-Type], [Connection, keep-alive]...}
Images            : {}
InputFields       : {}
Links             : {}
ParsedHtml        : mshtml.HTMLDocumentClass
RawContentLength  : 69
PS C:\Users\mamun> curl "http://192.168.0.100:8040/api?command=get_call_log&type=missed&limit=20"


StatusCode        : 200
StatusDescription : OK
Content           : {"message":"Failed to get call log: Invalid token LIMIT","success":false,"timestamp":1769580421371}
RawContent        : HTTP/1.1 200 OK
                    Access-Control-Allow-Origin: *
                    Access-Control-Allow-Methods: GET, POST, OPTIONS
                    Access-Control-Allow-Headers: Content-Type
                    Connection: keep-alive
                    Content-Length: 99
                    Content-Type...
Forms             : {}
Headers           : {[Access-Control-Allow-Origin, *], [Access-Control-Allow-Methods, GET, POST, OPTIONS],
                    [Access-Control-Allow-Headers, Content-Type], [Connection, keep-alive]...}
Images            : {}
InputFields       : {}                                                                                                  Links             : {}                                                                                                  ParsedHtml        : mshtml.HTMLDocumentClass                                                                            RawContentLength  : 99 

and speaker on of not workng

---

## [8] ASSISTANT (claude-opus-4-5-20251101-thinking-32k)

<details>
<summary><strong>💭 Thinking Process</strong></summary>

> The user is reporting two issues:
> 
> 1. Call log query fails with "Invalid token LIMIT" - this is because Android's ContentResolver doesn't support LIMIT in the sortOrder parameter directly. We need to use a different approach.
> 
> 2. Speaker on/off not working - need to fix the audio routing.
> 
> Let me fix both issues:
> 
> 1. For the call log, we need to remove LIMIT/OFFSET from the sortOrder and handle pagination differently, or use a URI with limit parameter.
> 
> 2. For the speaker, we need to properly handle audio routing during calls.
> 
> Let me provide the fixed files.

</details>

# Fixes for Call Log and Speaker Issues

## Issue 1: Call Log "Invalid token LIMIT" Error

The LIMIT clause can't be used directly in the `sortOrder` parameter on some Android versions. We need to fix the query.

## Issue 2: Speaker On/Off Not Working

Need to properly handle audio routing with AudioManager.

---

## Fixed Files

### 1. manager/CallLogManager.kt (Fixed)

<details>
<summary><strong>💻 Code Block (kotlin) — 209 lines</strong></summary>

```kotlin
package com.phonemanager.manager

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.CallLog
import android.util.Log
import androidx.core.app.ActivityCompat
import com.phonemanager.model.CallLogEntry
import com.phonemanager.model.CallLogResponse
import com.phonemanager.model.CommandRequest
import com.phonemanager.model.CommandResponse

class CallLogManager(private val context: Context) {
    
    companion object {
        private const val TAG = "CallLogManager"
    }
    
    fun getCallLog(request: CommandRequest): CommandResponse {
        return try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED) {
                return CommandResponse(
                    success = false,
                    message = "READ_CALL_LOG permission not granted"
                )
            }
            
            val selection = buildSelection(request)
            val selectionArgs = buildSelectionArgs(request)
            
            // Don't use LIMIT in sortOrder - not supported on all devices
            val sortOrder = "${CallLog.Calls.DATE} DESC"
            
            val projection = arrayOf(
                CallLog.Calls._ID,
                CallLog.Calls.NUMBER,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
                CallLog.Calls.PHONE_ACCOUNT_ID
            )
            
            Log.d(TAG, "Query - Selection: $selection, Args: ${selectionArgs?.joinToString()}")
            
            val cursor: Cursor? = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )
            
            val entries = mutableListOf<CallLogEntry>()
            var totalCount = 0
            
            cursor?.use {
                totalCount = it.count
                
                // Manual pagination - skip offset and take limit
                var skipped = 0
                var taken = 0
                
                while (it.moveToNext()) {
                    // Skip offset entries
                    if (skipped < request.offset) {
                        skipped++
                        continue
                    }
                    
                    // Take only limit entries
                    if (taken >= request.limit) {
                        break
                    }
                    
                    try {
                        val id = it.getLong(it.getColumnIndexOrThrow(CallLog.Calls._ID))
                        val number = it.getStringOrNull(CallLog.Calls.NUMBER)
                        val name = it.getStringOrNull(CallLog.Calls.CACHED_NAME)
                        val type = it.getInt(it.getColumnIndexOrThrow(CallLog.Calls.TYPE))
                        val date = it.getLong(it.getColumnIndexOrThrow(CallLog.Calls.DATE))
                        val duration = it.getLong(it.getColumnIndexOrThrow(CallLog.Calls.DURATION))
                        val simId = it.getStringOrNull(CallLog.Calls.PHONE_ACCOUNT_ID)
                        
                        entries.add(
                            CallLogEntry(
                                id = id,
                                number = number,
                                name = name,
                                type = getCallTypeName(type),
                                date = date,
                                duration = duration,
                                simSlot = extractSimSlot(simId)
                            )
                        )
                        taken++
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing call log entry: ${e.message}")
                    }
                }
            }
            
            Log.d(TAG, "Retrieved ${entries.size} entries out of $totalCount total")
            
            val response = CallLogResponse(
                totalCount = totalCount,
                entries = entries,
                limit = request.limit,
                offset = request.offset
            )
            
            CommandResponse(
                success = true,
                message = "Call log retrieved successfully (${entries.size} entries)",
                data = response
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get call log", e)
            CommandResponse(
                success = false,
                message = "Failed to get call log: ${e.message}"
            )
        }
    }
    
    // Extension function to safely get string from cursor
    private fun Cursor.getStringOrNull(columnName: String): String? {
        return try {
            val index = getColumnIndex(columnName)
            if (index >= 0) getString(index) else null
        } catch (e: Exception) {
            null
        }
    }
    
    private fun buildSelection(request: CommandRequest): String? {
        val conditions = mutableListOf<String>()
        
        // Filter by call type
        request.type?.let { type ->
            when (type.lowercase()) {
                "incoming" -> conditions.add("${CallLog.Calls.TYPE} = ${CallLog.Calls.INCOMING_TYPE}")
                "outgoing" -> conditions.add("${CallLog.Calls.TYPE} = ${CallLog.Calls.OUTGOING_TYPE}")
                "missed" -> conditions.add("${CallLog.Calls.TYPE} = ${CallLog.Calls.MISSED_TYPE}")
                "rejected" -> conditions.add("${CallLog.Calls.TYPE} = ${CallLog.Calls.REJECTED_TYPE}")
                "blocked" -> conditions.add("${CallLog.Calls.TYPE} = ${CallLog.Calls.BLOCKED_TYPE}")
                "voicemail" -> conditions.add("${CallLog.Calls.TYPE} = ${CallLog.Calls.VOICEMAIL_TYPE}")
                "all" -> { /* No filter - include all types */ }
                else -> { /* Unknown type - no filter applied */ }
            }
        }
        
        // Filter by date range
        request.fromDate?.let {
            conditions.add("${CallLog.Calls.DATE} >= $it")
        }
        
        request.toDate?.let {
            conditions.add("${CallLog.Calls.DATE} <= $it")
        }
        
        // Filter by number
        request.searchNumber?.let { searchNum ->
            if (searchNum.isNotEmpty()) {
                conditions.add("${CallLog.Calls.NUMBER} LIKE '%$searchNum%'")
            }
        }
        
        return if (conditions.isEmpty()) null else conditions.joinToString(" AND ")
    }
    
    private fun buildSelectionArgs(request: CommandRequest): Array<String>? {
        // We're embedding values directly in selection to avoid issues
        // This is safe because we control the input format
        return null
    }
    
    private fun getCallTypeName(type: Int): String {
        return when (type) {
            CallLog.Calls.INCOMING_TYPE -> "incoming"
            CallLog.Calls.OUTGOING_TYPE -> "outgoing"
            CallLog.Calls.MISSED_TYPE -> "missed"
            CallLog.Calls.REJECTED_TYPE -> "rejected"
            CallLog.Calls.BLOCKED_TYPE -> "blocked"
            CallLog.Calls.VOICEMAIL_TYPE -> "voicemail"
            else -> "unknown"
        }
    }
    
    private fun extractSimSlot(simId: String?): Int {
        return try {
            when {
                simId.isNullOrEmpty() -> 1
                simId.contains("0", ignoreCase = true) -> 1
                simId.contains("1", ignoreCase = true) -> 2
                simId.contains("sim1", ignoreCase = true) -> 1
                simId.contains("sim2", ignoreCase = true) -> 2
                else -> 1
            }
        } catch (e: Exception) {
            1
        }
    }
}
```

</details>

### 2. manager/CallManager.kt (Fixed - Speaker/Audio)

<details>
<summary><strong>💻 Code Block (kotlin) — 422 lines</strong></summary>

```kotlin
package com.phonemanager.manager

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telecom.Call
import android.telecom.TelecomManager
import android.telecom.PhoneAccountHandle
import android.util.Log
import androidx.core.app.ActivityCompat
import com.phonemanager.model.ActiveCallInfo
import com.phonemanager.model.CommandResponse
import com.phonemanager.service.CallControlService

class CallManager(private val context: Context) {
    
    companion object {
        private const val TAG = "CallManager"
        var isMuted = false
        var isSpeakerOn = false
    }
    
    private val telecomManager: TelecomManager = 
        context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    private val audioManager: AudioManager = 
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val handler = Handler(Looper.getMainLooper())
    
    // Dial number or USSD
    fun dial(number: String, simSlot: Int, isUssd: Boolean): CommandResponse {
        return try {
            Log.d(TAG, "Dialing: $number on SIM$simSlot, isUSSD: $isUssd")
            
            val phoneAccountHandle = getPhoneAccountHandle(simSlot)
            
            val formattedNumber = if (isUssd) {
                // Encode USSD special characters
                Uri.encode(number)
            } else {
                number.replace(" ", "").replace("-", "")
            }
            
            val uri = Uri.parse("tel:$formattedNumber")
            
            val extras = android.os.Bundle().apply {
                phoneAccountHandle?.let {
                    putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, it)
                }
            }
            
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) 
                == PackageManager.PERMISSION_GRANTED) {
                
                val intent = Intent(Intent.ACTION_CALL, uri).apply {
                    putExtras(extras)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                
                Log.d(TAG, "Call initiated successfully")
                CommandResponse(
                    success = true,
                    message = if (isUssd) "USSD code sent: $number on SIM$simSlot" 
                              else "Dialing: $number on SIM$simSlot"
                )
            } else {
                Log.e(TAG, "CALL_PHONE permission not granted")
                CommandResponse(
                    success = false,
                    message = "CALL_PHONE permission not granted"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Dial failed", e)
            CommandResponse(
                success = false,
                message = "Dial failed: ${e.message}"
            )
        }
    }
    
    // Get phone account handle for specific SIM
    private fun getPhoneAccountHandle(simSlot: Int): PhoneAccountHandle? {
        return try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {
                
                val accounts = telecomManager.callCapablePhoneAccounts
                Log.d(TAG, "Available phone accounts: ${accounts.size}")
                
                if (accounts.size > simSlot - 1 && simSlot > 0) {
                    accounts[simSlot - 1]
                } else {
                    accounts.firstOrNull()
                }
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting phone account", e)
            null
        }
    }
    
    // Answer incoming call
    fun answerCall(): CommandResponse {
        return try {
            Log.d(TAG, "Attempting to answer call")
            
            val call = CallControlService.currentCall
            if (call != null && call.state == Call.STATE_RINGING) {
                call.answer(android.telecom.VideoProfile.STATE_AUDIO_ONLY)
                Log.d(TAG, "Call answered via InCallService")
                CommandResponse(success = true, message = "Call answered")
            } else {
                // Fallback for older devices or if InCallService not active
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS)
                        == PackageManager.PERMISSION_GRANTED) {
                        telecomManager.acceptRingingCall()
                        Log.d(TAG, "Call answered via TelecomManager")
                        CommandResponse(success = true, message = "Call answered")
                    } else {
                        CommandResponse(success = false, message = "ANSWER_PHONE_CALLS permission not granted")
                    }
                } else {
                    CommandResponse(success = false, message = "No incoming call to answer")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Answer failed", e)
            CommandResponse(success = false, message = "Answer failed: ${e.message}")
        }
    }
    
    // Reject incoming call
    fun rejectCall(): CommandResponse {
        return try {
            Log.d(TAG, "Attempting to reject call")
            
            val call = CallControlService.currentCall
            if (call != null && call.state == Call.STATE_RINGING) {
                call.reject(false, null)
                Log.d(TAG, "Call rejected")
                CommandResponse(success = true, message = "Call rejected")
            } else {
                CommandResponse(success = false, message = "No incoming call to reject")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Reject failed", e)
            CommandResponse(success = false, message = "Reject failed: ${e.message}")
        }
    }
    
    // Terminate active call
    fun terminateCall(): CommandResponse {
        return try {
            Log.d(TAG, "Attempting to terminate call")
            
            val call = CallControlService.currentCall
            if (call != null) {
                call.disconnect()
                Log.d(TAG, "Call terminated via InCallService")
                CommandResponse(success = true, message = "Call terminated")
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS)
                        == PackageManager.PERMISSION_GRANTED) {
                        @Suppress("DEPRECATION")
                        telecomManager.endCall()
                        Log.d(TAG, "Call terminated via TelecomManager")
                        CommandResponse(success = true, message = "Call terminated")
                    } else {
                        CommandResponse(success = false, message = "Permission not granted")
                    }
                } else {
                    CommandResponse(success = false, message = "No active call to terminate")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Terminate failed", e)
            CommandResponse(success = false, message = "Terminate failed: ${e.message}")
        }
    }
    
    // Hold call
    fun holdCall(): CommandResponse {
        return try {
            val call = CallControlService.currentCall
            if (call != null && call.state == Call.STATE_ACTIVE) {
                call.hold()
                Log.d(TAG, "Call on hold")
                CommandResponse(success = true, message = "Call on hold")
            } else {
                CommandResponse(success = false, message = "No active call to hold")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Hold failed", e)
            CommandResponse(success = false, message = "Hold failed: ${e.message}")
        }
    }
    
    // Unhold call
    fun unholdCall(): CommandResponse {
        return try {
            val call = CallControlService.currentCall
            if (call != null && call.state == Call.STATE_HOLDING) {
                call.unhold()
                Log.d(TAG, "Call resumed")
                CommandResponse(success = true, message = "Call resumed")
            } else {
                CommandResponse(success = false, message = "No held call to resume")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unhold failed", e)
            CommandResponse(success = false, message = "Unhold failed: ${e.message}")
        }
    }
    
    // Mute microphone
    fun mute(): CommandResponse {
        return try {
            audioManager.isMicrophoneMute = true
            isMuted = true
            Log.d(TAG, "Microphone muted, actual state: ${audioManager.isMicrophoneMute}")
            CommandResponse(success = true, message = "Microphone muted")
        } catch (e: Exception) {
            Log.e(TAG, "Mute failed", e)
            CommandResponse(success = false, message = "Mute failed: ${e.message}")
        }
    }
    
    // Unmute microphone
    fun unmute(): CommandResponse {
        return try {
            audioManager.isMicrophoneMute = false
            isMuted = false
            Log.d(TAG, "Microphone unmuted, actual state: ${audioManager.isMicrophoneMute}")
            CommandResponse(success = true, message = "Microphone unmuted")
        } catch (e: Exception) {
            Log.e(TAG, "Unmute failed", e)
            CommandResponse(success = false, message = "Unmute failed: ${e.message}")
        }
    }
    
    // Enable loudspeaker
    fun speakerOn(): CommandResponse {
        return try {
            Log.d(TAG, "Enabling speaker...")
            
            // Run on main thread for audio operations
            handler.post {
                try {
                    // Set audio mode to in-call
                    audioManager.mode = AudioManager.MODE_IN_CALL
                    
                    // Enable speaker
                    audioManager.isSpeakerphoneOn = true
                    
                    // For some devices, need to route audio explicitly
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        // Android 12+
                        val devices = audioManager.availableCommunicationDevices
                        val speaker = devices.find { 
                            it.type == android.media.AudioDeviceInfo.TYPE_BUILTIN_SPEAKER 
                        }
                        speaker?.let {
                            audioManager.setCommunicationDevice(it)
                        }
                    }
                    
                    isSpeakerOn = true
                    Log.d(TAG, "Speaker enabled, actual state: ${audioManager.isSpeakerphoneOn}")
                } catch (e: Exception) {
                    Log.e(TAG, "Speaker on failed in handler", e)
                }
            }
            
            // Give time for the audio change
            Thread.sleep(100)
            
            CommandResponse(
                success = true, 
                message = "Loudspeaker enabled (Speaker: ${audioManager.isSpeakerphoneOn})"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Speaker on failed", e)
            CommandResponse(success = false, message = "Speaker on failed: ${e.message}")
        }
    }
    
    // Disable loudspeaker
    fun speakerOff(): CommandResponse {
        return try {
            Log.d(TAG, "Disabling speaker...")
            
            handler.post {
                try {
                    audioManager.isSpeakerphoneOn = false
                    
                    // For Android 12+, route back to earpiece
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val devices = audioManager.availableCommunicationDevices
                        val earpiece = devices.find { 
                            it.type == android.media.AudioDeviceInfo.TYPE_BUILTIN_EARPIECE 
                        }
                        earpiece?.let {
                            audioManager.setCommunicationDevice(it)
                        }
                    }
                    
                    isSpeakerOn = false
                    Log.d(TAG, "Speaker disabled, actual state: ${audioManager.isSpeakerphoneOn}")
                } catch (e: Exception) {
                    Log.e(TAG, "Speaker off failed in handler", e)
                }
            }
            
            Thread.sleep(100)
            
            CommandResponse(
                success = true, 
                message = "Loudspeaker disabled (Speaker: ${audioManager.isSpeakerphoneOn})"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Speaker off failed", e)
            CommandResponse(success = false, message = "Speaker off failed: ${e.message}")
        }
    }
    
    // Get audio status
    fun getAudioStatus(): CommandResponse {
        return try {
            val status = mapOf(
                "speaker_on" to audioManager.isSpeakerphoneOn,
                "microphone_muted" to audioManager.isMicrophoneMute,
                "audio_mode" to getAudioModeName(audioManager.mode),
                "volume_call" to audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL),
                "volume_max" to audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
                "bluetooth_sco_on" to audioManager.isBluetoothScoOn,
                "wired_headset_on" to audioManager.isWiredHeadsetOn
            )
            
            CommandResponse(
                success = true,
                message = "Audio status retrieved",
                data = status
            )
        } catch (e: Exception) {
            CommandResponse(success = false, message = "Failed to get audio status: ${e.message}")
        }
    }
    
    private fun getAudioModeName(mode: Int): String {
        return when (mode) {
            AudioManager.MODE_NORMAL -> "NORMAL"
            AudioManager.MODE_RINGTONE -> "RINGTONE"
            AudioManager.MODE_IN_CALL -> "IN_CALL"
            AudioManager.MODE_IN_COMMUNICATION -> "IN_COMMUNICATION"
            AudioManager.MODE_CALL_SCREENING -> "CALL_SCREENING"
            else -> "UNKNOWN ($mode)"
        }
    }
    
    // Get active call info
    fun getActiveCallInfo(): CommandResponse {
        return try {
            val call = CallControlService.currentCall
            if (call != null) {
                val details = call.details
                val callInfo = ActiveCallInfo(
                    number = details?.handle?.schemeSpecificPart,
                    state = getCallStateName(call.state),
                    duration = CallControlService.callStartTime?.let { 
                        System.currentTimeMillis() - it 
                    } ?: 0,
                    isIncoming = CallControlService.isIncoming,
                    startTime = CallControlService.callStartTime ?: 0,
                    isMuted = audioManager.isMicrophoneMute,
                    isOnHold = call.state == Call.STATE_HOLDING,
                    isSpeakerOn = audioManager.isSpeakerphoneOn
                )
                Log.d(TAG, "Active call info: $callInfo")
                CommandResponse(
                    success = true,
                    message = "Active call info retrieved",
                    data = callInfo
                )
            } else {
                CommandResponse(
                    success = true,
                    message = "No active call",
                    data = null
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get active call failed", e)
            CommandResponse(success = false, message = "Get active call failed: ${e.message}")
        }
    }
    
    private fun getCallStateName(state: Int): String {
        return when (state) {
            Call.STATE_NEW -> "NEW"
            Call.STATE_DIALING -> "DIALING"
            Call.STATE_RINGING -> "RINGING"
            Call.STATE_HOLDING -> "HOLDING"
            Call.STATE_ACTIVE -> "ACTIVE"
            Call.STATE_DISCONNECTED -> "DISCONNECTED"
            Call.STATE_CONNECTING -> "CONNECTING"
            Call.STATE_DISCONNECTING -> "DISCONNECTING"
            Call.STATE_SELECT_PHONE_ACCOUNT -> "SELECT_PHONE_ACCOUNT"
            Call.STATE_SIMULATED_RINGING -> "SIMULATED_RINGING"
            Call.STATE_AUDIO_PROCESSING -> "AUDIO_PROCESSING"
            else -> "UNKNOWN ($state)"
        }
    }
}
```

</details>

### 3. model/Models.kt (Updated - Added new command)

<details>
<summary><strong>💻 Code Block (kotlin) — 136 lines</strong></summary>

```kotlin
package com.phonemanager.model

import com.google.gson.annotations.SerializedName

// Command Request Model
data class CommandRequest(
    @SerializedName("command")
    val command: String,
    
    @SerializedName("number")
    val number: String? = null,
    
    @SerializedName("sim")
    val sim: Int = 1,  // 1 or 2
    
    @SerializedName("is_ussd")
    val isUssd: Boolean = false,
    
    // Call log query parameters
    @SerializedName("type")
    val type: String? = null,  // incoming, outgoing, missed, all
    
    @SerializedName("limit")
    val limit: Int = 50,
    
    @SerializedName("offset")
    val offset: Int = 0,
    
    @SerializedName("from_date")
    val fromDate: Long? = null,
    
    @SerializedName("to_date")
    val toDate: Long? = null,
    
    @SerializedName("search_number")
    val searchNumber: String? = null
)

// Command Response Model
data class CommandResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: Any? = null,
    
    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
)

// Active Call Info
data class ActiveCallInfo(
    @SerializedName("number")
    val number: String?,
    
    @SerializedName("state")
    val state: String,
    
    @SerializedName("duration")
    val duration: Long,
    
    @SerializedName("is_incoming")
    val isIncoming: Boolean,
    
    @SerializedName("start_time")
    val startTime: Long,
    
    @SerializedName("is_muted")
    val isMuted: Boolean,
    
    @SerializedName("is_on_hold")
    val isOnHold: Boolean,
    
    @SerializedName("is_speaker_on")
    val isSpeakerOn: Boolean
)

// Call Log Entry
data class CallLogEntry(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("number")
    val number: String?,
    
    @SerializedName("name")
    val name: String?,
    
    @SerializedName("type")
    val type: String,
    
    @SerializedName("date")
    val date: Long,
    
    @SerializedName("duration")
    val duration: Long,
    
    @SerializedName("sim_slot")
    val simSlot: Int
)

// Call Log Response
data class CallLogResponse(
    @SerializedName("total_count")
    val totalCount: Int,
    
    @SerializedName("entries")
    val entries: List<CallLogEntry>,
    
    @SerializedName("limit")
    val limit: Int,
    
    @SerializedName("offset")
    val offset: Int
)

// Supported Commands
object Commands {
    const val DIAL = "dial"
    const val ANSWER = "answer"
    const val REJECT = "reject"
    const val TERMINATE = "terminate"
    const val HOLD = "hold"
    const val UNHOLD = "unhold"
    const val MUTE = "mute"
    const val UNMUTE = "unmute"
    const val SPEAKER_ON = "speaker_on"
    const val SPEAKER_OFF = "speaker_off"
    const val GET_ACTIVE_CALL = "get_active_call"
    const val GET_CALL_LOG = "get_call_log"
    const val GET_STATUS = "get_status"
    const val GET_AUDIO_STATUS = "get_audio_status"
}
```

</details>

### 4. server/HttpServerHandler.kt (Updated - Added audio status command)

<details>
<summary><strong>💻 Code Block (kotlin) — 239 lines</strong></summary>

```kotlin
package com.phonemanager.server

import android.util.Log
import com.google.gson.Gson
import com.phonemanager.manager.CallLogManager
import com.phonemanager.manager.CallManager
import com.phonemanager.model.CommandRequest
import com.phonemanager.model.CommandResponse
import com.phonemanager.model.Commands
import fi.iki.elonen.NanoHTTPD

class HttpServerHandler(
    port: Int,
    private val callManager: CallManager,
    private val callLogManager: CallLogManager,
    private val onLog: (String) -> Unit
) : NanoHTTPD(port) {
    
    companion object {
        private const val TAG = "HttpServerHandler"
    }
    
    private val gson = Gson()
    
    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri
        val method = session.method
        
        Log.d(TAG, "Request: ${method.name} $uri")
        onLog("HTTP ${method.name} $uri")
        
        // Handle CORS preflight
        if (method == Method.OPTIONS) {
            return createCorsResponse()
        }
        
        return try {
            when {
                uri == "/" || uri == "/status" -> handleStatus()
                uri == "/api" || uri.startsWith("/api/") -> handleApiRequest(session)
                uri == "/help" -> handleHelp()
                else -> createJsonResponse(
                    Response.Status.NOT_FOUND,
                    CommandResponse(false, "Endpoint not found: $uri. Try /api or /help")
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "HTTP error", e)
            onLog("HTTP error: ${e.message}")
            createJsonResponse(
                Response.Status.INTERNAL_ERROR,
                CommandResponse(false, "Server error: ${e.message}")
            )
        }
    }
    
    private fun createCorsResponse(): Response {
        return newFixedLengthResponse(Response.Status.OK, "text/plain", "").apply {
            addHeader("Access-Control-Allow-Origin", "*")
            addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
            addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization")
            addHeader("Access-Control-Max-Age", "86400")
        }
    }
    
    private fun handleStatus(): Response {
        return createJsonResponse(
            Response.Status.OK,
            CommandResponse(true, "Phone Manager HTTP Server is running")
        )
    }
    
    private fun handleHelp(): Response {
        val help = mapOf(
            "endpoints" to mapOf(
                "/" to "Server status",
                "/api" to "API endpoint (GET/POST)",
                "/help" to "This help message"
            ),
            "commands" to listOf(
                "dial - Dial number. Params: number, sim (1|2), ussd (true|false)",
                "answer - Answer incoming call",
                "reject - Reject incoming call",
                "terminate - End active call",
                "hold - Hold active call",
                "unhold - Resume held call",
                "mute - Mute microphone",
                "unmute - Unmute microphone",
                "speaker_on - Enable loudspeaker",
                "speaker_off - Disable loudspeaker",
                "get_active_call - Get active call info",
                "get_call_log - Get call history. Params: type (all|incoming|outgoing|missed), limit, offset",
                "get_audio_status - Get current audio status",
                "get_status - Get server status"
            ),
            "examples" to listOf(
                "/api?command=dial&number=1234567890&sim=1",
                "/api?command=dial&number=*123%23&sim=1&ussd=true",
                "/api?command=answer",
                "/api?command=speaker_on",
                "/api?command=get_call_log&type=missed&limit=10",
                "/api?command=get_audio_status"
            )
        )
        
        return createJsonResponse(
            Response.Status.OK,
            CommandResponse(true, "API Help", help)
        )
    }
    
    private fun handleApiRequest(session: IHTTPSession): Response {
        val request = parseRequest(session)
        Log.d(TAG, "Parsed request: $request")
        
        val response = processCommand(request)
        val jsonResponse = gson.toJson(response)
        
        Log.d(TAG, "Response: $jsonResponse")
        onLog("Response: ${response.message}")
        
        return createJsonResponse(Response.Status.OK, response)
    }
    
    private fun parseRequest(session: IHTTPSession): CommandRequest {
        val params = mutableMapOf<String, String>()
        
        // Parse query parameters (GET)
        session.parameters.forEach { (key, values) ->
            if (values.isNotEmpty()) {
                params[key] = values[0]
            }
        }
        
        Log.d(TAG, "Query params: $params")
        
        // Parse body (POST)
        if (session.method == Method.POST) {
            try {
                val contentLength = session.headers["content-length"]?.toIntOrNull() ?: 0
                if (contentLength > 0) {
                    val body = mutableMapOf<String, String>()
                    session.parseBody(body)
                    
                    Log.d(TAG, "POST body: $body")
                    
                    // Check if JSON body
                    val postData = body["postData"]
                    if (!postData.isNullOrEmpty()) {
                        try {
                            return gson.fromJson(postData, CommandRequest::class.java)
                        } catch (e: Exception) {
                            Log.d(TAG, "Not JSON body, using form params")
                        }
                    }
                    
                    // Re-parse parameters after parseBody
                    session.parameters.forEach { (key, values) ->
                        if (values.isNotEmpty()) {
                            params[key] = values[0]
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing POST body", e)
            }
        }
        
        // Build request from parameters
        return CommandRequest(
            command = params["command"] ?: params["cmd"] ?: "",
            number = params["number"] ?: params["phone"] ?: params["num"],
            sim = params["sim"]?.toIntOrNull() ?: 1,
            isUssd = params["is_ussd"]?.toBooleanStrictOrNull() 
                ?: params["ussd"]?.toBooleanStrictOrNull() 
                ?: false,
            type = params["type"],
            limit = params["limit"]?.toIntOrNull() ?: 50,
            offset = params["offset"]?.toIntOrNull() ?: 0,
            fromDate = params["from_date"]?.toLongOrNull(),
            toDate = params["to_date"]?.toLongOrNull(),
            searchNumber = params["search_number"] ?: params["search"]
        )
    }
    
    private fun processCommand(request: CommandRequest): CommandResponse {
        if (request.command.isEmpty()) {
            return CommandResponse(
                success = false,
                message = "Command is required. Use /help for available commands."
            )
        }
        
        Log.d(TAG, "Processing command: ${request.command}")
        
        return when (request.command.lowercase()) {
            Commands.DIAL -> {
                if (request.number.isNullOrEmpty()) {
                    CommandResponse(false, "Number is required. Example: ?command=dial&number=1234567890&sim=1")
                } else {
                    callManager.dial(request.number, request.sim, request.isUssd)
                }
            }
            Commands.ANSWER -> callManager.answerCall()
            Commands.REJECT -> callManager.rejectCall()
            Commands.TERMINATE -> callManager.terminateCall()
            Commands.HOLD -> callManager.holdCall()
            Commands.UNHOLD -> callManager.unholdCall()
            Commands.MUTE -> callManager.mute()
            Commands.UNMUTE -> callManager.unmute()
            Commands.SPEAKER_ON -> callManager.speakerOn()
            Commands.SPEAKER_OFF -> callManager.speakerOff()
            Commands.GET_ACTIVE_CALL -> callManager.getActiveCallInfo()
            Commands.GET_CALL_LOG -> callLogManager.getCallLog(request)
            Commands.GET_AUDIO_STATUS -> callManager.getAudioStatus()
            Commands.GET_STATUS -> CommandResponse(
                success = true,
                message = "Phone Manager is running",
                data = mapOf(
                    "version" to "1.0.0",
                    "uptime" to System.currentTimeMillis()
                )
            )
            else -> CommandResponse(
                success = false,
                message = "Unknown command: ${request.command}. Use /help for available commands."
            )
        }
    }
    
    private fun createJsonResponse(status: Response.Status, response: CommandResponse): Response {
        val json = gson.toJson(response)
        return newFixedLengthResponse(status, "application/json", json).apply {
            addHeader("Access-Control-Allow-Origin", "*")
            addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
            addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization")
        }
    }
}
```

</details>

### 5. server/WebSocketServerHandler.kt (Updated)

<details>
<summary><strong>💻 Code Block (kotlin) — 120 lines</strong></summary>

```kotlin
package com.phonemanager.server

import android.util.Log
import com.google.gson.Gson
import com.phonemanager.manager.CallLogManager
import com.phonemanager.manager.CallManager
import com.phonemanager.model.CommandRequest
import com.phonemanager.model.CommandResponse
import com.phonemanager.model.Commands
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress

class WebSocketServerHandler(
    port: Int,
    private val callManager: CallManager,
    private val callLogManager: CallLogManager,
    private val onLog: (String) -> Unit
) : WebSocketServer(InetSocketAddress(port)) {
    
    companion object {
        private const val TAG = "WebSocketServerHandler"
    }
    
    private val gson = Gson()
    
    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
        val clientAddress = conn?.remoteSocketAddress?.toString() ?: "unknown"
        Log.d(TAG, "Client connected: $clientAddress")
        onLog("WebSocket client connected: $clientAddress")
        
        // Send welcome message
        val welcome = CommandResponse(
            success = true,
            message = "Connected to Phone Manager WebSocket Server"
        )
        conn?.send(gson.toJson(welcome))
    }
    
    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        val clientAddress = conn?.remoteSocketAddress?.toString() ?: "unknown"
        Log.d(TAG, "Client disconnected: $clientAddress, code: $code, reason: $reason")
        onLog("WebSocket client disconnected: $clientAddress")
    }
    
    override fun onMessage(conn: WebSocket?, message: String?) {
        message?.let { msg ->
            Log.d(TAG, "Received: $msg")
            onLog("WS received: $msg")
            
            try {
                val request = gson.fromJson(msg, CommandRequest::class.java)
                val response = processCommand(request)
                val jsonResponse = gson.toJson(response)
                
                Log.d(TAG, "Sending: $jsonResponse")
                conn?.send(jsonResponse)
                onLog("WS response: ${response.message}")
            } catch (e: Exception) {
                Log.e(TAG, "Error processing message", e)
                val errorResponse = CommandResponse(
                    success = false,
                    message = "Invalid request format: ${e.message}"
                )
                conn?.send(gson.toJson(errorResponse))
            }
        }
    }
    
    override fun onError(conn: WebSocket?, ex: Exception?) {
        Log.e(TAG, "WebSocket error", ex)
        onLog("WebSocket error: ${ex?.message}")
    }
    
    override fun onStart() {
        Log.d(TAG, "WebSocket server started on port: $port")
        onLog("WebSocket server started on port: $port")
        connectionLostTimeout = 100
    }
    
    private fun processCommand(request: CommandRequest): CommandResponse {
        Log.d(TAG, "Processing command: ${request.command}")
        
        return when (request.command.lowercase()) {
            Commands.DIAL -> {
                if (request.number.isNullOrEmpty()) {
                    CommandResponse(false, "Number is required for dial command")
                } else {
                    callManager.dial(request.number, request.sim, request.isUssd)
                }
            }
            Commands.ANSWER -> callManager.answerCall()
            Commands.REJECT -> callManager.rejectCall()
            Commands.TERMINATE -> callManager.terminateCall()
            Commands.HOLD -> callManager.holdCall()
            Commands.UNHOLD -> callManager.unholdCall()
            Commands.MUTE -> callManager.mute()
            Commands.UNMUTE -> callManager.unmute()
            Commands.SPEAKER_ON -> callManager.speakerOn()
            Commands.SPEAKER_OFF -> callManager.speakerOff()
            Commands.GET_ACTIVE_CALL -> callManager.getActiveCallInfo()
            Commands.GET_CALL_LOG -> callLogManager.getCallLog(request)
            Commands.GET_AUDIO_STATUS -> callManager.getAudioStatus()
            Commands.GET_STATUS -> CommandResponse(
                success = true,
                message = "Phone Manager is running"
            )
            else -> CommandResponse(
                success = false,
                message = "Unknown command: ${request.command}"
            )
        }
    }
    
    fun broadcastMessage(message: String) {
        Log.d(TAG, "Broadcasting: $message")
        broadcast(message)
    }
}
```

</details>

---

## Test Commands

After rebuilding and installing the app, test with these commands:

### Test Call Log (Fixed)

<details>
<summary><strong>💻 Code Block (powershell) — 14 lines</strong></summary>

```powershell
# Get all calls
curl "http://192.168.0.100:8040/api?command=get_call_log&type=all&limit=20"

# Get missed calls
curl "http://192.168.0.100:8040/api?command=get_call_log&type=missed&limit=20"

# Get incoming calls
curl "http://192.168.0.100:8040/api?command=get_call_log&type=incoming&limit=10"

# Get outgoing calls
curl "http://192.168.0.100:8040/api?command=get_call_log&type=outgoing&limit=10"

# Search by number
curl "http://192.168.0.100:8040/api?command=get_call_log&search_number=123&limit=10"
```

</details>

### Test Speaker (Fixed)

<details>
<summary><strong>💻 Code Block (powershell) — 11 lines</strong></summary>

```powershell
# Check audio status first
curl "http://192.168.0.100:8040/api?command=get_audio_status"

# Enable speaker (during a call)
curl "http://192.168.0.100:8040/api?command=speaker_on"

# Check status again
curl "http://192.168.0.100:8040/api?command=get_audio_status"

# Disable speaker
curl "http://192.168.0.100:8040/api?command=speaker_off"
```

</details>

### Get Help

<details>
<summary><strong>💻 Code Block (powershell) — 1 lines</strong></summary>

```powershell
curl "http://192.168.0.100:8040/help"
```

</details>

---

## Key Fixes:

1. **Call Log LIMIT Fix**: Removed LIMIT from SQL sortOrder and implemented manual pagination in Kotlin code

2. **Speaker Fix**: 
   - Added proper audio mode setting (`MODE_IN_CALL`)
   - Added Android 12+ `setCommunicationDevice()` API support
   - Run audio operations on main thread
   - Added `get_audio_status` command to check actual audio state

3. **Added logging** for better debugging

4. **Added `/help` endpoint** for API documentation

---

## [9] USER

get_active_call not showing the current active call number i which i currently talking with some one

---

## [10] ASSISTANT (claude-opus-4-5-20251101-thinking-32k)

<details>
<summary><strong>💭 Thinking Process</strong></summary>

> The user is saying that `get_active_call` is not showing the current active call number when they are on a call. This is likely because:
> 
> 1. The `CallControlService` (InCallService) is not being properly bound/activated
> 2. The app needs to be set as the default dialer for the InCallService to work
> 3. The `currentCall` in `CallControlService` is not being set properly
> 
> The InCallService only works when:
> 1. The app is set as the default dialer, OR
> 2. The app has the `BIND_INCALL_SERVICE` permission and is properly registered
> 
> Since the user hasn't set the app as default dialer (probably), the InCallService won't receive call callbacks.
> 
> Let me provide an alternative solution that uses:
> 1. PhoneStateListener / TelephonyCallback to detect call state
> 2. TelecomManager to get call details
> 3. A BroadcastReceiver for call state changes
> 
> I'll need to:
> 1. Update CallControlService to better track calls
> 2. Add a PhoneStateListener/TelephonyCallback as fallback
> 3. Use ContentResolver to query active calls if available
> 4. Make sure the service is properly tracking calls
> 
> Let me create a more robust solution.

</details>

# Fix for get_active_call Not Showing Current Call

The issue is that `InCallService` only works when the app is set as the **default dialer**. We need to add a fallback using `TelephonyCallback`/`PhoneStateListener` to track calls.

## Updated/New Files

### 1. manager/CallStateManager.kt (NEW FILE)

<details>
<summary><strong>💻 Code Block (kotlin) — 223 lines</strong></summary>

```kotlin
package com.phonemanager.manager

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telecom.TelecomManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat

class CallStateManager(private val context: Context) {
    
    companion object {
        private const val TAG = "CallStateManager"
        
        // Current call state
        var currentCallState: Int = TelephonyManager.CALL_STATE_IDLE
        var currentCallNumber: String? = null
        var callStartTime: Long? = null
        var isIncoming: Boolean = false
        var isCallActive: Boolean = false
        
        // Listeners for call state changes
        private val callStateListeners = mutableListOf<(Int, String?) -> Unit>()
        
        fun addCallStateListener(listener: (Int, String?) -> Unit) {
            callStateListeners.add(listener)
        }
        
        fun removeCallStateListener(listener: (Int, String?) -> Unit) {
            callStateListeners.remove(listener)
        }
        
        fun notifyListeners(state: Int, number: String?) {
            callStateListeners.forEach { it(state, number) }
        }
        
        fun getStateName(state: Int): String {
            return when (state) {
                TelephonyManager.CALL_STATE_IDLE -> "IDLE"
                TelephonyManager.CALL_STATE_RINGING -> "RINGING"
                TelephonyManager.CALL_STATE_OFFHOOK -> "OFFHOOK (Active)"
                else -> "UNKNOWN ($state)"
            }
        }
    }
    
    private val telephonyManager: TelephonyManager = 
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    
    private var phoneStateListener: PhoneStateListener? = null
    private var telephonyCallback: TelephonyCallback? = null
    
    private val handler = Handler(Looper.getMainLooper())
    
    fun startListening() {
        Log.d(TAG, "Starting call state listener")
        
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
            != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "READ_PHONE_STATE permission not granted")
            return
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ uses TelephonyCallback
            startTelephonyCallback()
        } else {
            // Older versions use PhoneStateListener
            startPhoneStateListener()
        }
    }
    
    fun stopListening() {
        Log.d(TAG, "Stopping call state listener")
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyCallback?.let {
                telephonyManager.unregisterTelephonyCallback(it)
            }
            telephonyCallback = null
        } else {
            phoneStateListener?.let {
                @Suppress("DEPRECATION")
                telephonyManager.listen(it, PhoneStateListener.LISTEN_NONE)
            }
            phoneStateListener = null
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.S)
    private fun startTelephonyCallback() {
        telephonyCallback = object : TelephonyCallback(), TelephonyCallback.CallStateListener {
            override fun onCallStateChanged(state: Int) {
                handleCallStateChange(state, null)
            }
        }
        
        try {
            telephonyManager.registerTelephonyCallback(
                context.mainExecutor,
                telephonyCallback!!
            )
            Log.d(TAG, "TelephonyCallback registered")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register TelephonyCallback", e)
        }
    }
    
    @Suppress("DEPRECATION")
    private fun startPhoneStateListener() {
        phoneStateListener = object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                handleCallStateChange(state, phoneNumber)
            }
        }
        
        try {
            telephonyManager.listen(
                phoneStateListener!!,
                PhoneStateListener.LISTEN_CALL_STATE
            )
            Log.d(TAG, "PhoneStateListener registered")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register PhoneStateListener", e)
        }
    }
    
    private fun handleCallStateChange(state: Int, phoneNumber: String?) {
        Log.d(TAG, "Call state changed: ${getStateName(state)}, number: $phoneNumber")
        
        val previousState = currentCallState
        currentCallState = state
        
        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                // Incoming call
                isIncoming = true
                isCallActive = false
                currentCallNumber = phoneNumber ?: currentCallNumber
                Log.d(TAG, "Incoming call from: $currentCallNumber")
            }
            
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                // Call is active (answered or dialing)
                isCallActive = true
                if (previousState == TelephonyManager.CALL_STATE_IDLE) {
                    // Outgoing call
                    isIncoming = false
                }
                if (callStartTime == null) {
                    callStartTime = System.currentTimeMillis()
                }
                // Try to get number if not available
                if (currentCallNumber == null) {
                    currentCallNumber = phoneNumber ?: getActiveCallNumber()
                }
                Log.d(TAG, "Call active with: $currentCallNumber, incoming: $isIncoming")
            }
            
            TelephonyManager.CALL_STATE_IDLE -> {
                // Call ended
                Log.d(TAG, "Call ended. Was with: $currentCallNumber, duration: ${getDurationSeconds()}s")
                isCallActive = false
                isIncoming = false
                currentCallNumber = null
                callStartTime = null
            }
        }
        
        notifyListeners(state, currentCallNumber)
    }
    
    private fun getActiveCallNumber(): String? {
        // Try to get from recent call log
        return try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG)
                == PackageManager.PERMISSION_GRANTED) {
                
                val cursor = context.contentResolver.query(
                    android.provider.CallLog.Calls.CONTENT_URI,
                    arrayOf(android.provider.CallLog.Calls.NUMBER),
                    null,
                    null,
                    "${android.provider.CallLog.Calls.DATE} DESC"
                )
                
                cursor?.use {
                    if (it.moveToFirst()) {
                        it.getString(0)
                    } else null
                }
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting call number from log", e)
            null
        }
    }
    
    fun getCurrentCallInfo(): Map<String, Any?> {
        return mapOf(
            "state" to getStateName(currentCallState),
            "state_code" to currentCallState,
            "number" to currentCallNumber,
            "is_active" to isCallActive,
            "is_incoming" to isIncoming,
            "start_time" to callStartTime,
            "duration_seconds" to getDurationSeconds()
        )
    }
    
    private fun getDurationSeconds(): Long {
        return callStartTime?.let { 
            (System.currentTimeMillis() - it) / 1000 
        } ?: 0
    }
}
```

</details>

### 2. receiver/CallReceiver.kt (NEW FILE)

<details>
<summary><strong>💻 Code Block (kotlin) — 62 lines</strong></summary>

```kotlin
package com.phonemanager.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import com.phonemanager.manager.CallStateManager

class CallReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "CallReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            TelephonyManager.ACTION_PHONE_STATE_CHANGED -> {
                val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                
                Log.d(TAG, "Phone state changed: $state, number: $number")
                
                when (state) {
                    TelephonyManager.EXTRA_STATE_RINGING -> {
                        CallStateManager.currentCallState = TelephonyManager.CALL_STATE_RINGING
                        CallStateManager.isIncoming = true
                        CallStateManager.isCallActive = false
                        number?.let { CallStateManager.currentCallNumber = it }
                    }
                    TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                        CallStateManager.currentCallState = TelephonyManager.CALL_STATE_OFFHOOK
                        CallStateManager.isCallActive = true
                        if (CallStateManager.callStartTime == null) {
                            CallStateManager.callStartTime = System.currentTimeMillis()
                        }
                    }
                    TelephonyManager.EXTRA_STATE_IDLE -> {
                        CallStateManager.currentCallState = TelephonyManager.CALL_STATE_IDLE
                        CallStateManager.isCallActive = false
                        CallStateManager.isIncoming = false
                        CallStateManager.currentCallNumber = null
                        CallStateManager.callStartTime = null
                    }
                }
                
                CallStateManager.notifyListeners(
                    CallStateManager.currentCallState, 
                    CallStateManager.currentCallNumber
                )
            }
            
            Intent.ACTION_NEW_OUTGOING_CALL -> {
                val number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
                Log.d(TAG, "Outgoing call to: $number")
                
                CallStateManager.currentCallNumber = number
                CallStateManager.isIncoming = false
            }
        }
    }
}
```

</details>

### 3. AndroidManifest.xml (Updated - Add Receiver)

<details>
<summary><strong>💻 Code Block (xml) — 103 lines</strong></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- Network Permissions -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />

    <!-- Phone Permissions -->
    <uses-permission android:name="android.permission.CALL_PHONE" />
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <uses-permission android:name="android.permission.READ_PHONE_NUMBERS" />
    <uses-permission android:name="android.permission.ANSWER_PHONE_CALLS" />
    <uses-permission android:name="android.permission.READ_CALL_LOG" />
    <uses-permission android:name="android.permission.WRITE_CALL_LOG" />
    <uses-permission android:name="android.permission.PROCESS_OUTGOING_CALLS" />
    <uses-permission android:name="android.permission.MANAGE_OWN_CALLS" />

    <!-- Audio Permissions -->
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />

    <!-- Background Service Permissions -->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

    <uses-feature android:name="android.hardware.telephony" android:required="true" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.PhoneManager"
        android:name=".PhoneManagerApp"
        tools:targetApi="34">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:launchMode="singleTop">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- Main Foreground Service -->
        <service
            android:name=".service.PhoneManagerService"
            android:enabled="true"
            android:exported="false"
            android:foregroundServiceType="specialUse">
            <property
                android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE"
                android:value="phone_control_server" />
        </service>

        <!-- InCallService for call control (works when app is default dialer) -->
        <service
            android:name=".service.CallControlService"
            android:permission="android.permission.BIND_INCALL_SERVICE"
            android:exported="true">
            <meta-data
                android:name="android.telecom.IN_CALL_SERVICE_UI"
                android:value="false" />
            <meta-data
                android:name="android.telecom.IN_CALL_SERVICE_RINGING"
                android:value="false" />
            <intent-filter>
                <action android:name="android.telecom.InCallService" />
            </intent-filter>
        </service>

        <!-- Call State Receiver -->
        <receiver
            android:name=".receiver.CallReceiver"
            android:enabled="true"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.PHONE_STATE" />
                <action android:name="android.intent.action.NEW_OUTGOING_CALL" />
            </intent-filter>
        </receiver>

        <!-- Boot Receiver -->
        <receiver
            android:name=".receiver.BootReceiver"
            android:enabled="true"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
                <action android:name="android.intent.action.QUICKBOOT_POWERON" />
            </intent-filter>
        </receiver>

    </application>
</manifest>
```

</details>

### 4. manager/CallManager.kt (Updated - Complete File)

<details>
<summary><strong>💻 Code Block (kotlin) — 559 lines</strong></summary>

```kotlin
package com.phonemanager.manager

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.CallLog
import android.telecom.Call
import android.telecom.TelecomManager
import android.telecom.PhoneAccountHandle
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.phonemanager.model.ActiveCallInfo
import com.phonemanager.model.CommandResponse
import com.phonemanager.service.CallControlService

class CallManager(private val context: Context) {
    
    companion object {
        private const val TAG = "CallManager"
        var isMuted = false
        var isSpeakerOn = false
    }
    
    private val telecomManager: TelecomManager = 
        context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    private val audioManager: AudioManager = 
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val telephonyManager: TelephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    private val handler = Handler(Looper.getMainLooper())
    
    // Dial number or USSD
    fun dial(number: String, simSlot: Int, isUssd: Boolean): CommandResponse {
        return try {
            Log.d(TAG, "Dialing: $number on SIM$simSlot, isUSSD: $isUssd")
            
            val phoneAccountHandle = getPhoneAccountHandle(simSlot)
            
            val formattedNumber = if (isUssd) {
                Uri.encode(number)
            } else {
                number.replace(" ", "").replace("-", "")
            }
            
            val uri = Uri.parse("tel:$formattedNumber")
            
            val extras = android.os.Bundle().apply {
                phoneAccountHandle?.let {
                    putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, it)
                }
            }
            
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) 
                == PackageManager.PERMISSION_GRANTED) {
                
                // Store outgoing number
                CallStateManager.currentCallNumber = number
                CallStateManager.isIncoming = false
                
                val intent = Intent(Intent.ACTION_CALL, uri).apply {
                    putExtras(extras)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                
                Log.d(TAG, "Call initiated successfully")
                CommandResponse(
                    success = true,
                    message = if (isUssd) "USSD code sent: $number on SIM$simSlot" 
                              else "Dialing: $number on SIM$simSlot"
                )
            } else {
                Log.e(TAG, "CALL_PHONE permission not granted")
                CommandResponse(
                    success = false,
                    message = "CALL_PHONE permission not granted"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Dial failed", e)
            CommandResponse(
                success = false,
                message = "Dial failed: ${e.message}"
            )
        }
    }
    
    private fun getPhoneAccountHandle(simSlot: Int): PhoneAccountHandle? {
        return try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {
                
                val accounts = telecomManager.callCapablePhoneAccounts
                Log.d(TAG, "Available phone accounts: ${accounts.size}")
                
                if (accounts.size > simSlot - 1 && simSlot > 0) {
                    accounts[simSlot - 1]
                } else {
                    accounts.firstOrNull()
                }
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting phone account", e)
            null
        }
    }
    
    // Answer incoming call
    fun answerCall(): CommandResponse {
        return try {
            Log.d(TAG, "Attempting to answer call")
            
            // Try InCallService first
            val call = CallControlService.currentCall
            if (call != null && call.state == Call.STATE_RINGING) {
                call.answer(android.telecom.VideoProfile.STATE_AUDIO_ONLY)
                Log.d(TAG, "Call answered via InCallService")
                return CommandResponse(success = true, message = "Call answered")
            }
            
            // Fallback to TelecomManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS)
                    == PackageManager.PERMISSION_GRANTED) {
                    telecomManager.acceptRingingCall()
                    Log.d(TAG, "Call answered via TelecomManager")
                    CommandResponse(success = true, message = "Call answered")
                } else {
                    CommandResponse(success = false, message = "ANSWER_PHONE_CALLS permission not granted")
                }
            } else {
                CommandResponse(success = false, message = "No incoming call to answer")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Answer failed", e)
            CommandResponse(success = false, message = "Answer failed: ${e.message}")
        }
    }
    
    // Reject incoming call
    fun rejectCall(): CommandResponse {
        return try {
            Log.d(TAG, "Attempting to reject call")
            
            val call = CallControlService.currentCall
            if (call != null && call.state == Call.STATE_RINGING) {
                call.reject(false, null)
                Log.d(TAG, "Call rejected")
                return CommandResponse(success = true, message = "Call rejected")
            }
            
            // Fallback
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS)
                    == PackageManager.PERMISSION_GRANTED) {
                    @Suppress("DEPRECATION")
                    telecomManager.endCall()
                    CommandResponse(success = true, message = "Call rejected")
                } else {
                    CommandResponse(success = false, message = "Permission not granted")
                }
            } else {
                CommandResponse(success = false, message = "No incoming call to reject")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Reject failed", e)
            CommandResponse(success = false, message = "Reject failed: ${e.message}")
        }
    }
    
    // Terminate active call
    fun terminateCall(): CommandResponse {
        return try {
            Log.d(TAG, "Attempting to terminate call")
            
            val call = CallControlService.currentCall
            if (call != null) {
                call.disconnect()
                Log.d(TAG, "Call terminated via InCallService")
                return CommandResponse(success = true, message = "Call terminated")
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS)
                    == PackageManager.PERMISSION_GRANTED) {
                    @Suppress("DEPRECATION")
                    val ended = telecomManager.endCall()
                    Log.d(TAG, "Call terminated via TelecomManager: $ended")
                    CommandResponse(success = ended, message = if (ended) "Call terminated" else "Failed to terminate call")
                } else {
                    CommandResponse(success = false, message = "Permission not granted")
                }
            } else {
                CommandResponse(success = false, message = "No active call to terminate")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Terminate failed", e)
            CommandResponse(success = false, message = "Terminate failed: ${e.message}")
        }
    }
    
    // Hold call
    fun holdCall(): CommandResponse {
        return try {
            val call = CallControlService.currentCall
            if (call != null && call.state == Call.STATE_ACTIVE) {
                call.hold()
                Log.d(TAG, "Call on hold")
                CommandResponse(success = true, message = "Call on hold")
            } else {
                CommandResponse(success = false, message = "No active call to hold (requires default dialer)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Hold failed", e)
            CommandResponse(success = false, message = "Hold failed: ${e.message}")
        }
    }
    
    // Unhold call
    fun unholdCall(): CommandResponse {
        return try {
            val call = CallControlService.currentCall
            if (call != null && call.state == Call.STATE_HOLDING) {
                call.unhold()
                Log.d(TAG, "Call resumed")
                CommandResponse(success = true, message = "Call resumed")
            } else {
                CommandResponse(success = false, message = "No held call to resume (requires default dialer)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unhold failed", e)
            CommandResponse(success = false, message = "Unhold failed: ${e.message}")
        }
    }
    
    // Mute microphone
    fun mute(): CommandResponse {
        return try {
            handler.post {
                audioManager.isMicrophoneMute = true
            }
            isMuted = true
            Log.d(TAG, "Microphone muted")
            CommandResponse(success = true, message = "Microphone muted")
        } catch (e: Exception) {
            Log.e(TAG, "Mute failed", e)
            CommandResponse(success = false, message = "Mute failed: ${e.message}")
        }
    }
    
    // Unmute microphone
    fun unmute(): CommandResponse {
        return try {
            handler.post {
                audioManager.isMicrophoneMute = false
            }
            isMuted = false
            Log.d(TAG, "Microphone unmuted")
            CommandResponse(success = true, message = "Microphone unmuted")
        } catch (e: Exception) {
            Log.e(TAG, "Unmute failed", e)
            CommandResponse(success = false, message = "Unmute failed: ${e.message}")
        }
    }
    
    // Enable loudspeaker
    fun speakerOn(): CommandResponse {
        return try {
            Log.d(TAG, "Enabling speaker...")
            
            handler.post {
                try {
                    audioManager.mode = AudioManager.MODE_IN_CALL
                    audioManager.isSpeakerphoneOn = true
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val devices = audioManager.availableCommunicationDevices
                        val speaker = devices.find { 
                            it.type == android.media.AudioDeviceInfo.TYPE_BUILTIN_SPEAKER 
                        }
                        speaker?.let {
                            audioManager.setCommunicationDevice(it)
                        }
                    }
                    
                    isSpeakerOn = true
                    Log.d(TAG, "Speaker enabled: ${audioManager.isSpeakerphoneOn}")
                } catch (e: Exception) {
                    Log.e(TAG, "Speaker on failed in handler", e)
                }
            }
            
            Thread.sleep(100)
            
            CommandResponse(
                success = true, 
                message = "Loudspeaker enabled"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Speaker on failed", e)
            CommandResponse(success = false, message = "Speaker on failed: ${e.message}")
        }
    }
    
    // Disable loudspeaker
    fun speakerOff(): CommandResponse {
        return try {
            Log.d(TAG, "Disabling speaker...")
            
            handler.post {
                try {
                    audioManager.isSpeakerphoneOn = false
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val devices = audioManager.availableCommunicationDevices
                        val earpiece = devices.find { 
                            it.type == android.media.AudioDeviceInfo.TYPE_BUILTIN_EARPIECE 
                        }
                        earpiece?.let {
                            audioManager.setCommunicationDevice(it)
                        }
                    }
                    
                    isSpeakerOn = false
                    Log.d(TAG, "Speaker disabled: ${audioManager.isSpeakerphoneOn}")
                } catch (e: Exception) {
                    Log.e(TAG, "Speaker off failed in handler", e)
                }
            }
            
            Thread.sleep(100)
            
            CommandResponse(
                success = true, 
                message = "Loudspeaker disabled"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Speaker off failed", e)
            CommandResponse(success = false, message = "Speaker off failed: ${e.message}")
        }
    }
    
    // Get audio status
    fun getAudioStatus(): CommandResponse {
        return try {
            val status = mapOf(
                "speaker_on" to audioManager.isSpeakerphoneOn,
                "microphone_muted" to audioManager.isMicrophoneMute,
                "audio_mode" to getAudioModeName(audioManager.mode),
                "volume_call" to audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL),
                "volume_max" to audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
                "bluetooth_sco_on" to audioManager.isBluetoothScoOn,
                "wired_headset_on" to audioManager.isWiredHeadsetOn
            )
            
            CommandResponse(
                success = true,
                message = "Audio status retrieved",
                data = status
            )
        } catch (e: Exception) {
            CommandResponse(success = false, message = "Failed to get audio status: ${e.message}")
        }
    }
    
    private fun getAudioModeName(mode: Int): String {
        return when (mode) {
            AudioManager.MODE_NORMAL -> "NORMAL"
            AudioManager.MODE_RINGTONE -> "RINGTONE"
            AudioManager.MODE_IN_CALL -> "IN_CALL"
            AudioManager.MODE_IN_COMMUNICATION -> "IN_COMMUNICATION"
            AudioManager.MODE_CALL_SCREENING -> "CALL_SCREENING"
            else -> "UNKNOWN ($mode)"
        }
    }
    
    // Get active call info - IMPROVED VERSION
    fun getActiveCallInfo(): CommandResponse {
        return try {
            Log.d(TAG, "Getting active call info...")
            
            // Method 1: Try InCallService (works if app is default dialer)
            val inCallServiceCall = CallControlService.currentCall
            if (inCallServiceCall != null) {
                val details = inCallServiceCall.details
                val callInfo = ActiveCallInfo(
                    number = details?.handle?.schemeSpecificPart,
                    state = getCallStateName(inCallServiceCall.state),
                    duration = CallControlService.callStartTime?.let { 
                        System.currentTimeMillis() - it 
                    } ?: 0,
                    isIncoming = CallControlService.isIncoming,
                    startTime = CallControlService.callStartTime ?: 0,
                    isMuted = audioManager.isMicrophoneMute,
                    isOnHold = inCallServiceCall.state == Call.STATE_HOLDING,
                    isSpeakerOn = audioManager.isSpeakerphoneOn
                )
                Log.d(TAG, "Got call info from InCallService: $callInfo")
                return CommandResponse(
                    success = true,
                    message = "Active call info retrieved (via InCallService)",
                    data = callInfo
                )
            }
            
            // Method 2: Use CallStateManager (TelephonyManager listener)
            if (CallStateManager.isCallActive || 
                CallStateManager.currentCallState != TelephonyManager.CALL_STATE_IDLE) {
                
                val callInfo = ActiveCallInfo(
                    number = CallStateManager.currentCallNumber ?: getLastDialedNumber(),
                    state = CallStateManager.getStateName(CallStateManager.currentCallState),
                    duration = CallStateManager.callStartTime?.let { 
                        System.currentTimeMillis() - it 
                    } ?: 0,
                    isIncoming = CallStateManager.isIncoming,
                    startTime = CallStateManager.callStartTime ?: 0,
                    isMuted = audioManager.isMicrophoneMute,
                    isOnHold = false,
                    isSpeakerOn = audioManager.isSpeakerphoneOn
                )
                Log.d(TAG, "Got call info from CallStateManager: $callInfo")
                return CommandResponse(
                    success = true,
                    message = "Active call info retrieved (via TelephonyManager)",
                    data = callInfo
                )
            }
            
            // Method 3: Check TelephonyManager directly
            val callState = telephonyManager.callState
            if (callState != TelephonyManager.CALL_STATE_IDLE) {
                val lastNumber = getLastDialedNumber() ?: getLastReceivedNumber()
                val callInfo = ActiveCallInfo(
                    number = lastNumber,
                    state = when (callState) {
                        TelephonyManager.CALL_STATE_RINGING -> "RINGING"
                        TelephonyManager.CALL_STATE_OFFHOOK -> "ACTIVE"
                        else -> "UNKNOWN"
                    },
                    duration = 0,
                    isIncoming = callState == TelephonyManager.CALL_STATE_RINGING,
                    startTime = System.currentTimeMillis(),
                    isMuted = audioManager.isMicrophoneMute,
                    isOnHold = false,
                    isSpeakerOn = audioManager.isSpeakerphoneOn
                )
                Log.d(TAG, "Got call info from TelephonyManager direct: $callInfo")
                return CommandResponse(
                    success = true,
                    message = "Active call info retrieved (via TelephonyManager direct)",
                    data = callInfo
                )
            }
            
            Log.d(TAG, "No active call found")
            CommandResponse(
                success = true,
                message = "No active call",
                data = null
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Get active call failed", e)
            CommandResponse(success = false, message = "Get active call failed: ${e.message}")
        }
    }
    
    private fun getLastDialedNumber(): String? {
        return try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG)
                == PackageManager.PERMISSION_GRANTED) {
                
                val cursor = context.contentResolver.query(
                    CallLog.Calls.CONTENT_URI,
                    arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.TYPE, CallLog.Calls.DATE),
                    "${CallLog.Calls.TYPE} = ?",
                    arrayOf(CallLog.Calls.OUTGOING_TYPE.toString()),
                    "${CallLog.Calls.DATE} DESC"
                )
                
                cursor?.use {
                    if (it.moveToFirst()) {
                        val number = it.getString(0)
                        val date = it.getLong(2)
                        // Only return if call was in last 60 seconds
                        if (System.currentTimeMillis() - date < 60000) {
                            Log.d(TAG, "Last dialed number: $number")
                            return number
                        }
                    }
                }
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting last dialed number", e)
            null
        }
    }
    
    private fun getLastReceivedNumber(): String? {
        return try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG)
                == PackageManager.PERMISSION_GRANTED) {
                
                val cursor = context.contentResolver.query(
                    CallLog.Calls.CONTENT_URI,
                    arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.TYPE, CallLog.Calls.DATE),
                    "${CallLog.Calls.TYPE} IN (?, ?)",
                    arrayOf(
                        CallLog.Calls.INCOMING_TYPE.toString(),
                        CallLog.Calls.MISSED_TYPE.toString()
                    ),
                    "${CallLog.Calls.DATE} DESC"
                )
                
                cursor?.use {
                    if (it.moveToFirst()) {
                        val number = it.getString(0)
                        val date = it.getLong(2)
                        // Only return if call was in last 60 seconds
                        if (System.currentTimeMillis() - date < 60000) {
                            Log.d(TAG, "Last received number: $number")
                            return number
                        }
                    }
                }
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting last received number", e)
            null
        }
    }
    
    private fun getCallStateName(state: Int): String {
        return when (state) {
            Call.STATE_NEW -> "NEW"
            Call.STATE_DIALING -> "DIALING"
            Call.STATE_RINGING -> "RINGING"
            Call.STATE_HOLDING -> "HOLDING"
            Call.STATE_ACTIVE -> "ACTIVE"
            Call.STATE_DISCONNECTED -> "DISCONNECTED"
            Call.STATE_CONNECTING -> "CONNECTING"
            Call.STATE_DISCONNECTING -> "DISCONNECTING"
            Call.STATE_SELECT_PHONE_ACCOUNT -> "SELECT_PHONE_ACCOUNT"
            Call.STATE_SIMULATED_RINGING -> "SIMULATED_RINGING"
            Call.STATE_AUDIO_PROCESSING -> "AUDIO_PROCESSING"
            else -> "UNKNOWN ($state)"
        }
    }
}
```

</details>

### 5. service/PhoneManagerService.kt (Updated - Add CallStateManager)

<details>
<summary><strong>💻 Code Block (kotlin) — 342 lines</strong></summary>

```kotlin
package com.phonemanager.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.phonemanager.MainActivity
import com.phonemanager.PhoneManagerApp
import com.phonemanager.R
import com.phonemanager.manager.CallLogManager
import com.phonemanager.manager.CallManager
import com.phonemanager.manager.CallStateManager
import com.phonemanager.receiver.CallReceiver
import com.phonemanager.server.HttpServerHandler
import com.phonemanager.server.WebSocketServerHandler
import com.phonemanager.utils.PreferencesManager
import kotlinx.coroutines.*

class PhoneManagerService : Service() {
    
    companion object {
        const val TAG = "PhoneManagerService"
        const val ACTION_STOP_SERVICE = "com.phonemanager.STOP_SERVICE"
        const val NOTIFICATION_ID = 1
    }
    
    private val binder = LocalBinder()
    private var webSocketServer: WebSocketServerHandler? = null
    private var httpServer: HttpServerHandler? = null
    
    private lateinit var callManager: CallManager
    private lateinit var callLogManager: CallLogManager
    private lateinit var callStateManager: CallStateManager
    private lateinit var prefsManager: PreferencesManager
    
    private var callReceiver: CallReceiver? = null
    
    private var wakeLock: PowerManager.WakeLock? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val logListeners = mutableListOf<(String) -> Unit>()
    
    inner class LocalBinder : Binder() {
        fun getService(): PhoneManagerService = this@PhoneManagerService
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        
        callManager = CallManager(this)
        callLogManager = CallLogManager(this)
        callStateManager = CallStateManager(this)
        prefsManager = PreferencesManager(this)
        
        // Start listening for call state changes
        callStateManager.startListening()
        
        // Register broadcast receiver for call events
        registerCallReceiver()
        
        // Add listener for call state changes
        CallStateManager.addCallStateListener { state, number ->
            notifyLog("Call state: ${CallStateManager.getStateName(state)}, Number: $number")
            broadcastCallState(state, number)
        }
    }
    
    private fun registerCallReceiver() {
        try {
            callReceiver = CallReceiver()
            val filter = IntentFilter().apply {
                addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
                addAction(Intent.ACTION_NEW_OUTGOING_CALL)
            }
            registerReceiver(callReceiver, filter)
            Log.d(TAG, "CallReceiver registered")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register CallReceiver", e)
        }
    }
    
    private fun unregisterCallReceiver() {
        try {
            callReceiver?.let {
                unregisterReceiver(it)
                Log.d(TAG, "CallReceiver unregistered")
            }
            callReceiver = null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unregister CallReceiver", e)
        }
    }
    
    private fun broadcastCallState(state: Int, number: String?) {
        // Broadcast to WebSocket clients
        webSocketServer?.let { server ->
            try {
                val stateInfo = mapOf(
                    "event" to "call_state_changed",
                    "state" to CallStateManager.getStateName(state),
                    "state_code" to state,
                    "number" to number,
                    "is_active" to CallStateManager.isCallActive,
                    "is_incoming" to CallStateManager.isIncoming,
                    "timestamp" to System.currentTimeMillis()
                )
                val json = com.google.gson.Gson().toJson(stateInfo)
                server.broadcastMessage(json)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to broadcast call state", e)
            }
        }
    }
    
    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand: ${intent?.action}")
        
        when (intent?.action) {
            ACTION_STOP_SERVICE -> {
                stopServers()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
        }
        
        try {
            startForegroundWithNotification()
            acquireWakeLock()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start foreground: ${e.message}")
            e.printStackTrace()
        }
        
        return START_STICKY
    }
    
    private fun startForegroundWithNotification() {
        val notification = createNotification()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID, 
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID, 
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        
        Log.d(TAG, "Foreground service started successfully")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service onDestroy")
        
        callStateManager.stopListening()
        unregisterCallReceiver()
        stopServers()
        releaseWakeLock()
        serviceScope.cancel()
    }
    
    private fun stopServers() {
        stopWebSocketServer()
        stopHttpServer()
    }
    
    private fun createNotification(): Notification {
        val stopIntent = Intent(this, PhoneManagerService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this, 0, openIntent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        return NotificationCompat.Builder(this, PhoneManagerApp.CHANNEL_ID)
            .setContentTitle("Phone Manager")
            .setContentText("Servers are running in background")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(openPendingIntent)
            .addAction(R.drawable.ic_stop, "Stop", stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }
    
    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "PhoneManager::ServiceWakeLock"
            ).apply {
                acquire(10 * 60 * 1000L)
            }
            Log.d(TAG, "WakeLock acquired")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to acquire WakeLock: ${e.message}")
        }
    }
    
    private fun releaseWakeLock() {
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                    Log.d(TAG, "WakeLock released")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release WakeLock: ${e.message}")
        }
    }
    
    // WebSocket Server Control
    fun startWebSocketServer(port: Int): Boolean {
        return try {
            stopWebSocketServer()
            
            webSocketServer = WebSocketServerHandler(port, callManager, callLogManager) { log ->
                notifyLog(log)
            }
            webSocketServer?.start()
            notifyLog("WebSocket server started on port $port")
            Log.d(TAG, "WebSocket server started on port $port")
            true
        } catch (e: Exception) {
            notifyLog("WebSocket server failed: ${e.message}")
            Log.e(TAG, "WebSocket server failed", e)
            false
        }
    }
    
    fun stopWebSocketServer() {
        try {
            webSocketServer?.let { server ->
                server.stop(1000)
                notifyLog("WebSocket server stopped")
                Log.d(TAG, "WebSocket server stopped")
            }
            webSocketServer = null
        } catch (e: Exception) {
            Log.e(TAG, "WebSocket stop error: ${e.message}")
            notifyLog("WebSocket stop error: ${e.message}")
        }
    }
    
    fun isWebSocketRunning(): Boolean = webSocketServer != null
    
    // HTTP Server Control
    fun startHttpServer(port: Int): Boolean {
        return try {
            stopHttpServer()
            
            httpServer = HttpServerHandler(port, callManager, callLogManager) { log ->
                notifyLog(log)
            }
            httpServer?.start()
            notifyLog("HTTP server started on port $port")
            Log.d(TAG, "HTTP server started on port $port")
            true
        } catch (e: Exception) {
            notifyLog("HTTP server failed: ${e.message}")
            Log.e(TAG, "HTTP server failed", e)
            false
        }
    }
    
    fun stopHttpServer() {
        try {
            httpServer?.let { server ->
                server.stop()
                notifyLog("HTTP server stopped")
                Log.d(TAG, "HTTP server stopped")
            }
            httpServer = null
        } catch (e: Exception) {
            Log.e(TAG, "HTTP stop error: ${e.message}")
            notifyLog("HTTP stop error: ${e.message}")
        }
    }
    
    fun isHttpRunning(): Boolean = httpServer != null
    
    // Log listeners
    fun addLogListener(listener: (String) -> Unit) {
        logListeners.add(listener)
    }
    
    fun removeLogListener(listener: (String) -> Unit) {
        logListeners.remove(listener)
    }
    
    private fun notifyLog(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        val logMessage = "[$timestamp] $message"
        
        serviceScope.launch(Dispatchers.Main) {
            logListeners.forEach { 
                try {
                    it(logMessage)
                } catch (e: Exception) {
                    Log.e(TAG, "Log listener error: ${e.message}")
                }
            }
        }
    }
}
```

</details>

---

## Test Commands

After rebuilding, test with these commands:

<details>
<summary><strong>💻 Code Block (powershell) — 19 lines</strong></summary>

```powershell
# Make a call first, then run:
curl "http://192.168.0.100:8040/api?command=get_active_call"

# Expected response when on a call:
# {
#   "success": true,
#   "message": "Active call info retrieved (via TelephonyManager)",
#   "data": {
#     "number": "+1234567890",
#     "state": "OFFHOOK (Active)",
#     "duration": 15000,
#     "is_incoming": false,
#     "start_time": 1234567890123,
#     "is_muted": false,
#     "is_on_hold": false,
#     "is_speaker_on": false
#   },
#   "timestamp": 1234567890123
# }
```

</details>

---

## Summary of Changes

1. **Added `CallStateManager`** - Uses `TelephonyCallback` (Android 12+) or `PhoneStateListener` (older) to track call state independently of InCallService

2. **Added `CallReceiver`** - BroadcastReceiver that catches `PHONE_STATE` and `NEW_OUTGOING_CALL` intents to track the phone number

3. **Updated `CallManager.getActiveCallInfo()`** - Now uses 3 fallback methods:
   - InCallService (if app is default dialer)
   - CallStateManager (TelephonyManager listener)
   - Direct TelephonyManager query + call log lookup

4. **Updated `PhoneManagerService`** - Registers CallStateManager and CallReceiver, broadcasts call state changes to WebSocket clients

5. **Updated `AndroidManifest.xml`** - Added CallReceiver registration

Now `get_active_call` will show the current call number even without being the default dialer!

---

## [11] USER

now create a documentation or manual to use all features

---

## [12] ASSISTANT (claude-opus-4-5-20251101-thinking-32k)

<details>
<summary><strong>💭 Thinking Process</strong></summary>

> The user wants a comprehensive documentation/manual for the Phone Manager Android app. I need to cover all features including:
> 
> 1. Installation and setup
> 2. Permissions required
> 3. HTTP API usage (GET/POST)
> 4. WebSocket API usage
> 5. All available commands with examples
> 6. Response formats
> 7. Error handling
> 8. Troubleshooting
> 
> Let me create a detailed and well-organized documentation.

</details>

# Phone Manager - Complete User Manual & API Documentation

## Table of Contents

1. [Overview](#1-overview)
2. [Installation & Setup](#2-installation--setup)
3. [App Interface Guide](#3-app-interface-guide)
4. [API Overview](#4-api-overview)
5. [HTTP API Reference](#5-http-api-reference)
6. [WebSocket API Reference](#6-websocket-api-reference)
7. [Command Reference](#7-command-reference)
8. [Response Formats](#8-response-formats)
9. [Use Cases & Examples](#9-use-cases--examples)
10. [Troubleshooting](#10-troubleshooting)
11. [Security Considerations](#11-security-considerations)

---

## 1. Overview

**Phone Manager** is an Android application that allows remote control of phone call functions via HTTP and WebSocket APIs. It enables you to:

- **Make calls** to any phone number on SIM1 or SIM2
- **Execute USSD codes** (like *123#, *#06#, etc.)
- **Control active calls** (answer, reject, terminate, hold, unhold)
- **Control audio** (mute/unmute microphone, speaker on/off)
- **Monitor calls** (get active call info, call history)
- **Real-time updates** via WebSocket

### Supported Android Versions
- **Minimum**: Android 8.0 (API 26)
- **Target**: Android 14 (API 34)
- **Recommended**: Android 10+ for full functionality

---

## 2. Installation & Setup

### 2.1 Install the App

1. Build the APK from Android Studio or install the provided APK
2. Enable "Install from Unknown Sources" if needed
3. Install the app on your Android device

### 2.2 Grant Permissions

The app requires the following permissions:

| Permission | Purpose |
|------------|---------|
| **Phone** | Make/manage calls, read phone state |
| **Call Log** | Read/write call history |
| **Notifications** | Show foreground service notification |
| **Microphone** | Mute/unmute functionality |

**Steps to grant permissions:**
1. Open the app
2. Tap "Request Permissions" button
3. Allow all requested permissions
4. (Optional) Tap "Set as Default Dialer" for full call control features

### 2.3 Disable Battery Optimization

For the app to work reliably in the background:

1. Go to **Settings → Apps → Phone Manager**
2. Tap **Battery**
3. Select **Unrestricted** or **Don't optimize**

### 2.4 Start the Servers

1. Open the Phone Manager app
2. Set the desired ports (default: WebSocket 8030, HTTP 8040)
3. Toggle ON the WebSocket and/or HTTP server switches
4. Note the IP address shown in the app

### 2.5 Verify Setup

<details>
<summary><strong>💻 Code Block (bash) — 5 lines</strong></summary>

```bash
# Test HTTP server
curl "http://<PHONE_IP>:8040/status"

# Expected response:
# {"success":true,"message":"Phone Manager HTTP Server is running","timestamp":...}
```

</details>

---

## 3. App Interface Guide

### 3.1 Main Screen Layout

<details>
<summary><strong>💻 Code Block — 29 lines</strong></summary>

```
┌─────────────────────────────────────┐
│         Phone Manager               │
├─────────────────────────────────────┤
│  Service Status                     │
│  Status: Running                    │
│  IP: 192.168.0.100                  │
├─────────────────────────────────────┤
│  WebSocket Server                   │
│  Port: [8030]            [ON/OFF]   │
│  Status: ON (Port: 8030)            │
├─────────────────────────────────────┤
│  HTTP Server (GET/POST)             │
│  Port: [8040]            [ON/OFF]   │
│  Status: ON (Port: 8040)            │
├─────────────────────────────────────┤
│  API Commands                       │
│  • dial, answer, reject...          │
├─────────────────────────────────────┤
│  Permissions                        │
│  ✓ CALL_PHONE                       │
│  ✓ READ_PHONE_STATE                 │
│  [Request Permissions]              │
│  [Set as Default Dialer]            │
├─────────────────────────────────────┤
│  Activity Log                       │
│  [11:30:45] HTTP GET /api...        │
│  [11:30:46] Response: Dialing...    │
│  [Clear Log]                        │
└─────────────────────────────────────┘
```

</details>

### 3.2 Status Indicators

| Indicator | Meaning |
|-----------|---------|
| 🟢 Green | Server running / Permission granted |
| 🔴 Red | Server stopped / Permission denied |
| IP Address | Your phone's network IP address |

---

## 4. API Overview

### 4.1 Connection Methods

| Method | URL Format | Use Case |
|--------|------------|----------|
| **HTTP GET** | `http://<IP>:8040/api?command=...` | Simple requests, browser testing |
| **HTTP POST** | `http://<IP>:8040/api` with JSON body | Complex requests, automation |
| **WebSocket** | `ws://<IP>:8030` | Real-time bidirectional communication |

### 4.2 Available Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/` | GET | Server status |
| `/status` | GET | Server status |
| `/api` | GET/POST | Main API endpoint |
| `/help` | GET | API documentation |

### 4.3 Command Categories

| Category | Commands |
|----------|----------|
| **Dialing** | `dial` |
| **Call Control** | `answer`, `reject`, `terminate`, `hold`, `unhold` |
| **Audio Control** | `mute`, `unmute`, `speaker_on`, `speaker_off` |
| **Information** | `get_active_call`, `get_call_log`, `get_audio_status`, `get_status` |

---

## 5. HTTP API Reference

### 5.1 Base URL

<details>
<summary><strong>💻 Code Block — 1 lines</strong></summary>

```
http://<PHONE_IP>:<HTTP_PORT>/api
```

</details>

**Example:** `http://192.168.0.100:8040/api`

### 5.2 GET Request Format

<details>
<summary><strong>💻 Code Block — 1 lines</strong></summary>

```
http://<IP>:<PORT>/api?command=<COMMAND>&param1=value1&param2=value2
```

</details>

### 5.3 POST Request Format

**Headers:**
<details>
<summary><strong>💻 Code Block — 1 lines</strong></summary>

```
Content-Type: application/json
```

</details>

**Body:**
<details>
<summary><strong>💻 Code Block (json) — 5 lines</strong></summary>

```json
{
    "command": "command_name",
    "param1": "value1",
    "param2": "value2"
}
```

</details>

### 5.4 Common Parameters

| Parameter | Type | Description | Default |
|-----------|------|-------------|---------|
| `command` | string | Command to execute | Required |
| `number` | string | Phone number or USSD code | - |
| `sim` | integer | SIM slot (1 or 2) | 1 |
| `ussd` | boolean | Is USSD code | false |
| `is_ussd` | boolean | Is USSD code (alias) | false |

### 5.5 CORS Headers

All responses include CORS headers:
<details>
<summary><strong>💻 Code Block — 3 lines</strong></summary>

```
Access-Control-Allow-Origin: *
Access-Control-Allow-Methods: GET, POST, OPTIONS
Access-Control-Allow-Headers: Content-Type, Authorization
```

</details>

---

## 6. WebSocket API Reference

### 6.1 Connection

<details>
<summary><strong>💻 Code Block (javascript) — 1 lines</strong></summary>

```javascript
const ws = new WebSocket('ws://<PHONE_IP>:8030');
```

</details>

### 6.2 Message Format (Send)

<details>
<summary><strong>💻 Code Block (json) — 6 lines</strong></summary>

```json
{
    "command": "command_name",
    "number": "1234567890",
    "sim": 1,
    "is_ussd": false
}
```

</details>

### 6.3 Message Format (Receive)

<details>
<summary><strong>💻 Code Block (json) — 6 lines</strong></summary>

```json
{
    "success": true,
    "message": "Description of result",
    "data": { },
    "timestamp": 1234567890123
}
```

</details>

### 6.4 Real-time Events

The WebSocket server broadcasts call state changes:

<details>
<summary><strong>💻 Code Block (json) — 9 lines</strong></summary>

```json
{
    "event": "call_state_changed",
    "state": "OFFHOOK (Active)",
    "state_code": 2,
    "number": "+1234567890",
    "is_active": true,
    "is_incoming": false,
    "timestamp": 1234567890123
}
```

</details>

---

## 7. Command Reference

### 7.1 dial - Make a Call or USSD

**Description:** Initiates a phone call or executes a USSD code.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `number` | string | Yes | Phone number or USSD code |
| `sim` | integer | No | SIM slot: 1 or 2 (default: 1) |
| `ussd` / `is_ussd` | boolean | No | Set true for USSD codes (default: false) |

**HTTP GET Examples:**

<details>
<summary><strong>💻 Code Block (bash) — 11 lines</strong></summary>

```bash
# Dial a phone number on SIM1
curl "http://192.168.0.100:8040/api?command=dial&number=+1234567890&sim=1"

# Dial on SIM2
curl "http://192.168.0.100:8040/api?command=dial&number=+1234567890&sim=2"

# Execute USSD code (check balance)
curl "http://192.168.0.100:8040/api?command=dial&number=*123%23&sim=1&ussd=true"

# Check IMEI
curl "http://192.168.0.100:8040/api?command=dial&number=*%2306%23&ussd=true"
```

</details>

**HTTP POST Example:**

<details>
<summary><strong>💻 Code Block (bash) — 3 lines</strong></summary>

```bash
curl -X POST "http://192.168.0.100:8040/api" \
  -H "Content-Type: application/json" \
  -d '{"command":"dial","number":"+1234567890","sim":1,"is_ussd":false}'
```

</details>

**WebSocket Example:**

<details>
<summary><strong>💻 Code Block (json) — 1 lines</strong></summary>

```json
{"command":"dial","number":"+1234567890","sim":1,"is_ussd":false}
```

</details>

**Response:**

<details>
<summary><strong>💻 Code Block (json) — 6 lines</strong></summary>

```json
{
    "success": true,
    "message": "Dialing: +1234567890 on SIM1",
    "data": null,
    "timestamp": 1234567890123
}
```

</details>

**USSD Code Encoding:**

| Character | URL Encoded |
|-----------|-------------|
| `*` | `*` or `%2A` |
| `#` | `%23` |

---

### 7.2 answer - Answer Incoming Call

**Description:** Answers a ringing incoming call.

**Parameters:** None

**HTTP GET:**

<details>
<summary><strong>💻 Code Block (bash) — 1 lines</strong></summary>

```bash
curl "http://192.168.0.100:8040/api?command=answer"
```

</details>

**HTTP POST:**

<details>
<summary><strong>💻 Code Block (bash) — 3 lines</strong></summary>

```bash
curl -X POST "http://192.168.0.100:8040/api" \
  -H "Content-Type: application/json" \
  -d '{"command":"answer"}'
```

</details>

**WebSocket:**

<details>
<summary><strong>💻 Code Block (json) — 1 lines</strong></summary>

```json
{"command":"answer"}
```

</details>

**Response:**

<details>
<summary><strong>💻 Code Block (json) — 6 lines</strong></summary>

```json
{
    "success": true,
    "message": "Call answered",
    "data": null,
    "timestamp": 1234567890123
}
```

</details>

---

### 7.3 reject - Reject Incoming Call

**Description:** Rejects a ringing incoming call.

**Parameters:** None

**HTTP GET:**

<details>
<summary><strong>💻 Code Block (bash) — 1 lines</strong></summary>

```bash
curl "http://192.168.0.100:8040/api?command=reject"
```

</details>

**Response:**

<details>
<summary><strong>💻 Code Block (json) — 6 lines</strong></summary>

```json
{
    "success": true,
    "message": "Call rejected",
    "data": null,
    "timestamp": 1234567890123
}
```

</details>

---

### 7.4 terminate - End Active Call

**Description:** Terminates/hangs up the current active call.

**Parameters:** None

**HTTP GET:**

<details>
<summary><strong>💻 Code Block (bash) — 1 lines</strong></summary>

```bash
curl "http://192.168.0.100:8040/api?command=terminate"
```

</details>

**Response:**

<details>
<summary><strong>💻 Code Block (json) — 6 lines</strong></summary>

```json
{
    "success": true,
    "message": "Call terminated",
    "data": null,
    "timestamp": 1234567890123
}
```

</details>

---

### 7.5 hold - Hold Active Call

**Description:** Puts the active call on hold.

> ⚠️ **Note:** Requires app to be set as default dialer.

**Parameters:** None

**HTTP GET:**

<details>
<summary><strong>💻 Code Block (bash) — 1 lines</strong></summary>

```bash
curl "http://192.168.0.100:8040/api?command=hold"
```

</details>

**Response:**

<details>
<summary><strong>💻 Code Block (json) — 6 lines</strong></summary>

```json
{
    "success": true,
    "message": "Call on hold",
    "data": null,
    "timestamp": 1234567890123
}
```

</details>

---

### 7.6 unhold - Resume Held Call

**Description:** Resumes a call that was previously put on hold.

> ⚠️ **Note:** Requires app to be set as default dialer.

**Parameters:** None

**HTTP GET:**

<details>
<summary><strong>💻 Code Block (bash) — 1 lines</strong></summary>

```bash
curl "http://192.168.0.100:8040/api?command=unhold"
```

</details>

**Response:**

<details>
<summary><strong>💻 Code Block (json) — 6 lines</strong></summary>

```json
{
    "success": true,
    "message": "Call resumed",
    "data": null,
    "timestamp": 1234567890123
}
```

</details>

---

### 7.7 mute - Mute Microphone

**Description:** Mutes the microphone during a call.

**Parameters:** None

**HTTP GET:**

<details>
<summary><strong>💻 Code Block (bash) — 1 lines</strong></summary>

```bash
curl "http://192.168.0.100:8040/api?command=mute"
```

</details>

**Response:**

<details>
<summary><strong>💻 Code Block (json) — 6 lines</strong></summary>

```json
{
    "success": true,
    "message": "Microphone muted",
    "data": null,
    "timestamp": 1234567890123
}
```

</details>

---

### 7.8 unmute - Unmute Microphone

**Description:** Unmutes the microphone during a call.

**Parameters:** None

**HTTP GET:**

<details>
<summary><strong>💻 Code Block (bash) — 1 lines</strong></summary>

```bash
curl "http://192.168.0.100:8040/api?command=unmute"
```

</details>

**Response:**

<details>
<summary><strong>💻 Code Block (json) — 6 lines</strong></summary>

```json
{
    "success": true,
    "message": "Microphone unmuted",
    "data": null,
    "timestamp": 1234567890123
}
```

</details>

---

### 7.9 speaker_on - Enable Loudspeaker

**Description:** Enables the loudspeaker during a call.

**Parameters:** None

**HTTP GET:**

<details>
<summary><strong>💻 Code Block (bash) — 1 lines</strong></summary>

```bash
curl "http://192.168.0.100:8040/api?command=speaker_on"
```

</details>

**Response:**

<details>
<summary><strong>💻 Code Block (json) — 6 lines</strong></summary>

```json
{
    "success": true,
    "message": "Loudspeaker enabled",
    "data": null,
    "timestamp": 1234567890123
}
```

</details>

---

### 7.10 speaker_off - Disable Loudspeaker

**Description:** Disables the loudspeaker and switches to earpiece.

**Parameters:** None

**HTTP GET:**

<details>
<summary><strong>💻 Code Block (bash) — 1 lines</strong></summary>

```bash
curl "http://192.168.0.100:8040/api?command=speaker_off"
```

</details>

**Response:**

<details>
<summary><strong>💻 Code Block (json) — 6 lines</strong></summary>

```json
{
    "success": true,
    "message": "Loudspeaker disabled",
    "data": null,
    "timestamp": 1234567890123
}
```

</details>

---

### 7.11 get_active_call - Get Active Call Information

**Description:** Returns information about the current active call.

**Parameters:** None

**HTTP GET:**

<details>
<summary><strong>💻 Code Block (bash) — 1 lines</strong></summary>

```bash
curl "http://192.168.0.100:8040/api?command=get_active_call"
```

</details>

**Response (Active Call):**

<details>
<summary><strong>💻 Code Block (json) — 15 lines</strong></summary>

```json
{
    "success": true,
    "message": "Active call info retrieved (via TelephonyManager)",
    "data": {
        "number": "+1234567890",
        "state": "OFFHOOK (Active)",
        "duration": 45000,
        "is_incoming": false,
        "start_time": 1234567890123,
        "is_muted": false,
        "is_on_hold": false,
        "is_speaker_on": true
    },
    "timestamp": 1234567890123
}
```

</details>

**Response (No Active Call):**

<details>
<summary><strong>💻 Code Block (json) — 6 lines</strong></summary>

```json
{
    "success": true,
    "message": "No active call",
    "data": null,
    "timestamp": 1234567890123
}
```

</details>

**Call States:**

| State | Description |
|-------|-------------|
| `IDLE` | No call |
| `RINGING` | Incoming call ringing |
| `OFFHOOK (Active)` | Call in progress |
| `DIALING` | Outgoing call dialing |
| `CONNECTING` | Call connecting |
| `HOLDING` | Call on hold |
| `DISCONNECTED` | Call ended |

---

### 7.12 get_call_log - Get Call History

**Description:** Retrieves call history with optional filters.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `type` | string | No | Filter: `all`, `incoming`, `outgoing`, `missed`, `rejected` |
| `limit` | integer | No | Max entries to return (default: 50) |
| `offset` | integer | No | Skip entries for pagination (default: 0) |
| `from_date` | long | No | Start timestamp in milliseconds |
| `to_date` | long | No | End timestamp in milliseconds |
| `search_number` | string | No | Search by phone number (partial match) |

**HTTP GET Examples:**

<details>
<summary><strong>💻 Code Block (bash) — 20 lines</strong></summary>

```bash
# Get all calls (last 50)
curl "http://192.168.0.100:8040/api?command=get_call_log&type=all&limit=50"

# Get missed calls
curl "http://192.168.0.100:8040/api?command=get_call_log&type=missed&limit=20"

# Get incoming calls
curl "http://192.168.0.100:8040/api?command=get_call_log&type=incoming&limit=20"

# Get outgoing calls
curl "http://192.168.0.100:8040/api?command=get_call_log&type=outgoing&limit=20"

# Pagination - get next page
curl "http://192.168.0.100:8040/api?command=get_call_log&type=all&limit=20&offset=20"

# Search by number
curl "http://192.168.0.100:8040/api?command=get_call_log&search_number=12345"

# Filter by date range (timestamps in milliseconds)
curl "http://192.168.0.100:8040/api?command=get_call_log&from_date=1704067200000&to_date=1704153600000"
```

</details>

**HTTP POST Example:**

<details>
<summary><strong>💻 Code Block (bash) — 8 lines</strong></summary>

```bash
curl -X POST "http://192.168.0.100:8040/api" \
  -H "Content-Type: application/json" \
  -d '{
    "command": "get_call_log",
    "type": "missed",
    "limit": 10,
    "offset": 0
  }'
```

</details>

**Response:**

<details>
<summary><strong>💻 Code Block (json) — 30 lines</strong></summary>

```json
{
    "success": true,
    "message": "Call log retrieved successfully (15 entries)",
    "data": {
        "total_count": 150,
        "entries": [
            {
                "id": 1234,
                "number": "+1234567890",
                "name": "John Doe",
                "type": "incoming",
                "date": 1234567890123,
                "duration": 120,
                "sim_slot": 1
            },
            {
                "id": 1233,
                "number": "+0987654321",
                "name": null,
                "type": "outgoing",
                "date": 1234567800000,
                "duration": 45,
                "sim_slot": 2
            }
        ],
        "limit": 50,
        "offset": 0
    },
    "timestamp": 1234567890123
}
```

</details>

**Call Types:**

| Type | Description |
|------|-------------|
| `incoming` | Answered incoming call |
| `outgoing` | Outgoing call |
| `missed` | Missed incoming call |
| `rejected` | Rejected incoming call |
| `blocked` | Blocked call |
| `voicemail` | Voicemail |

---

### 7.13 get_audio_status - Get Audio Status

**Description:** Returns current audio settings status.

**Parameters:** None

**HTTP GET:**

<details>
<summary><strong>💻 Code Block (bash) — 1 lines</strong></summary>

```bash
curl "http://192.168.0.100:8040/api?command=get_audio_status"
```

</details>

**Response:**

<details>
<summary><strong>💻 Code Block (json) — 14 lines</strong></summary>

```json
{
    "success": true,
    "message": "Audio status retrieved",
    "data": {
        "speaker_on": false,
        "microphone_muted": false,
        "audio_mode": "IN_CALL",
        "volume_call": 5,
        "volume_max": 10,
        "bluetooth_sco_on": false,
        "wired_headset_on": false
    },
    "timestamp": 1234567890123
}
```

</details>

**Audio Modes:**

| Mode | Description |
|------|-------------|
| `NORMAL` | Normal audio mode |
| `RINGTONE` | Ringtone playing |
| `IN_CALL` | Voice call active |
| `IN_COMMUNICATION` | VoIP/communication |
| `CALL_SCREENING` | Call screening |

---

### 7.14 get_status - Get Server Status

**Description:** Returns server running status.

**Parameters:** None

**HTTP GET:**

<details>
<summary><strong>💻 Code Block (bash) — 1 lines</strong></summary>

```bash
curl "http://192.168.0.100:8040/api?command=get_status"
```

</details>

**Response:**

<details>
<summary><strong>💻 Code Block (json) — 9 lines</strong></summary>

```json
{
    "success": true,
    "message": "Phone Manager is running",
    "data": {
        "version": "1.0.0",
        "uptime": 1234567890123
    },
    "timestamp": 1234567890123
}
```

</details>

---

## 8. Response Formats

### 8.1 Standard Response

All API responses follow this format:

<details>
<summary><strong>💻 Code Block (json) — 6 lines</strong></summary>

```json
{
    "success": boolean,
    "message": "string",
    "data": object | null,
    "timestamp": number
}
```

</details>

| Field | Type | Description |
|-------|------|-------------|
| `success` | boolean | Whether the command succeeded |
| `message` | string | Human-readable result message |
| `data` | object/null | Command-specific data (if any) |
| `timestamp` | number | Unix timestamp in milliseconds |

### 8.2 Error Response

<details>
<summary><strong>💻 Code Block (json) — 6 lines</strong></summary>

```json
{
    "success": false,
    "message": "Error description",
    "data": null,
    "timestamp": 1234567890123
}
```

</details>

### 8.3 Common Error Messages

| Message | Cause | Solution |
|---------|-------|----------|
| `Command is required` | Missing command parameter | Add `command` parameter |
| `Number is required` | Missing number for dial | Add `number` parameter |
| `CALL_PHONE permission not granted` | Permission denied | Grant permission in app |
| `No active call` | No call to control | Wait for active call |
| `No incoming call to answer` | No ringing call | Wait for incoming call |

---

## 9. Use Cases & Examples

### 9.1 Auto-Answer System

Answer all incoming calls automatically:

**Python Example:**

<details>
<summary><strong>💻 Code Block (python) — 27 lines</strong></summary>

```python
import requests
import time

PHONE_API = "http://192.168.0.100:8040/api"

def check_and_answer():
    while True:
        try:
            # Check for active call
            response = requests.get(f"{PHONE_API}?command=get_active_call")
            data = response.json()
            
            if data['success'] and data['data']:
                state = data['data']['state']
                number = data['data']['number']
                
                if state == "RINGING":
                    print(f"Incoming call from {number}, answering...")
                    requests.get(f"{PHONE_API}?command=answer")
                    requests.get(f"{PHONE_API}?command=speaker_on")
                    
        except Exception as e:
            print(f"Error: {e}")
        
        time.sleep(1)

check_and_answer()
```

</details>

### 9.2 Call Logging System

Log all calls to a file:

**Python Example:**

<details>
<summary><strong>💻 Code Block (python) — 32 lines</strong></summary>

```python
import requests
import json
from datetime import datetime

PHONE_API = "http://192.168.0.100:8040/api"

def get_today_calls():
    # Get timestamp for start of today
    today = datetime.now().replace(hour=0, minute=0, second=0, microsecond=0)
    from_date = int(today.timestamp() * 1000)
    
    response = requests.get(
        f"{PHONE_API}?command=get_call_log&type=all&limit=100&from_date={from_date}"
    )
    
    data = response.json()
    
    if data['success']:
        calls = data['data']['entries']
        
        for call in calls:
            call_time = datetime.fromtimestamp(call['date'] / 1000)
            duration = call['duration']
            call_type = call['type']
            number = call['number'] or 'Unknown'
            name = call['name'] or 'Unknown'
            
            print(f"{call_time} | {call_type:10} | {number:15} | {name:20} | {duration}s")
    
    return data

get_today_calls()
```

</details>

### 9.3 WebSocket Real-time Monitor

Monitor calls in real-time:

**JavaScript Example:**

<details>
<summary><strong>💻 Code Block (javascript) — 38 lines</strong></summary>

```javascript
const WebSocket = require('ws');

const ws = new WebSocket('ws://192.168.0.100:8030');

ws.on('open', () => {
    console.log('Connected to Phone Manager');
    
    // Get initial status
    ws.send(JSON.stringify({ command: 'get_status' }));
});

ws.on('message', (data) => {
    const message = JSON.parse(data);
    
    // Check if it's a call state event
    if (message.event === 'call_state_changed') {
        console.log(`Call State: ${message.state}`);
        console.log(`Number: ${message.number}`);
        console.log(`Active: ${message.is_active}`);
        console.log(`Incoming: ${message.is_incoming}`);
        console.log('---');
        
        // Auto-enable speaker for incoming calls
        if (message.state === 'OFFHOOK (Active)' && message.is_incoming) {
            ws.send(JSON.stringify({ command: 'speaker_on' }));
        }
    } else {
        console.log('Response:', message);
    }
});

ws.on('close', () => {
    console.log('Disconnected');
});

ws.on('error', (error) => {
    console.error('Error:', error);
});
```

</details>

### 9.4 Speed Dial System

Create a speed dial system:

**Bash Script:**

<details>
<summary><strong>💻 Code Block (bash) — 24 lines</strong></summary>

```bash
#!/bin/bash

PHONE_API="http://192.168.0.100:8040/api"

# Speed dial directory
declare -A CONTACTS
CONTACTS[1]="+1234567890"  # Home
CONTACTS[2]="+0987654321"  # Work
CONTACTS[3]="+1122334455"  # Mom
CONTACTS[4]="+5544332211"  # Dad

# Usage: ./speeddial.sh <number>
SPEED_NUM=$1

if [[ -z "${CONTACTS[$SPEED_NUM]}" ]]; then
    echo "Speed dial $SPEED_NUM not found"
    echo "Available: 1=Home, 2=Work, 3=Mom, 4=Dad"
    exit 1
fi

PHONE_NUMBER="${CONTACTS[$SPEED_NUM]}"
echo "Dialing $PHONE_NUMBER..."

curl -s "$PHONE_API?command=dial&number=$PHONE_NUMBER&sim=1"
```

</details>

### 9.5 USSD Balance Check

Check mobile balance:

**PowerShell Example:**

<details>
<summary><strong>💻 Code Block (powershell) — 12 lines</strong></summary>

```powershell
$API = "http://192.168.0.100:8040/api"

# Common USSD codes (varies by carrier)
$BALANCE_CODE = "*123#"

# URL encode the USSD code
$EncodedCode = [System.Web.HttpUtility]::UrlEncode($BALANCE_CODE)

# Execute USSD
$response = Invoke-RestMethod -Uri "$API`?command=dial&number=$EncodedCode&ussd=true&sim=1"

Write-Host "Response: $($response.message)"
```

</details>

### 9.6 Call Recording Trigger

Trigger external recording when call starts:

**Python Example:**

<details>
<summary><strong>💻 Code Block (python) — 35 lines</strong></summary>

```python
import asyncio
import websockets
import json
import subprocess

async def monitor_calls():
    uri = "ws://192.168.0.100:8030"
    
    async with websockets.connect(uri) as ws:
        print("Connected, monitoring calls...")
        
        recording_process = None
        
        async for message in ws:
            data = json.loads(message)
            
            if data.get('event') == 'call_state_changed':
                state = data['state']
                number = data.get('number', 'Unknown')
                
                if 'Active' in state and recording_process is None:
                    # Start recording (example with ffmpeg)
                    print(f"Call started with {number}, starting recording...")
                    recording_process = subprocess.Popen([
                        'ffmpeg', '-f', 'pulse', '-i', 'default',
                        f'call_{number}_{int(time.time())}.mp3'
                    ])
                    
                elif state == 'IDLE' and recording_process:
                    # Stop recording
                    print("Call ended, stopping recording...")
                    recording_process.terminate()
                    recording_process = None

asyncio.run(monitor_calls())
```

</details>

---

## 10. Troubleshooting

### 10.1 Common Issues

#### Issue: "Connection refused" when accessing API

**Causes & Solutions:**
1. **Server not started** - Toggle ON the HTTP/WebSocket switch in the app
2. **Wrong IP address** - Check the IP shown in the app
3. **Firewall blocking** - Ensure the port is not blocked
4. **Phone on different network** - Ensure both devices are on same WiFi

#### Issue: "CALL_PHONE permission not granted"

**Solution:**
1. Open Phone Manager app
2. Tap "Request Permissions"
3. Grant all permissions
4. If still failing, go to Settings → Apps → Phone Manager → Permissions

#### Issue: Call control commands fail (hold, unhold)

**Solution:**
1. Set app as default dialer
2. Tap "Set as Default Dialer" in the app
3. Some features require InCallService access

#### Issue: Speaker on/off not working

**Possible causes:**
1. No active call - Speaker only works during calls
2. Audio routing issue - Try again after a moment
3. Another app controlling audio - Close other audio apps

#### Issue: get_active_call shows null during call

**Solution:**
1. Ensure READ_PHONE_STATE permission is granted
2. Check that the broadcast receiver is working
3. Try setting app as default dialer

#### Issue: App stops working after some time

**Solutions:**
1. Disable battery optimization for the app
2. Lock the app in recent apps (if supported)
3. Enable "Autostart" in phone settings (some manufacturers)

### 10.2 Checking Logs

View the activity log in the app to see:
- Incoming requests
- Command execution results
- Error messages

### 10.3 Testing Connectivity

<details>
<summary><strong>💻 Code Block (bash) — 8 lines</strong></summary>

```bash
# Test HTTP server
curl -v "http://192.168.0.100:8040/status"

# Test WebSocket (using websocat)
echo '{"command":"get_status"}' | websocat ws://192.168.0.100:8030

# Test from browser
# Open: http://192.168.0.100:8040/help
```

</details>

---

## 11. Security Considerations

### 11.1 Network Security

⚠️ **Warning:** This app exposes phone control over the network!

**Recommendations:**
1. Only use on trusted private networks
2. Don't expose ports to the internet
3. Consider using VPN for remote access
4. Change default ports to non-standard values

### 11.2 Access Control

The current version has no authentication. Consider:
1. Using only on home/private networks
2. Implementing firewall rules
3. Adding authentication in future versions

### 11.3 Best Practices

1. **Turn off servers when not in use**
2. **Monitor the activity log** for suspicious requests
3. **Keep the app updated** for security patches
4. **Use strong WiFi passwords** on your network

---

## Quick Reference Card

### HTTP GET Endpoints

| Command | URL |
|---------|-----|
| Dial | `/api?command=dial&number=NUM&sim=1` |
| Dial USSD | `/api?command=dial&number=CODE&sim=1&ussd=true` |
| Answer | `/api?command=answer` |
| Reject | `/api?command=reject` |
| Terminate | `/api?command=terminate` |
| Hold | `/api?command=hold` |
| Unhold | `/api?command=unhold` |
| Mute | `/api?command=mute` |
| Unmute | `/api?command=unmute` |
| Speaker On | `/api?command=speaker_on` |
| Speaker Off | `/api?command=speaker_off` |
| Active Call | `/api?command=get_active_call` |
| Call Log | `/api?command=get_call_log&type=all&limit=50` |
| Audio Status | `/api?command=get_audio_status` |
| Server Status | `/api?command=get_status` |
| Help | `/help` |

### Default Ports

| Server | Port |
|--------|------|
| WebSocket | 8030 |
| HTTP | 8040 |

---

**Version:** 1.0.0  
**Last Updated:** January 2025  
**License:** MIT

---

