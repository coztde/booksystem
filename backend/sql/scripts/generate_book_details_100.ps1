param(
  [Parameter(Mandatory = $false)]
  [string]$CsvPath = "f:/Project/JavaProject/book/Book_Details.csv",

  [Parameter(Mandatory = $false)]
  [string]$OutPath = "f:/Project/JavaProject/book/backend/sql/book_details_100.sql",

  [Parameter(Mandatory = $false)]
  [int]$Count = 100,

  [Parameter(Mandatory = $false)]
  [int]$TotalQty = 5,

  [Parameter(Mandatory = $false)]
  [int]$Status = 1
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Cn([string[]]$hex) {
  return -join ($hex | ForEach-Object { [char]([Convert]::ToInt32($_, 16)) })
}

function CnConst() {
  return @{
    CatComputer = (Cn @('8BA1','7B97','673A'))
    CatEconomy  = (Cn @('7ECF','6D4E'))
    CatHistory  = (Cn @('5386','53F2'))
    CatPhilo    = (Cn @('54F2','5B66'))
    CatPsy      = (Cn @('5FC3','7406'))
    CatScience  = (Cn @('79D1','5B66'))
    CatLit      = (Cn @('6587','5B66'))

    LocTP3   = (Cn @('4E3B','9986')) + 'A' + (Cn @('533A')) + '-TP3'
    LocI2    = (Cn @('4E3B','9986')) + 'B' + (Cn @('533A')) + '-I2'
    LocK2    = (Cn @('4E3B','9986')) + 'C' + (Cn @('533A')) + '-K2'
    LocF0    = (Cn @('4E3B','9986')) + 'D' + (Cn @('533A')) + '-F0'
    LocB5    = (Cn @('4E3B','9986')) + 'E' + (Cn @('533A')) + '-B5'
    LocB8    = (Cn @('4E3B','9986')) + 'E' + (Cn @('533A')) + '-B8'
    LocQ1    = (Cn @('4E3B','9986')) + 'A' + (Cn @('533A')) + '-Q1'
    LocOther = (Cn @('4E3B','9986','7EFC','5408','533A'))
  }
}

function PublisherList() {
  return @(
    (Cn @('4EBA','6C11','90AE','7535','51FA','7248','793E')),
    (Cn @('673A','68B0','5DE5','4E1A','51FA','7248','793E')),
    (Cn @('7535','5B50','5DE5','4E1A','51FA','7248','793E')),
    (Cn @('4E2D','534E','4E66','5C40')),
    (Cn @('5546','52A1','5370','4E66','9986')),
    (Cn @('4EBA','6C11','6587','5B66','51FA','7248','793E'))
  )
}

function SqlLit([object]$v, [int]$maxLen = 0) {
  if ($null -eq $v) { return 'NULL' }
  $s = [string]$v
  $s = $s.Trim()
  if ($s.Length -eq 0) { return 'NULL' }
  if ($maxLen -gt 0 -and $s.Length -gt $maxLen) { $s = $s.Substring(0, $maxLen) }
  $s = $s -replace "`r`n|`n|`r", "\n"
  $s = $s -replace "'", "''"
  return "'$s'"
}

function FirstGenre([string]$genres) {
  if (-not $genres) { return '' }
  # genres looks like: ['Fantasy', 'Young Adult', ...]
  $m = [regex]::Match($genres, "'([^']+)'")
  if ($m.Success) { return $m.Groups[1].Value }
  return ''
}

function MapCategory([string]$genres) {
  $c = CnConst
  $g = (FirstGenre $genres).ToLowerInvariant()
  if ($g -match 'computer|programming|technology|software|coding') { return $c.CatComputer }
  if ($g -match 'econom|business|finance') { return $c.CatEconomy }
  if ($g -match 'history|biograph|memoir') { return $c.CatHistory }
  if ($g -match 'philosophy') { return $c.CatPhilo }
  if ($g -match 'psychology') { return $c.CatPsy }
  if ($g -match 'science|math') { return $c.CatScience }
  return $c.CatLit
}

function MapLocation([string]$cat) {
  $c = CnConst
  switch ($cat) {
    $c.CatComputer { return $c.LocTP3 }
    $c.CatLit { return $c.LocI2 }
    $c.CatHistory { return $c.LocK2 }
    $c.CatEconomy { return $c.LocF0 }
    $c.CatPhilo { return $c.LocB5 }
    $c.CatPsy { return $c.LocB8 }
    $c.CatScience { return $c.LocQ1 }
    Default { return $c.LocOther }
  }
}

Write-Host ("Reading CSV: {0}" -f $CsvPath)
$rows = Import-Csv -Encoding UTF8 $CsvPath |
  Where-Object { $_.book_title -and $_.cover_image_uri } |
  Select-Object -First $Count

if (-not $rows -or $rows.Count -eq 0) {
  throw "CSV has no usable rows"
}

$values = New-Object System.Collections.Generic.List[string]

foreach ($r in $rows) {
  $cat = MapCategory $r.genres
  $loc = MapLocation $cat
  $publishers = PublisherList

  $bookId = [string]$r.book_id
  if (-not $bookId) { $bookId = [string]$r.H1 }
  if (-not $bookId) { $bookId = [Guid]::NewGuid().ToString("N").Substring(0, 12) }

  # Dataset has no ISBN: use Goodreads book_id as a stable unique placeholder (fits varchar(20))
  $isbn = "GR$bookId"
  $publisher = $publishers[[int]([long]$bookId % $publishers.Count)]

  $desc = [string]$r.book_details
  if ($r.publication_info) { $desc = ($desc + "\n\n" + [string]$r.publication_info) }

  $cover = [string]$r.cover_image_uri
  if (-not $cover) { $cover = $null }

  $values.Add(
    "  (" +
      (SqlLit $cover 255) + ", " +
      (SqlLit $r.book_title 200) + ", " +
      (SqlLit $r.author 100) + ", " +
      (SqlLit $publisher 100) + ", " +
      (SqlLit $isbn 20) + ", " +
      (SqlLit $cat 50) + ", " +
      (SqlLit $loc 100) + ", " +
      (SqlLit $desc 1000) + ", " +
      "$TotalQty, $TotalQty, $Status" +
    ")"
  )
}

$script = New-Object System.Collections.Generic.List[string]
$script.Add("USE book;")
$script.Add("SET NAMES utf8mb4;")
$script.Add("")
$script.Add("INSERT INTO book (cover_url, title, author, publisher, isbn, category, location, description, total_qty, available_qty, status)")
$script.Add("VALUES")
$script.Add(($values -join ",`n"))
$script.Add("ON DUPLICATE KEY UPDATE")
$script.Add("  cover_url = VALUES(cover_url),")
$script.Add("  title = VALUES(title),")
$script.Add("  author = VALUES(author),")
$script.Add("  publisher = VALUES(publisher),")
$script.Add("  category = VALUES(category),")
$script.Add("  location = VALUES(location),")
$script.Add("  description = VALUES(description),")
$script.Add("  total_qty = VALUES(total_qty),")
$script.Add("  available_qty = VALUES(available_qty),")
$script.Add("  status = VALUES(status);")
$script.Add("")

$outDir = Split-Path -Parent $OutPath
if (-not (Test-Path $outDir)) {
  New-Item -ItemType Directory -Force -Path $outDir | Out-Null
}

$scriptText = $script -join "`n"
Set-Content -Encoding UTF8 -Path $OutPath -Value $scriptText
Write-Host ("Wrote SQL: {0} (rows: {1})" -f $OutPath, $rows.Count)
