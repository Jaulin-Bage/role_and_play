# 设置SDK根目录
$ANDROID_SDK_ROOT = "C:\Android\Sdk"
$CMD_TOOLS_VERSION = "11076708"

# 创建必要的目录结构
New-Item -Path "$ANDROID_SDK_ROOT\cmdline-tools" -ItemType Directory -Force | Out-Null
New-Item -Path "$ANDROID_SDK_ROOT\cmdline-tools\latest" -ItemType Directory -Force | Out-Null

# 下载并提取SDK命令行工具
$DOWNLOAD_URL = "https://dl.google.com/android/repository/commandlinetools-win-${CMD_TOOLS_VERSION}_latest.zip"
$ZIP_FILE = "$env:TEMP\commandlinetools-win.zip"

Write-Output "Downloading SDK command line tools..."
Invoke-WebRequest -Uri $DOWNLOAD_URL -OutFile $ZIP_FILE

Write-Output "Extracting SDK command line tools..."
Expand-Archive -Path $ZIP_FILE -DestinationPath "$env:TEMP\cmdline-tools" -Force

# 将工具移动到正确的目录
Write-Output "Moving SDK tools to correct location..."
Get-ChildItem -Path "$env:TEMP\cmdline-tools\cmdline-tools\*" -Recurse | Move-Item -Destination "$ANDROID_SDK_ROOT\cmdline-tools\latest" -Force

# 添加SDK工具到PATH环境变量
$SDK_PLATFORM_TOOLS = "$ANDROID_SDK_ROOT\platform-tools"
$SDK_TOOLS_BIN = "$ANDROID_SDK_ROOT\cmdline-tools\latest\bin"
$env:PATH = "$SDK_TOOLS_BIN;$SDK_PLATFORM_TOOLS;$env:PATH"

# 安装必要的SDK组件
Write-Output "Installing SDK components..."
$SDK_MANAGER = "$SDK_TOOLS_BIN\sdkmanager.bat"

& $SDK_MANAGER --sdk_root=$ANDROID_SDK_ROOT "platform-tools" "platforms;android-33" "build-tools;33.0.0" --verbose --no_https

Write-Output "SDK installation completed!"
Write-Output "SDK root directory: $ANDROID_SDK_ROOT"
Write-Output "SDK tools path: $SDK_TOOLS_BIN"