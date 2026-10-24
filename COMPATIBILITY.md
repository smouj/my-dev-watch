# Compatibility model

## Levels

Compatibility is a ladder, not a badge. Each level has an exit criterion that can
be checked by someone who does not trust us.

### Level 0 — Detected
- Advertisement observed, name and services recorded.
- Exit: a fingerprint is stored and the protocol hypothesis is explained.

### Level 1 — Communication
- GATT services discovered, writes and notifications succeed.
- Exit: battery and device information read without the vendor app.

### Level 2 — Companion
- Clock, activity, heart rate, SpO2, sleep and settings synchronise.
- Exit: a full day of normal use with the vendor app uninstalled.

### Level 3 — Personalisation
- Watch faces replace the stock face and survive a reboot.
- Exit: original face restored from a backup, byte-identical.

### Level 4 — Interface
- Icons, menus and themes modified where the firmware exposes them.
- Exit: rollback proven for every modified resource.

### Level 5 — Firmware
- Firmware read, verified, written and recovered.
- Exit: a recovery path executed on the same hardware, not in theory.

### Level 6 — Open system
- Documented SoC, bootloader, display, input, sensors, storage and recovery.
- Exit: an open firmware boots, shows the time and charges.

## Rules

1. A device is never listed above the level that has been proved on hardware.
2. A level applies to a model **and** a firmware build, not to a brand.
3. UNAVAILABLE means the hardware cannot do it. UNKNOWN means we have not checked.
4. Going down a level is allowed and is not a failure: new firmware can break things.

## Current status

No device has been verified yet. That sentence stays in this file until a capture,
a model and a firmware version are recorded together. An empty matrix is more
useful than an optimistic one.
