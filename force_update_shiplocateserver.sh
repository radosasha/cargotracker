#!/bin/bash

set -e

echo "🔄 Принудительно обновляем субмодули..."

# Обновляем основной субмодуль shiplocateserver
cd shiplocateserver
git fetch origin
git checkout develop
git reset --hard origin/develop
git clean -fd

# Обновляем веб-субмодуль shiplocate-web (находится внутри shiplocateserver)
cd shiplocate-web
git fetch origin
git checkout develop
git reset --hard origin/develop
git clean -fd

# Возвращаемся в корень проекта
cd ../..

echo "✅ Готово!"
git submodule status