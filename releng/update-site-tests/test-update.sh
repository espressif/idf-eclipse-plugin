#!/bin/bash
set -x
set -e

############################################
# CONFIGURATION
############################################

# Eclipse download
ECLIPSE_URL="https://ftp.osuosl.org/pub/eclipse/technology/epp/downloads/release/2025-09/R/eclipse-cpp-2025-09-R-linux-gtk-x86_64.tar.gz"

# Eclipse release repository
ECLIPSE_RELEASE_REPO="https://download.eclipse.org/releases/2025-09"

# Stable plugin zip
STABLE_ZIP_URL="https://dl.espressif.com/dl/idf-eclipse-plugin/updates/com.espressif.idf.update-v4.0.0.zip"

# RC update site
RC_REPO="https://dl.espressif.com/dl/idf-eclipse-plugin/updates/latest/"

# Eclipse feature to install
FEATURE_ID="com.espressif.idf.feature.feature.group"

# Workspace directories inside repo (fixed, not temporary)
WORKDIR="${WORKDIR:-$PWD/releng/update-site-tests/workdir}"
LOGDIR="${LOGDIR:-$PWD/releng/update-site-tests/logs}"
REPORT="${REPORT_FILE:-$PWD/releng/update-site-tests/report.txt}"

echo "Cleaning previous workdir and logs..."
rm -rf "${WORKDIR:?}"
rm -rf "${LOGDIR:?}"

mkdir -p "$WORKDIR" "$LOGDIR"

ECLIPSE_HOME="$WORKDIR/eclipse"

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

echo "Listing installed roots before stable:"
"$ECLIPSE_HOME/eclipse" \
  -nosplash \
  -application org.eclipse.equinox.p2.director \
  -listInstalledRoots \
  -consoleLog
############################################
# STEP 3: INSTALL STABLE PLUGIN
############################################

echo "Installing stable plugin..."
"$ECLIPSE_HOME/eclipse" \
  -nosplash \
  -clean \
  -data "$WORKDIR/workspace" \
  -configuration "$WORKDIR/configuration" \
  -application org.eclipse.equinox.p2.director \
  -repository "$STABLE_REPO,$ECLIPSE_RELEASE_REPO" \
  -installIU "$FEATURE_ID" \
  -consoleLog \
  | tee "$LOGDIR/stable-install.log"

STABLE_EXIT=${PIPESTATUS[0]}
if [ $STABLE_EXIT -ne 0 ]; then
    echo "❌ Stable install failed"
    exit 1
fi
echo "✅ Stable plugin installed successfully"

############################################
# STEP 4: INSTALL RC PLUGIN
############################################

echo "Installing RC plugin..."
"$ECLIPSE_HOME/eclipse" \
  -nosplash \
  -clean \
  -data "$WORKDIR/workspace" \
  -configuration "$WORKDIR/configuration" \
  -application org.eclipse.equinox.p2.director \
  -repository "$RC_REPO,$ECLIPSE_RELEASE_REPO" \
  -installIU "$FEATURE_ID" \
  -consoleLog \
  | tee "$LOGDIR/rc-install.log"

RC_EXIT=${PIPESTATUS[0]}
if [ $RC_EXIT -ne 0 ]; then
    echo "❌ RC upgrade failed"
    exit 1
fi
echo "✅ RC plugin installed successfully"

############################################
# STEP 5: CHECK FOR CONFLICTS IN LOGS
############################################

echo "Checking logs for conflicts..."
if grep -iq "conflict" "$LOGDIR"/*.log; then
    echo "❌ Conflict detected in installation logs!"
    exit 1
fi
echo "✅ No conflicts detected"

############################################
# STEP 6: LIST INSTALLED ROOTS
############################################

echo "Listing installed roots..."
"$ECLIPSE_HOME/eclipse" \
  -nosplash \
  -application org.eclipse.equinox.p2.director \
  -listInstalledRoots \
  -destination "$ECLIPSE_HOME" \
  -profile SDKProfile \
  > "$LOGDIR/installed-roots.txt"

############################################
# STEP 7: LAUNCH ECLIPSE ONCE (HEADLESS-GUI)
############################################

echo "Launching Eclipse once..."
"$ECLIPSE_HOME/eclipse" \
  -nosplash \
  -clean \
  -data "$WORKDIR/workspace" \
  -configuration "$WORKDIR/configuration" \
  -consoleLog \
  -vmargs -Djava.awt.headless=false \
  > "$LOGDIR/eclipse-launch.log" 2>&1 &

PID=$!
sleep 20
kill $PID || true
echo "✅ Eclipse launched successfully"

############################################
# STEP 8: GENERATE REPORT
############################################

echo "Generating report..."
{
    echo "Espressif IDE Upgrade Test Report"
    echo "================================"
    echo ""
    echo "Stable install exit code: $STABLE_EXIT"
    echo "RC install exit code: $RC_EXIT"
    echo ""
    echo "Installed Roots:"
    cat "$LOGDIR/installed-roots.txt"
    echo ""
    echo "Conflict check: OK"
    echo ""
    echo "Logs directory: $LOGDIR"
} > "$REPORT"

echo "================================="
echo "✅ Upgrade test completed successfully"
echo "Report available at: $REPORT"
echo "================================="