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
  <b>Ping diagnostics, filter ng server ayon sa yugto ng laban, at lokal na filter ng server para sa Mobile Legends: Bang Bang sa Android.</b><br>
  Walang malayong VPN server, walang account, walang sapilitang telemetry.
</p>

<p align="center">
  <a href="https://github.com/mrkoronalol/mlbb-network-doctor/releases/latest"><img src="https://img.shields.io/github/v/release/mrkoronalol/mlbb-network-doctor?label=i-download&color=2ea44f" alt="Pinakabagong release"></a>
  <a href="https://github.com/mrkoronalol/mlbb-network-doctor/releases/latest"><img src="https://img.shields.io/github/downloads/mrkoronalol/mlbb-network-doctor/total?label=downloads&color=blue" alt="Downloads"></a>
  <img src="https://img.shields.io/badge/Android-8.0%2B-3ddc84?logo=android&logoColor=white" alt="Android 8.0+">
  <img src="https://img.shields.io/badge/Kotlin-Jetpack%20Compose-7f52ff?logo=kotlin&logoColor=white" alt="Kotlin">
  <a href="LICENSE"><img src="https://img.shields.io/github/license/mrkoronalol/mlbb-network-doctor?color=orange" alt="GPL-3.0"></a>
</p>

**Nasa loob:** [Bakit](#bakit-may-app-na-ito) · [Ping bawat rehiyon](#ping-bawat-rehiyon) · [Mga feature](#mga-feature) · [Pag-install](#pag-install-4-hakbang) · [Paano gumagana](#paano-gumagana) · [Mga gabay](#mga-gabay) · [FAQ](#faq) · [Build](#build)

---

## Bakit may app na ito

Pinipili ng Mobile Legends ang match server batay sa ping at rehiyon ng account.
Sa Pilipinas at Indonesia, madalas 20–60 ms lang ang pinakamalapit na server
(Manila, Jakarta, Singapore), pero 150+ ms ang ibinibigay ng laro kapag napunta sa
malayong rehiyon. Kapag hinarang ang server pagkatapos ng “Nakita ang laban”,
may dodge penalty — kaya sinusukat muna ng app ang ruta, bago mag-filter.

## Ping bawat rehiyon

Sinukat sa Nokia A063 (Android 13) sa Wi‑Fi 2,4 GHz; parehong address mula sa PC
sa parehong network ay 39–42 ms — halos walang dagdag ang tunnel.

| Rehiyon | Pool ng server | Ping |
| --- | --- | --- |
| Khabarovsk (Far East) | `92.223.80.0/24`, `5.188.3.0/24` | **47 ms** |
| Yekaterinburg | `92.223.91.0/24` | 120 ms |
| Novosibirsk | `146.185.199.0/24` | 139 ms |
| Rostov-on-Don | `146.185.197.0/24` | 163 ms |
| Moscow (login at matchmaking) | `154.85.91.0/24:10003` | 145 ms |

## Mga feature

- **Dashboard** — live na ping mula sa match servers, status ng ruta, aktibong profile.
- **Yugto ng laban** — `IDLE → QUEUE → MATCH_FOUND → IN_GAME`; walang hinaharangan sa
  `MATCH_FOUND`, sa laro delay lang ang pinapayagan.
- **Reject, hindi blackhole** — ang naharang na traffic ay may TCP RST / ICMP unreachable.
- **Delay shaper (eksperimental)** — lokal na SOCKS5 relay na nagdadagdag ng 100–500 ms
  sa UDP ng hindi gustong rehiyon.
- **Ping diagnostics** — pinakamabilis na server na may jitter at packet loss.
- **TUN stack** — `system` (kernel) o `gvisor`.
- **Protektado ang login pool** — `154.85.91.0/24:10003` ay hindi kailanman hinaharangan.
- **Debug trace** — log na walang nilalaman ng packet, JSON export, 60-segundong test.
- **Wika** — English, Русский, Bahasa Indonesia, Filipino (0.22+).

## Pag-install (4 hakbang)

1. I-download ang `app-arm64-v8a-release.apk` mula sa **[Releases](https://github.com/mrkoronalol/mlbb-network-doctor/releases/latest)**.
2. Payagan ang VPN at notifications, i-on ang “Display over other apps”.
3. Pumili ng region profile.
4. I-on ang filter **bago** maghanap ng laban.

```bash
adb install -r app-arm64-v8a-release.apk
```

## Paano gumagana

1. Kinukuha ng VPN service ang traffic ng MLBB lang.
2. Ang pinapayagang network ay **direkta**.
3. Ang naka-block na match port ay sumasagot ng **RST/ICMP**, hindi tahimik.
4. Rules per yugto: delay sa `QUEUE`, wala sa `MATCH_FOUND`, delay lang sa laro.

## Mga gabay

- [Paano ayusin ang mataas na ping sa MLBB Android](docs/mlbb-high-ping-fix.md)
- [MLBB Khabarovsk / Far East servers](docs/khabarovsk-server-ping.md)
- [Ping ng MLBB sa Indonesia](docs/mlbb-ping-indonesia.md)
- [Ping ng MLBB sa Pilipinas](docs/mlbb-ping-philippines.md)

![Match card](docs/match-card.png)

## FAQ

**Kailangan ba ng root?** Hindi. `VpnService` at stock na sing-box lang.

**Binabago ba ang rehiyon ng account?** Hindi. Lokal na filter lang.

**Sigurado bang malapit na server?** Hindi. Hindi kayang pilitin ng lokal na filter
ang partikular na rehiyon.

**Bakit mas mataas ang ping sa laro kaysa PC?** Madalas dahil sa TUN stack (`gvisor`)
o 2,4 GHz Wi‑Fi. Gawing `system` at tingnan ang `Rx Link speed`.

**Saan napupunta ang traffic ko?** Wala — walang malayong VPN server, at ang telemetry
ay opt-in at pwedeng patayin nang tuluyan.

## Build

Kailangan: JDK 17, Android SDK 35, Git.

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\fetch-libbox.ps1
"sdk.dir=C\:\\Users\\<user>\\AppData\\Local\\Android\\Sdk" | Set-Content local.properties
.\gradlew.bat :app:assembleDebug
```

## Limitasyon

- Hindi binabago ng app ang rehiyon ng account at hindi lumalabag sa patakaran ng publisher.
- Hindi sinisiguro ng lokal na filter ang partikular na match server.
- Eksperimental pa ang delay shaper; hindi pa napatunayan ang “ranking by ping”.
- Walang root: `VpnService` at sing-box lang.

## Lisensya

**GNU GPL v3.0** — tingnan ang [LICENSE](LICENSE). Hindi kaakibat ng Moonton.
