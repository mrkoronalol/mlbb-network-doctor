# MLBB high ping fix on Android

Short guide: what raises ping in Mobile Legends on Android and how to diagnose it
locally without a remote VPN. Applies to players in Indonesia, the Philippines,
Russia and any region where the game assigns a distant server.

## Symptoms

- Ping in the lobby is 60 ms, in a match it jumps to 150–300 ms.
- Heroes with long combos feel unresponsive.
- The game picks a distant match server while a closer one exists.

## Causes (checked on a real device)

| Cause | Effect | Fix |
| --- | --- | --- |
| 2.4 GHz Wi‑Fi with retries | +60–150 ms, jitter | 5 GHz AP; check `Rx Link speed` |
| Userspace TUN stack (`gvisor`) | ~+100 ms on TCP | switch the stack to `system` in the app |
| Remote VPN in another region | +100–300 ms | use a local filter instead of a VPN |
| Background downloads or charging state throttling | spikes | charge and close apps before a match |
| Match server in the wrong region | +80–200 ms | see the region guides below |

## Step 1 — measure without the app

```bash
# Android (Termux or adb shell)
toybox nc -w 3 92.223.80.65 5501 </dev/null; echo $?
# Windows / Linux PC on the same Wi‑Fi
ping -n 4 92.223.80.65
```

Compare the number with what the game shows. If PC and phone differ by ~100 ms,
the tunnel stack is the problem, not the network.

## Step 2 — measure with MLBB Network Doctor

1. Install from [Releases](https://github.com/mrkoronalol/mlbb-network-doctor/releases/latest).
2. Open **Monitor → Measure ping to all servers**.
3. Read the table: average ping, jitter (`±N`) and loss. A stable 60 ms beats a
   40 ms average with 80 ms jitter.
4. If the tunnel shows a much higher number than the PC, change
   **Settings → TUN stack** to `system`.

## Step 3 — pick the right server

| Region | Pool | Expected ping |
| --- | --- | --- |
| Khabarovsk (Far East) | `92.223.80.0/24`, `5.188.3.0/24` | 32–60 ms |
| Jakarta / Singapore | Local pools | 20–60 ms |
| Manila | Local pools | 20–60 ms |
| Moscow (login + matchmaking) | `154.85.91.0/24:10003` | 145–160 ms |

Blocking the login pool breaks login and matchmaking — the app never blocks it.

## What actually helps

1. Switch to 5 GHz Wi‑Fi or a different mobile operator.
2. Set the TUN stack to `system`.
3. Remove a remote VPN while playing.
4. Filter only the unwanted match regions, never the login pool.

---

[MLBB Network Doctor](https://github.com/mrkoronalol/mlbb-network-doctor) · GPL-3.0 · not affiliated with Moonton.
