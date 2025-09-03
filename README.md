# 🔒 Privacy Diagnostic App

A comprehensive Android application that scans your device to reveal what information other apps can access, helping you understand your privacy exposure and take control of your data.

## 🌟 Features

### 📱 **Privacy Scanning**
- **Device Information**: Manufacturer, model, Android version, hardware details
- **Hardware Analysis**: CPU architecture, sensors, screen specifications
- **Network Exposure**: WiFi/Bluetooth MAC addresses, network operators
- **Location Services**: GPS status, location providers, location mode
- **App Permissions**: Installed apps count, system vs user apps
- **File System Access**: Storage permissions, directory access
- **Camera & Media**: Camera hardware, microphone, flash capabilities
- **System Settings**: Language, timezone, brightness, volume settings
- **Unique Identifiers**: Android ID, device ID, SIM information
- **Permission Analysis**: Dangerous, normal, and signature permissions
- **Privacy Score**: Overall privacy risk assessment with recommendations

### 🛡️ **Safety Features**
- **Crash Protection**: Graceful handling of missing permissions
- **Partial Scanning**: Works with available permissions
- **User Control**: Choose which permissions to grant
- **Error Handling**: Comprehensive error reporting

### 📁 **Export & Sharing**
- **Text Export**: Save results as timestamped text files
- **Copy/Paste**: Select and copy specific results or copy all
- **File Sharing**: Share results via email, messaging, or other apps
- **Cross-Platform**: Works on Android 5.0+ (API 21+)

## 🚀 Getting Started

### Prerequisites
- Android 5.0+ (API 21+)
- Java 17 or higher
- Android Studio (for development)

### Installation

#### Option 1: Download APK
1. Download the latest APK from [Releases](https://github.com/otreci4sgelt0nas/privacyDiagnosticApp/releases)
2. Enable "Install from Unknown Sources" in your device settings
3. Install the APK

#### Option 2: Build from Source
```bash
# Clone the repository
git clone https://github.com/otreci4sgelt0nas/privacyDiagnosticApp.git
cd privacyDiagnosticApp

# Build the app
./build_app.sh
# or manually with Gradle
gradle clean assembleDebug
```

### Usage

1. **Launch the App**: Open Privacy Diagnostic from your app drawer
2. **Review Permissions**: See which permissions are currently granted
3. **Scan Device**: Tap "Scan Device" to analyze your privacy exposure
4. **Review Results**: Scroll through comprehensive scan results
5. **Export/Share**: Use "Export" to save results or "Copy All" for clipboard
6. **Grant Permissions**: Use "Request Permissions" for more detailed results

## 🔐 Required Permissions

The app requests these permissions for comprehensive scanning:

- **Location**: `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`
- **Phone**: `READ_PHONE_STATE`, `READ_PHONE_NUMBERS`
- **Storage**: `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`
- **Contacts**: `READ_CONTACTS`
- **Call Log**: `READ_CALL_LOG`
- **SMS**: `READ_SMS`
- **Calendar**: `READ_CALENDAR`
- **Camera**: `CAMERA`
- **Microphone**: `RECORD_AUDIO`

**Note**: The app works with partial permissions - you can scan what's available without granting all permissions.

## 🏗️ Architecture

- **Language**: Java
- **Minimum SDK**: API 21 (Android 5.0)
- **Target SDK**: API 33 (Android 13)
- **Build System**: Gradle
- **Dependencies**: AndroidX, Material Design components

## 📁 Project Structure

```
PrivacyDiagnosticApp/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/privacydiagnostic/
│   │   │   └── MainActivity.java          # Main app logic
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   └── activity_main.xml     # UI layout
│   │   │   ├── drawable/                  # App icons and backgrounds
│   │   │   ├── mipmap/                    # Launcher icons
│   │   │   ├── values/                    # Strings, colors, themes
│   │   │   └── xml/                       # File provider paths
│   │   └── AndroidManifest.xml            # App permissions and components
│   └── build.gradle                       # App-level build configuration
├── build.gradle                           # Project-level build configuration
├── build_app.sh                           # Build script
├── gradle.properties                      # Gradle configuration
└── README.md                              # This file
```

## 🛠️ Development

### Building
```bash
# Clean and build
gradle clean assembleDebug

# Install on connected device
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Key Components

- **MainActivity**: Core scanning logic and UI management
- **Permission Handling**: Dynamic permission requests and validation
- **Privacy Scanner**: Comprehensive device analysis engine
- **Export System**: File creation, sharing, and clipboard integration
- **Error Handling**: Graceful degradation and user feedback

## 🔒 Privacy & Security

- **No Data Collection**: The app doesn't send any data to external servers
- **Local Processing**: All scanning and analysis happens on your device
- **Permission Transparency**: Clear explanation of why each permission is needed
- **User Control**: You decide which permissions to grant
- **Open Source**: Full transparency of what the app does

## 🤝 Contributing

Contributions are welcome! Please feel free to submit issues, feature requests, or pull requests.

### Development Guidelines
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Built with Android development best practices
- Uses modern AndroidX libraries
- Implements comprehensive error handling
- Designed for user privacy and control

## 📞 Support

If you encounter any issues or have questions:
- Check the [Issues](https://github.com/otreci4sgelt0nas/privacyDiagnosticApp/issues) page
- Create a new issue with detailed information
- Include your device model and Android version

---

**⚠️ Disclaimer**: This app is for educational and privacy awareness purposes. Always review app permissions and be cautious about granting sensitive permissions to any application.

