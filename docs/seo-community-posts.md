# SEO-тексты для сообществ (публикуешь ты)

Готовые тексты под площадки. Не спамь: один пост на площадку, отвечай в
комментариях, не вставляй ссылку больше одного раза на пост.

---

## 4PDA (RU)

**Заголовок:** MLBB Network Doctor — локальный фильтр серверов и диагностика пинга (без root)

Играл с ДВ, пинг до Хабаровска 40 мс на ПК, а в игре 150+ и матчмейкинг кидает в
Москву. Сделал приложение, которое:
- показывает пинг до всех известных матч-серверов (с джиттером и потерями);
- фильтрует только трафик MLBB, без удалённого VPN и аккаунтов;
- по фазам: в очереди фильтрует, в «Матч найден» не трогает (чтобы не ловить dodge);
- умеет не блокировать, а задерживать нежелательный регион.

Отдельно нашёл ошибку у себя: стек `gvisor` добавлял ~100 мс на TCP. После
перехода на `system` — Хабаровск 47 мс.

APK: https://github.com/mrkoronalol/mlbb-network-doctor/releases/latest
Исходники (GPL-3.0): https://github.com/mrkoronalol/mlbb-network-doctor

Буду рад логам: Настройки → Для разработчиков → Debug trace → экспорт.

---

## Reddit r/MobileLegendsGame (EN)

**Title:** I measured MLBB ping on a real phone — the VPN stack was adding 100 ms

Short version: on a Nokia A063 over 2.4 GHz Wi-Fi, Khabarovsk match servers answer in
**47 ms**, Moscow in **145 ms**. But through a VPN app using the userspace `gvisor`
TUN stack, the same server showed **142 ms**.

The fix was switching the stack to `system`. If your ping looks 80–120 ms worse than
a PC on the same network, that's usually it.

I packaged the diagnostics part into a small open-source app (local filter only, no
remote VPN, no account):
https://github.com/mrkoronalol/mlbb-network-doctor

It measures every known match server, shows jitter/loss, and has a phase machine so
it never blocks after “Match found” (which triggers the dodge penalty).

Happy to share raw numbers for other regions — especially Indonesia and the
Philippines, where I don't have test devices yet.

---

## Reddit r/MobileLegendsGame (short reply template)

If ping is +100 ms in game vs. PC:

1. `ping <server-ip>` on PC and compare.
2. If PC is fine, it's the tunnel: switch TUN stack to `system`.
3. Check Wi-Fi `Rx Link speed` — 1 Mbps on 2.4 GHz means retries.
4. Don't block the login pool `154.85.91.0/24:10003`.

---

## VK / Telegram (RU, группы ДВ)

**Заголовок:** Почему MLBB кидает в Москву при 40 мс до Хабаровска

Снял логи с телефона: игра пингует Хабаровск (`92.223.80.x`) и Москву
(`154.85.91.x`) одновременно, но очередь и логин идут через Москву. Если резать
Москву — не входит в аккаунт. Если не резать — часто кидает туда.

Вывод: локальная блокировка не «переключает регион», но с ней можно:
- точно увидеть, что игра выбирает;
- не ловить dodge, отключая фильтр на фазе «Матч найден»;
- задерживать дальние регионы вместо блокировки.

Сделал бесплатное приложение с этими логами: [ссылка]. APK без root, только
фильтр MLBB.

---

## Facebook групп (FIL)

**Pamagat:** Sinukat ko ang ping ng MLBB — 47 ms lang dapat sa malapit na server

Nag-test ako sa Nokia A063: kapag nasa 2.4 GHz Wi-Fi at gumagamit ng userspace VPN
stack (`gvisor`), 142 ms ang nakikita kahit 47 ms lang talaga sa server. Isang switch
lang sa `system` — bumaba sa 47 ms.

Libre at open source na app (lokal na filter lang, walang account):
https://github.com/mrkoronalol/mlbb-network-doctor

Puwede rin i-check kung ilan ang jitter at packet loss bago maghanap ng laban.

---

## Instagram/Facebook (ID)

**Judul:** Ping MLBB 150 ms padahal server Jakarta cuma 30 ms?

Penyebab paling sering bukan server, tapi:
1. Wi-Fi 2,4 GHz dengan retry (cek `Rx Link speed`);
2. Tumpukan VPN `gvisor` yang menambah ~100 ms;
3. VPN jarak jauh yang mengalihkan trafik ke Eropa.

Aplikasi gratis (open source) untuk mengukur ping semua server MLBB dan memfilter
hanya trafik game: https://github.com/mrkoronalol/mlbb-network-doctor

---

## Правила публикации

- 4PDA: раздел Android → «Игры», тема о MLBB; обязательно указать, что нет root,
  нет сбора личных данных и что это фанатский проект.
- Reddit: без ссылки в заголовке; ссылка в теле, один пост, отвечать на вопросы.
- VK/Telegram: только в тематические группы ДВ, не более одного поста.
- FB/IG: короткий пост + один скриншот диагностики.
