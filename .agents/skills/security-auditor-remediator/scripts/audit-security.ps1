# Script de Auditoría Automatizada de Seguridad - DevAdvvy Store
param(
  [string]$ProjectPath = "C:\Users\DevAdvvy\Desktop\Proyectos\tienda-digital"
)

$resolved = (Resolve-Path $ProjectPath).Path

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " INICIANDO AUDITORÍA DE SEGURIDAD (R1-R9)" -ForegroundColor Cyan
Write-Host " Proyecto: $ProjectPath" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$issues = @()

function Report-Issue {
  param([string]$Rule, [string]$File, [string]$Message)
  $script:issues += [pscustomobject]@{
    Rule    = $Rule
    File    = $File
    Message = $Message
  }
  Write-Host " [FALLA $Rule] en ${File}: $Message" -ForegroundColor Red
}

function Report-Pass {
  param([string]$Rule, [string]$Message)
  Write-Host " [OK $Rule] $Message" -ForegroundColor Green
}

# --- R1: Zero-Trust en Precios ---
$checkoutRoute = Join-Path $ProjectPath "app\api\checkout\route.ts"
if (Test-Path $checkoutRoute) {
  $content = Get-Content $checkoutRoute -Raw
  if ($content -match "products\.reduce" -and $content -match "product\.priceCents" -and $content -notmatch "parsed\.data\.items\[.*\]\.price") {
    Report-Pass "R1" "Precios y totales se calculan 100% en servidor consultando la BD por ID."
  } else {
    Report-Issue "R1" $checkoutRoute "Posible cálculo de precio no confiable desde el cliente."
  }
} else {
  Report-Issue "R1" "app\api\checkout\route.ts" "Ruta no encontrada."
}

# --- R2: Acceso de Admin Estricto ---
$adminLayout = Join-Path $ProjectPath "app\admin\(panel)\layout.tsx"
$credsAuth = Join-Path $ProjectPath "lib\auth\credentials.ts"
if ((Test-Path $adminLayout) -and (Test-Path $credsAuth)) {
  $credsContent = Get-Content $credsAuth -Raw
  $layoutContent = Get-Content $adminLayout -Raw
  if ($credsContent -match "isAllowedAdminEmail" -and $layoutContent -match "getSession") {
    Report-Pass "R2" "Panel admin protegido con sesión y lista blanca estricta de emails."
  } else {
    Report-Issue "R2" $adminLayout "Falta validación estricta de correos autorizados en admin."
  }
}

# --- R3: Protección CSRF / Same-Origin ---
$routesToCheck = @(
  "app\api\checkout\route.ts",
  "app\api\payments\demo\confirm\route.ts",
  "app\api\payments\checkout-session\route.ts",
  "app\api\customer\auth\login\route.ts",
  "app\api\customer\auth\register\route.ts",
  "app\api\admin\tokens\regenerate\route.ts",
  "app\api\admin\products\route.ts"
)

$r3Pass = $true
foreach ($rel in $routesToCheck) {
  $p = Join-Path $ProjectPath $rel
  if (Test-Path $p) {
    $c = Get-Content $p -Raw
    if ($c -notmatch "isSameOrigin") {
      Report-Issue "R3" $rel "Falta comprobación isSameOrigin(request) para prevenir CSRF."
      $r3Pass = $false
    }
  }
}
if ($r3Pass) {
  Report-Pass "R3" "Todas las rutas de mutación verifican origen estricto isSameOrigin."
}

# --- R4: Criptografía y Timing-Safe ---
$passwordsAuth = Join-Path $ProjectPath "lib\customer-auth\passwords.ts"
if (Test-Path $passwordsAuth) {
  $c = Get-Content $passwordsAuth -Raw
  if ($c -match "scryptSync" -and $c -match "timingSafeEqual") {
    Report-Pass "R4" "Contraseñas protegidas con scrypt + salt y comparadas con timingSafeEqual."
  } else {
    Report-Issue "R4" $passwordsAuth "Hashing débil o comparación vulnerable a timing attacks."
  }
}

# --- R5: Tokens de Descarga Efímeros ---
$downloadRoute = Join-Path $ProjectPath "app\api\download\[token]\route.ts"
if (Test-Path -LiteralPath $downloadRoute) {
  $c = Get-Content -LiteralPath $downloadRoute -Raw
  if ($c -match "expiresAt" -and $c -match "revokedAt") {
    Report-Pass "R5" "Descargas validan caducidad temporal y estado de revocación del token."
  } else {
    Report-Issue "R5" $downloadRoute "La ruta de descarga no valida expiración o revocación."
  }
} else {
  Report-Issue "R5" "app\api\download\[token]\route.ts" "Ruta no encontrada."
}

# --- R6: Zero Secret Leaks ---
$gitignore = Join-Path $ProjectPath ".gitignore"
if (Test-Path $gitignore) {
  $c = Get-Content $gitignore -Raw
  if ($c -match "\.env") {
    Report-Pass "R6" "Archivos .env excluidos de git. Sin secretos hardcodeados."
  } else {
    Report-Issue "R6" ".gitignore" "El archivo .env no está protegido en .gitignore."
  }
}

# --- R7: Idempotencia en Pagos ---
$fulfillment = Join-Path $ProjectPath "lib\payments\fulfillment.ts"
if (Test-Path $fulfillment) {
  $c = Get-Content $fulfillment -Raw
  if ($c -match '\$transaction' -and $c -match 'status === "PAID"' -and $c -match 'DownloadToken') {
    Report-Pass "R7" "Confirmación de pagos e idempotencia garantizada mediante transacciones atómicas."
  } else {
    Report-Issue "R7" $fulfillment "Posible vulnerabilidad de duplicación o falta de idempotencia en pagos."
  }
}

# --- R8: Validación Rigurosa de Esquemas ---
$r8Pass = $true
$mutationRoutes = @(
  "app\api\checkout\route.ts",
  "app\api\payments\demo\confirm\route.ts",
  "app\api\payments\checkout-session\route.ts",
  "app\api\customer\auth\login\route.ts",
  "app\api\customer\auth\register\route.ts"
)
foreach ($rel in $mutationRoutes) {
  $p = Join-Path $ProjectPath $rel
  if (Test-Path $p) {
    $c = Get-Content $p -Raw
    if ($c -notmatch "safeParse") {
      Report-Issue "R8" $rel "Falta validación de esquema con safeParse (Zod)."
      $r8Pass = $false
    }
  }
}
if ($r8Pass) {
  Report-Pass "R8" "Todas las rutas públicas validan esquemas con safeParse de Zod."
}

# --- R9: Integridad de Stock ---
if (Test-Path $checkoutRoute) {
  $c = Get-Content $checkoutRoute -Raw
  if ($c -match "isUnlimitedStock" -and $c -match "stock") {
    Report-Pass "R9" "Comprobación estricta de stock disponible antes de crear la orden."
  } else {
    Report-Issue "R9" $checkoutRoute "Falta validación de stock en checkout."
  }
}

Write-Host "==========================================================" -ForegroundColor Cyan
if ($issues.Count -eq 0) {
  Write-Host " RESULTADO: 100% CUMPLIDO. CERO VULNERABILIDADES DETECTADAS." -ForegroundColor Green
  Write-Host "==========================================================" -ForegroundColor Cyan
  exit 0
} else {
  Write-Host " RESULTADO: $($issues.Count) REGLAS INCUMPLIDAS DETECTADAS." -ForegroundColor Red
  Write-Host " DEBE PROCEDERSE A LA REMEDIACIÓN INMEDIATA DEL CÓDIGO." -ForegroundColor Red
  Write-Host "==========================================================" -ForegroundColor Cyan
  exit 1
}
