# Privacy

## The short version

My Dev Watch has no account, no server, no analytics and no advertising SDK. The
app talks to your watch over Bluetooth and writes to a database on your phone. That
is the entire data path.

## What is stored

| Data | Where | Why |
| --- | --- | --- |
| Device fingerprints (name, address, services) | Local database | Reconnect to the right watch |
| Activity, heart rate, SpO2, sleep | Local database | Show your history |
| Watch-face projects | App storage | Edit and export them |
| BLE captures | App storage, only when you start one | Protocol research |

Nothing is uploaded. There is no crash reporting service, no remote config, no
feature flag service and no "anonymous usage statistics".

## Notifications

When forwarding phone notifications to a watch, the content passes through memory
and is written to the BLE link. It is not stored, not logged, and not sent anywhere
else. Sensitive applications can be excluded per app, and content can be hidden
while the notification is still announced.

## Permissions

| Permission | Reason | Optional |
| --- | --- | --- |
| Bluetooth scan/connect | Find and talk to the watch | No, for the companion |
| Location, Android 11 and older | The OS requires it for BLE scanning | Only on those versions |
| Notifications | Keep the foreground service visible | Yes |

The app requests nothing else: no contacts, no storage, no camera, no microphone.

## Deletion

Settings → Data → Delete all removes the database, captures, exported projects and
cached device information. Because there is no server, deletion is immediate and
complete. A backup file you exported yourself is the only copy outside the phone.

## Backups

Android cloud backup and device transfer are explicitly disabled for the database,
preferences, captures and faces (see res/xml/data_extraction_rules.xml). Health data
does not silently travel to a new phone.

## Children

The app has no social features and collects no personal data. It is not directed at
children, but there is nothing to collect if it were.
