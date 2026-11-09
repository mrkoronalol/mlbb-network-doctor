<p align="center">
  <img src="docs/app-icon.png" width="112" alt="MLBB Network Doctor">
</p>

<h1 align="center">MLBB Network Doctor</h1>

<p align="center">
  <b>Ping diagnostics, match phases and a local server filter for Mobile Legends on Android.</b><br>
  No remote VPN server, no accounts, no mandatory analytics — only MLBB traffic
  (<code>com.mobile.legends</code>) goes through <code>VpnService</code> + <code>sing-box</code>.
</p>

<p align="center">
  <a href="https://github.com/mrkoronalol/mlbb-network-doctor/releases/latest"><img src="https://img.shields.io/github/v/release/mrkoronalol/mlbb-network-doctor?label=download&color=2ea44f" alt="Latest release"></a>
  <a href="https://github.com/mrkoronalol/mlbb-network-doctor/releases/latest"><img src="https://img.shields.io/github/downloads/mrkoronalol/mlbb-network-doctor/total?label=downloads&color=blue" alt="Downloads"></a>
  <img src="https://img.shields.io/badge/Android-8.0%2B-3ddc84?logo=android&logoColor=white" alt="Android 8.0+">
  <img src="https://img.shields.io/badge/Kotlin-Jetpack%20Compose-7f52ff?logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/core-sing--box-68a9e8" alt="sing-box">
  <a href="LICENSE"><img src="https://img.shields.io/github/license/mrkoronalol/mlbb-network-doctor?color=orange" alt="GPL-3.0"></a>
</p>

---

## Why this exists

Players in the Far East get **32–60 ms to Khabarovsk** and **145–260 ms to Moscow**,
but the game still assigns distant servers — and blocking them after “Match found”
causes a dodge penalty. This app measures the real route first, then filters
**before** the server is assigned.

| Ping in the app | Route |
| --- | --- |
| **47 ms** | Khabarovsk `92.223.80.0/24` (direct) |
| 120 ms | Yekaterinburg |
| 139 ms | Novosibirsk |
| 145 ms | Moscow (login/matchmaking pool) |

Measured on a real phone; the same addresses show 39–42 ms from a PC on the same
Wi‑Fi, so the numbers are honest.

## Features

- **Dashboard** — current ping (match servers only), route status and profile.
- **Match phases** — `IDLE → QUEUE → MATCH_FOUND → IN_GAME`. Nothing is blocked in
  `MATCH_FOUND`; in game only delays are allowed, never blocking.
- **Reject instead of blackhole** — blocked traffic gets a TCP RST / ICMP
  unreachable, so the client fails fast instead of hanging.
- **Delay shaper (experimental)** — a local SOCKS5 relay adds 100–500 ms to UDP
  traffic of unwanted regions instead of cutting it.
- **Ping diagnostics** — top servers with jitter and packet loss.
- **TUN stack switch** — `system` (kernel) or `gvisor`; the wrong stack alone cost
  ~100 ms on TCP.
- **Login pool** — `154.85.91.0/24:10003` is never blocked, so matchmaking works.
- **Debug trace** — metadata-only log with phases, export to JSON, “check the
  hypothesis” for 60 seconds while queueing.
- **Meta tab** — tier lists, counters, “lag-friendly” heroes.

## Install

1. Download the APK from **[Releases](https://github.com/mrkoronalol/mlbb-network-doctor/releases/latest)**.
2. Allow VPN and notifications; enable “Display over other apps” for the match card.
3. Pick a profile and turn the filter on **before** searching for a match.

```bash
adb install -r app-arm64-v8a-release.apk
```

Only install builds from this repository; releases are signed and there is no
Play Store listing yet.

## How it works

1. The VPN service captures only MLBB traffic.
2. Allowed networks (Far East, login pool, service ports) go **direct**.
3. Blocked match ports return **RST/ICMP** instead of silent drops.
4. In `QUEUE` delays apply, in `MATCH_FOUND` nothing is filtered, in game — delays
   only.

## Guides

- [MLBB high ping fix on Android](docs/mlbb-high-ping-fix.md)
- [MLBB Khabarovsk / Far East servers: ping and route](docs/khabarovsk-server-ping.md)

## Screenshot

![Match card](docs/match-card.png)

## Build

Requirements: JDK 17, Android SDK 35, Git.

```powershell
# libbox.aar is not stored in git
powershell -ExecutionPolicy Bypass -File .\scripts\fetch-libbox.ps1

"sdk.dir=C\:\\Users\\<user>\\AppData\\Local\\Android\\Sdk" | Set-Content local.properties

.\gradlew.bat :app:assembleDebug
```

## Data & licenses

- GeoIP: [DB-IP Lite City](https://db-ip.com), CC BY 4.0,
  via [sapics/ip-location-db](https://github.com/sapics/ip-location-db).
- VPN core: [sing-box](https://github.com/SagerNet/sing-box) /
  [libbox](https://github.com/singbox-android/libbox).
- UI: Kotlin, Jetpack Compose, Room, MaxMind DB Reader.

## Limitations

- The app does not change your account region and does not bypass publisher limits.
- Local filtering **cannot guarantee** a specific match server: the game may bind
  some sockets to Wi‑Fi directly. Switching region requires an account in that
  region or a remote relay.
- The delay shaper is experimental; the “ranked by ping” hypothesis is not proven.
- If your account is tied to Novosibirsk, delays may not change server selection.
- No root: only `VpnService` and stock sing-box features.

## License

**GNU GPL v3.0** — see [LICENSE](LICENSE). Not affiliated with Moonton.
