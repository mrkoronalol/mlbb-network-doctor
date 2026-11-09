# MLBB Network Doctor

<p align="center">
  <img src="docs/app-icon.png" width="104" alt="MLBB Network Doctor">
</p>

<p align="center">
  <a href="README.md">English</a> ·
  <a href="README.ru.md">Русский</a> ·
  <a href="README.id.md">Bahasa Indonesia</a> ·
  <a href="README.fil.md">Filipino</a>
</p>

<p align="center">
  <b>Ping diagnostics, match-phase filtering and a local server filter for Mobile Legends: Bang Bang on Android.</b>
</p>

<p align="center">
  <a href="https://github.com/mrkoronalol/mlbb-network-doctor/releases/latest"><img src="https://img.shields.io/github/v/release/mrkoronalol/mlbb-network-doctor?label=download&color=2ea44f" alt="Latest release"></a>
  <a href="https://github.com/mrkoronalol/mlbb-network-doctor/releases/latest"><img src="https://img.shields.io/github/downloads/mrkoronalol/mlbb-network-doctor/total?label=downloads&color=blue" alt="Downloads"></a>
  <img src="https://img.shields.io/badge/Android-8.0%2B-3ddc84?logo=android&logoColor=white" alt="Android 8.0+">
  <img src="https://img.shields.io/badge/Kotlin-Jetpack%20Compose-7f52ff?logo=kotlin&logoColor=white" alt="Kotlin">
  <a href="LICENSE"><img src="https://img.shields.io/github/license/mrkoronalol/mlbb-network-doctor?color=orange" alt="GPL-3.0"></a>
</p>

**Contents:** [Why](#why-this-exists) · [Ping by region](#ping-by-region) · [Features](#features) · [Install](#install-4-steps) · [How it works](#how-it-works) · [Guides](#guides) · [FAQ](#faq) · [Build](#build)

---

## Why this exists

Mobile Legends picks a match server by ping and account region. Players in the Far
East (Khabarovsk, Vladivostok) get **32–60 ms to local servers** and **145–260 ms to
Moscow**, yet the game often assigns a distant server. Blocking the wrong server
after “Match found” causes a dodge penalty — so this app measures first, then filters.

## Ping by region

Measured on a Nokia A063 (Android 13) over 2.4 GHz Wi‑Fi; the same addresses return
39–42 ms from a PC on the same network, so the tunnel adds almost nothing.

| Region | Server pool | Ping |
| --- | --- | --- |
| Khabarovsk (Far East) | `92.223.80.0/24`, `5.188.3.0/24` | **47 ms** |
| Yekaterinburg | `92.223.91.0/24` | 120 ms |
| Novosibirsk | `146.185.199.0/24` | 139 ms |
| Rostov-on-Don | `146.185.197.0/24` | 163 ms |
| Moscow (login + matchmaking) | `154.85.91.0/24:10003` | 145 ms |

If the same server shows 40 ms without the VPN and 140 ms with it, the TUN stack is
the culprit — switch it to `system` in Settings.

## Features

- **Dashboard** — live ping from match servers, route status, active profile.
- **Match phases** — `IDLE → QUEUE → MATCH_FOUND → IN_GAME`; nothing is blocked in
  `MATCH_FOUND`, in game only delays are allowed.
- **Reject instead of blackhole** — blocked traffic gets TCP RST / ICMP unreachable.
- **Delay shaper (experimental)** — local SOCKS5 relay adds 100–500 ms to UDP of
  unwanted regions instead of cutting it.
- **Ping diagnostics** — top servers with jitter and packet loss.
- **TUN stack switch** — `system` (kernel) or `gvisor`.
- **Login pool protection** — `154.85.91.0/24:10003` is never blocked.
- **Debug trace** — metadata-only log, JSON export, 60-second hypothesis check.
- **Language** — English, Русский, Bahasa Indonesia, Filipino (0.22+).

## Install (4 steps)

1. Download `app-arm64-v8a-release.apk` from **[Releases](https://github.com/mrkoronalol/mlbb-network-doctor/releases/latest)**.
2. Allow VPN access and notifications; enable “Display over other apps”.
3. Choose a region profile.
4. Turn the filter on **before** searching for a match.

```bash
adb install -r app-arm64-v8a-release.apk
```

## How it works

1. The VPN service captures only MLBB traffic (`com.mobile.legends`, `com.mobilelegends.mi`).
2. Allowed networks (chosen region, login pool, service ports) go **direct**.
3. Blocked match ports return **RST/ICMP** instead of silent drops.
4. Phase rules: delays in `QUEUE`, nothing in `MATCH_FOUND`, delays only in game.

## Guides

- [MLBB high ping fix on Android](docs/mlbb-high-ping-fix.md)
- [Khabarovsk / Far East MLBB servers: ping and route](docs/khabarovsk-server-ping.md)
- [MLBB ping in Indonesia](docs/mlbb-ping-indonesia.md)
- [MLBB ping in the Philippines](docs/mlbb-ping-philippines.md)

![Match card](docs/match-card.png)

## FAQ

**Does it need root?** No. Only `VpnService` and stock sing-box features.

**Does it change my account region?** No. It only filters network traffic locally.

**Will it guarantee a Far East server?** No. Local filtering cannot force a region;
it removes or slows distant servers so the matchmaker prefers closer ones.

**Why is my in-game ping higher than on PC?** Usually the TUN stack (`gvisor`) or a
2.4 GHz Wi‑Fi with retries. Set the stack to `system` and check `Rx Link speed`.

**Is my traffic uploaded anywhere?** No. The app has no remote VPN server; anonymous
telemetry is opt-in and stops completely when disabled.

## Build

Requirements: JDK 17, Android SDK 35, Git.

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\fetch-libbox.ps1
"sdk.dir=C\:\\Users\\<user>\\AppData\\Local\\Android\\Sdk" | Set-Content local.properties
.\gradlew.bat :app:assembleDebug
```

## Limitations

- The app does not change your account region or bypass publisher limits.
- Local filtering cannot guarantee a specific match server.
- The delay shaper is experimental; the “ranked by ping” hypothesis is unproven.
- No root: only `VpnService` plus sing-box.

## License

**GNU GPL v3.0** — see [LICENSE](LICENSE). Not affiliated with Moonton.
