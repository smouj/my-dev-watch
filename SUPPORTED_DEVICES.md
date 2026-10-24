# Supported devices

## Current matrix

No device is verified yet. This table is intentionally empty: a row appears only
after a capture, a model and a firmware version have been recorded together.

| Device | Protocol | Control | Health | Faces | Firmware | Level | Tested firmware |
| --- | --- | --- | --- | --- | --- | --- | --- |
| — | — | — | — | — | — | — | — |

Legend: ✅ stable · 🧪 experimental · 🔬 research · ❌ unavailable · ❓ unverified

## How to add a device

1. Scan it with the app and export the fingerprint (name, advertised and exposed
   services).
2. Record what works: battery, clock, steps, heart rate, sleep, notifications.
3. Export a .mdcap session for each working command.
4. Open the device report issue with the model, firmware string, phone model and
   Android version, plus the capture with addresses removed.
5. A maintainer reproduces it, then the row is added with a level.

## Why an empty table is better

Listing a device as "supported" because a similar model works is how users lose
data and trust. The compatibility ladder in [COMPATIBILITY.md](COMPATIBILITY.md)
exists so that "it should work" never becomes a feature claim.
