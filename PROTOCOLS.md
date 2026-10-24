# Protocols

## Evidence policy

Every command and every device attribute carries one of four levels:

| Level | Meaning |
| --- | --- |
| UNKNOWN | Nobody has looked. Not presented to users. |
| OBSERVED | Seen in a capture or vendor document, not reproduced here. |
| PROBABLE | Reproduced, but the sample is too small to rule out firmware variants. |
| VERIFIED | Reproduced on real hardware, with model and firmware recorded. |

A command may be catalogued for a long time without being implemented. The
catalogue is a research ledger, not a feature list.

## GloryFit / UTE family

GATT layout, corroborated by three independent public implementations:

| Service | Write | Notify | Used for |
| --- | --- | --- | --- |
| 55FF | 33F1 | 33F2 | Core commands and health |
| 56FF | 34F1 | 34F2 | Configuration, secondary commands |
| 60FF | 6001 | 6002 | Watch-face transfer and acknowledgement |

### Catalogue

| Opcode | Command | Confidence | Implemented |
| --- | --- | --- | --- |
| 0xA2 | Battery | PROBABLE | **Yes** |
| 0xE5 | Live heart rate | OBSERVED | No |
| 0xF7 | Heart-rate history | OBSERVED | No |
| 0x34 | Blood oxygen (34 FA) | OBSERVED | No |
| 0x32 | Sleep, variant A | OBSERVED | No |
| 0xCB | Sleep, variant B | OBSERVED | No |
| 0x12 | Notification | OBSERVED | No |
| 0xF9 | Visible pages | PROBABLE | No |
| 0x6001 | Watch-face transfer | OBSERVED | No |
| 0x6002 | Watch-face ACK | OBSERVED | No |

Battery is the only end-to-end path because it is the only one with a complete,
corroborated payload: request 0xA2, response 0xA2 followed by a percentage byte
(0x56 = 86 %). Everything else is documented honestly as research.

### Unresolved questions

- Is 0x34 FA a two-byte opcode or an opcode with a sub-command payload?
- Where does the heart-rate history record boundary sit?
- Does 0x32 and 0xCB differ by firmware generation or by sensor?
- Which 60FF chunk size do current firmwares accept at MTU 23 and 247?

These are recorded in [docs/protocols/gloryfit/unknown.md](docs/protocols/gloryfit/unknown.md)
so the next person does not start from zero.

## Capturing a session

1. Enable Bluetooth HCI snoop logging on Android.
2. Use the vendor app once, exactly as a normal user would.
3. Export the btsnoop log and convert the relevant writes and notifications.
4. Write them as a .mdcap file: one packet per line, hex payload.
5. Run ~~~my-dev-watch capture inspect session.mdcap~~~ and commit the fixture.

Captures contain device names and addresses: strip or hash them before publishing.

## Licensing of prior art

Studying public research is allowed and encouraged. Copying code is not automatic:

| Project | Licence | What we take |
| --- | --- | --- |
| Gadgetbridge | AGPL-3.0 | Behavioural knowledge only, no code |
| RyzeWaveWatch | PolyForm Noncommercial | Research reference only |
| air5 reverse engineering | MIT | Ideas; attribution if code is reused |
| GloryDial Studio | Check before use | Container layout documentation |

When in doubt, reimplement from the observed bytes and cite the source.
