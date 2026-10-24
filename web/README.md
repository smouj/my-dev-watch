# Web studio

The studio is the cross-platform surface of My Dev Watch: it runs on Android Chrome,
on iPhone Safari and on a Windows PC, with no build step and no runtime dependency
beyond Node for the tiny local server.

## Start

~~~bash
cd web
npm start          # no npm install needed: zero dependencies
~~~

Open <http://127.0.0.1:5173>. The Install tab shows a QR code for the LAN address so
a phone on the same Wi-Fi can open the same studio and add it to the home screen.

## What it does

- Watch-face editor: layers, drag, colour, size, shape, live simulated values.
- Projects saved as JSON on the PC (web/projects) and autosaved in the browser.
- Export/import .mdwface files, which are the same document.
- Web Bluetooth battery read from a real watch in Chrome or Edge, using the same
  0xA2 command as the Kotlin core.
- Installable PWA with an offline shell on Android and iPhone.

## What it does not do

- It does not flash firmware.
- It does not upload anything; the server writes only inside web/projects.
- It cannot use Web Bluetooth on iPhone, because iOS browsers do not implement it.

## Windows desktop shortcut

start-windows.cmd starts the server (in WSL if Node is not installed on Windows)
and opens the browser. install-desktop-shortcut.ps1 creates the shortcut on the
current user's desktop with the app icon:

~~~powershell
powershell -ExecutionPolicy Bypass -File .\install-desktop-shortcut.ps1
~~~

## Why a second UI

The Android app owns BLE background sync. The web studio owns design, and gives
iPhone users a way in. Both read the same protocol rules; neither invents them.
