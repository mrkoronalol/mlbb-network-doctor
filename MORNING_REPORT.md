# Morning report — Android

Ночная сессия: улучшения диагностики и меты. **Релиз не публиковался** — тег
`v0.20.1` нужно поставить утром.

## Готово

- **Диагностика пинга**: к среднему пингу добавлены **джиттер** (`±N мс`) и
  **потери** (`потери N%`), а также время последнего замера
  («Обновлено: HH:mm»). `PingReport` теперь хранит `measuredAt`,
  `PingTargetResult` — `jitterMs`/`lossPercent`.
- **Мета**: в карточке героя появился бейдж **Lag-friendly** / **Нужен низкий пинг**
  по `pingSensitivity`.
- **Тесты**: `PingCollectorTest` (login-pool политика, allow-only профиль),
  `AutoProfileAdvisorTest` (выбор региона по пингу, пустые данные).
- Версия: **0.20.1 (versionCode 29)**, CHANGELOG обновлён.
- APK собран: `app/build/outputs/apk/release/app-arm64-v8a-release.apk`
  (подпись та же, `versionName=0.20.1`).
- `:app:testDebugUnitTest` и `:app:assembleRelease` — успешно.

## Не сделано / почему

- **Динамические цели из `rules.json`**: в `knownExact`/`knownSubnets` нет портов,
  а угадывать порт для каждого IP нельзя (будут ложные таймауты). Оставил
  проверенный курируемый список матч-серверов (DV/MSK/EKB/ROSTOV/NSK/EU) —
  его данные совпадают с аудитом.
- **Установка на телефон**: устройство `a7f243e8` отключилось от ADB во время
  сборки. На телефоне осталась 0.20.0; 0.20.1 не установлена.
- `HeroBuild.items -> Json` и сид матчапов — рискованные/данные, не трогал.

## Утром

1. Подключить телефон, установить:
   ```powershell
   adb install -r app\build\outputs\apk\release\app-arm64-v8a-release.apk
   ```
2. Проверить: диагностика (джиттер/потери/время), бейдж в карточке героя,
   профиль ДВ и логин-пул.
3. Если всё ок — опубликовать:
   ```bash
   git tag -a v0.20.1 -m "MLBB Network Doctor 0.20.1"
   git push origin v0.20.1
   ```
   GitHub Actions `release.yml` соберёт APK, подпишет `rules.json` и опубликует
   релиз. Подпись не меняется, данные пользователей сохраняются.
4. Напомнить: `applicationId` остаётся `ru.mlbb.blocker`, сертификат —
   `CN=MLBB Blocker` (менять нельзя без потери обновлений).

## Коммиты

- `829018c` — diagnostics jitter/loss + lag-friendly badge (0.20.1)
- `640485b` — ребрендинг системных строк
- `7e76aba` — переименование репозитория
