#!/bin/bash
set -euo pipefail

CASE_ID="${CASE_ID:?CASE_ID not set}"
BASE_PRODUCT="${BASE_PRODUCT:?BASE_PRODUCT not set}"
ECLIPSE_URL="${ECLIPSE_URL:-}"
ECLIPSE_P2_REPO="${ECLIPSE_P2_REPO:-}"

IEP_STABLE_REPO="${IEP_STABLE_REPO:?IEP_STABLE_REPO not set}"
IEP_DEV_REPO="${IEP_DEV_REPO:?IEP_DEV_REPO not set}"
FEATURE_ID="${FEATURE_ID:-com.espressif.idf.feature.feature.group}"

ROOT_DIR="$(pwd)/releng/update-site-tests"
WORKROOT="${WORKROOT:-$ROOT_DIR/workdir}"
OUTROOT="${OUTROOT:-$ROOT_DIR/out}"

CASE_DIR="$OUTROOT/$CASE_ID"
LOGDIR="$CASE_DIR/logs"
WORKDIR="$WORKROOT/$CASE_ID"

mkdir -p "$LOGDIR" "$WORKDIR"

RUNLOG="$LOGDIR/run.log"
exec > >(tee -a "$RUNLOG") 2>&1

echo "=== [$CASE_ID] Starting case ==="
echo "Base product: $BASE_PRODUCT"

# Build the base installation into "$WORKDIR/eclipse"
ECLIPSE_HOME="$WORKDIR/eclipse"

if [[ "$BASE_PRODUCT" == "eclipse-cdt" ]]; then
  : "${ECLIPSE_URL:?ECLIPSE_URL required for eclipse-cdt}"
  echo "Downloading Eclipse from: $ECLIPSE_URL"
  curl -fsSL "$ECLIPSE_URL" -o "$WORKDIR/eclipse.tar.gz"
  tar -xzf "$WORKDIR/eclipse.tar.gz" -C "$WORKDIR"

  if [[ -x "$WORKDIR/eclipse/eclipse" ]]; then
    ECLIPSE_HOME="$WORKDIR/eclipse"
  else
    ECLIPSE_EXE="$(find "$WORKDIR" -maxdepth 3 -type f -name eclipse -perm -111 | head -n1 || true)"
    [[ -n "$ECLIPSE_EXE" ]] || { echo "❌ Eclipse executable not found after extraction"; exit 1; }
    ECLIPSE_HOME="$(dirname "$ECLIPSE_EXE")"
  fi

elif [[ "$BASE_PRODUCT" == "espressif-ide" ]]; then
  : "${ESPRESSIF_IDE_SOURCE:?ESPRESSIF_IDE_SOURCE not set (path to preinstalled IDE or extracted IDE root)}"
  echo "Copying Espressif-IDE from: $ESPRESSIF_IDE_SOURCE"
  mkdir -p "$ECLIPSE_HOME"
  cp -a "$ESPRESSIF_IDE_SOURCE"/. "$ECLIPSE_HOME"/
else
  echo "Unknown BASE_PRODUCT: $BASE_PRODUCT"
  exit 1
fi

echo "ECLIPSE_HOME=$ECLIPSE_HOME"

if [[ -x "$ECLIPSE_HOME/eclipse" ]]; then
  LAUNCHER="$ECLIPSE_HOME/eclipse"
elif [[ -x "$ECLIPSE_HOME/espressif-ide" ]]; then
  LAUNCHER="$ECLIPSE_HOME/espressif-ide"
else
  echo "❌ No launcher found in $ECLIPSE_HOME (expected 'eclipse' or 'espressif-ide')"
  ls -la "$ECLIPSE_HOME" || true
  exit 1
fi
echo "Using launcher: $LAUNCHER"

JAVA_LAUNCHER=""
EQUINOX_LAUNCHER_JAR=""

if [[ "$BASE_PRODUCT" == "espressif-ide" ]]; then
  EQUINOX_LAUNCHER_JAR="$(find "$ECLIPSE_HOME/plugins" -maxdepth 1 -name 'org.eclipse.equinox.launcher_*.jar' | head -n1 || true)"
  [[ -n "$EQUINOX_LAUNCHER_JAR" ]] || { echo "❌ Equinox launcher jar not found under $ECLIPSE_HOME/plugins"; exit 1; }

  JAVA_LAUNCHER=( java -jar "$EQUINOX_LAUNCHER_JAR" )
  echo "Using Equinox launcher jar for Espressif-IDE: $EQUINOX_LAUNCHER_JAR"
fi

# Common director flags: IMPORTANT to keep same destination/profile/bundlepool across steps
P2_DEST_ARGS=( -destination "$ECLIPSE_HOME" -profile SDKProfile -bundlepool "$WORKDIR/p2" -roaming )
WORKSPACE_DIR="$WORKDIR/workspace"
mkdir -p "$WORKSPACE_DIR"

build_repo_list() {
  local primary="$1"
  local extra="${2:-}"
  if [[ -n "$extra" ]]; then
    echo "$primary,$extra"
  else
    echo "$primary"
  fi
}

install_iu () {
  local repo="$1"

  if [[ "$BASE_PRODUCT" == "espressif-ide" ]]; then
    "${JAVA_LAUNCHER[@]}" \
      -clean \
      -nosplash \
      -data "$WORKSPACE_DIR" \
      -application org.eclipse.equinox.p2.director \
      -repository "$repo" \
      -installIU "$FEATURE_ID" \
      -consoleLog
  else
    "$LAUNCHER" \
      -nosplash \
      -application org.eclipse.equinox.p2.director \
      -repository "$repo" \
      -installIU "$FEATURE_ID" \
      "${P2_DEST_ARGS[@]}" \
      -consoleLog
  fi
}

replace_iu () {
  local repo="$1"

  if [[ "$BASE_PRODUCT" == "espressif-ide" ]]; then
    "${JAVA_LAUNCHER[@]}" \
      -clean \
      -nosplash \
      -data "$WORKSPACE_DIR" \
      -application org.eclipse.equinox.p2.director \
      -repository "$repo" \
      -uninstallIU "$FEATURE_ID" \
      -installIU "$FEATURE_ID" \
      -consoleLog
  else
    "$LAUNCHER" \
      -nosplash \
      -application org.eclipse.equinox.p2.director \
      -repository "$repo" \
      -uninstallIU "$FEATURE_ID" \
      -installIU "$FEATURE_ID" \
      "${P2_DEST_ARGS[@]}" \
      -consoleLog
  fi
}

list_ius () {
  local out="$1"

  if [[ "$BASE_PRODUCT" == "espressif-ide" ]]; then
    "${JAVA_LAUNCHER[@]}" \
      -clean \
      -nosplash \
      -data "$WORKSPACE_DIR" \
      -application org.eclipse.equinox.p2.director \
      -list \
      -consoleLog \
      > "$LOGDIR/$out" 2>&1 || true
  else
    "$LAUNCHER" \
      -nosplash \
      -application org.eclipse.equinox.p2.director \
      -list \
      "${P2_DEST_ARGS[@]}" \
      -consoleLog \
      > "$LOGDIR/$out" 2>&1 || true
  fi
}

list_roots () {
  local out="$1"

  if [[ "$BASE_PRODUCT" == "espressif-ide" ]]; then
    "${JAVA_LAUNCHER[@]}" \
      -clean \
      -nosplash \
      -data "$WORKSPACE_DIR" \
      -application org.eclipse.equinox.p2.director \
      -listInstalledRoots \
      "${P2_DEST_ARGS[@]}" \
      -consoleLog \
      > "$LOGDIR/$out" 2>&1 || true
  else
    "$LAUNCHER" \
      -nosplash \
      -application org.eclipse.equinox.p2.director \
      -listInstalledRoots \
      "${P2_DEST_ARGS[@]}" \
      -consoleLog \
      > "$LOGDIR/$out" 2>&1 || true
  fi
}

# --- Step A: install stable ---
if [[ "${SKIP_STABLE_INSTALL:-0}" != "1" ]]; then
  echo "Installing STABLE from $IEP_STABLE_REPO"
  install_iu "$(build_repo_list "$IEP_STABLE_REPO" "${ECLIPSE_P2_REPO:-}")"
else
  echo "Skipping STABLE install (SKIP_STABLE_INSTALL=1)"
fi

# --- Step B: optional update to dev/nightly ---
if [[ "${DO_DEV_UPDATE:-0}" == "1" ]]; then
  echo "Updating to DEV/NIGHTLY from $IEP_DEV_REPO"
  replace_iu "$(build_repo_list "$IEP_DEV_REPO" "${ECLIPSE_P2_REPO:-}")"
fi

# Diagnostics (kept as small separate files)
list_ius "installed-ius.txt"
list_roots "installed-roots.txt"

# Conflict scan (from combined log)
ERROR_PATTERNS='conflict|cannot complete|missing requirement|Only one of the following can be installed'
if grep -Ein "$ERROR_PATTERNS" "$RUNLOG" >/dev/null 2>&1; then
  HAS_CONFLICTS=1
else
  HAS_CONFLICTS=0
fi

# Summary
echo ""
echo "ESP Eclipse Plug-in Update Path Summary"
echo "======================================="
echo ""
echo "Case: $CASE_ID"
echo "Base product: $BASE_PRODUCT"
[[ -n "${ECLIPSE_P2_REPO:-}" ]] && echo "Eclipse P2 repo: $ECLIPSE_P2_REPO"
[[ -n "${ECLIPSE_URL:-}" ]] && echo "Eclipse URL: $ECLIPSE_URL"
echo ""
echo "IEP stable repo:  $IEP_STABLE_REPO"
echo "IEP dev repo:     $IEP_DEV_REPO"
echo ""

ERROR_PATTERNS='conflict|cannot complete|missing requirement|Only one of the following can be installed'
if grep -Ein "$ERROR_PATTERNS" "$RUNLOG" >/dev/null 2>&1; then
  echo "Conflicts (grep scan in logs/run.log):"
  echo "Found (see above in run.log)"
  echo ""
  echo "STATUS: FAILED"
  exit 2
else
  echo "Conflicts (grep scan in logs/run.log):"
  echo "None"
  echo ""
  echo "STATUS: PASSED"
  exit 0
fi