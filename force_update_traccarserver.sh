#!/bin/bash

set -e

echo "🔄 Принудительно обновляем субмодули..."

cd traccarserver
git fetch origin
git checkout develop
git reset --hard origin/develop
git clean -fd

cd traccar-web
git fetch origin
git checkout develop
git reset --hard origin/develop
git clean -fd

cd ../..

echo "✅ Готово!"
git submodule status