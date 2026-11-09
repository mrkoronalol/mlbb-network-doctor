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
  <b>Diagnosa ping, filter server per fase pertandingan, dan filter server lokal untuk Mobile Legends: Bang Bang di Android.</b><br>
  Tanpa server VPN jarak jauh, tanpa akun, tanpa telemetri wajib.
</p>

<p align="center">
  <a href="https://github.com/mrkoronalol/mlbb-network-doctor/releases/latest"><img src="https://img.shields.io/github/v/release/mrkoronalol/mlbb-network-doctor?label=unduh&color=2ea44f" alt="Rilis terbaru"></a>
  <a href="https://github.com/mrkoronalol/mlbb-network-doctor/releases/latest"><img src="https://img.shields.io/github/downloads/mrkoronalol/mlbb-network-doctor/total?label=unduhan&color=blue" alt="Unduhan"></a>
  <img src="https://img.shields.io/badge/Android-8.0%2B-3ddc84?logo=android&logoColor=white" alt="Android 8.0+">
  <img src="https://img.shields.io/badge/Kotlin-Jetpack%20Compose-7f52ff?logo=kotlin&logoColor=white" alt="Kotlin">
  <a href="LICENSE"><img src="https://img.shields.io/github/license/mrkoronalol/mlbb-network-doctor?color=orange" alt="GPL-3.0"></a>
</p>

**Isi:** [Kenapa](#kenapa-aplikasi-ini-ada) · [Ping per wilayah](#ping-per-wilayah) · [Fitur](#fitur) · [Instalasi](#instalasi-4-langkah) · [Cara kerja](#cara-kerja) · [Panduan](#panduan) · [FAQ](#faq) · [Build](#build)

---

## Kenapa aplikasi ini ada

Mobile Legends memilih server pertandingan berdasarkan ping dan wilayah akun.
Pemain di Rusia timur atau di Indonesia sering mendapat server yang jauh sehingga
ping naik 150–260 ms, padahal server terdekat memberi 30–60 ms. Memblokir server
setelah “Pertandingan ditemukan” justru menyebabkan penalti dodge — karena itu
aplikasi ini lebih dulu mengukur rute, lalu memfilter.

## Ping per wilayah

Diukur pada Nokia A063 (Android 13) lewat Wi‑Fi 2,4 GHz; dari PC di jaringan yang
sama alamat yang sama memberi 39–42 ms, jadi tunnel hampir tanpa tambahan.

| Wilayah | Kumpulan server | Ping |
| --- | --- | --- |
| Khabarovsk (Timur Jauh) | `92.223.80.0/24`, `5.188.3.0/24` | **47 ms** |
| Yekaterinburg | `92.223.91.0/24` | 120 ms |
| Novosibirsk | `146.185.199.0/24` | 139 ms |
| Rostov-on-Don | `146.185.197.0/24` | 163 ms |
| Moskow (login dan matchmaking) | `154.85.91.0/24:10003` | 145 ms |

Di wilayah Indonesia dan Filipina, server terdekat (Singapura, Jakarta, Manila)
biasanya 20–60 ms; kalau game memberi 150+ ms, cek rute dan tumpukan TUN.

## Fitur

- **Dasbor** — ping langsung dari server pertandingan, status rute, profil aktif.
- **Fase pertandingan** — `IDLE → QUEUE → MATCH_FOUND → IN_GAME`; tidak ada blokir di
  `MATCH_FOUND`, saat bermain hanya delay yang diizinkan.
- **Tolak, bukan blackhole** — trafik yang diblokir mendapat TCP RST / ICMP unreachable.
- **Delay shaper (eksperimen)** — relay SOCKS5 lokal menambah 100–500 ms ke UDP
  wilayah yang tidak diinginkan, bukan memutusnya.
- **Diagnosa ping** — server tercepat dengan jitter dan packet loss.
- **Pilihan tumpukan TUN** — `system` (kernel) atau `gvisor`.
- **Kumpulan login dilindungi** — `154.85.91.0/24:10003` tidak pernah diblokir.
- **Debug trace** — catatan tanpa isi paket, ekspor JSON, uji hipotesis 60 detik.
- **Bahasa** — English, Русский, Bahasa Indonesia, Filipino (0.22+).

## Instalasi (4 langkah)

1. Unduh `app-arm64-v8a-release.apk` dari **[Releases](https://github.com/mrkoronalol/mlbb-network-doctor/releases/latest)**.
2. Izinkan VPN dan notifikasi; aktifkan “Tampilkan di atas aplikasi lain”.
3. Pilih profil wilayah.
4. Nyalakan filter **sebelum** mencari pertandingan.

```bash
adb install -r app-arm64-v8a-release.apk
```

## Cara kerja

1. Layanan VPN hanya menangkap trafik MLBB.
2. Jaringan yang diizinkan masuk **langsung**.
3. Port pertandingan yang diblokir menjawab **RST/ICMP**, bukan diam.
4. Aturan per fase: delay di `QUEUE`, tanpa filter di `MATCH_FOUND`, hanya delay saat bermain.

## Panduan

- [Cara memperbaiki ping tinggi MLBB di Android](docs/mlbb-high-ping-fix.md)
- [Server MLBB Khabarovsk / Timur Jauh: ping dan rute](docs/khabarovsk-server-ping.md)
- [Ping MLBB di Indonesia](docs/mlbb-ping-indonesia.md)
- [Ping MLBB di Filipina](docs/mlbb-ping-philippines.md)

![Kartu pertandingan](docs/match-card.png)

## FAQ

**Butuh root?** Tidak. Hanya `VpnService` dan fitur bawaan sing-box.

**Mengubah wilayah akun?** Tidak. Filter hanya di jaringan ponsel.

**Menjamin server terdekat?** Tidak. Filter lokal tidak bisa memaksa wilayah tertentu.

**Kenapa ping di game lebih tinggi dari PC?** Biasanya karena tumpukan TUN (`gvisor`)
atau Wi‑Fi 2,4 GHz. Ubah ke `system` dan cek `Rx Link speed`.

**Ke mana trafik saya pergi?** Tidak ke mana pun: tidak ada server VPN jauh, telemetri
hanya jika diaktifkan dan bisa dimatikan sepenuhnya.

## Build

Butuh: JDK 17, Android SDK 35, Git.

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\fetch-libbox.ps1
"sdk.dir=C\:\\Users\\<user>\\AppData\\Local\\Android\\Sdk" | Set-Content local.properties
.\gradlew.bat :app:assembleDebug
```

## Batasan

- Aplikasi tidak mengubah wilayah akun dan tidak melewati batasan penerbit.
- Filter lokal tidak menjamin server pertandingan tertentu.
- Delay shaper masih eksperimental; hipotesis “peringkat berdasarkan ping” belum terbukti.
- Tanpa root: hanya `VpnService` dan sing-box.

## Lisensi

**GNU GPL v3.0** — lihat [LICENSE](LICENSE). Tidak berafiliasi dengan Moonton.
