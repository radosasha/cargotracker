#!/bin/bash

# Script for switching between build profiles
# Usage: ./switch-profile.sh [dev|prod|ci]

PROFILE=$1

if [ -z "$PROFILE" ]; then
    echo "Error: Specify profile: dev, prod or ci"
    echo "Usage: ./switch-profile.sh [dev|prod|ci]"
    exit 1
fi

case $PROFILE in
    dev)
        echo "Switching to DEV profile (fast build, Android only)"
        cp gradle-dev.properties gradle.properties
        echo "DEV profile activated"
        echo "   - iOS build disabled"
        echo "   - Lint checks disabled"
        echo "   - R8 disabled"
        ;;
    prod)
        echo "Switching to PROD profile (full build with optimizations)"
        cp gradle-prod.properties gradle.properties
        echo "PROD profile activated"
        echo "   - All platforms enabled"
        echo "   - All optimizations enabled"
        ;;
    ci)
        echo "Switching to CI profile (for CI/CD servers)"
        cp gradle-ci.properties gradle.properties
        echo "CI profile activated"
        echo "   - Daemon disabled"
        echo "   - All checks enabled"
        ;;
    *)
        echo "Error: Unknown profile: $PROFILE"
        echo "Available profiles: dev, prod, ci"
        exit 1
        ;;
esac

echo ""
echo "Current settings:"
echo "---"
head -20 gradle.properties
echo "..."
