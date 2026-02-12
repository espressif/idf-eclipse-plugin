#!/bin/bash
set -euo pipefail

RELEASE_XML="https://ftp2.osuosl.org/pub/eclipse/technology/epp/downloads/release/release.xml"
EPP_BASE="https://ftp2.osuosl.org/pub/eclipse/technology/epp/downloads/release"

IEP_TAGS_API="https://api.github.com/repos/espressif/idf-eclipse-plugin/tags"
ESP_IDE_BASE="https://dl.espressif.com/dl/idf-eclipse-plugin/ide"

echo "Fetching Eclipse release list from $RELEASE_XML"
XML="$(curl -fsSL "$RELEASE_XML")"

TOKENS="$(printf '%s\n' "$XML" | tr ' ' '\n' | sed '/^$/d')"

ECLIPSE_R_RELEASE="$(
  printf '%s\n' "$TOKENS" \
    | grep -E '^[0-9]{4}-[0-9]{2}/R$' \
    | sed 's|/R||' \
    | sort -r \
    | head -n1
)"

ECLIPSE_M_RELEASE="$(
  printf '%s\n' "$TOKENS" \
    | grep -E '^[0-9]{4}-[0-9]{2}/M[0-9]+$' \
    | sort -r \
    | head -n1 || true
)"

ECLIPSE_RC_RELEASE="$(
  printf '%s\n' "$TOKENS" \
    | grep -E '^[0-9]{4}-[0-9]{2}/RC[0-9]+$' \
    | sort -r \
    | head -n1 || true
)"

ECLIPSE_CASE3_STREAM="${ECLIPSE_M_RELEASE:-}"
if [[ -n "${ECLIPSE_RC_RELEASE:-}" ]]; then
  ECLIPSE_CASE3_STREAM="$ECLIPSE_RC_RELEASE"
fi

ECLIPSE_CASE3_URL=""
ECLIPSE_CASE3_P2=""

if [[ -n "${ECLIPSE_CASE3_STREAM:-}" ]]; then
  CASE3_STREAM="${ECLIPSE_CASE3_STREAM%/*}"   # YYYY-MM
  CASE3_TAG="${ECLIPSE_CASE3_STREAM#*/}"      # M* or RC*
  ECLIPSE_CASE3_URL="$EPP_BASE/$CASE3_STREAM/$CASE3_TAG/eclipse-cpp-$CASE3_STREAM-$CASE3_TAG-linux-gtk-x86_64.tar.gz"
  ECLIPSE_CASE3_P2="https://download.eclipse.org/releases/$CASE3_STREAM"
else
  echo "⚠️ No milestone or RC detected. Case 3 will be skipped."
fi
# -----------------------------------------------

if [[ -z "${ECLIPSE_R_RELEASE:-}" ]]; then
  echo "❌ Could not detect latest /R release"
  exit 1
fi

# Build URLs (stable)
ECLIPSE_R_URL="$EPP_BASE/$ECLIPSE_R_RELEASE/R/eclipse-cpp-$ECLIPSE_R_RELEASE-R-linux-gtk-x86_64.tar.gz"
ECLIPSE_R_P2="https://download.eclipse.org/releases/$ECLIPSE_R_RELEASE"

# Verify URLs exist (stable) - non-fatal
if ! curl -fsI "$ECLIPSE_R_URL" >/dev/null; then
  echo "⚠️ Could not verify Eclipse archive URL. Continuing anyway."
fi

# Verify Case 3 URL exists - if unreachable, skip Case 3
if [[ -n "${ECLIPSE_CASE3_URL:-}" ]]; then
  if ! curl -fsI "$ECLIPSE_CASE3_URL" >/dev/null; then
    echo "⚠️ Case 3 pre-release archive not reachable. Case 3 will be skipped."
    ECLIPSE_CASE3_STREAM=""
    ECLIPSE_CASE3_URL=""
    ECLIPSE_CASE3_P2=""
  fi
fi

# -------- Espressif-IDE stable version (tags) --------
echo "Detecting latest *stable* Espressif-IDE tag..."
TAGS_JSON="$(curl -fsSL "$IEP_TAGS_API")"

ESP_IDE_TAG="$(
  printf '%s\n' "$TAGS_JSON" \
    | grep -oE '"name":[[:space:]]*"v?[0-9]+\.[0-9]+\.[0-9]+"' \
    | sed -E 's/.*"name":[[:space:]]*"([^"]+)".*/\1/' \
    | head -n1
)"

if [[ -z "${ESP_IDE_TAG:-}" ]]; then
  echo "❌ Could not detect stable Espressif-IDE tag (vX.Y.Z)"
  exit 1
fi

ESP_IDE_VERSION="${ESP_IDE_TAG#v}"
ESPRESSIF_IDE_URL="$ESP_IDE_BASE/Espressif-IDE-${ESP_IDE_VERSION}-linux.gtk.x86_64.tar.gz"

echo "Verifying Espressif-IDE archive exists..."
if ! curl -sfI "$ESPRESSIF_IDE_URL" >/dev/null; then
  echo "⚠️ Could not verify Espressif-IDE archive URL. Continuing anyway."
fi

export ECLIPSE_R_RELEASE ECLIPSE_R_URL ECLIPSE_R_P2
export ECLIPSE_M_RELEASE ECLIPSE_RC_RELEASE
export ECLIPSE_CASE3_STREAM ECLIPSE_CASE3_URL ECLIPSE_CASE3_P2
export ESP_IDE_VERSION ESPRESSIF_IDE_URL

echo "✅ Latest stable Eclipse: $ECLIPSE_R_RELEASE"
echo "✅ Stable URL: $ECLIPSE_R_URL"
echo "✅ Stable P2:  $ECLIPSE_R_P2"
echo "✅ Latest milestone Eclipse: ${ECLIPSE_M_RELEASE:-<none>}"
echo "✅ Latest RC Eclipse:        ${ECLIPSE_RC_RELEASE:-<none>}"
echo "✅ Case 3 pre-release pick:  ${ECLIPSE_CASE3_STREAM:-<none>}"
if [[ -n "${ECLIPSE_CASE3_STREAM:-}" ]]; then
  echo "✅ Case 3 URL: $ECLIPSE_CASE3_URL"
  echo "✅ Case 3 P2:  $ECLIPSE_CASE3_P2"
fi
echo "✅ Espressif-IDE version: $ESP_IDE_VERSION"
echo "✅ Espressif-IDE URL: $ESPRESSIF_IDE_URL"