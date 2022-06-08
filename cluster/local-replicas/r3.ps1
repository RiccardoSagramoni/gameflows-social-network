$host.ui.RawUI.WindowTitle = "Server 3 (localhost:27020)"

$folder = (Get-Item .).FullName
$replica = $folder + '\r3'

New-Item -Path . -Name "r3" -ItemType "directory" -Force

Set-Location "C:\Program Files\MongoDB\Server\5.0\bin"

.\mongod.exe --replSet lsmdb --dbpath $replica --port 27020 --bind_ip localhost --oplogSize 200

Set-Location $folder
pause
