# LocalTradeAI

Android-first research app for screen analysis and strategy testing. The first milestone is a user-approved screen-capture preview. No broker credentials, real orders, or automated trading are included.

## Stack
- Kotlin + Jetpack Compose + Material 3
- Android MediaProjection API
- Google ML Kit Text Recognition for on-device OCR
- Foreground service with explicit user consent
- GitHub Actions build workflow

## Build
Requires JDK 17 and Android SDK (API 35).

```bash
gradle assembleDebug
gradle testDebugUnitTest
```

Install `app/build/outputs/apk/debug/app-debug.apk` on an Android device. Tap **Start capture** and approve Android's system consent dialog. Tap **Stop** to end the session. After a frame is captured, tap **Recognize text** to run OCR on that frame.

## OCR and privacy
OCR is performed on the captured bitmap using Google ML Kit Text Recognition. This app does not send the captured bitmap to a custom cloud OCR service. ML Kit's own terms and privacy documentation apply; depending on the SDK/model delivery configuration, Google may process diagnostic or performance metrics and retrieve model updates. Before public distribution, review the current ML Kit terms and Google Play User Data / Data safety requirements, and provide a complete privacy notice appropriate to the shipped configuration. Do not describe the app as fully offline until model delivery and network behavior have been verified on-device.

## Roadmap
1. Screen-capture preview and lifecycle handling.
2. Local OCR and chart-image analysis.
3. Historical market data, backtesting, paper trading and risk limits.
4. Evaluate local inference only if it adds measurable value.

## Reuse policy
Before adapting code from other repositories, verify the exact upstream files, license, notices and dependency terms. Keep upstream notices when required. The current OCR integration uses the documented ML Kit API and does not copy the donor repository's implementation or UI.

## Safety
This project is research-only. Backtests and simulated results do not guarantee future returns. No real trading is implemented.
