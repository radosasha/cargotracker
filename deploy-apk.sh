#!/bin/bash

# =========================================
# Production APK Build & Deploy Script
# Собирает, подписывает и загружает APK на сервер
# Создает QR код для скачивания
# =========================================

set -e

# Цвета для вывода
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Данные сервера (из deploy-simple.sh)
EC2_IP="44.216.176.38"
SSH_KEY="shiplocateserver/shiplocate-october.pem"
SSH_USER="ubuntu"
SSH_OPTS="-i $SSH_KEY -o StrictHostKeyChecking=no"

# Независимая папка на сервере (не затирается при деплое)
SERVER_APK_DIR="/home/ubuntu/apk"
SERVER_APK_PATH="$SERVER_APK_DIR/build.apk"
# Порт для Python HTTP сервера (для раздачи APK)
APK_SERVER_PORT="8888"
APK_DOWNLOAD_URL="http://$EC2_IP:$APK_SERVER_PORT/build.apk"

# Локальные пути
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
KEYSTORE_PATH="$PROJECT_ROOT/tracker.jks"
KEYSTORE_PASSWORD="1133511"
KEY_ALIAS="key0"
KEY_PASSWORD="1133511"
APK_BUILD_PATH="$PROJECT_ROOT/composeApp/build/outputs/apk/release/composeApp-release-unsigned.apk"
APK_FINAL_PATH="$PROJECT_ROOT/composeApp/build/outputs/apk/release/composeApp-release.apk"
QR_CODE_PATH="$PROJECT_ROOT/qr-code-apk.png"

echo -e "${BLUE}🚀 Production APK Build & Deploy${NC}"
echo -e "${BLUE}=================================${NC}"
echo ""

# Функция для проверки успешности команды
check_success() {
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ $1${NC}"
    else
        echo -e "${RED}✗ $1${NC}"
        exit 1
    fi
}

# Функция для поиска утилиты в стандартных местах
find_utility() {
    local util_name=$1
    local util_path=""
    
    # Сначала проверяем в PATH
    if command -v "$util_name" &> /dev/null; then
        which "$util_name"
        return 0
    fi
    
    case "$util_name" in
        apksigner)
            # Ищем Android SDK Build Tools
            if [ -n "$ANDROID_HOME" ]; then
                for build_tool in "$ANDROID_HOME"/build-tools/*/apksigner; do
                    if [ -f "$build_tool" ]; then
                        echo "$build_tool"
                        return 0
                    fi
                done
            fi
            
            # Стандартные места для Android SDK (macOS)
            ANDROID_SDK_PATHS=(
                "$HOME/Library/Android/sdk"
                "$HOME/Android/Sdk"
                "$HOME/.android/sdk"
                "/opt/android-sdk"
            )
            
            for sdk_path in "${ANDROID_SDK_PATHS[@]}"; do
                if [ -d "$sdk_path/build-tools" ]; then
                    # Берем последнюю версию
                    latest_build_tool=$(ls -d "$sdk_path"/build-tools/* 2>/dev/null | sort -V | tail -1)
                    if [ -n "$latest_build_tool" ] && [ -f "$latest_build_tool/apksigner" ]; then
                        echo "$latest_build_tool/apksigner"
                        return 0
                    fi
                fi
            done
            ;;
        jarsigner)
            # Ищем Java JDK
            if [ -n "$JAVA_HOME" ]; then
                util_path="$JAVA_HOME/bin/jarsigner"
                if [ -f "$util_path" ]; then
                    echo "$util_path"
                    return 0
                fi
            fi
            
            # Попробуем найти через java_home (macOS)
            if command -v /usr/libexec/java_home &> /dev/null; then
                JAVA_HOME=$(/usr/libexec/java_home 2>/dev/null)
                if [ -n "$JAVA_HOME" ] && [ -f "$JAVA_HOME/bin/jarsigner" ]; then
                    echo "$JAVA_HOME/bin/jarsigner"
                    return 0
                fi
            fi
            
            # Проверяем стандартные места
            for java_home in /Library/Java/JavaVirtualMachines/*/Contents/Home /usr/lib/jvm/*; do
                if [ -f "$java_home/bin/jarsigner" ]; then
                    echo "$java_home/bin/jarsigner"
                    return 0
                fi
            done
            ;;
    esac
    
    return 1
}

# Функция для запроса пароля администратора
request_sudo() {
    if [ "$EUID" -ne 0 ]; then
        echo -e "${YELLOW}   Требуются права администратора...${NC}"
        sudo -v
        if [ $? -eq 0 ]; then
            return 0
        else
            return 1
        fi
    fi
    return 0
}

# Функция для установки недостающих утилит
install_utility() {
    local util_name=$1
    
    case "$util_name" in
        apksigner)
            echo -e "${YELLOW}   Попытка установки Android SDK Build-Tools...${NC}"
            
            # Ищем sdkmanager
            SDK_MANAGER=""
            ANDROID_SDK_PATHS=(
                "$HOME/Library/Android/sdk"
                "$HOME/Android/Sdk"
                "$HOME/.android/sdk"
                "/opt/android-sdk"
            )
            
            for sdk_path in "${ANDROID_SDK_PATHS[@]}"; do
                if [ -d "$sdk_path" ]; then
                    # Ищем cmdline-tools
                    for cmd_tool in "$sdk_path/cmdline-tools"/latest/bin/sdkmanager \
                                    "$sdk_path/cmdline-tools"/*/bin/sdkmanager \
                                    "$sdk_path/tools/bin/sdkmanager"; do
                        if [ -f "$cmd_tool" ]; then
                            SDK_MANAGER="$cmd_tool"
                            break 2
                        fi
                    done
                fi
            done
            
            if [ -n "$SDK_MANAGER" ]; then
                SDK_ROOT="$(dirname "$(dirname "$(dirname "$SDK_MANAGER")")")"
                echo -e "${YELLOW}   Android SDK найден: $SDK_ROOT${NC}"
                echo -e "${YELLOW}   Установка build-tools (это может занять несколько минут)...${NC}"
                
                # Устанавливаем ANDROID_HOME
                export ANDROID_HOME="$SDK_ROOT"
                
                # Принимаем все лицензии
                echo -e "${YELLOW}   Принятие лицензий...${NC}"
                yes | "$SDK_MANAGER" --licenses > /dev/null 2>&1 || true
                
                # Устанавливаем build-tools с автоматическим принятием лицензий
                echo -e "${YELLOW}   Загрузка и установка build-tools...${NC}"
                if yes | "$SDK_MANAGER" "build-tools;latest" 2>&1 | grep -i "install\|done" > /dev/null 2>&1; then
                    echo -e "${YELLOW}   Установка завершена, проверяю...${NC}"
                    sleep 3
                    # Ищем установленный apksigner
                    NEW_APKSIGNER=$(find_utility apksigner)
                    if [ -n "$NEW_APKSIGNER" ]; then
                        echo "$NEW_APKSIGNER"
                        return 0
                    fi
                else
                    # Пробуем установить еще раз (может быть интерактивным)
                    echo -e "${YELLOW}   Повторная попытка установки...${NC}"
                    "$SDK_MANAGER" "build-tools;latest" 2>&1 || true
                    sleep 3
                    NEW_APKSIGNER=$(find_utility apksigner)
                    if [ -n "$NEW_APKSIGNER" ]; then
                        echo "$NEW_APKSIGNER"
                        return 0
                    fi
                fi
                
                # Проверяем еще раз после задержки
                sleep 2
                NEW_APKSIGNER=$(find_utility apksigner)
                if [ -n "$NEW_APKSIGNER" ]; then
                    echo "$NEW_APKSIGNER"
                    return 0
                fi
            else
                echo -e "${YELLOW}   Android SDK не найден в стандартных местах${NC}"
                echo -e "${YELLOW}   Проверяемые пути:${NC}"
                for sdk_path in "${ANDROID_SDK_PATHS[@]}"; do
                    if [ -d "$sdk_path" ]; then
                        echo -e "${YELLOW}     ✓ Найден: $sdk_path (но нет sdkmanager)${NC}"
                    fi
                done
            fi
            
            echo -e "${YELLOW}   Автоматическая установка не удалась${NC}"
            ;;
        jarsigner)
            echo -e "${YELLOW}   Попытка установки Java JDK...${NC}"
            
            # Попробуем через Homebrew (не требует sudo обычно)
            if command -v brew &> /dev/null; then
                echo -e "${YELLOW}   Установка через Homebrew (может занять несколько минут)...${NC}"
                if brew install openjdk; then
                    sleep 2
                    # Попробуем снова найти
                    JAVA_HOME=$(brew --prefix openjdk 2>/dev/null || echo "")
                    if [ -z "$JAVA_HOME" ]; then
                        # Попробуем найти установленный openjdk
                        JAVA_HOME=$(find /opt/homebrew/opt -name "openjdk*" -type d 2>/dev/null | head -1)
                        [ -z "$JAVA_HOME" ] && JAVA_HOME=$(find /usr/local/opt -name "openjdk*" -type d 2>/dev/null | head -1)
                    fi
                    
                    if [ -n "$JAVA_HOME" ] && [ -f "$JAVA_HOME/bin/jarsigner" ]; then
                        echo "$JAVA_HOME/bin/jarsigner"
                        return 0
                    fi
                    
                    # Попробуем через /usr/libexec/java_home
                    JAVA_HOME=$(/usr/libexec/java_home 2>/dev/null || echo "")
                    if [ -n "$JAVA_HOME" ] && [ -f "$JAVA_HOME/bin/jarsigner" ]; then
                        echo "$JAVA_HOME/bin/jarsigner"
                        return 0
                    fi
                fi
            fi
            
            # Альтернатива: скачать Oracle JDK (требует интерактивного согласия)
            echo -e "${YELLOW}   Homebrew не доступен или установка не удалась${NC}"
            ;;
    esac
    
    return 1
}

# Проверка наличия необходимых инструментов
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}Проверка инструментов${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

# Поиск apksigner (предпочтительно, создает V2/V3 подпись)
APKSIGNER_PATH=$(find_utility apksigner)
if [ -z "$APKSIGNER_PATH" ]; then
    echo -e "${YELLOW}⚠ apksigner не найден в PATH${NC}"
    echo -e "${YELLOW}   Ищу в стандартных местах Android SDK...${NC}"
    
    # Пробуем установить автоматически
    echo -e "${YELLOW}   Попытка автоматической установки...${NC}"
    APKSIGNER_PATH=$(install_utility apksigner)
    
    if [ -z "$APKSIGNER_PATH" ]; then
        echo -e "${RED}✗ apksigner не найден и автоматическая установка не удалась.${NC}"
        echo -e "${YELLOW}   Пожалуйста, установите Android SDK Build Tools:${NC}"
        echo ""
        echo -e "${YELLOW}   1. Через Android Studio (рекомендуется):${NC}"
        echo -e "${YELLOW}      Android Studio → Preferences → Android SDK → SDK Tools${NC}"
        echo -e "${YELLOW}      Установите Android SDK Build-Tools${NC}"
        echo ""
        echo -e "${YELLOW}   2. Или через командную строку:${NC}"
        echo -e "${YELLOW}      \$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager 'build-tools;latest'${NC}"
        exit 1
    fi
fi

# Создаем команду для apksigner
APKSIGNER_CMD="$APKSIGNER_PATH"
echo -e "${GREEN}✓ apksigner найден: $APKSIGNER_CMD${NC}"


# Проверка QR кода библиотеки
if ! python3 -c "import qrcode" 2>/dev/null; then
    echo -e "${YELLOW}⚠ Библиотека qrcode не найдена. Устанавливаю...${NC}"
    pip3 install qrcode[pil] --quiet
    check_success "Библиотека qrcode установлена"
else
    echo -e "${GREEN}✓ Библиотека qrcode найдена${NC}"
fi

echo ""

# Проверка keystore
if [ ! -f "$KEYSTORE_PATH" ]; then
    echo -e "${RED}✗ Keystore не найден: $KEYSTORE_PATH${NC}"
    exit 1
fi
check_success "Keystore найден"

# Проверка SSH ключа (проверяем относительно PROJECT_ROOT)
SSH_KEY_ABS="$PROJECT_ROOT/$SSH_KEY"
if [ ! -f "$SSH_KEY_ABS" ]; then
    # Пробуем найти в текущей директории
    if [ -f "$SSH_KEY" ]; then
        SSH_KEY_ABS="$(cd "$(dirname "$SSH_KEY")" && pwd)/$(basename "$SSH_KEY")"
    else
        echo -e "${RED}✗ SSH ключ не найден: $SSH_KEY${NC}"
        echo -e "${RED}   Проверяемые пути:${NC}"
        echo -e "${RED}   - $SSH_KEY_ABS${NC}"
        echo -e "${RED}   - $SSH_KEY${NC}"
        exit 1
    fi
fi
SSH_KEY="$SSH_KEY_ABS"
chmod 400 "$SSH_KEY"
check_success "SSH ключ проверен"

echo ""

# Шаг 1: Сборка production APK
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}Шаг 1: Сборка production APK${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

echo "🔨 Сборка release APK..."
cd "$PROJECT_ROOT"
./gradlew :composeApp:assembleRelease --no-daemon
check_success "APK собран"

if [ ! -f "$APK_BUILD_PATH" ]; then
    echo -e "${RED}✗ APK не найден по пути: $APK_BUILD_PATH${NC}"
    echo "   Ищу в альтернативных местах..."
    ALTERNATIVE_APK=$(find "$PROJECT_ROOT/composeApp/build/outputs/apk" -name "*.apk" -type f | head -1)
    if [ -n "$ALTERNATIVE_APK" ]; then
        APK_BUILD_PATH="$ALTERNATIVE_APK"
        echo -e "${YELLOW}   Найден APK: $APK_BUILD_PATH${NC}"
    else
        echo -e "${RED}✗ APK не найден ни в одном месте${NC}"
        exit 1
    fi
fi

APK_SIZE=$(ls -lh "$APK_BUILD_PATH" | awk '{print $5}')
echo "  Размер unsigned APK: $APK_SIZE"
echo ""

# Шаг 2: Подпись APK (без дополнительного выравнивания)
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}Шаг 2: Подпись APK${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

echo "🔐 Подпись APK с помощью apksigner (V2/V3 подпись)..."
echo "   Использование apksigner обеспечивает V2 и V3 подписи, необходимые для современных Android"

# apksigner подписывает APK in-place, поэтому копируем сначала
cp "$APK_BUILD_PATH" "$APK_FINAL_PATH"
check_success "APK скопирован для подписи"

# Подписываем APK с V1, V2 и V3 подписями
"$APKSIGNER_CMD" sign \
    --ks "$KEYSTORE_PATH" \
    --ks-pass "pass:$KEYSTORE_PASSWORD" \
    --ks-key-alias "$KEY_ALIAS" \
    --key-pass "pass:$KEY_PASSWORD" \
    --v1-signing-enabled true \
    --v2-signing-enabled true \
    --v3-signing-enabled true \
    "$APK_FINAL_PATH"
check_success "APK подписан с V1/V2/V3 подписями"

# Проверяем подпись
echo "🔍 Проверка подписи APK..."
"$APKSIGNER_CMD" verify --verbose "$APK_FINAL_PATH" > /dev/null 2>&1
check_success "Подпись APK проверена"

FINAL_APK_SIZE=$(ls -lh "$APK_FINAL_PATH" | awk '{print $5}')
echo "  Размер final APK: $FINAL_APK_SIZE"
echo ""

# Шаг 4: Загрузка на сервер
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}Шаг 4: Загрузка на сервер${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

echo "📡 Проверка подключения к серверу..."
ssh $SSH_OPTS "$SSH_USER@$EC2_IP" "echo '✓ Подключение работает'" > /dev/null 2>&1
check_success "Сервер доступен"

echo "📁 Создание директории на сервере..."
ssh $SSH_OPTS "$SSH_USER@$EC2_IP" "mkdir -p $SERVER_APK_DIR"
check_success "Директория создана"

echo "🗑️  Удаление старого APK (если существует)..."
ssh $SSH_OPTS "$SSH_USER@$EC2_IP" "rm -f $SERVER_APK_PATH" 2>/dev/null || true
echo "  ✓ Старый APK удален (если был)"

echo "📤 Загрузка APK на сервер..."
echo "   Локальный файл: $APK_FINAL_PATH"
echo "   Серверный путь: $SERVER_APK_PATH"
scp $SSH_OPTS "$APK_FINAL_PATH" "$SSH_USER@$EC2_IP:$SERVER_APK_PATH"
check_success "APK загружен на сервер"

echo "🔒 Установка прав доступа..."
ssh $SSH_OPTS "$SSH_USER@$EC2_IP" "chmod 644 $SERVER_APK_PATH"
check_success "Права установлены"

echo ""

# Шаг 5: Настройка простого HTTP сервера для раздачи APK
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}Шаг 5: Настройка HTTP сервера для раздачи APK${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

echo "🌐 Настройка Python HTTP сервера для раздачи APK..."
ssh $SSH_OPTS "$SSH_USER@$EC2_IP" bash << ENDSSH
    APK_PORT="$APK_SERVER_PORT"
    APK_DIR="$SERVER_APK_DIR"
    
    # Останавливаем старый HTTP сервер если запущен
    if pgrep -f "python3.*http.server.*\$APK_PORT" > /dev/null; then
        echo "  🛑 Остановка старого HTTP сервера..."
        pkill -f "python3.*http.server.*\$APK_PORT"
        sleep 1
    fi
    
    # Останавливаем старый systemd service если существует
    if sudo systemctl is-active --quiet apk-server.service 2>/dev/null; then
        echo "  🛑 Остановка старого systemd service..."
        sudo systemctl stop apk-server.service
    fi
    
    # Создаем systemd service для HTTP сервера
    echo "  📝 Создание systemd service..."
    sudo tee /etc/systemd/system/apk-server.service > /dev/null <<EOFSERVICE
[Unit]
Description=APK Download HTTP Server
After=network.target

[Service]
Type=simple
User=ubuntu
WorkingDirectory=$SERVER_APK_DIR
ExecStart=/usr/bin/python3 -m http.server $APK_SERVER_PORT
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOFSERVICE
    
    # Перезагружаем systemd и запускаем сервис
    sudo systemctl daemon-reload
    sudo systemctl enable apk-server.service
    sudo systemctl restart apk-server.service
    
    sleep 2
    
    if sudo systemctl is-active --quiet apk-server.service; then
        echo "  ✓ HTTP сервер запущен на порту \$APK_PORT"
    else
        echo "  ⚠ Не удалось запустить через systemd, пробуем в фоне..."
        cd \$APK_DIR
        nohup python3 -m http.server \$APK_PORT > /dev/null 2>&1 &
        sleep 1
        if pgrep -f "python3.*http.server.*\$APK_PORT" > /dev/null; then
            echo "  ✓ HTTP сервер запущен в фоне"
        else
            echo "  ⚠ Не удалось запустить HTTP сервер"
        fi
    fi
ENDSSH

check_success "HTTP сервер настроен"

echo ""

# Шаг 6: Создание QR кода
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}Шаг 6: Создание QR кода${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

echo "📱 Генерация QR кода..."
python3 << EOF
import qrcode
from qrcode.image.pil import PilImage

qr = qrcode.QRCode(
    version=1,
    error_correction=qrcode.constants.ERROR_CORRECT_L,
    box_size=10,
    border=4,
)
qr.add_data('$APK_DOWNLOAD_URL')
qr.make(fit=True)

img = qr.make_image(fill_color="black", back_color="white")
img.save('$QR_CODE_PATH')
print("  ✓ QR код создан: $QR_CODE_PATH")
EOF
check_success "QR код создан"

echo ""

# Итоги
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}✅ Готово!${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo -e "${GREEN}📱 APK загружен на сервер:${NC}"
echo -e "   ${YELLOW}$SERVER_APK_PATH${NC}"
echo ""
echo -e "${GREEN}🔗 URL для скачивания:${NC}"
echo -e "   ${YELLOW}$APK_DOWNLOAD_URL${NC}"
echo ""
echo -e "${GREEN}📱 QR код сохранен:${NC}"
echo -e "   ${YELLOW}$QR_CODE_PATH${NC}"
echo ""
echo -e "${BLUE}💡 Использование:${NC}"
echo "   1. Откройте QR код на телефоне для скачивания APK"
echo "   2. Или перейдите по URL: $APK_DOWNLOAD_URL"
echo ""
echo -e "${YELLOW}⚠️  ВАЖНО:${NC}"
echo "   Убедитесь, что порт $APK_SERVER_PORT открыт в AWS Security Groups:"
echo "   1. Откройте EC2 Console → Security Groups"
echo "   2. Найдите Security Group для вашего инстанса"
echo "   3. Добавьте Inbound Rule:"
echo "      - Type: Custom TCP"
echo "      - Port: $APK_SERVER_PORT"
echo "      - Source: 0.0.0.0/0 (или ваш IP)"
echo ""
echo -e "${GREEN}Готово! 🚀${NC}"

