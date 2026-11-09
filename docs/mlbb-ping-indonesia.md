# Ping MLBB di Indonesia: 20–60 ms kalau server benar

Panduan singkat untuk pemain Mobile Legends di Indonesia: bagaimana mengecek ping
ke server terdekat, kenapa kadang 150+ ms, dan apa yang bisa dilakukan tanpa VPN
jarak jauh.

## Server terdekat untuk Indonesia

| Lokasi | Perkiraan ping |
| --- | --- |
| Jakarta / Singapura | 20–60 ms |
| Batam / Johor | 30–70 ms |
| Server di luar kawasan (Moskow, Rostov) | 150–260 ms |

Kalau aplikasi menunjukkan 150+ ms ke Jakarta, masalahnya hampir selalu di
perangkat, bukan di server.

## Tiga penyebab ping tinggi yang sering terlewat

1. **Wi‑Fi 2,4 GHz dengan banyak retry** — cek `Rx Link speed`; kalau 1–10 Mbps
   sementara Tx ratusan Mbps, pindah ke 5 GHz.
2. **Tumpukan TUN userspace (`gvisor`)** — di Android sebagian aplikasi VPN memakai
   gvisor dan menambah ~100 ms pada TCP. Ubah ke `system`.
3. **VPN jarak jauh** — server VPN di Eropa membuat ping ke Jakarta naik 150–300 ms.
   Filter lokal lebih aman daripada VPN.

## Cara cek dengan MLBB Network Doctor

1. Pasang dari [Releases](https://github.com/mrkoronalol/mlbb-network-doctor/releases/latest).
2. Buka **Monitor → Ukur ping ke semua server**.
3. Lihat jitter (`±N`) dan loss — ping 40 ms dengan jitter 80 lebih buruk daripada
   ping 60 ms yang stabil.
4. Kalau tunnel menunjukkan jauh lebih tinggi dari tanpa VPN, ganti **Pengaturan →
   Tumpukan TUN** ke `system`.

Aplikasi hanya memfilter trafik MLBB (`com.mobile.legends`), tidak memakai akun, dan
telemetri bisa dimatikan sepenuhnya.

---

[MLBB Network Doctor](https://github.com/mrkoronalol/mlbb-network-doctor) · GPL-3.0 · bukan afiliasi Moonton.
