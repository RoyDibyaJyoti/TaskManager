#!/bin/bash
set -e

# File paths
SRC_PNG="src/main/resources/TaskManager.png"
DEST_DIR="src/main/resources"
mkdir -p "$DEST_DIR"

ICONSET="TaskManager.iconset"
# Clean up any leftover folder
rm -rf "$ICONSET"
mkdir -p "$ICONSET"

# Resize images using sips (native macOS tool) with explicit png format
sips -s format png -z 16 16     "$SRC_PNG" --out "$ICONSET/icon_16x16.png"
sips -s format png -z 32 32     "$SRC_PNG" --out "$ICONSET/icon_16x16@2x.png"
sips -s format png -z 32 32     "$SRC_PNG" --out "$ICONSET/icon_32x32.png"
sips -s format png -z 64 64     "$SRC_PNG" --out "$ICONSET/icon_32x32@2x.png"
sips -s format png -z 128 128   "$SRC_PNG" --out "$ICONSET/icon_128x128.png"
sips -s format png -z 256 256   "$SRC_PNG" --out "$ICONSET/icon_128x128@2x.png"
sips -s format png -z 256 256   "$SRC_PNG" --out "$ICONSET/icon_256x256.png"
sips -s format png -z 512 512   "$SRC_PNG" --out "$ICONSET/icon_256x256@2x.png"
sips -s format png -z 512 512   "$SRC_PNG" --out "$ICONSET/icon_512x512.png"
sips -s format png -z 1024 1024 "$SRC_PNG" --out "$ICONSET/icon_512x512@2x.png"

# Generate icns using iconutil (target format is icns)
iconutil -c icns "$ICONSET"

# Move generated icns to destination folder
mv TaskManager.icns "$DEST_DIR/"

# Clean up
rm -rf "$ICONSET"

echo "Successfully generated TaskManager.icns inside $DEST_DIR"
