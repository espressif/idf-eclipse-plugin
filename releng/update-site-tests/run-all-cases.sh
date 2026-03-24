#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUTROOT="$ROOT_DIR/out"
WORKROOT="$ROOT_DIR/workdir"

rm -rf "$OUTROOT" "$WORKROOT"
mkdir -p "$OUTROOT" "$WORKROOT"

# Populate:
# ECLIPSE_R_URL, ECLIPSE_R_P2, and optionally ECLIPSE_M_RELEASE/ECLIPSE_M_URL/ECLIPSE_M_P2
# shellcheck source=/dev/null
source "$ROOT_DIR/detect-latest-eclipse-steams.sh"
source "$ROOT_DIR/variables.sh"

export IEP_STABLE_REPO
export IEP_DEV_REPO

FAIL=0

echo "=== Case 1: Plain Eclipse (latest R) + stable IEP install ==="

if CASE_ID="case1-Eclipse-R-install-stable" \
   BASE_PRODUCT="eclipse-cdt" \
   ECLIPSE_URL="$ECLIPSE_R_URL" \
   ECLIPSE_P2_REPO="$ECLIPSE_R_P2" \
   DO_DEV_UPDATE=0 \
   WORKROOT="$WORKROOT" OUTROOT="$OUTROOT" \
   bash "$ROOT_DIR/run-single-case.sh"; then

  echo "✅ Test Case 1 completed successfully"

else
  echo "❌ Test Case 1 FAILED"
  FAIL=1
fi

echo "=== Case 2: Plain Eclipse (latest R) stable -> dev update ==="

if CASE_ID="case2-Eclipse-R-stable-to-nightly" \
   BASE_PRODUCT="eclipse-cdt" \
   ECLIPSE_URL="$ECLIPSE_R_URL" \
   ECLIPSE_P2_REPO="$ECLIPSE_R_P2" \
   DO_DEV_UPDATE=1 \
   WORKROOT="$WORKROOT" OUTROOT="$OUTROOT" \
   bash "$ROOT_DIR/run-single-case.sh"; then

  echo "✅ Test Case 2 completed successfully"

else
  echo "❌ Test Case 2 FAILED"
  FAIL=1
fi

echo "=== Case 3: Eclipse (latest pre-release) stable -> dev update ==="

CASE3_ID="case3-Eclipse-RC-Milestone-to-nightly"
CASE3_DIR="$OUTROOT/$CASE3_ID"
CASE3_LOGDIR="$CASE3_DIR/logs"
CASE3_RUNLOG="$CASE3_LOGDIR/run.log"

if [[ -n "${ECLIPSE_CASE3_STREAM:-}" && -n "${ECLIPSE_CASE3_URL:-}" && -n "${ECLIPSE_CASE3_P2:-}" ]]; then

  if CASE_ID="$CASE3_ID" \
     BASE_PRODUCT="eclipse-cdt" \
     ECLIPSE_URL="$ECLIPSE_CASE3_URL" \
     ECLIPSE_P2_REPO="$ECLIPSE_CASE3_P2" \
     DO_DEV_UPDATE=1 \
     WORKROOT="$WORKROOT" OUTROOT="$OUTROOT" \
     bash "$ROOT_DIR/run-single-case.sh"; then

      echo "✅ Test Case 3 completed successfully"

  else
      echo "❌ Test Case 3 FAILED"
      FAIL=1
  fi

else
  mkdir -p "$CASE3_LOGDIR"

  {
    echo "=== [$CASE3_ID] Starting case ==="
    echo "Base product: eclipse-cdt"
    echo ""
    echo "SKIP_REASON: No Eclipse RC or Milestone found"
    echo "Case 3 requires a pre-release Eclipse stream (M* or RC*)."
    echo "Therefore this test case was skipped."
    echo ""
    echo "STATUS: SKIPPED"
  } > "$CASE3_RUNLOG"

  echo "⚠️ Skipping Case 3 (no milestone/RC detected)."
fi

echo "=== Preparing Espressif-IDE (latest stable) for Case 4 ==="
: "${ESP_IDE_VERSION:?ESP_IDE_VERSION not set by detection script}"
: "${ESPRESSIF_IDE_URL:?ESPRESSIF_IDE_URL not set by detection script}"

ESP_IDE_WORK="$WORKROOT/espressif-ide-${ESP_IDE_VERSION}"
rm -rf "$ESP_IDE_WORK"
mkdir -p "$ESP_IDE_WORK"

echo "Downloading Espressif-IDE from: $ESPRESSIF_IDE_URL"
curl -fsSL "$ESPRESSIF_IDE_URL" -o "$ESP_IDE_WORK/espressif-ide.tar.gz"

echo "Extracting Espressif-IDE..."
tar -xzf "$ESP_IDE_WORK/espressif-ide.tar.gz" -C "$ESP_IDE_WORK" \
  2> >(grep -v "LIBARCHIVE.creationtime" >&2)

# Figure out where the eclipse executable is after extraction
# Usually archive contains a top-level "eclipse/" directory
if [[ -x "$ESP_IDE_WORK/Espressif-IDE/espressif-ide" ]]; then
  export ESPRESSIF_IDE_SOURCE="$ESP_IDE_WORK/Espressif-IDE"
else
  # fallback: search for an eclipse executable
  FOUND_IDE="$(find "$ESP_IDE_WORK" -maxdepth 4 -type f -name espressif-ide -perm -111 | head -n1 || true)"
  if [[ -z "$FOUND_IDE" ]]; then
    echo "❌ Could not locate 'espressif-ide' launcher inside extracted Espressif-IDE."
    echo "Top-level contents:"
    ls -la "$ESP_IDE_WORK"
    exit 1
  fi
  export ESPRESSIF_IDE_SOURCE="$(dirname "$FOUND_IDE")"
fi

echo "ESPRESSIF_IDE_SOURCE resolved to: $ESPRESSIF_IDE_SOURCE"
test -x "$ESPRESSIF_IDE_SOURCE/espressif-ide" || { echo "❌ Invalid ESPRESSIF_IDE_SOURCE: $ESPRESSIF_IDE_SOURCE"; exit 1; }

echo "=== Case 4: Espressif-IDE stable -> dev update ==="
: "${ESPRESSIF_IDE_SOURCE:?Provide ESPRESSIF_IDE_SOURCE for Case 4 (path to extracted IDE root)}"

if CASE_ID="case4-Espressif-IDE-stable-to-nightly" \
   BASE_PRODUCT="espressif-ide" \
   DO_DEV_UPDATE=1 \
   SKIP_STABLE_INSTALL=1 \
   ESPRESSIF_IDE_SOURCE="$ESPRESSIF_IDE_SOURCE" \
   WORKROOT="$WORKROOT" OUTROOT="$OUTROOT" \
   bash "$ROOT_DIR/run-single-case.sh"; then

  echo "✅ Test Case 4 completed successfully"

else
  echo "❌ Test Case 4 FAILED"
  FAIL=1
fi

echo ""
echo "All cases finished. Artifacts in: $OUTROOT"
exit "$FAIL"
