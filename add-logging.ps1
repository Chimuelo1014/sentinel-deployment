# Script para agregar logging: driver: "none" a TODOS los servicios
# Ejecuta esto en PowerShell en la carpeta del proyecto

$file = "docker-compose.yml"
$content = Get-Content $file -Raw

# Patrón: buscar "deploy:" y agregar logging antes
$pattern = '(\s+)(deploy:)'
$replacement = '$1logging:' + "`n" + '$1  driver: "none"' + "`n" + '$1$2'

$newContent = $content -replace $pattern, $replacement

# Guardar
$newContent | Set-Content $file -NoNewline

Write-Host "✅ Agregado logging: driver: none a todos los servicios"
Write-Host "🔍 Verifica el archivo docker-compose.yml"
