#!/usr/bin/env bash

BUILDDIR=$(cd $(dirname $0); pwd)
parentdir="$(dirname "$BUILDDIR")"

echo "Create DMG..."
  
genisoimage -V espressif-ide -D -R -apple -no-pad -o /opt/actions-runner/_work/idf-eclipse-plugin/idf-eclipse-plugin/releng/ide-dmg-builder/Espressif-IDE.dmg /opt/actions-runner/_work/idf-eclipse-plugin/idf-eclipse-plugin/relang/com.espressif.idf.product/target/products/com.espressif.idf.product/macosx/cocoa/x86_64/Espressif-IDE.app