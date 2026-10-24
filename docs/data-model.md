# Data model (planned)

Room, with UTC timestamps plus the device timezone, because travel and DST must not
move activity between days.

## Entities

| Entity | Key | Notes |
| --- | --- | --- |
| DeviceEntity | address | Last seen, name, protocol, level |
| DeviceProfileEntity | profileId | Capabilities and quirks, versioned |
| DailyActivityEntity | (deviceId, localDate) | Steps, distance, calories, active minutes |
| HeartRateSampleEntity | (deviceId, timestamp) | bpm, resting flag |
| Spo2SampleEntity | (deviceId, timestamp) | percent |
| SleepSessionEntity | (deviceId, start) | Stages as children |
| SleepStageEntity | (sessionId, start) | Deep, light, REM, awake |
| SyncStateEntity | (deviceId, phase) | Pending, running, completed, partial, failed, unsupported |
| WatchFaceProjectEntity | projectId | .mdf manifest and assets |
| CaptureEntity | captureId | .mdcap reference, only when the user records one |

## Rules

- Natural keys include the device: two watches must not merge their steps.
- Sync writes are idempotent; re-connecting never duplicates a measurement.
- A failed phase does not cancel the others: sleep failure must not hide steps.
- Deletion is a real delete, not a flag, and it cascades to samples.
