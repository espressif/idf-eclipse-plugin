#!/usr/bin/env bash

BUILDDIR=$(cd $(dirname $0); pwd)
parentdir="$(dirname "$BUILDDIR")"

echo "Create DMG installer for aarch64..."
echo $parentdir

$BUILDDIR/create-dmg/create-dmg \
  --volname "Espressif-IDE" \
  --volicon "$BUILDDIR/espressif.icns" \
  --background "$BUILDDIR/background.png" \
  --window-pos 200 120 \
  --window-size 800 350 \
  --icon-size 100 \
  --icon "Espressif-IDE.app" 140 155 \
  --hide-extension "Espressif-IDE.app" \
  --app-drop-link 625 155 \
  "$BUILDDIR/Espressif-IDE-macosx-cocoa-aarch64.dmg" \
  "$parentdir/com.espressif.idf.product/target/products/com.espressif.idf.product/macosx/cocoa/aarch64/Espressif-IDE.app"

  
