[CmdletBinding()]
param(
    [ValidatePattern('^[A-Za-z0-9._()-]+$')]
    [string]$ResourceGroup = 'zestia-free-rg',

    [ValidatePattern('^[a-z0-9-]+$')]
    [string]$AppName = 'zestia-api-2ae7144d',

    [ValidatePattern('^[A-Za-z0-9-]+\.database\.windows\.net$')]
    [string]$SqlServer = 'zestia-sql-12345.database.windows.net',

    [ValidatePattern('^[A-Za-z0-9_-]+$')]
    [string]$Database = 'zestia',

    [ValidatePattern('^[A-Za-z0-9_-]+$')]
    [string]$CloudinaryCloudName = 'oryewua7',

    [string]$TemporaryFrontendOrigin = 'https://example.invalid',

    [string]$AzCommand
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

function Resolve-AzureCliCommand {
    param([string]$ExplicitCommand)

    if (-not [string]::IsNullOrWhiteSpace($ExplicitCommand)) {
        $resolvedExplicit = [IO.Path]::GetFullPath($ExplicitCommand)
        if (-not (Test-Path -LiteralPath $resolvedExplicit -PathType Leaf)) {
            throw "Azure CLI command not found: $resolvedExplicit"
        }
        return $resolvedExplicit
    }

    $pathCommand = Get-Command az -ErrorAction SilentlyContinue
    if ($null -ne $pathCommand) {
        return $pathCommand.Source
    }

    $portableCommand = Join-Path ([Environment]::GetFolderPath('Desktop')) `
        'ZESTIA_TOOLS\azure-cli-2.90.0\bin\az.cmd'
    if (Test-Path -LiteralPath $portableCommand -PathType Leaf) {
        return $portableCommand
    }

    throw 'Azure CLI was not found in PATH or Desktop\ZESTIA_TOOLS.'
}

function ConvertTo-PlainTextInMemory {
    param([Parameter(Mandatory = $true)][Security.SecureString]$SecureValue)

    $pointer = [IntPtr]::Zero
    try {
        $pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($SecureValue)
        return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer)
    }
    finally {
        if ($pointer -ne [IntPtr]::Zero) {
            [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer)
        }
    }
}

function Get-AzureCliScalar {
    param(
        [Parameter(Mandatory = $true)][string]$Command,
        [Parameter(Mandatory = $true)][string[]]$Arguments,
        [Parameter(Mandatory = $true)][string]$ErrorMessage
    )

    $value = (& $Command @Arguments --output tsv --only-show-errors | Out-String).Trim()
    if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($value)) {
        throw $ErrorMessage
    }
    return $value
}

$frontendOrigin = $TemporaryFrontendOrigin.TrimEnd('/')
$frontendUri = $null
if (-not [Uri]::TryCreate($frontendOrigin, [UriKind]::Absolute, [ref]$frontendUri) -or
    $frontendUri.Scheme -ne 'https' -or
    -not [string]::IsNullOrEmpty($frontendUri.Query) -or
    -not [string]::IsNullOrEmpty($frontendUri.Fragment) -or
    ($frontendUri.AbsolutePath -ne '/')) {
    throw 'TemporaryFrontendOrigin must be an HTTPS origin without a path, query, fragment, or trailing slash.'
}

$az = Resolve-AzureCliCommand -ExplicitCommand $AzCommand
$subscriptionName = Get-AzureCliScalar -Command $az `
    -Arguments @('account', 'show', '--query', 'name') `
    -ErrorMessage 'Azure CLI is not signed in.'
if ($subscriptionName -ne 'Azure for Students') {
    throw "Expected the active subscription to be 'Azure for Students', found '$subscriptionName'."
}

$subscriptionId = Get-AzureCliScalar -Command $az `
    -Arguments @('account', 'show', '--query', 'id') `
    -ErrorMessage 'Could not read the active Azure subscription ID.'
if ($subscriptionId -notmatch '^[0-9a-fA-F-]{36}$') {
    throw 'The active Azure subscription ID is invalid.'
}

$armEndpoint = Get-AzureCliScalar -Command $az `
    -Arguments @('cloud', 'show', '--query', 'endpoints.resourceManager') `
    -ErrorMessage 'Could not read the Azure Resource Manager endpoint.'
$armEndpoint = $armEndpoint.TrimEnd('/')
if ($armEndpoint -ne 'https://management.azure.com') {
    throw "Unexpected Azure Resource Manager endpoint: $armEndpoint"
}

$webAppHost = Get-AzureCliScalar -Command $az `
    -Arguments @('webapp', 'show', '--resource-group', $ResourceGroup, '--name', $AppName, '--query', 'defaultHostName') `
    -ErrorMessage "Azure App Service '$AppName' was not found in resource group '$ResourceGroup'."
if ($webAppHost -ne "$AppName.azurewebsites.net") {
    throw "Unexpected App Service host name: $webAppHost"
}

Write-Host 'Zestia Azure backend settings' -ForegroundColor Green
Write-Host "Subscription : $subscriptionName"
Write-Host "Resource group: $ResourceGroup"
Write-Host "App Service  : $AppName"
Write-Host "Backend URL  : https://$webAppHost"
Write-Host "SQL target   : $SqlServer/$Database"
Write-Host "Cloudinary   : $CloudinaryCloudName"
Write-Host "Temporary UI : $frontendOrigin"
Write-Host ''
Write-Warning 'Paste only each value, without DB_PASSWORD= or another NAME= prefix. Input remains hidden. Secrets are sent directly to Azure over HTTPS and are not written to a file or command line.'

$confirmation = Read-Host "Type the exact App Service name '$AppName' to continue"
if ($confirmation -cne $AppName) {
    throw 'Cancelled: the confirmation did not match the App Service name.'
}

$dbSecure = $null
$dbConfirmSecure = $null
$jwtSecure = $null
$adminSecure = $null
$cloudinaryKeySecure = $null
$cloudinarySecretSecure = $null
$dbPassword = $null
$dbConfirmation = $null
$jwtSecret = $null
$adminPassword = $null
$cloudinaryApiKey = $null
$cloudinaryApiSecret = $null
$accessToken = $null
$headers = $null
$submittedSecrets = $null
$settingsToApply = $null
$mergedSettings = $null
$requestBody = $null
$currentResponse = $null
$updateResponse = $null
$verifyResponse = $null

try {
    $dbSecure = Read-Host 'DB_PASSWORD for zestia_app (hidden)' -AsSecureString
    $dbConfirmSecure = Read-Host 'Repeat DB_PASSWORD (hidden)' -AsSecureString
    $jwtSecure = Read-Host 'JWT_SECRET (hidden)' -AsSecureString
    $adminSecure = Read-Host 'DEPLOYMENT_ADMIN_PASSWORD (hidden)' -AsSecureString
    $cloudinaryKeySecure = Read-Host 'Cloudinary API Key (hidden)' -AsSecureString
    $cloudinarySecretSecure = Read-Host 'Cloudinary API Secret (hidden)' -AsSecureString

    $dbPassword = ConvertTo-PlainTextInMemory -SecureValue $dbSecure
    $dbConfirmation = ConvertTo-PlainTextInMemory -SecureValue $dbConfirmSecure
    $jwtSecret = ConvertTo-PlainTextInMemory -SecureValue $jwtSecure
    $adminPassword = ConvertTo-PlainTextInMemory -SecureValue $adminSecure
    $cloudinaryApiKey = ConvertTo-PlainTextInMemory -SecureValue $cloudinaryKeySecure
    $cloudinaryApiSecret = ConvertTo-PlainTextInMemory -SecureValue $cloudinarySecretSecure

    $submittedSecrets = [ordered]@{
        DB_PASSWORD = $dbPassword
        JWT_SECRET = $jwtSecret
        DEPLOYMENT_ADMIN_PASSWORD = $adminPassword
        CLOUDINARY_API_KEY = $cloudinaryApiKey
        CLOUDINARY_API_SECRET = $cloudinaryApiSecret
    }
    if ($dbPassword -cne $dbConfirmation) {
        throw 'DB_PASSWORD entries did not match.'
    }
    foreach ($entry in $submittedSecrets.GetEnumerator()) {
        if ([string]::IsNullOrWhiteSpace([string]$entry.Value)) {
            throw "$($entry.Key) cannot be empty."
        }
        if ([string]$entry.Value -like "$($entry.Key)=*") {
            throw "Paste only the value for $($entry.Key), without the '$($entry.Key)=' prefix."
        }
        if ([string]$entry.Value -match "[\r\n]") {
            throw "$($entry.Key) cannot contain a line break."
        }
    }
    if ($dbPassword.Length -lt 16 -or $dbPassword.Length -gt 128) {
        throw 'DB_PASSWORD must contain between 16 and 128 characters.'
    }
    if ([Text.Encoding]::UTF8.GetByteCount($jwtSecret) -lt 64) {
        throw 'JWT_SECRET must contain at least 64 UTF-8 bytes.'
    }
    if ($adminPassword.Length -lt 16) {
        throw 'DEPLOYMENT_ADMIN_PASSWORD must contain at least 16 characters.'
    }
    if ($cloudinaryApiKey.Length -lt 6 -or $cloudinaryApiSecret.Length -lt 12) {
        throw 'The Cloudinary API Key or API Secret is shorter than expected.'
    }

    $settingsToApply = [ordered]@{
        SPRING_PROFILES_ACTIVE = 'prod'
        SERVER_PORT = '80'
        DB_URL = "jdbc:sqlserver://$SqlServer`:1433;databaseName=$Database;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;"
        DB_USERNAME = 'zestia_app'
        DB_PASSWORD = $dbPassword
        JWT_SECRET = $jwtSecret
        APP_BACKEND_URL = "https://$webAppHost"
        APP_FRONTEND_URL = $frontendOrigin
        APP_PAYMENT_RESULT_URL = "$frontendOrigin/payment-result"
        APP_CORS_ALLOWED_ORIGINS = $frontendOrigin
        MEDIA_STORAGE_PROVIDER = 'cloudinary'
        CLOUDINARY_CLOUD_NAME = $CloudinaryCloudName
        CLOUDINARY_API_KEY = $cloudinaryApiKey
        CLOUDINARY_API_SECRET = $cloudinaryApiSecret
        JAVA_OPTS = '-Xms128m -Xmx512m -Duser.timezone=Asia/Ho_Chi_Minh -Dfile.encoding=UTF-8'
        JPA_DDL_AUTO = 'validate'
        JPA_SHOW_SQL = 'false'
        PASSWORD_RESET_TOKEN_MINUTES = '30'
        DEPLOYMENT_ADMIN_USERNAME = 'admin'
        DEPLOYMENT_ADMIN_PASSWORD = $adminPassword
        PAYMENT_MOMO_ENABLED = 'false'
        PAYMENT_ZALOPAY_ENABLED = 'false'
        PAYMENT_REFUND_REMOTE_ENABLED = 'false'
        WEBSITES_CONTAINER_START_TIME_LIMIT = '600'
    }

    $accessToken = Get-AzureCliScalar -Command $az `
        -Arguments @('account', 'get-access-token', '--resource-type', 'arm', '--subscription', $subscriptionId, '--query', 'accessToken') `
        -ErrorMessage 'Could not obtain an Azure Resource Manager access token.'
    $headers = @{ Authorization = "Bearer $accessToken" }
    $resourceGroupSegment = [Uri]::EscapeDataString($ResourceGroup)
    $appNameSegment = [Uri]::EscapeDataString($AppName)
    $baseUri = "$armEndpoint/subscriptions/$subscriptionId/resourceGroups/$resourceGroupSegment/providers/Microsoft.Web/sites/$appNameSegment/config/appsettings"
    $listUri = "$baseUri/list?api-version=2026-07-15"
    $updateUri = "$baseUri`?api-version=2026-07-15"

    Write-Host 'Reading existing App Service setting names...'
    $currentResponse = Invoke-RestMethod -Method Post -Uri $listUri -Headers $headers -ContentType 'application/json; charset=utf-8'
    if ($null -eq $currentResponse -or $null -eq $currentResponse.properties) {
        throw 'Azure returned a malformed application-settings response; no update was attempted.'
    }
    $mergedSettings = [Collections.Generic.Dictionary[string, string]]::new(
        [StringComparer]::OrdinalIgnoreCase
    )
    foreach ($property in $currentResponse.properties.PSObject.Properties) {
        $mergedSettings[$property.Name] = [string]$property.Value
    }
    foreach ($entry in $settingsToApply.GetEnumerator()) {
        $mergedSettings[$entry.Key] = [string]$entry.Value
    }

    $requestBody = @{ properties = $mergedSettings } | ConvertTo-Json -Depth 4 -Compress
    Write-Host "Sending $($settingsToApply.Count) settings directly to Azure over HTTPS..."
    $updateResponse = Invoke-RestMethod -Method Put -Uri $updateUri -Headers $headers `
        -ContentType 'application/json; charset=utf-8' -Body $requestBody
    $updateResponse = $null

    $verifyResponse = Invoke-RestMethod -Method Post -Uri $listUri -Headers $headers -ContentType 'application/json; charset=utf-8'
    foreach ($entry in $settingsToApply.GetEnumerator()) {
        $actualProperty = $verifyResponse.properties.PSObject.Properties[$entry.Key]
        if ($null -eq $actualProperty -or [string]$actualProperty.Value -cne [string]$entry.Value) {
            throw "Azure verification failed for setting name '$($entry.Key)'."
        }
    }

    Write-Host ''
    Write-Host 'Azure backend settings uploaded and verified successfully.' -ForegroundColor Green
    Write-Host "Configured setting count: $($settingsToApply.Count)"
    Write-Host 'No setting values were printed or written to a local file.'
    Write-Warning 'APP_FRONTEND_URL and CORS currently use https://example.invalid. They will be replaced after the Netlify site is created.'
}
finally {
    if ($null -ne $headers) { $headers.Clear() }
    if ($null -ne $settingsToApply) { $settingsToApply.Clear() }
    if ($null -ne $mergedSettings) { $mergedSettings.Clear() }

    $submittedSecrets = $null
    $requestBody = $null
    $currentResponse = $null
    $updateResponse = $null
    $verifyResponse = $null
    $accessToken = $null
    $dbPassword = $null
    $dbConfirmation = $null
    $jwtSecret = $null
    $adminPassword = $null
    $cloudinaryApiKey = $null
    $cloudinaryApiSecret = $null

    if ($null -ne $dbSecure) { $dbSecure.Dispose() }
    if ($null -ne $dbConfirmSecure) { $dbConfirmSecure.Dispose() }
    if ($null -ne $jwtSecure) { $jwtSecure.Dispose() }
    if ($null -ne $adminSecure) { $adminSecure.Dispose() }
    if ($null -ne $cloudinaryKeySecure) { $cloudinaryKeySecure.Dispose() }
    if ($null -ne $cloudinarySecretSecure) { $cloudinarySecretSecure.Dispose() }
}
