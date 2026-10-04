#!/usr/bin/env bash
set -e

echo "==================================================="
echo "    MotoScan ADV - Auto Installer via USB Debugging"
echo "==================================================="
echo ""

# 1. Check ADB
echo "[1/4] Memeriksa koneksi HP via kabel USB..."
if ! command -v adb &> /dev/null; then
    echo "[ERROR] ADB tidak ditemukan di PATH komputer Anda."
    echo "Silakan pasang Android SDK platform-tools atau buka folder 'android' langsung di Android Studio."
    exit 1
fi

DEVICES=$(adb devices | grep -w "device" || true)
if [ -z "$DEVICES" ]; then
    echo ""
    echo "[PERINGATAN] HP Android belum terdeteksi via USB Debugging!"
    echo "Pastikan:"
    echo "1. Hubungkan HP ke laptop/komputer via kabel USB."
    echo "2. Aktifkan 'Developer Options' & 'USB Debugging' di HP."
    echo "3. Izinkan popup 'Allow USB Debugging' di layar HP Anda."
    echo ""
    read -p "Tekan ENTER setelah menghubungkan HP..."
fi

# 2. Build Debug APK
echo ""
echo "[2/4] Mengompilasi APK MotoScan ADV (Debug)..."
cd android
chmod +x ./gradlew
./gradlew assembleDebug
cd ..

# 3. Install APK
echo ""
echo "[3/4] Memasang (Install) APK ke HP Android..."
adb install -r android/app/build/outputs/apk/debug/app-debug.apk

# 4. Launch App
echo ""
echo "[4/4] Membuka MotoScan ADV di HP..."
adb shell am start -n com.example.motoscanadv/.MainActivity

echo ""
echo "==================================================="
echo "[SUKSES] MotoScan ADV Berhasil Terpasang di HP Anda!"
echo "==================================================="
