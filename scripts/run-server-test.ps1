$ErrorActionPreference = "Continue"
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path

if (-not (Test-Path "run\eula.txt")) {
	New-Item -ItemType Directory -Force -Path run | Out-Null
	Set-Content -Path "run\eula.txt" -Value "eula=true"
}

$log = Join-Path (Get-Location) "server-test.log"
if (Test-Path $log) { Remove-Item $log }
if (Test-Path "server-test.err.log") { Remove-Item "server-test.err.log" }

$p = Start-Process -FilePath "cmd.exe" -ArgumentList "/c","gradlew.bat runServer --no-daemon" `
	-WorkingDirectory (Get-Location) -RedirectStandardOutput $log `
	-RedirectStandardError "server-test.err.log" -PassThru -NoNewWindow

$booted = $false
for ($i = 0; $i -lt 16; $i++) {
	Start-Sleep -Seconds 15
	if ($p.HasExited) { break }
	if (Test-Path $log) {
		$content = Get-Content $log -Raw
		if ($content -match 'Done \(') { $booted = $true; break }
	}
}
Write-Output ("BOOTED=" + $booted + " EXITED=" + $p.HasExited)
if (-not $p.HasExited) {
	& taskkill /PID $p.Id /T /F | Out-Null
}