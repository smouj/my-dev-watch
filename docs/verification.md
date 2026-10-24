# Verification

What has actually been executed, and what has not.

## Executed

| Check | Where | Result |
| --- | --- | --- |
| Core unit tests (40) | GitHub Actions, JDK 17 | passing |
| CLI smoke test | CI: commands, battery A2 56, installDist | passing |
| Watch-face codec round-trip | unit tests | CRC32 and structural validation |
| Capture parse and replay | unit tests | battery decoded through the full stack |
| Firmware analyser | unit tests | entropy, strings, signatures |
| Android debug APK | CI, API 35 | assembled successfully |

The workflow is in [ci.yml](../.github/workflows/ci.yml). The Android job always
provides an Android SDK, so the app cannot rot silently; the core job runs with
-PwithAndroid=false so a plain JDK is enough for protocol work.

## Not executed

| Claim | Why not |
| --- | --- |
| Android app on a phone | No device attached to the build environment |
| Android app against a watch | No verified GloryFit watch available |
| Watch-face install | Needs the transfer protocol proved, with rollback |
| Firmware read/write | Deliberately absent |

"Compiles" is not "runs", and this file will not pretend otherwise.
