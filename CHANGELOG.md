# Changelog

All notable changes are documented here. The format follows Keep a Changelog and
the project uses semantic versioning.

## [Unreleased]

## [0.1.0] — 2026-10-24

### Added

- Kotlin core: capability model, support levels, confidence levels, device and
  display profiles.
- Protocol registry with weighted, explainable identification and a 60 % threshold.
- GloryFit GATT map and command catalogue with per-command evidence.
- Battery decoder, verified against the documented A2/0x56 frame.
- Watch-face container codec: header, declared length, CRC32, defensive validation.
- .mdcap capture format, parser, writer and replay transport.
- Read-only firmware analyser: SHA-256, entropy, strings, magic detection.
- CLI: hex, battery, commands, identify, watchface, firmware, capture, levels.
- Android Compose shell: five destinations, scanner, capability matrix, honest
  empty states, light and dark themes.
- Documentation: architecture, roadmap, protocols, compatibility ladder, privacy,
  security, contribution rules and agent rules.
- CI for core tests and the Android build; release workflow with checksums.

### Notes

- No device has been verified on hardware yet. The compatibility matrix is empty on
  purpose.
- The Android app is an alpha scaffold: it compiles, but it has not been run against
  a physical watch.
