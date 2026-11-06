# Loud Speaking Alarm Android App

An Android application in Kotlin featuring:
- **Pre-Set Alarms**: One-tap preset alarms (6:00 AM, 7:00 AM, 8:00 AM, 9:00 PM) plus a custom time picker.
- **10-Minute Snooze (Max 3 Times)**: Tracks snooze count across alarm triggers, rescheduling 10 minutes ahead up to 3 times before disabling the snooze button.
- **Loud Time Announcement**: Forces `AudioManager.STREAM_ALARM` to 100% device maximum volume and uses Android `TextToSpeech` over `USAGE_ALARM` to announce the current time every 6 seconds until snoozed or dismissed.
- **GitHub Actions APK Builder**: Automatically builds installable Debug and Release APKs (`.github/workflows/build-apk.yml`) on every push, pull request, or manual run.

## Building APK via GitHub Actions
1. Push the contents of `LoudSpeakingAlarmProject` to the root of a GitHub repository.
2. Open the **Actions** tab in your GitHub repository and select **Build Android APK Installer**.
3. Once the workflow completes, download `LoudSpeakingAlarm-Debug-APK` or `LoudSpeakingAlarm-Release-APK` from the workflow run's **Artifacts** section. Both APKs are signed and ready to install on any Android device (API 24+).
