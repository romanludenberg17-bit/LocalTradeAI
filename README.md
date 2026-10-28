# LocalTradeAI

Android-first research app for screen analysis and strategy testing. The first milestone is a user-approved screen-capture preview. No broker credentials, real orders, or automated trading are included.

## Stack
- Kotlin + Jetpack Compose + Material 3
- Android MediaProjection API
- Foreground service with explicit user consent
- GitHub Actions build workflow

## Build
Requires JDK 17 and Android SDK (API 35).

```bash
gradle assembleDebug
gradle testDebugUnitTest
```

Install `app/build/outputs/apk/debug/app-debug.apk` on an Android device. Tap **Start capture** and approve Android's system consent dialog. Tap **Stop** to end the session.

## Roadmap
1. Screen-capture preview and lifecycle handling.
2. Local OCR and chart-image analysis.
3. Historical market data, backtesting, paper trading and risk limits.
4. Evaluate local inference only if it adds measurable value.

## Reuse policy
Before adapting code from other repositories, verify the exact upstream files, license, notices and dependency terms. Keep upstream notices when required. No third-party implementation is claimed to have been copied in this initial scaffold.

## Safety
This project is research-only. Backtests and simulated results do not guarantee future returns. No real trading is implemented.
