# Route Planner — Android App

A full Android WebView app wrapping your Route Planner web app.
Includes the Amazon Flex import feature, Arabic/English support,
GPS location, WhatsApp/call integration, and Smart Route (Held-Karp).

---

## Requirements

| Tool | Version | Download |
|------|---------|----------|
| Android Studio | Hedgehog 2023.1+ | https://developer.android.com/studio |
| JDK | 17+ (bundled with Android Studio) | — |
| Android SDK | API 34 (Android 14) | via Android Studio SDK Manager |

---

## How to Build (Step by Step)

### Step 1 — Install Android Studio
Download and install Android Studio from https://developer.android.com/studio

### Step 2 — Open the project
1. Open Android Studio
2. Click **"Open"** (not "New Project")
3. Navigate to this folder (`RouteplannerAndroid`) and click **OK**
4. Wait for Gradle sync to finish (first time ~2-3 minutes, downloads dependencies)

### Step 3 — Accept SDK licenses (first time only)
If prompted about missing SDK or licenses:
- Go to **Tools → SDK Manager**
- Make sure **Android 14 (API 34)** is installed
- Click **Apply** and accept all licenses

### Step 4 — Run on a real phone (recommended)
1. On your Android phone: go to **Settings → About Phone**
2. Tap **"Build Number"** 7 times to enable Developer Mode
3. Go to **Settings → Developer Options** → enable **"USB Debugging"**
4. Connect phone via USB cable
5. In Android Studio, select your phone from the device dropdown
6. Click the green **▶ Run** button

### Step 5 — Build an APK to share
1. Go to **Build → Build Bundle(s) / APK(s) → Build APK(s)**
2. Wait for build to finish
3. Click **"locate"** in the popup — the APK is at:
   `app/build/outputs/apk/debug/app-debug.apk`
4. Copy the APK to your phone and install it
   (make sure "Install from unknown sources" is enabled in phone settings)

---

## Project Structure

```
RouteplannerAndroid/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml          ← App permissions & settings
│   │   ├── java/com/routeplanner/
│   │   │   └── MainActivity.java        ← WebView setup, GPS bridge, back button
│   │   ├── assets/www/
│   │   │   └── index.html               ← Your complete web app (all features)
│   │   └── res/
│   │       ├── layout/activity_main.xml ← Full-screen WebView layout
│   │       ├── values/
│   │       │   ├── strings.xml          ← App name
│   │       │   ├── styles.xml           ← Full-screen theme
│   │       │   └── colors.xml
│   │       ├── xml/
│   │       │   └── network_security_config.xml  ← HTTPS whitelist
│   │       └── mipmap-*/
│   │           └── ic_launcher*.png     ← App icons (all densities)
│   └── build.gradle                     ← App dependencies
├── build.gradle                         ← Project build config
├── settings.gradle
└── gradle.properties
```

---

## Features & Permissions

| Feature | Permission | Why |
|---------|-----------|-----|
| Map tiles & geocoding | `INTERNET` | Load OpenStreetMap + Nominatim |
| Live GPS blue dot | `ACCESS_FINE_LOCATION` | navigator.geolocation in HTML |
| Arrival alerts | `ACCESS_FINE_LOCATION` | Distance check every 5 seconds |
| Call button | `CALL_PHONE` | Direct dial from stop card |
| WhatsApp button | `INTERNET` | Opens wa.me link |
| Dark/language prefs | `DOM_STORAGE` | localStorage in WebView |
| Screen stays on | `WAKE_LOCK` | Driver-friendly while navigating |

---

## Customising

### Change app name
Edit `app/src/main/res/values/strings.xml` → change `app_name`

### Change app icon
Replace the `ic_launcher.png` files in each `mipmap-*` folder with your own.
Use Android Studio's **"Image Asset"** tool for best results:
Right-click `res` → New → Image Asset

### Change package name (before publishing to Play Store)
In `app/build.gradle`, change `applicationId "com.routeplanner"` to something unique
like `com.yourname.routeplanner`

### Update the web app
Just replace `app/src/main/assets/www/index.html` with your new version.
No Java changes needed.

---

## Publishing to Google Play Store

1. Build a **signed release APK** or **AAB** (Android App Bundle):
   - Build → Generate Signed Bundle/APK
   - Create a new keystore (keep it safe — you need it for all future updates)
2. Create a Google Play Developer account ($25 one-time fee)
3. Upload the AAB at https://play.google.com/console

---

## Troubleshooting

**Map doesn't load**
→ Check internet connection. The map loads from OpenStreetMap CDN.

**Location not working**
→ Make sure you tapped "Allow" when the permission dialog appeared.
→ Go to phone Settings → Apps → Route Planner → Permissions → Location → Allow.

**White screen on launch**
→ Gradle sync may have failed. Try File → Sync Project with Gradle Files.

**"INSTALL_FAILED_TEST_ONLY" when installing APK**
→ Use `adb install -t app-debug.apk` or build a release APK instead.

**Back button closes app instead of navigating**
→ Normal — if there's no browser history, it shows an exit confirmation.
