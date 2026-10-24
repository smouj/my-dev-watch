# Architecture

## What this repository is

My Dev Watch is three products sharing one core:

- **Companion** — connect to a watch, sync activity and health, show the truth.
- **Studio** — design watch faces and, where the firmware allows it, themes.
- **Laboratory** — inspect BLE traffic, profiles, captures and firmware images.

The core is Kotlin/JVM and has no Android dependency. That is deliberate: every
protocol decision must be testable in a millisecond, on a laptop, without a watch,
without an emulator and without a phone. Android is one front end, not the product.

~~~
                    +----------------------+
                    |   Android app        |  Compose UI, BLE transport
                    |   (android/app)      |  permissions, lifecycle
                    +----------+-----------+
                               |
          +--------------------+---------------------+
          |                                          |
+---------v----------+                    +----------v-----------+
|  protocol-api      |                    |  watchface           |
|  registry          |                    |  container codec     |
|  transport contract|                    |  CRC32, validation   |
+---------+----------+                    +----------+-----------+
          |                                          |
+---------v----------+   +------------------+  +------v-----------+
| protocol-gloryfit  |   |  capture (.mdcap)|  |  firmware        |
| GATT map, commands |   |  parse/replay    |  |  read-only scan  |
| battery decoder    |   +------------------+  +------------------+
+---------+----------+
          |
+---------v----------+
|  core:model        |  capabilities, support levels, confidence, profiles
+--------------------+
          |
+---------v----------+
|  tools:cli         |  hex, battery, identify, watchface, firmware, capture
+--------------------+
~~~

## Rules the code follows

1. **The UI never builds packets.** Screens render state; protocols own bytes.
2. **No capability without a level.** Every feature is STABLE, EXPERIMENTAL,
   RESEARCH, UNAVAILABLE or UNKNOWN. There is no boolean "supported".
3. **No protocol without evidence.** Commands live in a catalogue with an
   evidence string and a confidence level, whether or not they are implemented.
4. **No destructive exploration.** The app cannot write firmware. A firmware
   inspector that only reads cannot brick a watch.
5. **No cloud.** No account, no backend, no telemetry. The radio and the local
   database are the whole system.
6. **Parsing is defensive.** Short, corrupt, foreign and future packets return a
   typed failure. They never throw at the edge of the UI.

## Modules

| Module | Purpose | Depends on |
| --- | --- | --- |
| core:model | Capabilities, support levels, confidence, device and display profiles | — |
| core:protocol-api | Transport contract, registry, fingerprint scoring, results | model |
| core:protocol-gloryfit | GATT map, command catalogue, battery codec | protocol-api |
| core:watchface | .bin container read/write, CRC32, structural validation | model |
| core:capture | .mdcap parse/serialise, replay transport | protocol-api |
| core:firmware | SHA-256, entropy, strings, magic detection (read-only) | — |
| tools:cli | Laboratory commands over the core | all core modules |
| android/app | Compose shell, scanner, capability matrix | model, protocol-api, protocol-gloryfit |

## Transport contract

~~~kotlin
interface WatchTransport {
    val mtu: Int
    val notifications: Flow<WatchNotification>
    suspend fun write(channel: GattChannel, payload: ByteArray)
    suspend fun subscribe(channel: GattChannel)
    suspend fun requestMtu(desired: Int): Int
    suspend fun close()
}
~~~

Three implementations exist or are planned:

- **Android GATT** — real radio, serialised operation queue (next milestone).
- **ReplayTransport** — a recorded .mdcap file pretending to be a watch. Used by
  the test that decodes a real battery frame through the full protocol stack.
- **FakeTransport** — scripted responses for protocol unit tests.

If a protocol cannot run against all three, it is not finished.

## Identification, not guessing

A retail name is not a model. The same "S10" shell has shipped with different
firmwares, so identification is a weighted score over observed facts:

| Signal | Weight |
| --- | --- |
| Exposes service 55FF | 3.0 |
| Exposes service 56FF | 2.0 |
| Advertises 55FF | 1.0 |
| Name mentions GloryFit / UTE | 1.0 |

Below 60 % the result is UNKNOWN and the UI says so. The score and the reasons are
both stored, because "why did the app decide this?" is a support question.

## Watch-face container

The documented layout is implemented as a strict codec:

~~~
0x00  4  identifier (opaque)
0x04  4  payload length
0x08  4  CRC32 of payload
0x0C  2  width
0x0E 10  metadata (opaque)
0x18  ..  payload (usually compressed)
~~~

The height is not in the header, so it is not invented. The codec validates the
declared length against the file size and reports a checksum mismatch instead of
"fixing" the file.

## Persistence (planned)

Room, with the data model documented in [docs/data-model.md](docs/data-model.md).
Until it exists the app holds no history, and the dashboard shows an empty state
rather than demo numbers.

## Companion surfaces

| Surface | Status | Notes |
| --- | --- | --- |
| Android app | Alpha scaffold | Compiles in CI; not yet run against a watch |
| CLI | Working | Verified by tests |
| Web studio (PWA) | Working | Installable on Android and iPhone; edits projects locally |
| Windows desktop | Planned | Wraps the web studio and the CLI |

iOS cannot install a native app built here: that requires Apple tooling and
signing. The installable path for iPhone is the PWA, which is why the studio is a
web application with no build step and no runtime dependencies.
