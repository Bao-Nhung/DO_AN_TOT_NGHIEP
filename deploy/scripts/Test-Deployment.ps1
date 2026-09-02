[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$FrontendUrl,

    [Parameter(Mandatory = $true)]
    [string]$BackendUrl,

    [switch]$SkipPublicApi
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$script:failureCount = 0

function Write-Pass {
    param([string]$Message)
    Write-Host "[PASS] $Message" -ForegroundColor Green
}

function Write-Fail {
    param([string]$Message)
    $script:failureCount++
    Write-Host "[FAIL] $Message" -ForegroundColor Red
}

function Invoke-SafeWebRequest {
    param(
        [string]$Uri,
        [string]$Method = 'GET',
        [hashtable]$Headers = @{},
        [int]$TimeoutSec = 30
    )
    return Invoke-WebRequest -Uri $Uri -Method $Method -Headers $Headers -TimeoutSec $TimeoutSec -MaximumRedirection 5 -UseBasicParsing
}

$FrontendUrl = $FrontendUrl.Trim().TrimEnd('/')
$BackendUrl = $BackendUrl.Trim().TrimEnd('/')

foreach ($pair in @(
    @{ Name = 'FrontendUrl'; Value = $FrontendUrl },
    @{ Name = 'BackendUrl'; Value = $BackendUrl }
)) {
    $parsed = $null
    if (-not [Uri]::TryCreate($pair.Value, [UriKind]::Absolute, [ref]$parsed) -or $parsed.Scheme -ne 'https') {
        throw "$($pair.Name) phai la URL HTTPS day du."
    }
}

$frontendOrigin = ([Uri]$FrontendUrl).GetLeftPart([UriPartial]::Authority)
$healthUrl = "$BackendUrl/actuator/health"

Write-Host ""
Write-Host 'KIEM TRA SAU KHI DEPLOY ZESTIA' -ForegroundColor Cyan
Write-Host "Frontend: $FrontendUrl"
Write-Host "Backend : $BackendUrl"
Write-Host ""

try {
    $frontendResponse = Invoke-SafeWebRequest -Uri $FrontendUrl
    if ($frontendResponse.StatusCode -ge 200 -and $frontendResponse.StatusCode -lt 400 -and
        $frontendResponse.Content -match '<html') {
        Write-Pass "Frontend tra ve HTML (HTTP $($frontendResponse.StatusCode))."
    } else {
        Write-Fail "Frontend khong tra ve trang HTML hop le."
    }
} catch {
    Write-Fail "Khong truy cap duoc frontend: $($_.Exception.Message)"
}

$healthResponse = $null
for ($attempt = 1; $attempt -le 6; $attempt++) {
    try {
        $healthResponse = Invoke-SafeWebRequest -Uri $healthUrl -TimeoutSec 45
        break
    } catch {
        if ($attempt -lt 6) {
            Write-Host "Backend co the dang cold start; thu lai lan $($attempt + 1)/6 sau 10 giay..."
            Start-Sleep -Seconds 10
        } else {
            Write-Fail "Health check that bai sau 6 lan: $($_.Exception.Message)"
        }
    }
}
if ($null -ne $healthResponse) {
    if ($healthResponse.StatusCode -eq 200) {
        Write-Pass "Backend health hoat dong (HTTP 200)."
    } else {
        Write-Fail "Backend health tra ve HTTP $($healthResponse.StatusCode)."
    }
}

try {
    $corsResponse = Invoke-SafeWebRequest -Uri $healthUrl -Method 'OPTIONS' -Headers @{
        Origin = $frontendOrigin
        'Access-Control-Request-Method' = 'GET'
        'Access-Control-Request-Headers' = 'authorization,content-type'
    }
    $allowOrigin = [string]$corsResponse.Headers['Access-Control-Allow-Origin']
    if ($allowOrigin -eq $frontendOrigin) {
        Write-Pass "CORS cho phep dung frontend origin $frontendOrigin."
    } else {
        Write-Fail "CORS sai. Access-Control-Allow-Origin='$allowOrigin', can '$frontendOrigin'."
    }
} catch {
    Write-Fail "CORS preflight that bai: $($_.Exception.Message)"
}

if (-not $SkipPublicApi) {
    try {
        $publicResponse = Invoke-SafeWebRequest -Uri "$BackendUrl/api/storefront/summary" -TimeoutSec 45
        if ($publicResponse.StatusCode -eq 200) {
            Write-Pass 'API storefront cong khai tra ve HTTP 200.'
        } else {
            Write-Fail "API storefront tra ve HTTP $($publicResponse.StatusCode)."
        }
    } catch {
        Write-Fail "API storefront khong hoat dong: $($_.Exception.Message)"
    }

    try {
        $paymentResponse = Invoke-SafeWebRequest -Uri "$BackendUrl/api/payment/methods" -TimeoutSec 45
        $paymentMethods = $paymentResponse.Content | ConvertFrom-Json
        if ($paymentResponse.StatusCode -eq 200 -and $null -ne $paymentMethods.COD) {
            $momoState = if ($paymentMethods.MOMO.available) { 'ready' } else { 'disabled' }
            $zaloState = if ($paymentMethods.ZALOPAY.available) { 'ready' } else { 'disabled' }
            Write-Pass "Payment capabilities tra ve an toan (MoMo=$momoState, ZaloPay=$zaloState)."
        } else {
            Write-Fail 'Payment capabilities khong tra ve du lieu hop le.'
        }
    } catch {
        Write-Fail "Khong doc duoc payment capabilities: $($_.Exception.Message)"
    }
}

Write-Host ""
if ($script:failureCount -gt 0) {
    Write-Host "KET QUA: $script:failureCount muc chua dat." -ForegroundColor Red
    exit 1
}

Write-Host 'KET QUA: Cac kiem tra cong khai deu dat.' -ForegroundColor Green
exit 0
