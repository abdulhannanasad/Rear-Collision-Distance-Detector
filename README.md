# 🚗 Rear Obstacle Detector - Android App

A real-time rear collision avoidance Android application that uses your phone's back camera and on-device machine learning to detect obstacles and provide distance warnings - just like a car's reverse parking sensor system.

## 📱 Features

- **Live Camera Feed**: Real-time video processing using Android CameraX
- **Object Detection**: ML Kit-powered obstacle identification (vehicles, walls, poles, pedestrians)
- **Distance Estimation**: Approximate distance calculation based on object size in frame
- **Visual Warnings**: Color-coded proximity alerts (Green/Yellow/Orange/Red)
- **Progress Bar**: Visual distance indicator showing obstacle proximity
- **Works Offline**: All processing done on-device, no internet required

## 🎯 How It Works

```
Camera Feed → Object Detection → Size Analysis → Distance Estimation → Visual Warning
```

| Distance   | Warning              | Color     |
|------------|-----------------------|-----------|
| > 3 meters | Safe                  | 🟢 Green  |
| 2-3 meters | Caution               | 🟡 Yellow |
| 1-2 meters | Warning - Too Close   | 🟠 Orange |
| < 1 meter  | DANGER - STOP!        | 🔴 Red    |

## 🛠️ Tech Stack

- **Language**: Java
- **Camera**: Android CameraX
- **ML/AI**: Google ML Kit Object Detection
- **UI**: Material Design Components
- **Minimum SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 15 (API 35)

## 📋 Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- Android device/emulator with camera (API 24+)
- Gradle 8.5+

## 🚀 Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/Rear-Collision-Distance-Detector.git
   ```

2. **Open in Android Studio**
   ```
   File → Open → Select the project folder
   ```

3. **Sync Gradle**
   ```
   Click "Sync Now" when prompted
   ```

4. **Run the app**
   ```
   Build → Run (or press Shift + F10)
   ```

## 📱 Building APK

### Debug APK
```bash
./gradlew assembleDebug
```
APK location: `app/build/outputs/apk/debug/app-debug.apk`

### Release APK
```bash
./gradlew assembleRelease
```
APK location: `app/build/outputs/apk/release/app-release.apk`

## 🎮 Usage

1. Launch the app on your Android device
2. Grant camera permission when prompted
3. Point the back camera at obstacles behind you
4. Watch real-time distance updates on screen
5. Follow color-coded warnings for safe distance

> **Pro Tip:** For best results, mount your phone in a fixed position (like a phone holder) for consistent distance estimation.

## 📂 Project Structure

```
RearObstacleDetector/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/rearobstacledetector/
│   │       │   └── MainActivity.java
│   │       ├── res/
│   │       │   └── layout/
│   │       │       └── activity_main.xml
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## ⚙️ How Distance is Calculated

The app uses a bounding box analysis approach:

1. ML Kit detects objects and draws bounding boxes
2. The largest object (by area) is assumed to be closest
3. Distance is estimated using the formula:

   ```
   Distance = 1.5 / (boundingBoxHeight / screenHeight + 0.1)
   ```

4. Result is capped at 10 meters maximum

> **Note:** This provides approximate distances for demonstration purposes. For production use, integrate with actual distance sensors (ultrasonic/radar) for precise measurements.

## ⚠️ Limitations

- **Single camera** - Cannot measure actual depth (unlike stereo cameras)
- **No real sensors** - Estimates distance visually, not with ultrasonic/radar
- **Object size variation** - Large far objects may appear closer than small near objects
- **Calibration needed** - Distance accuracy depends on device camera and mounting position
- **Not safety-critical** - For demonstration/prototype purposes only

## 🔮 Future Enhancements

- [ ] Audio beeping alerts (frequency increases as distance decreases)
- [ ] Grid lines overlay like actual reverse cameras
- [ ] ARCore integration for true depth measurement
- [ ] Bluetooth connectivity with ultrasonic sensors
- [ ] Custom object detection model for vehicles
- [ ] Recording capability with distance overlay
- [ ] Widget for quick launch
- [ ] Multi-camera support for wider field of view

## 🤝 Contributing

Contributions are welcome! Here's how:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request


## 🙏 Acknowledgments

- Google ML Kit for on-device machine learning
- Android CameraX for camera functionality
- Material Design for UI components

## 📞 Contact

Hannan Asad - (abdul.hannan9969@gmail.com)

