$host.ui.RawUI.WindowTitle = "Server 1 (localhost:27018)"

$folder = (Get-Item .).FullName
$replica = $folder + '\r1'

New-Item -Path . -Name "r1" -ItemType "directory" -Force

Set-Location "C:\Program Files\MongoDB\Server\5.0\bin"

.\mongod.exe --replSet lsmdb --dbpath $replica --port 27018 --bind_ip localhost --oplogSize 200

Set-Location $folder
pause
