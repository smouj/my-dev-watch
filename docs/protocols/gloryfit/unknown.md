# GloryFit — unresolved questions

A list of things we do *not* know, kept in the repository so nobody re-derives them
from a forum post.

1. **0x34 FA** — is FA a sub-command byte, a payload prefix, or a second opcode?
   Evidence is a single public note. Confidence: OBSERVED.
2. **Heart-rate history framing (0xF7)** — record size, timestamp encoding and
   whether the watch paginates by day or by count are unknown.
3. **Sleep variants 0x32 / 0xCB** — do they depend on firmware generation or on the
   sensor package? Unknown; treated as separate commands.
4. **Watch-face transfer (0x6001)** — chunk size at MTU 23 and 247, whether the
   watch ACKs every chunk, and whether a partial upload leaves a broken face.
5. **Visible pages (0xF9)** — payload bit order is not confirmed. Hiding pages is
   reversible; treat as PROBABLE, not VERIFIED.
6. **Battery charging flag** — every observed response carries only a percentage.
   There may be a flag in a second byte on some firmwares. Do not invent one.
7. **Maximum notification length** — unknown, and likely model-specific.

## How to close one of these

1. Capture the vendor app doing the thing, twice, on the same firmware.
2. Reproduce it with the CLI or a scripted test.
3. Record the model and firmware in the issue.
4. Add a fixture, a decoder and a test.
5. Raise the confidence only after the second reproduction.
