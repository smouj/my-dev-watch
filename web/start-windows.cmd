@echo off
setlocal
cd /d "%~dp0"

set "DISTRO=Ubuntu-24.04"
set "LINUX_DIR=/home/smouj/.openclaw/workspace/main/projects/my-dev-watch/web"

rem Prefer a native Windows Node install; fall back to the WSL toolchain.
where node >nul 2>nul
if %errorlevel%==0 (
  start "My Dev Watch server" /min cmd /c "node server.mjs"
) else (
  start "My Dev Watch server" /min wsl.exe -d %DISTRO% -- bash -lc "cd %LINUX_DIR% && exec node server.mjs"
)

rem Give the server a moment, then open the studio.
timeout /t 3 /nobreak >nul
start "" "http://127.0.0.1:5173"
endlocal
