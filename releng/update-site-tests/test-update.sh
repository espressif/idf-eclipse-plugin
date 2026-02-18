#!/bin/bash
set -x
set -e
set -o pipefail

# CONFIGURATION
ECLIPSE_URL="${ECLIPSE_URL:?ECLIPSE_URL not set}"
LATEST_ECLIPSE_RELEASE="${LATEST_ECLIPSE_RELEASE:?LATEST_ECLIPSE_RELEASE not set}"
RC_ZIP="${RC_ZIP:?RC_ZIP not set}"

ECLIPSE_RELEASE_REPO="https://download.eclipse.org/releases/$LATEST_ECLIPSE_RELEASE"
STABLE_PLUGIN_RELEASE_REPO="https://dl.espressif.com/dl/idf-eclipse-plugin/updates/latest/"
FEATURE_ID="com.espressif.idf.feature.feature.group"

WORKDIR="${WORKDIR:-$PWD/releng/update-site-tests/workdir}"
LOGDIR="${LOGDIR:-$PWD/releng/update-site-tests/logs}"
REPORT="${REPORT_FILE:-$PWD/releng/update-site-tests/report.txt}"

echo "Cleaning previous workdir and logs..."
rm -rf "${WORKDIR:?}"
rm -rf "${LOGDIR:?}"

mkdir -p "$WORKDIR" "$LOGDIR"

STEP_SUMMARY=()

# STEP 1: DOWNLOAD AND EXTRACT ECLIPSE
echo "Downloading Eclipse..."
wget -q "$ECLIPSE_URL" -O "$WORKDIR/eclipse.tar.gz"
tar -xzf "$WORKDIR/eclipse.tar.gz" -C "$WORKDIR"
ECLIPSE_HOME=$(find "$WORKDIR" -maxdepth 1 -type d -name "eclipse*" | head -n1)
echo "Eclipse installed at: $ECLIPSE_HOME"
STEP_SUMMARY+=("Step 1: Eclipse downloaded and extracted - ✅")

# STEP 2: UNZIP RC
mkdir -p "$WORKDIR/rc-repo"
unzip -q "$RC_ZIP" -d "$WORKDIR/rc-repo"

RC_REPO="file://$WORKDIR/rc-repo/com.espressif.idf.update-*"

STEP_SUMMARY+=("Step 2: RC unzipped - ✅")

# STEP 3: INSTALL PLUGIN STABLE RELEASE
echo "Installing plugin stable release..."
if ! "$ECLIPSE_HOME/eclipse" \
  -nosplash \
  -application org.eclipse.equinox.p2.director \
  -repository "$STABLE_PLUGIN_RELEASE_REPO,$ECLIPSE_RELEASE_REPO" \
  -installIU "$FEATURE_ID" \
  -destination "$ECLIPSE_HOME" \
  -profile SDKProfile \
  -bundlepool "$WORKDIR/p2" \
  -roaming \
  -consoleLog \
  | tee "$LOGDIR/stable-install.log"
then
 STEP_SUMMARY+=("Step 3: Plugin stable release installation - ❌ FAILED")
  echo "❌ Plugin stable release installation failed"
  exit 1
fi

echo "✅ Plugin stable release installed successfully"
STEP_SUMMARY+=("Step 3: Plugin stable release installed successfully - ✅")

# STEP 4: INSTALL RC UPDATE
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
  STEP_SUMMARY+=("Step 4: Release Candidate update installation - ❌ FAILED")
  echo "❌ Release Candidate update failed"
  exit 1
fi

echo "✅ Release Candidate update installed successfully"
STEP_SUMMARY+=("Step 4: Release Candidate update installed successfully - ✅")

# STEP 5: EXTRACT INSTALLED VERSIONS
STABLE_VERSION=$(grep -Eo "Installing $FEATURE_ID [0-9\.]+" "$LOGDIR/stable-install.log" | awk '{print $3}')
RC_VERSION=$(grep -Eo "Installing $FEATURE_ID [0-9\.]+" "$LOGDIR/rc-installation-verify.log" | awk '{print $3}')
UNINSTALL_VERSION=$(grep -Eo "Installing $FEATURE_ID [0-9\.]+" "$LOGDIR/rc-installation-verify.log" | awk '{print $3}')

echo "✅ Versions summary:"
echo "  Stable installed: $STABLE_VERSION"
echo "  RC update applied: $RC_VERSION"
echo "  RC update replaced: $UNINSTALL_VERSION"

# STEP 6: CHECK FOR CONFLICTS
echo "Checking logs for conflicts..."
ERROR_PATTERNS="conflict|cannot complete|missing requirement"
CONFLICT_FILE="$LOGDIR/conflicts-detected.txt"

if grep -Ei "$ERROR_PATTERNS" "$LOGDIR"/*.log > "$CONFLICT_FILE"; then
    echo "❌ Conflicts detected"
    STEP_SUMMARY+=("Step 5: Conflict check - ❌ conflicts found")
    CONFLICT_STATUS="FAILED"
else
    echo "✅ No conflicts detected"
    echo "No conflicts found." > "$CONFLICT_FILE"
    STEP_SUMMARY+=("Step 5: Conflict check - ✅")
    CONFLICT_STATUS="PASSED"
fi

# STEP 7: CAPTURE INSTALLED ROOTS
echo "Capturing installed roots..."
if ! "$ECLIPSE_HOME/eclipse" \
  -nosplash \
  -application org.eclipse.equinox.p2.director \
  -listInstalledRoots \
  -destination "$ECLIPSE_HOME" \
  -profile SDKProfile \
  -consoleLog \
  | tee "$LOGDIR/installed-roots.txt"
then
  STEP_SUMMARY+=("Step 6: Installed roots captured - ❌ FAILED")
  echo "❌ Installed roots captured failed"
  exit 1
fi
echo "✅ Installed roots captured"
STEP_SUMMARY+=("Step 6: Installed roots captured - ✅")

# STEP 8: GENERATE REPORT
{
    echo "ESP Eclipse Plug-in 'Update Site Test' Report"
    echo "=============================================="
    echo ""

    echo "Summary:"
    for step in "${STEP_SUMMARY[@]}"; do
        echo "  - $step"
    done

    echo ""
    echo "Environment:"
    echo "  - Eclipse Version: $LATEST_ECLIPSE_RELEASE"
    echo "  - Eclipse URL: $ECLIPSE_URL"
    echo "  - Release Repo: $ECLIPSE_RELEASE_REPO"

    echo ""
    echo "Versions Summary:"
    echo "  - Stable installed: $STABLE_VERSION"
    echo "  - RC update applied: $RC_VERSION"
    echo "  - RC update replaced: $UNINSTALL_VERSION"

    echo ""
    echo "Conflict Status: $CONFLICT_STATUS"
    echo "Conflict Details:"
    cat "$CONFLICT_FILE"

    echo ""
    echo "Installed Roots:"
    cat "$LOGDIR/installed-roots.txt"

    echo ""
    echo "Logs directory: $LOGDIR"
} > "$REPORT"