@echo off
setlocal
pushd "%~dp0"

set "DISTRO=Ubuntu-24.04"
set "LINUX_DIR=/home/smouj/.openclaw/workspace/main/projects/my-dev-watch/web"
set "URL=http://127.0.0.1:5173"

rem Prefer a native Windows Node install; fall back to the WSL toolchain.
where node >nul 2>nul
if %errorlevel%==0 (
  start "My Dev Watch server" /min cmd /c "node server.mjs"
) else (
  start "My Dev Watch server" /min wsl.exe -d %DISTRO% -- bash -lc "cd %LINUX_DIR% && exec node server.mjs"
)

rem Give the server a moment, then open the studio as an app window when possible.
timeout /t 3 /nobreak >nul
set "BROWSER="
if exist "%ProgramFiles(x86)%\Microsoft\Edge\Application\msedge.exe" set "BROWSER=%ProgramFiles(x86)%\Microsoft\Edge\Application\msedge.exe"
if not defined BROWSER if exist "%ProgramFiles%\Google\Chrome\Application\chrome.exe" set "BROWSER=%ProgramFiles%\Google\Chrome\Application\chrome.exe"
if defined BROWSER (
  start "" "%BROWSER%" --app=%URL% --window-size=1320,900
) else (
  start "" "%URL%"
)
endlocal
