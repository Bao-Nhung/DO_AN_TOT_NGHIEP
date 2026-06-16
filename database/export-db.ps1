# =====================================================================
# Export toan bo database fashion_shop -> 1 file SQL chuan (schema + data)
# Dung ADO.NET (System.Data.SqlClient) - khong phu thuoc SMO.
# Chay:  powershell -ExecutionPolicy Bypass -File export-db.ps1
# =====================================================================
$ErrorActionPreference = 'Stop'

$connStr = "Server=localhost,1433;Database=fashion_shop;User Id=sa;Password=123456;TrustServerCertificate=True;Encrypt=True;"
$dbName  = 'fashion_shop'
$outFile = Join-Path $PSScriptRoot 'fashion_shop.sql'

# Thu tu phu thuoc khoa ngoai (cha truoc, con sau)
$tableOrder = @(
  'Vai_tro','Nhan_vien','Khach_hang','Dia_chi',
  'Loai_vay','Chat_lieu','Mau_Sac','Kich_Thuoc','Nha_cung_cap','Tai_tro',
  'Khuyen_mai','Giam_gia',
  'Vay','Vay_chi_tiet','Anh',
  'Gio_hang','Gio_hang_chi_tiet',
  'Hoa_don','Hoa_don_chi_tiet','Lich_su_thanh_toan',
  'Danh_gia','San_pham_yeu_thich','Lich_su_xem','Thong_bao','Nhat_ky'
)

Add-Type -AssemblyName System.Data
$conn = New-Object System.Data.SqlClient.SqlConnection($connStr)
$conn.Open()

# Lay dinh nghia cot 1 bang
function Get-Columns($table) {
  $sql = @"
SELECT c.name AS col, t.name AS type, c.max_length AS maxlen,
       c.precision AS prec, c.scale AS scale, c.is_nullable AS nullable,
       c.is_identity AS ident, c.column_id AS ord
FROM sys.columns c
JOIN sys.types t ON c.user_type_id = t.user_type_id
WHERE c.object_id = OBJECT_ID(N'dbo.$table')
ORDER BY c.column_id;
"@
  $cmd = $conn.CreateCommand(); $cmd.CommandText = $sql
  $r = $cmd.ExecuteReader()
  $cols = @()
  while ($r.Read()) {
    $cols += [PSCustomObject]@{
      Name=$r['col']; Type=$r['type']; MaxLen=[int]$r['maxlen'];
      Prec=[int]$r['prec']; Scale=[int]$r['scale'];
      Nullable=[bool]$r['nullable']; Identity=[bool]$r['ident']
    }
  }
  $r.Close()
  ,$cols
}

# Lay cot khoa chinh
function Get-PrimaryKey($table) {
  $sql = @"
SELECT c.name AS col
FROM sys.indexes i
JOIN sys.index_columns ic ON i.object_id=ic.object_id AND i.index_id=ic.index_id
JOIN sys.columns c ON ic.object_id=c.object_id AND ic.column_id=c.column_id
WHERE i.is_primary_key=1 AND i.object_id=OBJECT_ID(N'dbo.$table')
ORDER BY ic.key_ordinal;
"@
  $cmd = $conn.CreateCommand(); $cmd.CommandText = $sql
  $r = $cmd.ExecuteReader(); $pk=@()
  while ($r.Read()) { $pk += $r['col'] }
  $r.Close(); ,$pk
}

# Sinh kieu cot cho CREATE TABLE
function Format-Type($c) {
  $t = $c.Type.ToLower()
  switch -regex ($t) {
    '^(nvarchar|nchar)$' { $len = if ($c.MaxLen -eq -1) {'max'} else {[int]($c.MaxLen/2)}; return "$($c.Type)($len)" }
    '^(varchar|char|varbinary|binary)$' { $len = if ($c.MaxLen -eq -1) {'max'} else {$c.MaxLen}; return "$($c.Type)($len)" }
    '^(decimal|numeric)$' { return "$($c.Type)($($c.Prec),$($c.Scale))" }
    '^(datetime2|time|datetimeoffset)$' { return "$($c.Type)($($c.Scale))" }
    default { return $c.Type }
  }
}

# Escape gia tri 1 o du lieu thanh literal SQL
function Format-Value($val, $type) {
  if ($val -is [System.DBNull]) { return 'NULL' }
  $t = $type.ToLower()
  switch -regex ($t) {
    '^(nvarchar|nchar|ntext)$'      { return "N'" + ($val -replace "'","''") + "'" }
    '^(varchar|char|text)$'         { return "N'" + ($val -replace "'","''") + "'" }
    '^bit$'                         { if ([bool]$val) {return '1'} else {return '0'} }
    '^(tinyint|smallint|int|bigint)$' { return [string]$val }
    '^(decimal|numeric|money|smallmoney)$' { return ([decimal]$val).ToString([System.Globalization.CultureInfo]::InvariantCulture) }
    '^(float|real)$'                { return ([double]$val).ToString('R',[System.Globalization.CultureInfo]::InvariantCulture) }
    '^(date)$'                      { return "'" + ([datetime]$val).ToString('yyyy-MM-dd') + "'" }
    '^(datetime|datetime2|smalldatetime|datetimeoffset)$' { return "'" + ([datetime]$val).ToString('yyyy-MM-ddTHH:mm:ss.fff') + "'" }
    '^(time)$'                      { return "'" + ([timespan]$val).ToString() + "'" }
    '^uniqueidentifier$'            { return "'" + [string]$val + "'" }
    '^(varbinary|binary|image)$'    { return '0x' + (($val | ForEach-Object { $_.ToString('x2') }) -join '') }
    default                         { return "N'" + ($val -replace "'","''") + "'" }
  }
}

$sb = New-Object System.Text.StringBuilder
function W($s) { [void]$sb.AppendLine($s) }

W "-- ============================================================"
W "-- ZESTIA  -  fashion_shop  (schema + du lieu day du)"
W "-- File chuan, tu dong export tu DB dang chay."
W "-- Cach dung: mo SSMS hoac sqlcmd, mo file nay, Execute 1 lan."
W "-- ============================================================"
W "IF DB_ID('$dbName') IS NULL CREATE DATABASE [$dbName];"
W "GO"
W "USE [$dbName];"
W "GO"
W ""

foreach ($table in $tableOrder) {
  $cols = Get-Columns $table
  if ($cols.Count -eq 0) { Write-Warning "Bo qua (khong thay bang): $table"; continue }
  $pk = Get-PrimaryKey $table
  $hasIdentity = @($cols | Where-Object { $_.Identity }).Count -gt 0

  Write-Output "Export: $table"

  # ---- CREATE TABLE IF NOT EXISTS ----
  W "-- ===== $table ====="
  W "IF OBJECT_ID(N'dbo.$table','U') IS NULL"
  W "BEGIN"
  $colDefs = @()
  foreach ($c in $cols) {
    $def = "  [$($c.Name)] $(Format-Type $c)"
    if ($c.Identity) { $def += " IDENTITY(1,1)" }
    if (-not $c.Nullable) { $def += " NOT NULL" } else { $def += " NULL" }
    $colDefs += $def
  }
  if ($pk.Count -gt 0) {
    $pkCols = ($pk | ForEach-Object { "[$_]" }) -join ', '
    $colDefs += "  CONSTRAINT [PK_$table] PRIMARY KEY ($pkCols)"
  }
  W ("CREATE TABLE [dbo].[$table] (`r`n" + ($colDefs -join ",`r`n") + "`r`n);")
  W "END"
  W "GO"

  # ---- DATA ----
  $colList = ($cols | ForEach-Object { "[$($_.Name)]" }) -join ', '
  $dcmd = $conn.CreateCommand(); $dcmd.CommandText = "SELECT * FROM [dbo].[$table];"
  $r = $dcmd.ExecuteReader()
  $rows = @()
  while ($r.Read()) {
    $vals = @()
    foreach ($c in $cols) {
      $vals += (Format-Value $r[$c.Name] $c.Type)
    }
    $rows += "($($vals -join ', '))"
  }
  $r.Close()

  if ($rows.Count -gt 0) {
    W "IF NOT EXISTS (SELECT 1 FROM [dbo].[$table])"
    W "BEGIN"
    if ($hasIdentity) { W "SET IDENTITY_INSERT [dbo].[$table] ON;" }
    # chia lo 200 dong/INSERT
    for ($i=0; $i -lt $rows.Count; $i += 200) {
      $chunk = $rows[$i..([Math]::Min($i+199,$rows.Count-1))]
      W "INSERT INTO [dbo].[$table] ($colList) VALUES"
      W (($chunk -join ",`r`n") + ";")
    }
    if ($hasIdentity) { W "SET IDENTITY_INSERT [dbo].[$table] OFF;" }
    W "END"
    W "GO"
  }
  W ""
}

$conn.Close()
[System.IO.File]::WriteAllText($outFile, $sb.ToString(), (New-Object System.Text.UTF8Encoding($false)))
Write-Output ""
Write-Output "DA XUAT: $outFile  ($([math]::Round((Get-Item $outFile).Length/1KB,1)) KB)"
