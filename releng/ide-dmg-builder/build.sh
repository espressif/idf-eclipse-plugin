#!/usr/bin/env bash

export BUILDDIR=$(cd $(dirname $0); pwd)

echo "Create DMG installer..."
./ide-dmg-builder/create-dmg/create-dmg \
  --volname "Espressif-IDE" \
  --volicon "espressif.icns" \
  --background "background.png" \
  --window-pos 200 120 \
  --window-size 800 350 \
  --icon-size 100 \
  --icon "Espressif-IDE.app" 140 155 \
  --hide-extension "Espressif-IDE.app" \
  --app-drop-link 625 155 \
  "/target/products/Espressif-IDE.dmg" \
  "/target/products/com.espressif.idf.product/macosx/cocoa/x86_64/Espressif-IDE.app"
  