# Contributing

## Before you write code

Read [AGENTS.md](AGENTS.md) — it is the rule set, not a suggestion. The short
version: no invented protocols, no invented compatibility, no copying
incompatibly-licensed code, and no firmware writes without a recovery path.

## Development environment

| Tool | Version |
| --- | --- |
| JDK | 17 |
| Gradle | wrapper (8.10.2) |
| Android SDK | API 35, only for the app module |
| Node | 20+, only for the web studio |

~~~bash
git clone https://github.com/smouj/my-dev-watch.git
cd my-dev-watch
./gradlew test          # core modules, no Android SDK needed
./gradlew build         # same, plus CLI distribution
ANDROID_HOME=... ./gradlew :android:app:assembleDebug
~~~

The Android module is only configured when an Android SDK is present. CI always
provides one, so the app cannot rot silently.

## Pull requests

1. One concern per pull request. A protocol change and a UI restyle do not belong
   together.
2. New bytes need a fixture. Put it in the module's test resources and reference it
   from a test.
3. New capabilities need a status per device profile. Adding a capability without a
   status means the UI will claim something unproven.
4. Update the catalogue in [PROTOCOLS.md](PROTOCOLS.md) and the changelog.
5. If you touched safety logic, say which hardware you tested on.

## Code style

- Kotlin official style, 4-space indent, no wildcard imports.
- Public API gets KDoc explaining *why*, not *what*.
- No new dependency without a note in the pull request explaining what it replaces.
- Prefer a typed failure over an exception at a module boundary.

## Commit messages

Imperative subject under 72 characters, body explains the evidence:

~~~
protocol: decode battery answers from a recorded capture

Fixture from a real S10 session (RX A2 56). The decoder rejects values above
100 instead of clamping, because a wrong percentage hides a broken frame.
~~~

## Device reports

Use the device report issue template. A report without a model, a firmware version
and a capture is a rumour, and will be labelled as such.
