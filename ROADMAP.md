# Roadmap

The order is not negotiable. Personalisation before reliability is how projects
brick watches and lose users.

| Phase | Milestone | Exit criterion | Status |
| --- | --- | --- | --- |
| M0 | Research foundation | Protocol catalogue, capture format, codecs, CLI, tests green | **Done** |
| M1 | Connection | Scan, identify, connect, battery, reconnect on a real watch | In progress |
| M2 | Activity | Steps, distance, calories, heart rate history | Not started |
| M3 | Daily companion | Sleep, alarms, goals, notifications, music, find device | Not started |
| M4 | Data | Room persistence, export, deletion, Health Connect | Not started |
| M5 | Faces | Read a vendor face, recompile it byte-identically | Not started |
| M6 | Studio | Layers, assets, preview simulation, .mdf project format | Preview only |
| M7 | Firmware lab | Import, fingerprint, strings, entropy, backup manifests | Analyser done |
| M8 | Resources | Icons and menus on one verified model | Not started |
| M9 | Firmware | Validated install with recovery on one model | Not started |
| M10 | Open OS bring-up | Boot, display, input, battery, BLE | Not started |
| M11 | Open OS daily | Notifications, settings, faces, alarms | Not started |

## M1 — the only milestone that matters right now

1. A real GloryFit watch, a capture file, and the phone in the same room.
2. Prove clock sync and battery without the vendor app open.
3. Record the session as .mdcap and commit it as a fixture.
4. Only then claim Level 2.

## Definition of done

A feature is done when all four are true:

- the protocol path has unit tests and, where bytes are involved, a fixture;
- the UI shows it only on profiles that declare the capability;
- the docs state the evidence level and the tested model;
- a field test is recorded in SUPPORTED_DEVICES.md.

Anything less is RESEARCH, and the interface says so.
