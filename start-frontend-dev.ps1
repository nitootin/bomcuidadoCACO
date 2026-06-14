Set-Location -LiteralPath (Join-Path $PSScriptRoot "FrontendCuidar-")
$env:BROWSER = "none"
$env:CI = "true"
& npm.cmd start > (Join-Path $PSScriptRoot "frontend-dev.log") 2>&1
