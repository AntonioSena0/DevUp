@echo off
setlocal
set JPACKAGE="C:\Program Files\Java\jdk-25\bin\jpackage.exe"
where jpackage >nul 2>nul
if %ERRORLEVEL%==0 set JPACKAGE=jpackage
set PATH=%LOCALAPPDATA%\wix311;%PATH%

call mvn -q package "-DskipTests"
if %ERRORLEVEL% neq 0 exit /b %ERRORLEVEL%

del /q installer\DevUp-1.0.0.msi 2>nul
%JPACKAGE% --type msi --name DevUp --app-version 1.0.0 --vendor DevUp ^
  --description "DevUp" --copyright "DevUp" ^
  --input target --main-jar devup-1.0.0-all.jar ^
  --main-class org.devup.Main --icon src\main\resources\devup.ico --dest installer ^
  --win-per-user-install --win-shortcut --win-menu --win-menu-group DevUp ^
  --win-upgrade-uuid B6D61DE8-7D66-4D08-B5EC-225909BD1FB9
if %ERRORLEVEL% neq 0 exit /b %ERRORLEVEL%

echo Instalando...
powershell -NoProfile -Command "$u = Get-ItemProperty 'HKCU:\Software\Microsoft\Windows\CurrentVersion\Uninstall\*','HKLM:\SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\*' -ErrorAction SilentlyContinue | Where-Object { $_.DisplayName -eq 'DevUp' } | Select-Object -First 1; if ($u) { Start-Process msiexec -ArgumentList '/x',$u.PSChildName,'/passive' -Wait }"
msiexec /i installer\DevUp-1.0.0.msi /passive
if %ERRORLEVEL% neq 0 exit /b %ERRORLEVEL%

echo Abrindo DevUp...
start "" "%LOCALAPPDATA%\DevUp\DevUp.exe"
