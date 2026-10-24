# Security

## Reporting a vulnerability

Open a private security advisory:
https://github.com/smouj/my-dev-watch/security/advisories/new

Include the affected version, the phone and watch model, and steps to reproduce.
Do not open a public issue for a vulnerability that can brick a device or expose
health data. You will get an acknowledgement within 72 hours.

## Threat model

| Asset | Threat | Mitigation |
| --- | --- | --- |
| Health data | Local malware reading the database | No cloud sync; app-private storage; deletion path |
| Watch firmware | A bad write bricks the device | No flashing code exists in the app |
| BLE traffic | Passive sniffing of notifications | Android BLE encryption where the watch supports it; no secrets in logs |
| Captures | Published files containing addresses | Tooling warns; docs require stripping identifiers |
| Build supply chain | Malicious dependency | Small dependency set; version catalog; Dependabot |

## What the app will never do

- Write firmware automatically, or at all in the current milestone.
- Send health data to a server.
- Log notification content.
- Require an account to use a watch.
- Obfuscate what it does with your data.

## Firmware safety

The repository contains a read-only firmware analyser. It computes hashes, entropy
and strings. It cannot flash, erase or write. Any future installer must implement
all of these before it is merged:

1. model and hardware identification,
2. backup that has been read back and verified,
3. image size and hash validation,
4. battery threshold check,
5. a recovery method proven on the same hardware.

A missing recovery method is a hard stop, not a warning dialog.

## Supported versions

Only the latest released version receives fixes. This is a young project; please
report issues on the current main branch as well.
