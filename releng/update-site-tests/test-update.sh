#!/bin/bash
set -x
set -e
set -o pipefail

############################################
# CONFIGURATION
############################################

ECLIPSE_URL="https://ftp.osuosl.org/pub/eclipse/technology/epp/downloads/release/2025-09/R/eclipse-cpp-2025-09-R-linux-gtk-x86_64.tar.gz"
ECLIPSE_RELEASE_REPO="https://download.eclipse.org/releases/2025-09"
STABLE_ZIP_URL="https://dl.espressif.com/dl/idf-eclipse-plugin/updates/com.espressif.idf.update-v4.0.0.zip"
RC_REPO="https://dl.espressif.com/dl/idf-eclipse-plugin/updates/latest/"
FEATURE_ID="com.espressif.idf.feature.feature.group"

WORKDIR="${WORKDIR:-$PWD/releng/update-site-tests/workdir}"
LOGDIR="${LOGDIR:-$PWD/releng/update-site-tests/logs}"
REPORT="${REPORT_FILE:-$PWD/releng/update-site-tests/report.txt}"

echo "Cleaning previous workdir and logs..."
rm -rf "${WORKDIR:?}"
rm -rf "${LOGDIR:?}"

mkdir -p "$WORKDIR" "$LOGDIR"

############################################
# STEP 1: DOWNLOAD AND EXTRACT ECLIPSE
############################################

echo "Downloading Eclipse..."
wget -q "$ECLIPSE_URL" -O "$WORKDIR/eclipse.tar.gz"
tar -xzf "$WORKDIR/eclipse.tar.gz" -C "$WORKDIR"
ECLIPSE_HOME=$(find "$WORKDIR" -maxdepth 1 -type d -name "eclipse*" | head -n1)
echo "Eclipse installed at: $ECLIPSE_HOME"

############################################
# STEP 2: DOWNLOAD AND UNZIP STABLE PLUGIN
############################################

echo "Downloading stable plugin zip..."
wget -q "$STABLE_ZIP_URL" -O "$WORKDIR/stable.zip"

echo "Extracting stable plugin..."
mkdir -p "$WORKDIR/stable-repo"
unzip -q "$WORKDIR/stable.zip" -d "$WORKDIR/stable-repo"

STABLE_REPO="file://$WORKDIR/stable-repo/artifacts/update"

############################################
# STEP 3: INSTALL STABLE PLUGIN
############################################

echo "Installing stable plugin..."
if ! "$ECLIPSE_HOME/eclipse" \
  -nosplash \
  -application org.eclipse.equinox.p2.director \
  -repository "$STABLE_REPO,$ECLIPSE_RELEASE_REPO" \
  -installIU "$FEATURE_ID" \
  -destination "$ECLIPSE_HOME" \
  -profile SDKProfile \
  -bundlepool "$WORKDIR/p2" \
  -roaming \
  -consoleLog \
  | tee "$LOGDIR/stable-install.log"
then
  echo "❌ Stable plugin installation failed"
  exit 1
fi

echo "✅ Stable plugin installed successfully"

############################################
# STEP 4: INSTALL RC UPDATE
############################################

echo "Installing Release Candidate update..."
if ! "$ECLIPSE_HOME/eclipse" \
  -nosplash \
  -application org.eclipse.equinox.p2.director \
  -repository "$RC_REPO,$ECLIPSE_RELEASE_REPO" \
  -uninstallIU "$FEATURE_ID" \
  -installIU "$FEATURE_ID" \
  -destination "$ECLIPSE_HOME" \
  -profile SDKProfile \
  -bundlepool "$WORKDIR/p2" \
  -roaming \
  -consoleLog \
  | tee "$LOGDIR/rc-installation-verify.log"
then
  echo "❌ Release Candidate update failed"
  exit 1
fi

echo "✅ Release Candidate update installed successfully"

############################################
# STEP 5: CHECK FOR CONFLICTS
############################################

echo "Checking logs for conflicts..."
if grep -Ei "conflict|cannot complete|missing requirement" "$LOGDIR"/*.log; then
    echo "❌ Conflict detected in logs"
    exit 1
fi
echo "✅ No conflicts detected"

############################################
# STEP 6: GENERATE REPORT
############################################

{
    echo "Espressif IDE Upgrade Test Report"
    echo "================================"
    echo ""
    echo "Installed Roots:"
    cat "$LOGDIR/installed-roots.txt"
    echo ""
    echo "Logs directory: $LOGDIR"
} > "$REPORT"

echo "================================="
echo "✅ Upgrade test completed successfully"
echo "Report available at: $REPORT"
echo "================================="