$host.ui.RawUI.WindowTitle = "Server 2 (localhost:27019)"

$folder = (Get-Item .).FullName
$replica = $folder + '\r2'

New-Item -Path . -Name "r2" -ItemType "directory" -Force

Set-Location "C:\Program Files\MongoDB\Server\5.0\bin"

.\mongod.exe --replSet lsmdb --dbpath $replica --port 27019 --bind_ip localhost --oplogSize 200

Set-Location $folder
pause
