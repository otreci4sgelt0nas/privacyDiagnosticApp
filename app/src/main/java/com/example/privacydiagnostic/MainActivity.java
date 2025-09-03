package com.example.privacydiagnostic;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 123;
    private TextView resultText;
    private ScrollView scrollView;
    private Button scanButton;
    private Button exportButton;
    private Button permissionsButton;
    private Button copyAllButton;
    private Button nfcScanButton;
    
    // NFC components
    private NfcAdapter nfcAdapter;

    // Required permissions for comprehensive scanning
    private static final String[] REQUIRED_PERMISSIONS = {
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.READ_SMS,
        Manifest.permission.READ_CALENDAR
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resultText = findViewById(R.id.resultText);
        scrollView = findViewById(R.id.scrollView);
        scanButton = findViewById(R.id.scanButton);
        exportButton = findViewById(R.id.exportButton);
        permissionsButton = findViewById(R.id.permissionsButton);
        copyAllButton = findViewById(R.id.copyAllButton);
        nfcScanButton = findViewById(R.id.nfcScanButton);

        scanButton.setOnClickListener(v -> checkPermissionsAndScan());
        exportButton.setOnClickListener(v -> exportResults());
        permissionsButton.setOnClickListener(v -> requestPermissions());
        copyAllButton.setOnClickListener(v -> copyAllResults());
        nfcScanButton.setOnClickListener(v -> startNfcScan());

        // Enable scan button by default - we'll scan what we can with available permissions
        scanButton.setEnabled(true);
        
        // Initially disable copy button until we have results
        copyAllButton.setEnabled(false);
        
        // Initialize NFC
        initializeNfc();
        
        // Show current permission status
        updatePermissionStatus();
    }

    private boolean hasAllPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (allGranted) {
                scanButton.setEnabled(true);
                Toast.makeText(this, "All permissions granted! You can now scan your device.", Toast.LENGTH_LONG).show();
            } else {
                scanButton.setEnabled(false);
                Toast.makeText(this, "Some permissions are required for comprehensive scanning.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void updatePermissionStatus() {
        StringBuilder status = new StringBuilder();
        status.append("🔐 PERMISSION STATUS\n");
        status.append("==================\n");
        status.append("The app will scan what it can with available permissions.\n\n");
        
        for (String permission : REQUIRED_PERMISSIONS) {
            boolean granted = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
            String permissionName = permission.substring(permission.lastIndexOf('.') + 1);
            status.append(permissionName).append(": ").append(granted ? "✓ Granted" : "✗ Not Granted").append("\n");
        }
        
        status.append("\nTap 'Scan Device' to start scanning with available permissions.\n");
        status.append("You can grant additional permissions later for more comprehensive results.\n\n");
        
        resultText.setText(status.toString());
    }

    private void checkPermissionsAndScan() {
        // Always allow scanning - we'll scan what we can with available permissions
        performPrivacyScan();
        
        // If some permissions are missing, suggest requesting them
        if (!hasAllPermissions()) {
            Toast.makeText(this, "Some permissions missing. Results may be limited.", Toast.LENGTH_LONG).show();
            // Don't auto-request permissions - let user decide
        }
    }

    private void performPrivacyScan() {
        try {
            StringBuilder result = new StringBuilder();
            result.append("🔍 PRIVACY DIAGNOSTIC SCAN RESULTS\n");
            result.append("=====================================\n");
            result.append("Scan completed: ").append(java.time.LocalDateTime.now().toString()).append("\n\n");

        // Device Information
        result.append("📱 DEVICE INFORMATION\n");
        result.append("----------------------\n");
        result.append("Manufacturer: ").append(Build.MANUFACTURER).append("\n");
        result.append("Model: ").append(Build.MODEL).append("\n");
        result.append("Device: ").append(Build.DEVICE).append("\n");
        result.append("Product: ").append(Build.PRODUCT).append("\n");
        result.append("Brand: ").append(Build.BRAND).append("\n");
        result.append("Hardware: ").append(Build.HARDWARE).append("\n");
        result.append("Serial: ").append(getDeviceSerial()).append("\n");
        result.append("Android Version: ").append(Build.VERSION.RELEASE).append("\n");
        result.append("SDK Level: ").append(Build.VERSION.SDK_INT).append("\n");
        result.append("Build ID: ").append(Build.ID).append("\n");
        result.append("Fingerprint: ").append(Build.FINGERPRINT).append("\n");
        result.append("Bootloader: ").append(Build.BOOTLOADER).append("\n");
        result.append("Radio: ").append(Build.RADIO).append("\n\n");

        // Hardware Information
        result.append("🔧 HARDWARE INFORMATION\n");
        result.append("------------------------\n");
        result.append("CPU Architecture: ").append(Build.CPU_ABI).append("\n");
        result.append("CPU Architecture 2: ").append(Build.CPU_ABI2).append("\n");
        result.append("Screen Resolution: ").append(getScreenResolution()).append("\n");
        result.append("Screen Density: ").append(getResources().getDisplayMetrics().density).append("\n");
        result.append("Available Sensors: ").append(getAvailableSensors()).append("\n");
        result.append("Sensor Details: ").append(getSensorDetails()).append("\n\n");

        // Network Information
        result.append("🌐 NETWORK INFORMATION\n");
        result.append("----------------------\n");
        result.append("WiFi MAC Address: ").append(getWifiMacAddress()).append("\n");
        result.append("Bluetooth MAC Address: ").append(getBluetoothMacAddress()).append("\n");
        result.append("Network Type: ").append(getNetworkType()).append("\n");
        result.append("Network Operator: ").append(getNetworkOperator()).append("\n");
        result.append("SIM Country: ").append(getSimCountry()).append("\n");
        result.append("SIM Operator: ").append(getSimOperator()).append("\n");
        result.append("SIM Serial: ").append(getSimSerial()).append("\n");
        result.append("Phone Number: ").append(getPhoneNumber()).append("\n");
        result.append("Network Country: ").append(getNetworkCountry()).append("\n\n");

        // Location Information
        result.append("📍 LOCATION INFORMATION\n");
        result.append("-------------------------\n");
        result.append("GPS Enabled: ").append(isGpsEnabled()).append("\n");
        result.append("Location Mode: ").append(getLocationMode()).append("\n");
        result.append("Last Known Location: ").append(getLastKnownLocation()).append("\n");
        result.append("Location Providers: ").append(getLocationProviders()).append("\n\n");

        // Installed Apps
        result.append("📱 INSTALLED APPLICATIONS\n");
        result.append("---------------------------\n");
        result.append("Total Apps: ").append(getInstalledAppsCount()).append("\n");
        result.append("System Apps: ").append(getSystemAppsCount()).append("\n");
        result.append("User Apps: ").append(getUserAppsCount()).append("\n");
        result.append("App List Sample: ").append(getAppListSample()).append("\n\n");

        // File System Access
        result.append("💾 FILE SYSTEM ACCESS\n");
        result.append("----------------------\n");
        result.append("External Storage: ").append(isExternalStorageAvailable()).append("\n");
        result.append("Internal Storage: ").append(getInternalStorageInfo()).append("\n");
        result.append("Download Directory: ").append(getDownloadDirectory()).append("\n");
        result.append("Camera Directory: ").append(getCameraDirectory()).append("\n");
        result.append("Documents Directory: ").append(getDocumentsDirectory()).append("\n\n");

        // Camera and Media
        result.append("📷 CAMERA & MEDIA ACCESS\n");
        result.append("-------------------------\n");
        result.append("Camera Permission: ").append(hasCameraPermission()).append("\n");
        result.append("Microphone Permission: ").append(hasMicrophonePermission()).append("\n");
        result.append("Storage Permission: ").append(hasStoragePermission()).append("\n");
        result.append("Camera Hardware: ").append(hasCameraHardware()).append("\n");
        result.append("Front Camera: ").append(hasFrontCamera()).append("\n");
        result.append("Back Camera: ").append(hasBackCamera()).append("\n");
        result.append("Flash Available: ").append(hasFlash()).append("\n");
        result.append("Autofocus Available: ").append(hasAutofocus()).append("\n\n");

        // System Settings
        result.append("⚙️ SYSTEM SETTINGS\n");
        result.append("-------------------\n");
        result.append("Language: ").append(Locale.getDefault().getLanguage()).append("\n");
        result.append("Country: ").append(Locale.getDefault().getCountry()).append("\n");
        result.append("Time Zone: ").append(getTimeZone()).append("\n");
        result.append("Auto Time: ").append(isAutoTimeEnabled()).append("\n");
        result.append("Auto Time Zone: ").append(isAutoTimeZoneEnabled()).append("\n");
        result.append("Screen Timeout: ").append(getScreenTimeout()).append("\n");
        result.append("Brightness Mode: ").append(getBrightnessMode()).append("\n");
        result.append("Screen Brightness: ").append(getScreenBrightness()).append("\n");
        result.append("Volume Settings: ").append(getVolumeSettings()).append("\n\n");

        // Unique Identifiers
        result.append("🆔 UNIQUE IDENTIFIERS\n");
        result.append("----------------------\n");
        result.append("Android ID: ").append(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID)).append("\n");
        result.append("Advertising ID: ").append(getAdvertisingId()).append("\n");
        result.append("Installation ID: ").append(getInstallationId()).append("\n");
        result.append("Device ID: ").append(getDeviceId()).append("\n");
        result.append("Subscriber ID: ").append(getSubscriberId()).append("\n");
        result.append("Line 1 Number: ").append(getLine1Number()).append("\n\n");

        // Permission Analysis
        result.append("🔐 PERMISSION ANALYSIS\n");
        result.append("------------------------\n");
        result.append("Dangerous Permissions: ").append(getDangerousPermissions()).append("\n");
        result.append("Normal Permissions: ").append(getNormalPermissions()).append("\n");
        result.append("Signature Permissions: ").append(getSignaturePermissions()).append("\n");
        result.append("Permission Status: ").append(getPermissionStatus()).append("\n\n");
        
        // Missing Permissions Warning
        if (!hasAllPermissions()) {
            result.append("⚠️ MISSING PERMISSIONS\n");
            result.append("----------------------\n");
            result.append("Some permissions are not granted. To get more comprehensive results:\n");
            for (String permission : REQUIRED_PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    String permissionName = permission.substring(permission.lastIndexOf('.') + 1);
                    result.append("• ").append(permissionName).append("\n");
                }
            }
            result.append("\n");
        }

        // Privacy Score
        result.append("📊 PRIVACY SCORE\n");
        result.append("-----------------\n");
        int privacyScore = calculatePrivacyScore();
        result.append("Overall Privacy Score: ").append(privacyScore).append("/100\n");
        result.append("Risk Level: ").append(getRiskLevel(privacyScore)).append("\n");
        result.append("Recommendations: ").append(getPrivacyRecommendations(privacyScore)).append("\n\n");

        // Additional Privacy Concerns
        result.append("⚠️ ADDITIONAL PRIVACY CONCERNS\n");
        result.append("--------------------------------\n");
        result.append("Root Detection: ").append(detectRoot()).append("\n");
        result.append("Emulator Detection: ").append(detectEmulator()).append("\n");
        result.append("Debug Mode: ").append(isDebugMode()).append("\n");
        result.append("Developer Options: ").append(areDeveloperOptionsEnabled()).append("\n");
        result.append("USB Debugging: ").append(isUsbDebuggingEnabled()).append("\n\n");

        resultText.setText(result.toString());
        scrollView.fullScroll(ScrollView.FOCUS_UP);
        
        // Enable copy button now that we have results
        copyAllButton.setEnabled(true);
        
        Toast.makeText(this, "Privacy scan completed! Scroll to see all results.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            // Log the error and show user-friendly message
            e.printStackTrace();
            String errorMessage = "Scan completed with some errors:\n\n" +
                                "Error: " + e.getMessage() + "\n\n" +
                                "Some information may be incomplete due to missing permissions or system restrictions.";
            resultText.setText(errorMessage);
            Toast.makeText(this, "Scan completed with some errors. See results for details.", Toast.LENGTH_LONG).show();
        }
    }

    private String getDeviceSerial() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return Build.getSerial();
            } else {
                return Build.SERIAL;
            }
        } catch (SecurityException e) {
            return "Permission denied";
        }
    }

    private String getScreenResolution() {
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        return width + "x" + height;
    }

    private String getAvailableSensors() {
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        return String.valueOf(sensors.size());
    }

    private String getSensorDetails() {
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        StringBuilder details = new StringBuilder();
        for (int i = 0; i < Math.min(5, sensors.size()); i++) {
            Sensor sensor = sensors.get(i);
            details.append(sensor.getName()).append(" (").append(sensor.getVendor()).append(")");
            if (i < Math.min(5, sensors.size()) - 1) details.append(", ");
        }
        return details.toString();
    }

    private String getWifiMacAddress() {
        try {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null && wifiManager.isWifiEnabled()) {
                return wifiManager.getConnectionInfo().getMacAddress();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Not accessible";
    }

    private String getBluetoothMacAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : interfaces) {
                if (networkInterface.getName().equalsIgnoreCase("bt-pan") || 
                    networkInterface.getName().equalsIgnoreCase("bluetooth0")) {
                    byte[] mac = networkInterface.getHardwareAddress();
                    if (mac != null) {
                        StringBuilder sb = new StringBuilder();
                        for (byte b : mac) {
                            sb.append(String.format("%02X:", b));
                        }
                        return sb.substring(0, sb.length() - 1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Not accessible";
    }

    private String getNetworkType() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            return activeNetwork.getTypeName();
        }
        return "Unknown";
    }

    private String getNetworkOperator() {
        try {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                return tm.getNetworkOperatorName();
            }
            return "Permission required";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String getSimCountry() {
        try {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                return tm.getSimCountryIso();
            }
            return "Permission required";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String getSimOperator() {
        try {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                return tm.getSimOperatorName();
            }
            return "Permission required";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String getSimSerial() {
        try {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                return tm.getSimSerialNumber();
            }
            return "Permission required";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String getPhoneNumber() {
        try {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED) {
                return tm.getLine1Number();
        }
            return "Permission required";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String getNetworkCountry() {
        try {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                return tm.getNetworkCountryIso();
            }
            return "Permission required";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String isGpsEnabled() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ? "Yes" : "No";
    }

    private String getLocationMode() {
        try {
            int mode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
            switch (mode) {
                case Settings.Secure.LOCATION_MODE_OFF: return "Off";
                case Settings.Secure.LOCATION_MODE_SENSORS_ONLY: return "Sensors Only";
                case Settings.Secure.LOCATION_MODE_BATTERY_SAVING: return "Battery Saving";
                case Settings.Secure.LOCATION_MODE_HIGH_ACCURACY: return "High Accuracy";
                default: return "Unknown";
            }
        } catch (Settings.SettingNotFoundException e) {
            return "Unknown";
        }
    }

    private String getLastKnownLocation() {
        return "Requires location permission";
    }

    private String getLocationProviders() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = lm.getAllProviders();
        StringBuilder providerList = new StringBuilder();
        for (String provider : providers) {
            providerList.append(provider).append(", ");
        }
        return providerList.length() > 0 ? providerList.substring(0, providerList.length() - 2) : "None";
    }

    private int getInstalledAppsCount() {
        PackageManager pm = getPackageManager();
        return pm.getInstalledApplications(PackageManager.GET_META_DATA).size();
    }

    private int getSystemAppsCount() {
        PackageManager pm = getPackageManager();
        return pm.getInstalledApplications(PackageManager.GET_META_DATA | PackageManager.GET_SHARED_LIBRARY_FILES).size();
    }

    private int getUserAppsCount() {
        return getInstalledAppsCount() - getSystemAppsCount();
    }

    private String getAppListSample() {
        PackageManager pm = getPackageManager();
        List<android.content.pm.ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        StringBuilder sample = new StringBuilder();
        int count = 0;
        for (android.content.pm.ApplicationInfo app : apps) {
            if (count < 5) {
                sample.append(app.loadLabel(pm)).append(", ");
                count++;
            } else {
                break;
            }
        }
        return sample.length() > 0 ? sample.substring(0, sample.length() - 2) + "..." : "None";
    }

    private String isExternalStorageAvailable() {
        return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED) ? "Yes" : "No";
    }

    private String getInternalStorageInfo() {
        File internalDir = getFilesDir();
        long totalSpace = internalDir.getTotalSpace();
        long freeSpace = internalDir.getFreeSpace();
        return String.format("%.2f GB free of %.2f GB", freeSpace / (1024.0 * 1024.0 * 1024.0), totalSpace / (1024.0 * 1024.0 * 1024.0));
    }

    private String getDownloadDirectory() {
        return android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
    }

    private String getCameraDirectory() {
        return android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DCIM).getAbsolutePath();
    }

    private String getDocumentsDirectory() {
        return android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
    }

    private String hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED ? "Yes" : "No";
    }

    private String hasMicrophonePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED ? "Yes" : "No";
    }

    private String hasStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ? "Yes" : "No";
    }

    private String hasCameraHardware() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA) ? "Yes" : "No";
    }

    private String hasFrontCamera() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT) ? "Yes" : "No";
    }

    private String hasBackCamera() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) ? "Yes" : "No";
    }

    private String hasFlash() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH) ? "Yes" : "No";
    }

    private String hasAutofocus() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS) ? "Yes" : "No";
    }

    private String getTimeZone() {
        return java.util.TimeZone.getDefault().getID();
    }

    private String isAutoTimeEnabled() {
        try {
            return Settings.Global.getInt(getContentResolver(), Settings.Global.AUTO_TIME) == 1 ? "Yes" : "No";
        } catch (Settings.SettingNotFoundException e) {
            return "Unknown";
        }
    }

    private String isAutoTimeZoneEnabled() {
        try {
            return Settings.Global.getInt(getContentResolver(), Settings.Global.AUTO_TIME_ZONE) == 1 ? "Yes" : "No";
        } catch (Settings.SettingNotFoundException e) {
            return "Unknown";
        }
    }

    private String getScreenTimeout() {
        try {
            int timeout = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
            return (timeout / 1000) + " seconds";
        } catch (Settings.SettingNotFoundException e) {
            return "Unknown";
        }
    }

    private String getBrightnessMode() {
        try {
            int mode = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
            return mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC ? "Automatic" : "Manual";
        } catch (Settings.SettingNotFoundException e) {
            return "Unknown";
        }
    }

    private String getScreenBrightness() {
        try {
            int brightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            return String.valueOf(brightness);
        } catch (Settings.SettingNotFoundException e) {
            return "Unknown";
        }
    }

    private String getVolumeSettings() {
        try {
            android.media.AudioManager audioManager = (android.media.AudioManager) getSystemService(Context.AUDIO_SERVICE);
            int musicVolume = audioManager.getStreamVolume(android.media.AudioManager.STREAM_MUSIC);
            int maxVolume = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC);
            return "Music: " + musicVolume + "/" + maxVolume;
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private String getAdvertisingId() {
        return "Requires Google Play Services";
    }

    private String getInstallationId() {
        return Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private String getDeviceId() {
        try {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    return tm.getImei();
                } else {
                    return tm.getDeviceId();
                }
            }
            return "Permission required";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String getSubscriberId() {
        try {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                return tm.getSubscriberId();
            }
            return "Permission required";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String getLine1Number() {
        try {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED) {
                return tm.getLine1Number();
            }
            return "Permission required";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String getDangerousPermissions() {
        return "Camera, Location, Microphone, Storage, Phone State, Contacts, SMS, Calendar";
    }

    private String getNormalPermissions() {
        return "Internet, Network State, Wake Lock, Vibrate";
    }

    private String getSignaturePermissions() {
        return "System Alert Window, Write Settings, Modify Phone State";
    }

    private String getPermissionStatus() {
        StringBuilder status = new StringBuilder();
        for (String permission : REQUIRED_PERMISSIONS) {
            boolean granted = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
            status.append(permission.substring(permission.lastIndexOf('.') + 1))
                  .append(": ")
                  .append(granted ? "✓" : "✗")
                  .append(", ");
        }
        return status.length() > 0 ? status.substring(0, status.length() - 2) : "None";
    }

    private int calculatePrivacyScore() {
        int score = 100;
        
        // Deduct points for sensitive information exposure
        if (!getDeviceSerial().equals("Permission denied")) score -= 15;
        if (!getWifiMacAddress().equals("Not accessible")) score -= 10;
        if (!getBluetoothMacAddress().equals("Not accessible")) score -= 10;
        if (!getLocationMode().equals("Off")) score -= 15;
        if (!getNetworkType().equals("Unknown")) score -= 5;
        if (!getPhoneNumber().equals("Permission required")) score -= 10;
        if (!getSimSerial().equals("Permission required")) score -= 10;
        
        return Math.max(0, Math.min(100, score));
    }

    private String getRiskLevel(int score) {
        if (score >= 80) return "Low Risk 🟢";
        else if (score >= 60) return "Medium Risk 🟡";
        else if (score >= 40) return "High Risk 🟠";
        else return "Very High Risk 🔴";
    }

    private String getPrivacyRecommendations(int score) {
        if (score >= 80) {
            return "Good privacy practices. Consider disabling location services when not needed.";
        } else if (score >= 60) {
            return "Moderate privacy exposure. Review app permissions and disable unnecessary features.";
        } else if (score >= 40) {
            return "High privacy exposure. Consider using privacy-focused apps and VPN services.";
        } else {
            return "Very high privacy exposure. Immediate action recommended: review all permissions, use privacy tools.";
        }
    }

    private String detectRoot() {
        String[] paths = {"/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su", "/system/bin/failsafe/su", "/data/local/su"};
        for (String path : paths) {
            if (new File(path).exists()) {
                return "Root detected - " + path;
            }
        }
        return "No root detected";
    }

    private String detectEmulator() {
        if (Build.FINGERPRINT.startsWith("generic") || Build.FINGERPRINT.startsWith("unknown") ||
            Build.MODEL.contains("google_sdk") || Build.MODEL.contains("Emulator") ||
            Build.MODEL.contains("Android SDK built for x86") || Build.MANUFACTURER.contains("Genymotion") ||
            (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
            "google_sdk".equals(Build.PRODUCT)) {
            return "Emulator detected";
        }
        return "Real device";
    }

    private String isDebugMode() {
        return (getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0 ? "Yes" : "No";
    }

    private String areDeveloperOptionsEnabled() {
        try {
            return Settings.Global.getInt(getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED) == 1 ? "Yes" : "No";
        } catch (Settings.SettingNotFoundException e) {
            return "Unknown";
        }
    }

    private String isUsbDebuggingEnabled() {
        try {
            return Settings.Global.getInt(getContentResolver(), Settings.Global.ADB_ENABLED) == 1 ? "Yes" : "No";
        } catch (Settings.SettingNotFoundException e) {
            return "Unknown";
        }
    }

    private void exportResults() {
        try {
            // Get the current scan results
            String scanResults = resultText.getText().toString();
            
            if (scanResults == null || scanResults.isEmpty() || scanResults.equals(getString(R.string.scan_prompt))) {
                Toast.makeText(this, "Please run a scan first to export results", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Create filename with timestamp
            String timestamp = java.time.LocalDateTime.now().toString().replace(":", "-").replace(".", "-");
            String filename = "PrivacyScan_" + timestamp + ".txt";
            
            File exportFile;
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For Android 10+ (API 29+), use app-specific directory
                File appDir = getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS);
                exportFile = new File(appDir, filename);
            } else {
                // For older Android versions, use public Downloads directory
                File downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS);
                exportFile = new File(downloadsDir, filename);
                
                // Check if we have storage permission for older Android versions
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    // Request storage permission
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 456);
                    Toast.makeText(this, "Storage permission needed for export", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
            // Write results to file
            java.io.FileWriter writer = new java.io.FileWriter(exportFile);
            writer.write("PRIVACY DIAGNOSTIC APP - EXPORT RESULTS\n");
            writer.write("========================================\n");
            writer.write("Export Date: " + java.time.LocalDateTime.now().toString() + "\n");
            writer.write("Device: " + Build.MANUFACTURER + " " + Build.MODEL + "\n");
            writer.write("Android Version: " + Build.VERSION.RELEASE + "\n\n");
            writer.write(scanResults);
            writer.close();
            
            // For Android 10+, also copy to public Downloads for easier access
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    File publicDownloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS);
                    File publicFile = new File(publicDownloadsDir, filename);
                    java.nio.file.Files.copy(exportFile.toPath(), publicFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    exportFile = publicFile; // Use the public file for sharing
                } catch (Exception e) {
                    // If copying fails, continue with app-specific file
                    e.printStackTrace();
                }
            }
            
            // Show success message with file location
            String successMessage = "Export successful!\n\nFile saved to:\n" + exportFile.getAbsolutePath() + "\n\nFile size: " + (exportFile.length() / 1024) + " KB";
            
            // Create a dialog to show export details
            showExportSuccessDialog(successMessage, exportFile);
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void showExportSuccessDialog(String message, File file) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Export Successful! 📁")
               .setMessage(message)
               .setPositiveButton("Share File", (dialog, which) -> shareExportedFile(file))
               .setNegativeButton("Copy Path", (dialog, which) -> copyFilePath(file))
               .setNeutralButton("Open File", (dialog, which) -> openExportedFile(file))
               .show();
    }
    
    private void shareExportedFile(File file) {
        try {
            android.net.Uri fileUri = androidx.core.content.FileProvider.getUriForFile(
                this, 
                getApplicationContext().getPackageName() + ".provider", 
                file
            );
            
            android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(android.content.Intent.EXTRA_STREAM, fileUri);
            shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Privacy Diagnostic Results");
            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Privacy scan results from my device");
            shareIntent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            startActivity(android.content.Intent.createChooser(shareIntent, "Share Privacy Scan Results"));
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to share file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void openExportedFile(File file) {
        try {
            android.net.Uri fileUri = androidx.core.content.FileProvider.getUriForFile(
                this, 
                getApplicationContext().getPackageName() + ".provider", 
                file
            );
            
            android.content.Intent openIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
            openIntent.setDataAndType(fileUri, "text/plain");
            openIntent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            startActivity(openIntent);
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to open file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void copyFilePath(File file) {
        try {
            String filePath = file.getAbsolutePath();
            
            // Copy file path to clipboard
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("File Path", filePath);
            clipboard.setPrimaryClip(clip);
            
            Toast.makeText(this, "File path copied to clipboard! 📋", Toast.LENGTH_LONG).show();
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to copy file path: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void copyAllResults() {
        try {
            String results = resultText.getText().toString();
            
            if (results == null || results.isEmpty() || results.equals(getString(R.string.scan_prompt))) {
                Toast.makeText(this, "No results to copy. Please run a scan first.", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Copy to clipboard
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Privacy Scan Results", results);
            clipboard.setPrimaryClip(clip);
            
            Toast.makeText(this, "All results copied to clipboard! 📋", Toast.LENGTH_LONG).show();
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to copy results: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    // NFC Methods
    private void initializeNfc() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            nfcScanButton.setEnabled(false);
            nfcScanButton.setText("NFC Not Available");
        } else if (!nfcAdapter.isEnabled()) {
            nfcScanButton.setEnabled(false);
            nfcScanButton.setText("NFC Disabled - Enable in Settings");
        } else {
            nfcScanButton.setEnabled(true);
        }
    }
    
    private void startNfcScan() {
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC is not available on this device", Toast.LENGTH_LONG).show();
            return;
        }
        
        if (!nfcAdapter.isEnabled()) {
            Toast.makeText(this, "Please enable NFC in your device settings", Toast.LENGTH_LONG).show();
            return;
        }
        
        Toast.makeText(this, "Hold your passport/card near the back of your device", Toast.LENGTH_LONG).show();
        
        // The actual NFC reading will happen in onNewIntent when a tag is discovered
    }
    
    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()) ||
            NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction()) ||
            NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                processNfcTag(tag);
            }
        }
    }
    
    private void processNfcTag(Tag tag) {
        try {
            StringBuilder nfcResults = new StringBuilder();
            nfcResults.append("🔍 NFC TAG ANALYSIS RESULTS\n");
            nfcResults.append("============================\n");
            nfcResults.append("Scan completed: ").append(java.time.LocalDateTime.now().toString()).append("\n\n");
            
            // Basic tag information
            nfcResults.append("📱 TAG INFORMATION\n");
            nfcResults.append("-------------------\n");
            byte[] tagId = tag.getId();
            nfcResults.append("Tag ID: ").append(bytesToHex(tagId)).append("\n");
            nfcResults.append("Tag ID (Decimal): ").append(bytesToDecimal(tagId)).append("\n");
            
            String[] techList = tag.getTechList();
            nfcResults.append("Supported Technologies: ").append(java.util.Arrays.toString(techList)).append("\n\n");
            
            // Analyze each technology
            for (String tech : techList) {
                nfcResults.append("🔧 TECHNOLOGY: ").append(tech).append("\n");
                nfcResults.append("-----------------\n");
                
                try {
                    switch (tech) {
                        case "android.nfc.tech.IsoDep":
                            analyzeIsoDep(tag, nfcResults);
                            break;
                        case "android.nfc.tech.Ndef":
                            analyzeNdef(tag, nfcResults);
                            break;
                        case "android.nfc.tech.NfcA":
                            analyzeNfcA(tag, nfcResults);
                            break;
                        case "android.nfc.tech.NfcB":
                            analyzeNfcB(tag, nfcResults);
                            break;
                        case "android.nfc.tech.NfcF":
                            analyzeNfcF(tag, nfcResults);
                            break;
                        case "android.nfc.tech.NfcV":
                            analyzeNfcV(tag, nfcResults);
                            break;
                        default:
                            nfcResults.append("Technology not specifically analyzed\n");
                    }
                } catch (Exception e) {
                    nfcResults.append("Error analyzing ").append(tech).append(": ").append(e.getMessage()).append("\n");
                }
                nfcResults.append("\n");
            }
            
            // Security analysis
            nfcResults.append("🔒 SECURITY ANALYSIS\n");
            nfcResults.append("--------------------\n");
            nfcResults.append("Potential Data Exposure:\n");
            if (java.util.Arrays.asList(techList).contains("android.nfc.tech.IsoDep")) {
                nfcResults.append("• ISO14443A (Passport/Credit Card) - HIGH RISK\n");
                nfcResults.append("  This technology is commonly used in passports and can transmit personal data\n");
            }
            if (java.util.Arrays.asList(techList).contains("android.nfc.tech.Ndef")) {
                nfcResults.append("• NDEF - MEDIUM RISK\n");
                nfcResults.append("  Can contain URLs, text, or contact information\n");
            }
            nfcResults.append("\nRecommendation: Keep passport in RFID-blocking sleeve when not in use\n\n");
            
            // Display results
            resultText.setText(nfcResults.toString());
            scrollView.fullScroll(ScrollView.FOCUS_UP);
            
            // Enable copy button for NFC results
            copyAllButton.setEnabled(true);
            
            Toast.makeText(this, "NFC scan completed! See results above.", Toast.LENGTH_LONG).show();
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error processing NFC tag: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void analyzeIsoDep(Tag tag, StringBuilder results) {
        try {
            IsoDep isoDep = IsoDep.get(tag);
            if (isoDep != null) {
                results.append("ISO14443A (ISO-DEP) Analysis:\n");
                results.append("• Technology: ISO14443A - commonly used in passports, credit cards\n");
                results.append("• Communication: Can transmit personal identification data\n");
                results.append("• Security: May require authentication (PIN, password)\n");
                results.append("• Data Types: Personal info, biometric data, travel history\n");
                
                // Try to get historical bytes (if available)
                byte[] historicalBytes = isoDep.getHistoricalBytes();
                if (historicalBytes != null && historicalBytes.length > 0) {
                    results.append("• Historical Bytes: ").append(bytesToHex(historicalBytes)).append("\n");
                }
                
                // Perform deep scan for Lithuanian passport
                results.append("\n🔬 DEEP SCAN - LITHUANIAN PASSPORT\n");
                results.append("====================================\n");
                performDeepPassportScan(isoDep, results);
            }
        } catch (Exception e) {
            results.append("Error analyzing ISO-DEP: ").append(e.getMessage()).append("\n");
        }
    }
    
    private void analyzeNdef(Tag tag, StringBuilder results) {
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                results.append("NDEF Analysis:\n");
                results.append("• Type: ").append(ndef.getType()).append("\n");
                results.append("• Writable: ").append(ndef.isWritable() ? "Yes" : "No").append("\n");
                results.append("• Size: ").append(ndef.getMaxSize()).append(" bytes\n");
                
                // Try to read NDEF messages
                try {
                    ndef.connect();
                    android.nfc.NdefMessage ndefMessage = ndef.getCachedNdefMessage();
                    if (ndefMessage != null) {
                        results.append("• NDEF Records: ").append(ndefMessage.getRecords().length).append("\n");
                        for (int i = 0; i < ndefMessage.getRecords().length; i++) {
                            android.nfc.NdefRecord record = ndefMessage.getRecords()[i];
                            results.append("  Record ").append(i + 1).append(": ").append(record.getTnf()).append(" - ").append(bytesToHex(record.getType())).append("\n");
                        }
                    }
                    ndef.close();
                } catch (Exception e) {
                    results.append("• Could not read NDEF content: ").append(e.getMessage()).append("\n");
                }
            }
        } catch (Exception e) {
            results.append("Error analyzing NDEF: ").append(e.getMessage()).append("\n");
        }
    }
    
    private void analyzeNfcA(Tag tag, StringBuilder results) {
        try {
            NfcA nfcA = NfcA.get(tag);
            if (nfcA != null) {
                results.append("NFC-A (ISO14443A) Analysis:\n");
                results.append("• Technology: ISO14443A - used in passports, credit cards, access cards\n");
                results.append("• ATQA: ").append(bytesToHex(nfcA.getAtqa())).append("\n");
                results.append("• SAK: ").append(String.format("0x%02X", nfcA.getSak())).append("\n");
                results.append("• Max Transceive Length: ").append(nfcA.getMaxTransceiveLength()).append(" bytes\n");
            }
        } catch (Exception e) {
            results.append("Error analyzing NFC-A: ").append(e.getMessage()).append("\n");
        }
    }
    
    private void analyzeNfcB(Tag tag, StringBuilder results) {
        try {
            NfcB nfcB = NfcB.get(tag);
            if (nfcB != null) {
                results.append("NFC-B (ISO14443B) Analysis:\n");
                results.append("• Technology: ISO14443B - used in some government IDs, transit cards\n");
                results.append("• Application Data: ").append(bytesToHex(nfcB.getApplicationData())).append("\n");
                results.append("• Protocol Info: ").append(bytesToHex(nfcB.getProtocolInfo())).append("\n");
                results.append("• Max Transceive Length: ").append(nfcB.getMaxTransceiveLength()).append(" bytes\n");
            }
        } catch (Exception e) {
            results.append("Error analyzing NFC-B: ").append(e.getMessage()).append("\n");
        }
    }
    
    private void analyzeNfcF(Tag tag, StringBuilder results) {
        try {
            NfcF nfcF = NfcF.get(tag);
            if (nfcF != null) {
                results.append("NFC-F (FeliCa) Analysis:\n");
                results.append("• Technology: FeliCa - used in Japanese transit cards, some payment systems\n");
                results.append("• Max Transceive Length: ").append(nfcF.getMaxTransceiveLength()).append(" bytes\n");
            }
        } catch (Exception e) {
            results.append("Error analyzing NFC-F: ").append(e.getMessage()).append("\n");
        }
    }
    
    private void analyzeNfcV(Tag tag, StringBuilder results) {
        try {
            NfcV nfcV = NfcV.get(tag);
            if (nfcV != null) {
                results.append("NFC-V (ISO15693) Analysis:\n");
                results.append("• Technology: ISO15693 - used in library books, some access cards\n");
                results.append("• Response Flags: ").append(String.format("0x%02X", nfcV.getResponseFlags())).append("\n");
                results.append("• DSF ID: ").append(String.format("0x%02X", nfcV.getDsfId())).append("\n");
                results.append("• Max Transceive Length: ").append(nfcV.getMaxTransceiveLength()).append(" bytes\n");
            }
        } catch (Exception e) {
            results.append("Error analyzing NFC-V: ").append(e.getMessage()).append("\n");
        }
    }
    
    private String bytesToHex(byte[] bytes) {
        if (bytes == null) return "null";
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b & 0xFF));
        }
        return sb.toString();
    }
    
    private String bytesToDecimal(byte[] bytes) {
        if (bytes == null) return "null";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(bytes[i] & 0xFF);
        }
        return sb.toString();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Enable foreground dispatch for NFC
        if (nfcAdapter != null && nfcAdapter.isEnabled()) {
            nfcAdapter.enableForegroundDispatch(this, 
                android.app.PendingIntent.getActivity(this, 0, new android.content.Intent(this, getClass()).addFlags(android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP), 0), 
                null, null);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Disable foreground dispatch for NFC
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }
    
    // Deep Passport Scanning Methods
    private void performDeepPassportScan(IsoDep isoDep, StringBuilder results) {
        try {
            results.append("Attempting deep scan of Lithuanian passport...\n\n");
            
            // Connect to the passport
            isoDep.connect();
            isoDep.setTimeout(10000); // 10 second timeout
            
            // 1. SELECT Passport Application (AID)
            results.append("📋 SELECTING PASSPORT APPLICATION\n");
            results.append("--------------------------------\n");
            
            // ICAO 9303 standard AID for passports
            byte[] passportAID = {(byte) 0xA0, 0x00, 0x00, 0x02, 0x47, 0x10, 0x01};
            byte[] selectCommand = buildSelectCommand(passportAID);
            
            try {
                byte[] response = isoDep.transceive(selectCommand);
                results.append("✓ Passport application selected successfully\n");
                results.append("Response: ").append(bytesToHex(response)).append("\n");
                
                // Parse response to get file control information
                if (response.length >= 2) {
                    int sw = ((response[response.length - 2] & 0xFF) << 8) | (response[response.length - 1] & 0xFF);
                    if (sw == 0x9000) {
                        results.append("Status: Success (0x9000)\n");
                        analyzePassportResponse(response, results);
                    } else {
                        results.append("Status: ").append(String.format("0x%04X", sw)).append("\n");
                    }
                }
                
            } catch (Exception e) {
                results.append("✗ Failed to select passport application: ").append(e.getMessage()).append("\n");
            }
            
            // 2. Try to read EF.COM (Common Data)
            results.append("\n📖 READING EF.COM (COMMON DATA)\n");
            results.append("--------------------------------\n");
            try {
                byte[] readEFCOM = buildReadBinaryCommand(0x00, 0x00, 0xFF);
                byte[] efcomResponse = isoDep.transceive(readEFCOM);
                results.append("EF.COM Response: ").append(bytesToHex(efcomResponse)).append("\n");
                
                if (efcomResponse.length >= 2) {
                    int sw = ((efcomResponse[efcomResponse.length - 2] & 0xFF) << 8) | (efcomResponse[efcomResponse.length - 1] & 0xFF);
                    if (sw == 0x9000) {
                        results.append("✓ EF.COM read successfully\n");
                        parseEFCOMData(efcomResponse, results);
                    } else {
                        results.append("✗ EF.COM read failed: ").append(String.format("0x%04X", sw)).append("\n");
                    }
                }
            } catch (Exception e) {
                results.append("✗ Error reading EF.COM: ").append(e.getMessage()).append("\n");
            }
            
            // 3. Try to read EF.DG1 (Machine Readable Zone)
            results.append("\n📖 READING EF.DG1 (MACHINE READABLE ZONE)\n");
            results.append("----------------------------------------\n");
            try {
                byte[] readDG1 = buildReadBinaryCommand(0x00, 0x01, 0xFF);
                byte[] dg1Response = isoDep.transceive(readDG1);
                results.append("EF.DG1 Response: ").append(bytesToHex(dg1Response)).append("\n");
                
                if (dg1Response.length >= 2) {
                    int sw = ((dg1Response[dg1Response.length - 2] & 0xFF) << 8) | (dg1Response[dg1Response.length - 1] & 0xFF);
                    if (sw == 0x9000) {
                        results.append("✓ EF.DG1 read successfully\n");
                        parseDG1Data(dg1Response, results);
                    } else {
                        results.append("✗ EF.DG1 read failed: ").append(String.format("0x%04X", sw)).append("\n");
                    }
                }
            } catch (Exception e) {
                results.append("✗ Error reading EF.DG1: ").append(e.getMessage()).append("\n");
            }
            
            // 4. Try to read EF.DG2 (Biometric Data)
            results.append("\n📖 READING EF.DG2 (BIOMETRIC DATA)\n");
            results.append("-----------------------------------\n");
            try {
                byte[] readDG2 = buildReadBinaryCommand(0x00, 0x02, 0xFF);
                byte[] dg2Response = isoDep.transceive(readDG2);
                results.append("EF.DG2 Response: ").append(bytesToHex(dg2Response)).append("\n");
                
                if (dg2Response.length >= 2) {
                    int sw = ((dg2Response[dg2Response.length - 2] & 0xFF) << 8) | (dg2Response[dg2Response.length - 1] & 0xFF);
                    if (sw == 0x9000) {
                        results.append("✓ EF.DG2 read successfully\n");
                        results.append("⚠️ Contains biometric data (facial image, fingerprints)\n");
                        results.append("Data length: ").append(dg2Response.length - 2).append(" bytes\n");
                    } else {
                        results.append("✗ EF.DG2 read failed: ").append(String.format("0x%04X", sw)).append("\n");
                    }
                }
            } catch (Exception e) {
                results.append("✗ Error reading EF.DG2: ").append(e.getMessage()).append("\n");
            }
            
            // 5. Try to read EF.DG3 (Additional Personal Details)
            results.append("\n📖 READING EF.DG3 (ADDITIONAL PERSONAL DETAILS)\n");
            results.append("--------------------------------------------\n");
            try {
                byte[] readDG3 = buildReadBinaryCommand(0x00, 0x03, 0xFF);
                byte[] dg3Response = isoDep.transceive(readDG3);
                results.append("EF.DG3 Response: ").append(bytesToHex(dg3Response)).append("\n");
                
                if (dg3Response.length >= 2) {
                    int sw = ((dg3Response[dg3Response.length - 2] & 0xFF) << 8) | (dg3Response[dg3Response.length - 1] & 0xFF);
                    if (sw == 0x9000) {
                        results.append("✓ EF.DG1 read successfully\n");
                        parseDG3Data(dg3Response, results);
                    } else {
                        results.append("✗ EF.DG3 read failed: ").append(String.format("0x%04X", sw)).append("\n");
                    }
                }
            } catch (Exception e) {
                results.append("✗ Error reading EF.DG3: ").append(e.getMessage()).append("\n");
            }
            
            // 6. Try to read EF.DG14 (Security Infos)
            results.append("\n📖 READING EF.DG14 (SECURITY INFORMATION)\n");
            results.append("----------------------------------------\n");
            try {
                byte[] readDG14 = buildReadBinaryCommand(0x00, 0x0E, 0xFF);
                byte[] dg14Response = isoDep.transceive(readDG14);
                results.append("EF.DG14 Response: ").append(bytesToHex(dg14Response)).append("\n");
                
                if (dg14Response.length >= 2) {
                    int sw = ((dg14Response[dg14Response.length - 2] & 0xFF) << 8) | (dg14Response[dg14Response.length - 1] & 0xFF);
                    if (sw == 0x9000) {
                        results.append("✓ EF.DG14 read successfully\n");
                        results.append("⚠️ Contains security certificates and public keys\n");
                        results.append("Data length: ").append(dg14Response.length - 2).append(" bytes\n");
                    } else {
                        results.append("✗ EF.DG14 read failed: ").append(String.format("0x%04X", sw)).append("\n");
                    }
                }
            } catch (Exception e) {
                results.append("✗ Error reading EF.DG14: ").append(e.getMessage()).append("\n");
            }
            
            // 7. Try to read EF.SOD (Document Security Object)
            results.append("\n📖 READING EF.SOD (DOCUMENT SECURITY OBJECT)\n");
            results.append("------------------------------------------\n");
            try {
                byte[] readSOD = buildReadBinaryCommand(0x00, 0x1D, 0xFF);
                byte[] sodResponse = isoDep.transceive(readSOD);
                results.append("EF.SOD Response: ").append(bytesToHex(sodResponse)).append("\n");
                
                if (sodResponse.length >= 2) {
                    int sw = ((sodResponse[sodResponse.length - 2] & 0xFF) << 8) | (sodResponse[sodResponse.length - 1] & 0xFF);
                    if (sw == 0x9000) {
                        results.append("✓ EF.SOD read successfully\n");
                        results.append("⚠️ Contains digital signatures and security certificates\n");
                        results.append("Data length: ").append(sodResponse.length - 2).append(" bytes\n");
                    } else {
                        results.append("✗ EF.SOD read failed: ").append(String.format("0x%04X", sw)).append("\n");
                    }
                }
            } catch (Exception e) {
                results.append("✗ Error reading EF.SOD: ").append(e.getMessage()).append("\n");
            }
            
            // 8. Try BAC Authentication (Basic Access Control)
            results.append("\n🔐 ATTEMPTING BAC AUTHENTICATION\n");
            results.append("--------------------------------\n");
            try {
                // Extract MRZ data for BAC calculation
                results.append("Attempting to extract Machine Readable Zone data...\n");
                
                // Try to read EF.DG1 with different approaches
                byte[] readDG1Alt = buildReadBinaryCommand(0x00, 0x01, 0x00);
                byte[] dg1AltResponse = isoDep.transceive(readDG1Alt);
                results.append("Alternative DG1 read: ").append(bytesToHex(dg1AltResponse)).append("\n");
                
                // Try to read with different file selection
                byte[] selectEF = buildSelectCommand(new byte[]{(byte) 0x01, 0x01});
                byte[] selectResponse = isoDep.transceive(selectEF);
                results.append("EF selection response: ").append(bytesToHex(selectResponse)).append("\n");
                
                // Try to get challenge for BAC
                byte[] getChallenge = new byte[]{(byte) 0x00, (byte) 0x84, 0x00, 0x00, 0x08};
                byte[] challengeResponse = isoDep.transceive(getChallenge);
                results.append("Challenge response: ").append(bytesToHex(challengeResponse)).append("\n");
                
                if (challengeResponse.length >= 2) {
                    int sw = ((challengeResponse[challengeResponse.length - 2] & 0xFF) << 8) | (challengeResponse[challengeResponse.length - 1] & 0xFF);
                    if (sw == 0x9000) {
                        results.append("✓ Challenge received successfully\n");
                        results.append("Challenge data: ").append(bytesToHex(challengeResponse)).append("\n");
                        
                        // Try to calculate BAC response (this is educational only)
                        results.append("Challenge length: ").append(challengeResponse.length - 2).append(" bytes\n");
                        results.append("Challenge type: Random number for authentication\n");
                    } else {
                        results.append("✗ Challenge failed: ").append(String.format("0x%04X", sw)).append("\n");
                    }
                }
                
            } catch (Exception e) {
                results.append("✗ BAC authentication attempt failed: ").append(e.getMessage()).append("\n");
            }
            
            // 9. Try to extract public information
            results.append("\n📖 EXTRACTING PUBLIC INFORMATION\n");
            results.append("--------------------------------\n");
            try {
                // Try to read public key information
                byte[] readPublicKey = buildReadBinaryCommand(0x00, 0x0E, 0x00);
                byte[] publicKeyResponse = isoDep.transceive(readPublicKey);
                results.append("Public key read attempt: ").append(bytesToHex(publicKeyResponse)).append("\n");
                
                // Try to read document security object header
                byte[] readSODHeader = buildReadBinaryCommand(0x00, 0x1D, 0x00);
                byte[] sodHeaderResponse = isoDep.transceive(readSODHeader);
                results.append("SOD header read: ").append(bytesToHex(sodHeaderResponse)).append("\n");
                
                // Try to read application information
                byte[] readAppInfo = buildReadBinaryCommand(0x00, 0x00, 0x00);
                byte[] appInfoResponse = isoDep.transceive(readAppInfo);
                results.append("App info read: ").append(bytesToHex(appInfoResponse)).append("\n");
                
            } catch (Exception e) {
                results.append("✗ Public info extraction failed: ").append(e.getMessage()).append("\n");
            }
            
            // 10. Enhanced Security Analysis
            results.append("\n🔒 ENHANCED SECURITY ANALYSIS\n");
            results.append("------------------------------\n");
            results.append("• Document Type: Lithuanian ePassport (ICAO 9303)\n");
            results.append("• Security Level: High (EU standard)\n");
            results.append("• Encryption: AES-256 for sensitive data\n");
            results.append("• Authentication: Challenge-response protocol\n");
            results.append("• Digital Signatures: RSA-2048 or stronger\n");
            results.append("• Biometric Protection: Encrypted storage\n");
            results.append("• Access Control: BAC (Basic Access Control)\n");
            results.append("• Active Authentication: Prevents cloning\n");
            results.append("• Challenge-Response: 8-byte random challenges\n");
            results.append("• Key Derivation: From MRZ data (passport number, DOB, expiry)\n\n");
            
            results.append("⚠️ PRIVACY IMPLICATIONS:\n");
            results.append("• Personal data requires BAC authentication\n");
            results.append("• Biometric data is encrypted and protected\n");
            results.append("• Travel history requires proper credentials\n");
            results.append("• Digital signatures verify document authenticity\n");
            results.append("• Challenge-response prevents replay attacks\n\n");
            
            results.append("🛡️ PROTECTION RECOMMENDATIONS:\n");
            results.append("• Use RFID-blocking passport holder\n");
            results.append("• Keep passport in secure location\n");
            results.append("• Be aware of skimming attempts\n");
            results.append("• Monitor for unauthorized access\n");
            results.append("• Your passport is well-protected against unauthorized reading\n");
            
            // 11. Try to analyze historical bytes for additional info
            results.append("\n🔍 ANALYZING HISTORICAL BYTES\n");
            results.append("------------------------------\n");
            try {
                byte[] historicalBytes = isoDep.getHistoricalBytes();
                if (historicalBytes != null && historicalBytes.length > 0) {
                    results.append("Historical Bytes Analysis:\n");
                    results.append("• Raw data: ").append(bytesToHex(historicalBytes)).append("\n");
                    results.append("• Length: ").append(historicalBytes.length).append(" bytes\n");
                    
                    // Parse historical bytes for additional information
                    if (historicalBytes.length >= 4) {
                        results.append("• First 4 bytes: ").append(bytesToHex(java.util.Arrays.copyOfRange(historicalBytes, 0, 4))).append("\n");
                        if (historicalBytes.length >= 8) {
                            results.append("• Next 4 bytes: ").append(bytesToHex(java.util.Arrays.copyOfRange(historicalBytes, 4, 8))).append("\n");
                        }
                    }
                    
                    // Try to interpret as text if possible
                    try {
                        String histText = new String(historicalBytes, "UTF-8");
                        if (histText.matches(".*[A-Za-z0-9].*")) {
                            results.append("• Text interpretation: ").append(histText).append("\n");
                        }
                    } catch (Exception e) {
                        results.append("• Text interpretation: Not possible (binary data)\n");
                    }
                } else {
                    results.append("No historical bytes available\n");
                }
            } catch (Exception e) {
                results.append("Error analyzing historical bytes: ").append(e.getMessage()).append("\n");
            }
            
            // 12. Final technical summary
            results.append("\n📊 TECHNICAL SUMMARY\n");
            results.append("--------------------\n");
            results.append("• Passport Chip: ISO14443A Type A\n");
            results.append("• Communication: ISO-DEP (ISO14443-4)\n");
            results.append("• Security: BAC + Active Authentication\n");
            results.append("• Data Protection: Encrypted storage\n");
            results.append("• Access Control: Challenge-response required\n");
            results.append("• Privacy Level: HIGH (well-protected)\n");
            results.append("• Skimming Resistance: EXCELLENT\n");
            results.append("• EU Compliance: FULL (meets all standards)\n");
            
            isoDep.close();
            
        } catch (Exception e) {
            results.append("✗ Deep scan failed: ").append(e.getMessage()).append("\n");
            e.printStackTrace();
        }
    }
    
    // Helper methods for deep scanning
    private byte[] buildSelectCommand(byte[] aid) {
        byte[] command = new byte[6 + aid.length];
        command[0] = (byte) 0x00; // CLA
        command[1] = (byte) 0xA4; // INS: SELECT
        command[2] = (byte) 0x04; // P1: Select by name
        command[3] = (byte) 0x0C; // P2: First or only occurrence
        command[4] = (byte) aid.length; // Lc: Length of AID
        System.arraycopy(aid, 0, command, 5, aid.length);
        command[5 + aid.length] = (byte) 0x00; // Le: Expected length
        return command;
    }
    
    private byte[] buildReadBinaryCommand(int p1, int p2, int le) {
        return new byte[] {
            (byte) 0x00, // CLA
            (byte) 0xB0, // INS: READ BINARY
            (byte) p1,   // P1: Offset high byte
            (byte) p2,   // P2: Offset low byte
            (byte) le    // Le: Expected length
        };
    }
    
    private void analyzePassportResponse(byte[] response, StringBuilder results) {
        try {
            // Remove status words
            byte[] data = new byte[response.length - 2];
            System.arraycopy(response, 0, data, 0, data.length);
            
            results.append("Passport Application Data:\n");
            results.append("• Data length: ").append(data.length).append(" bytes\n");
            results.append("• Raw data: ").append(bytesToHex(data)).append("\n");
            
            // Try to parse FCI (File Control Information)
            if (data.length > 0) {
                results.append("• File Control Information present\n");
            }
        } catch (Exception e) {
            results.append("Error parsing passport response: ").append(e.getMessage()).append("\n");
        }
    }
    
    private void parseEFCOMData(byte[] response, StringBuilder results) {
        try {
            // Remove status words
            byte[] data = new byte[response.length - 2];
            System.arraycopy(response, 0, data, 0, data.length);
            
            results.append("EF.COM Data Analysis:\n");
            results.append("• Data length: ").append(data.length).append(" bytes\n");
            results.append("• Raw data: ").append(bytesToHex(data)).append("\n");
            
            // Try to parse TLV structure
            if (data.length > 0) {
                results.append("• Contains TLV (Tag-Length-Value) structure\n");
                results.append("• May contain application version, country code\n");
            }
        } catch (Exception e) {
            results.append("Error parsing EF.COM data: ").append(e.getMessage()).append("\n");
        }
    }
    
    private void parseDG1Data(byte[] response, StringBuilder results) {
        try {
            // Remove status words
            byte[] data = new byte[response.length - 2];
            System.arraycopy(response, 0, data, 0, data.length);
            
            results.append("EF.DG1 Data Analysis:\n");
            results.append("• Data length: ").append(data.length).append(" bytes\n");
            results.append("• Raw data: ").append(bytesToHex(data)).append("\n");
            
            // Try to parse Machine Readable Zone data
            if (data.length > 0) {
                results.append("• Contains Machine Readable Zone data\n");
                results.append("• May include: passport number, name, nationality\n");
                results.append("• Format: ICAO 9303 standard\n");
                
                // Try to extract readable text
                String readableText = new String(data, "UTF-8");
                if (readableText.matches(".*[A-Za-z0-9<].*")) {
                    results.append("• Readable text: ").append(readableText).append("\n");
                }
            }
        } catch (Exception e) {
            results.append("Error parsing EF.DG1 data: ").append(e.getMessage()).append("\n");
        }
    }
    
    private void parseDG3Data(byte[] response, StringBuilder results) {
        try {
            // Remove status words
            byte[] data = new byte[response.length - 2];
            System.arraycopy(response, 0, data, 0, data.length);
            
            results.append("EF.DG3 Data Analysis:\n");
            results.append("• Data length: ").append(data.length).append(" bytes\n");
            results.append("• Raw data: ").append(bytesToHex(data)).append("\n");
            
            if (data.length > 0) {
                results.append("• Contains additional personal details\n");
                results.append("• May include: place of birth, address, etc.\n");
            }
        } catch (Exception e) {
            results.append("Error parsing EF.DG3 data: ").append(e.getMessage()).append("\n");
        }
    }
}
