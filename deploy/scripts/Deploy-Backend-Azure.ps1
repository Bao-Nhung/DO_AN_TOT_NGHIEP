[CmdletBinding(SupportsShouldProcess = $true, ConfirmImpact = 'Medium')]
param(
    [Parameter(Mandatory = $true)]
    [ValidateNotNullOrEmpty()]
    [string]$ResourceGroup,

    [Parameter(Mandatory = $true)]
    [ValidatePattern('^[a-zA-Z0-9-]+$')]
    [string]$AppName,

    [string]$JarPath
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Resolve-AzureCliCommand {
    $pathCommand = Get-Command az -ErrorAction SilentlyContinue
    if ($null -ne $pathCommand) {
        return $pathCommand.Source
    }

    $portableCommand = Join-Path ([Environment]::GetFolderPath('Desktop')) `
        'ZESTIA_TOOLS\azure-cli-2.90.0\bin\az.cmd'
    if (Test-Path -LiteralPath $portableCommand -PathType Leaf) {
        return $portableCommand
    }

    throw "Chua tim thay Azure CLI trong PATH hoac Desktop\ZESTIA_TOOLS."
}

$az = Resolve-AzureCliCommand

$projectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\..'))
if ([string]::IsNullOrWhiteSpace($JarPath)) {
    $JarPath = Join-Path $projectRoot 'release\zestia-backend.jar'
}
$JarPath = [IO.Path]::GetFullPath($JarPath)

if (-not (Test-Path -LiteralPath $JarPath -PathType Leaf)) {
    throw "Khong tim thay JAR: $JarPath. Hay chay Build-Release.ps1 truoc."
}
if ([IO.Path]::GetExtension($JarPath) -ne '.jar') {
    throw "JarPath phai tro toi file .jar."
}

Write-Host 'Kiem tra dang nhap Azure CLI...'
& $az account show --only-show-errors --output none
if ($LASTEXITCODE -ne 0) {
    throw "Chua dang nhap Azure CLI. Chay 'az login' va chon dung subscription."
}

& $az webapp show --resource-group $ResourceGroup --name $AppName --only-show-errors --output none
if ($LASTEXITCODE -ne 0) {
    throw "Khong tim thay App Service '$AppName' trong resource group '$ResourceGroup'."
}

$requiredSettings = @(
    'SPRING_PROFILES_ACTIVE',
    'SERVER_PORT',
    'DB_URL',
    'DB_USERNAME',
    'DB_PASSWORD',
    'JWT_SECRET',
    'APP_BACKEND_URL',
    'APP_FRONTEND_URL',
    'APP_PAYMENT_RESULT_URL',
    'APP_CORS_ALLOWED_ORIGINS',
    'MEDIA_STORAGE_PROVIDER',
    'CLOUDINARY_CLOUD_NAME',
    'CLOUDINARY_API_KEY',
    'CLOUDINARY_API_SECRET',
    'PAYMENT_MOMO_ENABLED',
    'PAYMENT_ZALOPAY_ENABLED',
    'PAYMENT_REFUND_REMOTE_ENABLED',
    'JAVA_OPTS'
)
$settingNames = @(
    & $az webapp config appsettings list --resource-group $ResourceGroup --name $AppName --query '[].name' --output tsv --only-show-errors
)
if ($LASTEXITCODE -ne 0) {
    throw 'Khong doc duoc danh sach App Settings.'
}

$missing = @($requiredSettings | Where-Object { $_ -notin $settingNames })
if ($missing.Count -gt 0) {
    throw "Tu choi deploy vi Azure dang thieu ten bien: $($missing -join ', '). Bo sung theo deploy/env/backend-azure-app-settings.example. Script khong doc hay in gia tri bi mat."
}

$target = "Azure App Service $AppName trong resource group $ResourceGroup"
if (-not $PSCmdlet.ShouldProcess($target, "Cau hinh Java startup va deploy $JarPath bang OneDeploy (type jar)")) {
    return
}

$startupCommand = 'java $JAVA_OPTS -jar /home/site/wwwroot/app.jar --server.port=80'
$currentStartupCommand = (& $az webapp config show --resource-group $ResourceGroup --name $AppName `
    --query 'appCommandLine' --output tsv --only-show-errors | Out-String).Trim()
if ($LASTEXITCODE -ne 0) {
    throw "Khong doc duoc startup command cua Azure App Service."
}
if ($currentStartupCommand -cne $startupCommand) {
    Write-Host 'Dang cau hinh startup command Java SE...'
    & $az webapp config set --resource-group $ResourceGroup --name $AppName `
        --startup-file $startupCommand --only-show-errors --output none
    if ($LASTEXITCODE -ne 0) {
        throw "Khong cau hinh duoc startup command cho Azure App Service."
    }
}
else {
    Write-Host 'Startup command da dung; bo qua mot lan restart khong can thiet.'
}

Write-Host "Dang deploy $JarPath ..."
& $az webapp deploy --resource-group $ResourceGroup --name $AppName --src-path $JarPath --type jar --only-show-errors
if ($LASTEXITCODE -ne 0) {
    throw "Azure deploy that bai voi exit code $LASTEXITCODE."
}

Write-Host ""
Write-Host 'Deploy backend hoan tat.' -ForegroundColor Green
Write-Host "URL: https://$AppName.azurewebsites.net"
Write-Host "Test: .\deploy\scripts\Test-Deployment.ps1 -FrontendUrl https://<frontend> -BackendUrl https://$AppName.azurewebsites.net"
