#!/bin/bash
set -e
set -o pipefail

RELEASE_XML="https://ftp2.osuosl.org/pub/eclipse/technology/epp/downloads/release/release.xml"
EPP_BASE="https://ftp2.osuosl.org/pub/eclipse/technology/epp/downloads/release"

echo "Fetching Eclipse release list..."

LATEST_ECLIPSE_RELEASE=$(curl -s "$RELEASE_XML" \
    | grep -oE '[0-9]{4}-[0-9]{2}/R' \
    | sed 's|/R||' \
    | sort -r \
    | head -n1)

if [ -z "$LATEST_ECLIPSE_RELEASE" ]; then
    echo "❌ Could not detect latest Eclipse release"
    exit 1
fi

echo "Latest Eclipse release detected: $LATEST_ECLIPSE_RELEASE"

ECLIPSE_URL="$EPP_BASE/$LATEST_ECLIPSE_RELEASE/R/eclipse-cpp-$LATEST_ECLIPSE_RELEASE-R-linux-gtk-x86_64.tar.gz"

echo "Resolved Eclipse URL:"
echo "$ECLIPSE_URL"

if ! curl -sfI "$ECLIPSE_URL" > /dev/null; then
    echo "❌ Eclipse archive not found at resolved URL"
    exit 1
fi

echo "✅ Eclipse archive verified"
# Export for test script
export ECLIPSE_URL
export LATEST_ECLIPSE_RELEASE

# Run your test
bash releng/update-site-tests/test-update.sh
