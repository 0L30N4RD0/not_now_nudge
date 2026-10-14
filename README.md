# Context Nudge 0.2

Independent Android Kotlin prototype. Local, opt-in notification processing via `NotificationListenerService`.

## Installation
Upload the *contents* of this directory to the root of your GitHub repository. Preserve `.github/workflows/build.yml`; remove old duplicate workflows. The GitHub Action compiles `app-debug.apk`.

If the new APK does not install over the old one because the debug signing key differs, uninstall the old version first (it does not retain user data). Back up anything you need beforehand.

## Test
1. Launch app. Notification analysis defaults OFF.
2. Grant notification posting permission and enable Context Nudge under Android Notification access.
3. Toggle analysis ON in the app. Optional: exclude source apps by package name, one per line.
4. Have another device send a test message: `Amanhã às 15h vamos ao shopping` to an app that displays its text in a notification.
5. Tap the Context Nudge suggestion notification; review the calendar event and save manually if desired.
6. Turn analysis OFF to stop processing. Revoke notification access in Android Settings to remove access completely.

Privacy: notification text is processed in memory, no text history or network permission. App notification only contains generic text. Avoid testing with sensitive real messages. Android notification access is broad even though the app filters content; exclusions are applied after Android grants access. The parser is deliberately conservative: relative dates (hoje/amanhã/depois de amanhã) plus explicit times. Not all notifications expose full text; some apps hide it.

No overlay, Accessibility Service, keyboard interception, automatic calendar write, or server upload.
