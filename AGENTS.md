# AGENTS.md

Rules for humans and automated agents working on this repository. They exist
because a watch is somebody's property and a wrong byte can destroy it.

## Non-negotiable

1. Never invent a protocol command, an opcode or a payload layout.
2. Never claim compatibility that has not been tested on real hardware.
3. Never copy code from an incompatibly licensed project (see PROTOCOLS.md).
4. Every new command needs a test and a documented evidence level.
5. Every device needs a profile with evidence, not a name.
6. No firmware operation is marked stable without a physical device and a recovery
   path that was executed at least once.
7. No mandatory cloud, account or subscription.
8. No telemetry, analytics or advertising SDK.
9. Never log notification content, health values or device addresses at info level.
10. Keep UI and protocol strictly separated.
11. Preserve backwards compatibility of stored profiles and capture files.
12. Document quirks as data (DeviceQuirk), never as model-name branches.
13. Keep builds reproducible: pinned versions, wrapper committed, no floating tags.
14. Destructive commands are never used for exploration.
15. Nothing is flashed without a verified backup and a known recovery method.

## When adding a feature

- Capability first: add it to the model with a label and category.
- Protocol second: implement it behind WatchProtocol with a typed result.
- Tests third: fixtures for the bytes, a fake transport for the flow.
- UI last: render only what the profile declares.
- Docs: update the catalogue, the compatibility matrix and the changelog.

## When you are unsure

Write UNKNOWN. The project is built to make uncertainty explicit; an honest gap is
always cheaper than a confident lie.
