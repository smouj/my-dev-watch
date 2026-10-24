# Verification

What has actually been executed, and what has not.

## Executed

| Check | Where | Result |
| --- | --- | --- |
| Core unit tests | local, JDK 17 | see CI badge |
| CLI smoke test | local + CI | commands, battery, identify |
| Watch-face codec round-trip | local | CRC32 and structural validation |
| Capture parse/replay | local | battery decoded through the full stack |
| Firmware analyser | local | entropy, strings, signatures |

## Not executed

| Claim | Why not |
| --- | --- |
| Android app on a phone | No device attached to the build environment |
| Android app against a watch | No verified GloryFit watch available |
| Watch-face install | Needs verified transfer protocol and a recovery path |
| Firmware read/write | Deliberately absent |

The Android APK is built by CI on every push, so "it compiles" is checked. That is
not the same as "it runs", and the README says so.
