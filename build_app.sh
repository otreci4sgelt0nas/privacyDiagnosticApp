#!/bin/bash

# Privacy Diagnostic App Build Script
# This script helps build the Android app from source

echo "🔍 Building Privacy Diagnostic App..."
echo "====================================="

# Check if we're in the right directory
if [ ! -f "app/build.gradle" ]; then
    echo "❌ Error: Please run this script from the PrivacyDiagnosticApp directory"
    exit 1
fi

# Check if Android SDK is available
if [ -z "$ANDROID_HOME" ]; then
    echo "⚠️  Warning: ANDROID_HOME environment variable not set"
    echo "   This might cause build issues if Android SDK is not in PATH"
fi

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "❌ Error: Java is not installed or not in PATH"
    echo "   Please install Java 8 or higher"
    exit 1
fi

echo "✅ Java version: $(java -version 2>&1 | head -n 1)"

# Check if Gradle is available
if ! command -v gradle &> /dev/null; then
    echo "⚠️  Warning: Gradle not found in PATH"
    echo "   Attempting to use Gradle wrapper..."
    
    if [ -f "gradlew" ]; then
        echo "✅ Found Gradle wrapper"
        GRADLE_CMD="./gradlew"
    else
        echo "❌ Error: No Gradle or Gradle wrapper found"
        echo "   Please install Gradle or ensure gradlew exists"
        exit 1
    fi
else
    echo "✅ Gradle version: $(gradle --version | head -n 1)"
    GRADLE_CMD="gradle"
fi

echo ""
echo "🚀 Starting build process..."

# Clean previous builds
echo "🧹 Cleaning previous builds..."
$GRADLE_CMD clean

# Build the app
echo "🔨 Building app..."
$GRADLE_CMD assembleDebug

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Build successful!"
    echo ""
    echo "📱 APK location: app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    echo "📋 To install on your device:"
    echo "   1. Enable 'Install from Unknown Sources' in your device settings"
    echo "   2. Transfer the APK to your device"
    echo "   3. Install the APK"
    echo ""
    echo "🔍 The app will show you exactly what information other apps can access!"
else
    echo ""
    echo "❌ Build failed!"
    echo "   Check the error messages above for details"
    echo ""
    echo "💡 Common solutions:"
    echo "   - Make sure Android SDK is properly installed"
    echo "   - Check that all dependencies are available"
    echo "   - Verify Java version compatibility"
    exit 1
fi
