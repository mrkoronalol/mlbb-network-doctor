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
  <b>Диагностика пинга, фильтр серверов по фазам матча и локальный фильтр серверов Mobile Legends на Android.</b><br>
  Без удалённого VPN-сервера, без аккаунтов и обязательной аналитики.
</p>

<p align="center">
  <a href="https://github.com/mrkoronalol/mlbb-network-doctor/releases/latest"><img src="https://img.shields.io/github/v/release/mrkoronalol/mlbb-network-doctor?label=%D1%81%D0%BA%D0%B0%D1%87%D0%B0%D1%82%D1%8C&color=2ea44f" alt="Последний релиз"></a>
  <a href="https://github.com/mrkoronalol/mlbb-network-doctor/releases/latest"><img src="https://img.shields.io/github/downloads/mrkoronalol/mlbb-network-doctor/total?label=%D1%81%D0%BA%D0%B0%D1%87%D0%B8%D0%B2%D0%B0%D0%BD%D0%B8%D0%B9&color=blue" alt="Скачивания"></a>
  <img src="https://img.shields.io/badge/Android-8.0%2B-3ddc84?logo=android&logoColor=white" alt="Android 8.0+">
  <img src="https://img.shields.io/badge/Kotlin-Jetpack%20Compose-7f52ff?logo=kotlin&logoColor=white" alt="Kotlin">
  <a href="LICENSE"><img src="https://img.shields.io/github/license/mrkoronalol/mlbb-network-doctor?color=orange" alt="GPL-3.0"></a>
</p>

**Содержание:** [Зачем](#зачем-это-нужно) · [Пинг по регионам](#пинг-по-регионам) · [Возможности](#возможности) · [Установка](#установка-4-шага) · [Как-это-работает](#как-это-работает) · [Гайды](#гайды) · [Вопросы](#частые-вопросы) · [Сборка](#сборка)

---

## Зачем это нужно

Mobile Legends выбирает матч-сервер по пингу и региону аккаунта. Игроки с Дальнего
Востока (Хабаровск, Владивосток) получают **32–60 мс до местных серверов** и
**145–260 мс до Москвы**, но игра часто назначает дальний сервер. Блокировка сервера
после «Матч найден» приводит к штрафу за dodge — поэтому приложение сначала
измеряет маршрут, а потом фильтрует.

## Пинг по регионам

Измерено на Nokia A063 (Android 13) по Wi‑Fi 2,4 ГГц; те же адреса с ПК в той же
сети отдают 39–42 мс, то есть туннель почти ничего не добавляет.

| Регион | Пул серверов | Пинг |
| --- | --- | --- |
| Хабаровск (ДВ) | `92.223.80.0/24`, `5.188.3.0/24` | **47 мс** |
| Екатеринбург | `92.223.91.0/24` | 120 мс |
| Новосибирск | `146.185.199.0/24` | 139 мс |
| Ростов-на-Дону | `146.185.197.0/24` | 163 мс |
| Москва (логин и матчмейкинг) | `154.85.91.0/24:10003` | 145 мс |

Если без VPN тот же сервер дает 40 мс, а с VPN 140 мс — виноват стек TUN.
Переключите его на `system` в настройках.

## Возможности

- **Дашборд** — живой пинг с матч-серверов, статус маршрута, активный профиль.
- **Фазы матча** — `IDLE → QUEUE → MATCH_FOUND → IN_GAME`; в `MATCH_FOUND` ничего не
  блокируется, в игре разрешены только задержки.
- **Отказ вместо «черной дыры»** — заблокированный трафик получает TCP RST или
  ICMP unreachable.
- **Шейпер задержки (эксперимент)** — локальный SOCKS5-релей добавляет 100–500 мс к
  UDP-трафику нежелательных регионов вместо обрыва.
- **Диагностика пинга** — топ серверов с джиттером и потерями.
- **Выбор стека TUN** — `system` (ядро) или `gvisor`.
- **Защита логин-пула** — `154.85.91.0/24:10003` не блокируется.
- **Debug trace** — лог без содержимого пакетов, экспорт JSON и проверка гипотезы
  за 60 секунд.
- **Языки** — English, Русский, Bahasa Indonesia, Filipino (0.22+).

## Установка (4 шага)

1. Скачайте `app-arm64-v8a-release.apk` из **[Releases](https://github.com/mrkoronalol/mlbb-network-doctor/releases/latest)**.
2. Разрешите VPN и уведомления, включите «Поверх других приложений».
3. Выберите профиль региона.
4. Включите фильтр **до** поиска матча.

```bash
adb install -r app-arm64-v8a-release.apk
```

## Как это работает

1. VPN-сервис перехватывает только трафик MLBB.
2. Разрешённые сети идут **напрямую**.
3. Заблокированные матч-порты отвечают **RST/ICMP**, а не молчат.
4. Правила по фазам: задержки в `QUEUE`, ничего в `MATCH_FOUND`, только задержки в игре.

## Гайды

- [Высокий пинг в MLBB на Android: как исправить](docs/mlbb-high-ping-fix.md)
- [Серверы MLBB Хабаровск / ДВ: пинг и маршрут](docs/khabarovsk-server-ping.md)
- [Пинг MLBB в Индонезии](docs/mlbb-ping-indonesia.md)
- [Пинг MLBB на Филиппинах](docs/mlbb-ping-philippines.md)

![Карточка матча](docs/match-card.png)

## Частые вопросы

**Нужен ли root?** Нет. Только `VpnService` и штатные возможности sing-box.

**Меняет ли регион аккаунта?** Нет. Фильтрация только на уровне сети телефона.

**Гарантирует ли сервер ДВ?** Нет. Локальная фильтрация не может заставить игру
выбрать конкретный сервер.

**Почему в игре пинг выше, чем на ПК?** Обычно из-за стека TUN (`gvisor`) или Wi‑Fi
2,4 ГГц с ретрансмиссиями. Поставьте `system` и проверьте `Rx Link speed`.

**Куда уходит мой трафик?** Никуда: удалённого VPN-сервера нет, телеметрия
включается по желанию и отключается полностью.

## Сборка

Требования: JDK 17, Android SDK 35, Git.

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\fetch-libbox.ps1
"sdk.dir=C\:\\Users\\<user>\\AppData\\Local\\Android\\Sdk" | Set-Content local.properties
.\gradlew.bat :app:assembleDebug
```

## Ограничения

- Приложение не меняет регион аккаунта и не обходит ограничения издателя.
- Локальная фильтрация не гарантирует конкретный матч-сервер.
- Шейпер задержки экспериментальный, гипотеза «выбор по пингу» не доказана.
- Root не используется.

## Лицензия

**GNU GPL v3.0** — см. [LICENSE](LICENSE). Проект не связан с Moonton.
