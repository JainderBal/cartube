# CarTube

YouTube (m.youtube.com in a WebView) on the Android Auto screen. Personal sideload app.

## How it works
- Registers as an Android Auto **navigation** app, which Android Auto hands a raw drawing surface.
- A private `VirtualDisplay` renders onto that surface; a `Presentation` on it holds the WebView.
- Car taps (`onClick`) are replayed as touch events into the WebView; drags (in PAN mode) scroll it.
- Top buttons: **Search** (car keyboard/voice), **Back**, **Home**. PAN button = drag-to-scroll mode.
- The phone app is a normal YouTube WebView: sign in there and the car side shares the login.

## Build (Windows, Android Studio)
1. Install Android Studio (it bundles JDK 17 and the SDK).
2. File → Open → this `CarTube` folder. Let Gradle sync (it creates the wrapper and downloads deps).
3. Build → Build App Bundle(s)/APK(s) → Build APK(s).
   APK: `app/build/outputs/apk/debug/app-debug.apk`
   Or plug in your phone with USB debugging on and press Run.

## Enable it in Android Auto (one-time, on the phone)
1. Phone Settings → search "Android Auto" → open Android Auto settings.
2. Scroll to the bottom, tap **Version** about 10 times → OK to enable developer mode.
3. ⋮ menu (top right) → **Developer settings** → turn on **Unknown sources**.
4. Back in Android Auto settings → **Customize launcher** → make sure CarTube is checked.
5. Connect to the car (or the Desktop Head Unit emulator) and open CarTube.

## Testing without the car: Desktop Head Unit (DHU)
1. Android Studio → SDK Manager → SDK Tools → check **Android Auto Desktop Head Unit Emulator**.
2. Android Auto developer settings (⋮) → **Start head unit server**.
3. On the PC: `adb forward tcp:5277 tcp:5277`
4. Run `desktop-head-unit.exe` from `%LOCALAPPDATA%\Android\Sdk\extras\google\auto\`.
   Add `--usb` instead if your phone is plugged in and you skip steps 2–3.

## Known limits
- Taps require Car API level 5+ hosts (current Android Auto is fine).
- Text boxes on the page can't open a keyboard on the car; use the Search button.
- If Android Auto ever stops showing the app, re-check "Unknown sources" (updates can reset it).
