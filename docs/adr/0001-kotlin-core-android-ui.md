# ADR 0001 — Kotlin core, Android UI, no cross-platform framework

Status: accepted (2026-10-24)

## Context

The product needs BLE, background sync, notification access, Health Connect and a
fast dashboard. It also needs a lab, a CLI and eventually a desktop surface.

## Decision

- Core logic in pure Kotlin/JVM modules with no Android dependency.
- Android UI in Jetpack Compose, native.
- A separate web studio for design work and for iPhone users, sharing the same
  protocol rules but not the same code.

## Consequences

- Protocol work is testable in milliseconds, in CI, without an emulator.
- The Android app cannot leak protocol logic into the UI by accident: it does not
  own any.
- Two UI implementations exist (Compose and web). That is a real cost, accepted
  because the alternative is no iPhone story at all.
- Flutter, React Native and Electron were rejected for the companion: their BLE
  and background-service control is weaker on Android, which is where the product
  must be strongest.
