const fs = require("fs");
const path = require("path");

const root = path.join("D:", "mlbb blocker", "app", "src", "main", "res");
const base = fs.readFileSync(path.join(root, "values", "strings.xml"), "utf8");

// [en, ru, id, fil] — ru = base string (may be reused when identical).
const T = {
  app_name: ["MLBB Network Doctor", "MLBB Network Doctor", "MLBB Network Doctor", "MLBB Network Doctor"],
  app_description: [
    "Network diagnostics for Mobile Legends: ping, route and a local server filter",
    "Диагностика сети Mobile Legends: пинг, маршрут и локальная фильтрация серверов",
    "Diagnosa jaringan Mobile Legends: ping, rute, dan filter server lokal",
    "Ping diagnostics ng Mobile Legends: ping, rute, at lokal na filter ng server",
  ],
  status_stopped: ["Off", "Выключен", "Mati", "Naka-off"],
  status_starting: ["Starting…", "Запуск…", "Memulai…", "Nagsisimula…"],
  status_running: ["Active", "Активен", "Aktif", "Aktibo"],
  status_error: ["Error", "Ошибка", "Galat", "Error"],
  main_title: ["MLBB Network Doctor", "MLBB Network Doctor", "MLBB Network Doctor", "MLBB Network Doctor"],
  main_subtitle: [
    "Network diagnostics and ping tuning for MLBB",
    "Диагностика сети и адаптация к пингу MLBB",
    "Diagnosa jaringan dan penyesuaian ping MLBB",
    "Network diagnostics at ping tuning para sa MLBB",
  ],
  connect: ["Turn on blocking", "Включить блокировку", "Nyalakan pemblokiran", "I-on ang pagharang"],
  disconnect: ["Turn off", "Выключить", "Matikan", "I-off"],
  rules_count: ["Rules/networks: %1$d", "Правил/сетей: %1$d", "Aturan/jaringan: %1$d", "Mga patakaran/network: %1$d"],
  rules_stat: ["Rules/networks", "Правил/сетей", "Aturan/jaringan", "Mga patakaran/network"],
  discovery_tab: ["Discovery", "Обнаружение", "Penemuan", "Pagtuklas"],
  connections_tab: ["Connections", "Соединения", "Koneksi", "Koneksyon"],
  blocklist_tab: ["Blocklist", "Блок-лист", "Daftar blokir", "Blocklist"],
  monitor_tab: ["Monitor", "Монитор", "Monitor", "Monitor"],
  meta_tab: ["Meta", "Мета", "Meta", "Meta"],
  settings_tab: ["Settings", "Настройки", "Pengaturan", "Mga setting"],
  home_tab: ["Home", "Главная", "Beranda", "Home"],
  match_tab: ["Match", "Матч", "Laga", "Laban"],
  dashboard_ping: ["Ping", "Пинг", "Ping", "Ping"],
  dashboard_route: ["Route", "Маршрут", "Rute", "Rute"],
  dashboard_region: ["Region", "Регион", "Wilayah", "Rehiyon"],
  route_direct: ["Direct", "Прямой", "Langsung", "Direkta"],
  route_filtered: ["Filtered", "Активен", "Difilter", "Naka-filter"],
  route_off: ["Off", "Выкл.", "Mati", "Off"],
  profile_label: ["Profile", "Профиль", "Profil", "Profile"],
  region_auto: ["Auto", "Авто-выбор", "Otomatis", "Auto"],
  region_custom: ["Custom", "Пользовательский", "Kustom", "Custom"],
  region_custom_short: ["Custom", "Свой", "Kustom", "Sarili"],
  profile_off: ["no profile", "не выбран", "belum dipilih", "wala pang profile"],
  filter_mode_title: ["Filter mode", "Режим фильтрации", "Mode filter", "Mode ng filter"],
  filter_mode_block: ["Block", "Блокировать", "Blokir", "Harangin"],
  filter_mode_delay: ["Delay", "Задерживать", "Tunda", "I-delay"],
  delay_label: ["Delay: %1$d ms", "Задержка: %1$d мс", "Penundaan: %1$d ms", "Delay: %1$d ms"],
  dodge_warning: [
    "Delaying is safer for your credit score than blocking. Blocking can cause a dodge penalty.",
    "Задержка безопаснее для кредитного счёта, чем блокировка. Блокировка может привести к dodge-штрафу.",
    "Menunda lebih aman untuk skor kredit daripada memblokir. Memblokir bisa memicu penalti dodge.",
    "Mas ligtas ang delay kaysa harangin para sa credit score. Puwedeng magdodge penalty ang pagharang.",
  ],
  phase_title: ["Phase", "Фаза", "Fase", "Yugto"],
  phase_idle: ["Idle", "Ожидание", "Siaga", "Naghihintay"],
  phase_queue: ["Queue", "Очередь", "Antrean", "Pila"],
  phase_match_found: ["Match found", "Матч найден", "Laga ditemukan", "Nakita ang laban"],
  phase_in_game: ["In game", "В игре", "Dalam laga", "Nasa laban"],
  diagnostics_title: ["Ping diagnostics", "Диагностика пинга", "Diagnosa ping", "Ping diagnostics"],
  diagnostics_run: ["Measure ping to all servers", "Замерить пинг до всех серверов", "Ukur ping ke semua server", "Sukatin ang ping sa lahat ng server"],
  diagnostics_running: ["Measuring…", "Замеряю…", "Mengukur…", "Sinusukat…"],
  diagnostics_fastest: ["Fastest", "Самые быстрые", "Tercepat", "Pinakamabilis"],
  diagnostics_blocked: ["Blocked by profile", "Заблокированы профилем", "Diblokir profil", "Hinarang ng profile"],
  diagnostics_empty: ["Press Measure to see the best servers", "Нажмите «Замерить», чтобы увидеть лучшие серверы", "Tekan Ukur untuk melihat server terbaik", "Pindutin ang Sukatin para makita ang pinakamagagandang server"],
  diagnostics_none: ["No server answered. Check the connection.", "Серверы не ответили. Проверьте соединение.", "Tidak ada server yang menjawab. Periksa koneksi.", "Walang server na sumagot. Tingnan ang koneksyon."],
  diagnostics_updated: ["Updated: %1$s", "Обновлено: %1$s", "Diperbarui: %1$s", "Na-update: %1$s"],
  diagnostics_sent: ["Ping sent to the website", "Пинг отправлен на сайт", "Ping dikirim ke situs", "Naipadala ang ping sa site"],
  low_ping_filter: ["High-ping only", "Только для высокого пинга", "Hanya untuk ping tinggi", "Para sa mataas na ping lang"],
  low_ping_hint: ["Heroes that forgive lag: tanks, AOE mages, supports", "Герои, которые прощают задержку: танки, AOE-маги, саппорты", "Hero yang memaafkan lag: tank, mage AOE, support", "Mga hero na pinapatawad ang lag: tank, AOE mage, support"],
  low_ping_badge: ["Lag-friendly", "Для лагов", "Ramah lag", "Lag-friendly"],
  high_ping_badge: ["Low ping required", "Нужен низкий пинг", "Butuh ping rendah", "Kailangan ng mababang ping"],
  auto_needs_ping: [
    "Collect ping in Monitor first so Auto can compare regions.",
    "Сначала соберите пинг в «Мониторе»: тогда авто-выбор сможет сравнить регионы.",
    "Kumpulkan ping di Monitor dulu agar Otomatis bisa membandingkan wilayah.",
    "Kolektahin muna ang ping sa Monitor para macompare ng Auto ang mga rehiyon.",
  ],
  settings_reliability_title: ["Reliability", "Надёжность", "Keandalan", "Pagkakaaasahan"],
  settings_reliability_subtitle: ["VPN in background and battery", "VPN в фоне и батарея", "VPN di latar dan baterai", "VPN sa background at baterya"],
  settings_battery_ok: ["Battery optimization is off", "Оптимизация батареи отключена", "Optimasi baterai mati", "Naka-off ang battery optimization"],
  settings_battery_bad: ["Battery optimization is on", "Оптимизация батареи включена", "Optimasi baterai aktif", "Naka-on ang battery optimization"],
  settings_battery_action: ["Turn off", "Отключить", "Matikan", "I-off"],
  settings_battery_settings: ["Settings", "Настройки", "Pengaturan", "Mga setting"],
  settings_battery_hint: [
    "On realme/OPPO also enable autostart and background activity, otherwise the system may kill the VPN during a match.",
    "На realme/OPPO также включите автозапуск и «Разрешить фоновую активность»: иначе система может убить VPN во время игры.",
    "Di realme/OPPO aktifkan juga autostart dan aktivitas latar, kalau tidak sistem bisa mematikan VPN saat bertanding.",
    "Sa realme/OPPO i-enable din ang autostart at background activity, kung hindi papatayin ng system ang VPN habang naglalaro.",
  ],
  settings_allow_login_title: ["Allow login pool", "Разрешать логин-пул", "Izinkan kumpulan login", "Payagan ang login pool"],
  settings_allow_login_desc: [
    "154.85.91.151 and the whole 154.85.91.0/24:10003. Without it some sessions cannot log in; the match filter gets weaker.",
    "154.85.91.151 и весь 154.85.91.0/24:10003. Без этого часть сессий не входит в игру; фильтр матч-серверов ослабляется.",
    "154.85.91.151 dan seluruh 154.85.91.0/24:10003. Tanpa ini sebagian sesi tidak bisa login; filter server jadi lebih lemah.",
    "154.85.91.151 at ang buong 154.85.91.0/24:10003. Kung wala ito, may sesyon na hindi makakalogin; humihina ang filter.",
  ],
  region_ranges_title: ["Region ranges", "Диапазоны регионов", "Rentang wilayah", "Mga range ng rehiyon"],
  region_ranges_desc: [
    "Known match servers: Far East is not filtered, Novosibirsk is delayed by the shaper.",
    "Известные матч-серверы: ДВ не фильтруются, Новосибирск задерживается шейпером.",
    "Server pertandingan yang dikenal: Timur Jauh tidak difilter, Novosibirsk ditunda oleh shaper.",
    "Kilalang match server: hindi fini-filter ang Far East, may delay ang Novosibirsk.",
  ],
  tun_stack_title: ["TUN stack", "Стек TUN", "Tumpukan TUN", "TUN stack"],
  tun_stack_system: ["system (faster)", "system (быстрее)", "system (lebih cepat)", "system (mas mabilis)"],
  tun_stack_gvisor: ["gvisor", "gvisor", "gvisor", "gvisor"],
  tun_stack_applied: ["TUN stack applied, rules reloaded", "Стек TUN применён — правила перезагружены", "Tumpukan TUN diterapkan, aturan dimuat ulang", "Na-apply ang TUN stack, ni-reload ang rules"],
  tun_stack_hint: [
    "If in-game ping is higher than on a PC, switch to system: it stresses Wi-Fi less.",
    "Если пинг в игре выше, чем на ПК, переключите на system: он меньше нагружает Wi-Fi.",
    "Kalau ping di laga lebih tinggi dari PC, ganti ke system: lebih ringan untuk Wi-Fi.",
    "Kung mas mataas ang ping sa laro kaysa sa PC, ilipat sa system: mas magaan sa Wi-Fi.",
  ],
  settings_language_title: ["Interface language", "Язык интерфейса", "Bahasa antarmuka", "Wika ng interface"],
  settings_language_subtitle: ["Russian, English, Indonesia, Filipino", "Русский, English, Indonesia, Filipino", "Rusia, Inggris, Indonesia, Filipino", "Russian, English, Indonesia, Filipino"],
  settings_language_hint: [
    "System follows your phone. The change applies immediately; the VPN is not restarted.",
    "Системный — язык телефона. Выбор применяется сразу, VPN не перезапускается.",
    "Sistem mengikuti bahasa ponsel. Perubahan langsung berlaku; VPN tidak di-restart.",
    "Sinusundan ng System ang wika ng phone. Agad na nag-aapply; hindi nagre-restart ang VPN.",
  ],
  dev_trace_title: ["Debug trace", "Debug trace", "Debug trace", "Debug trace"],
  dev_trace_desc: [
    "Log MLBB contacts: IP:port, phase, protocol. Packet contents are never stored.",
    "Логировать контакты MLBB-трафика: IP:порт, фаза, протокол. Содержимое пакетов не сохраняется.",
    "Catat koneksi MLBB: IP:port, fase, protokol. Isi paket tidak disimpan.",
    "I-log ang MLBB contacts: IP:port, yugto, protocol. Hindi itinatago ang nilalaman ng packet.",
  ],
  dev_trace_export: ["Export log (JSON)", "Экспорт лога (JSON)", "Ekspor log (JSON)", "I-export ang log (JSON)"],
  dev_hypothesis: ["Check hypothesis", "Проверить гипотезу", "Uji hipotesis", "Subukan ang hypothesis"],
  hypothesis_running: ["Collecting 60 s…", "Сбор 60 с…", "Mengumpulkan 60 dtk…", "Nangongolekta 60 s…"],
  hypothesis_not_running: ["Turn on blocking and start a match search first.", "Сначала включите блокировку и начните поиск матча.", "Nyalakan pemblokiran dan mulai cari laga dulu.", "I-on muna ang pagharang at maghanap ng laban."],
  trace_exported: ["Log exported", "Лог экспортирован", "Log diekspor", "Na-export ang log"],
  trace_export_failed: ["Could not export the log", "Не удалось экспортировать лог", "Gagal mengekspor log", "Hindi ma-export ang log"],
};

const langs = ["en", "id", "fil"];
const index = { en: 0, ru: 1, id: 2, fil: 3 };

function escape(value) {
  return value
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;");
}

let report = [];
for (const lang of langs) {
  const dir = path.join(root, `values-${lang}`);
  fs.mkdirSync(dir, { recursive: true });
  const lines = [];
  for (const [key, values] of Object.entries(T)) {
    lines.push(`    <string name="${key}">${escape(values[index[lang]])}</string>`);
  }
  const target = path.join(dir, "strings.xml");
  const header = `<?xml version="1.0" encoding="utf-8"?>\n<resources>\n    <!-- ${lang}: translated core UI. Missing keys fall back to values/strings.xml (Russian). -->\n`;
  fs.writeFileSync(target, header + lines.join("\n") + "\n</resources>\n", "utf8");
  report.push(`${lang}: ${Object.keys(T).length} strings -> ${target}`);
}

// Missing keys per language (still in Russian).
const baseKeys = new Set([...base.matchAll(/<string name="([^"]+)"/g)].map((m) => m[1]));
for (const lang of langs) {
  const translated = new Set(Object.keys(T).filter((key) => T[key][index[lang]] !== T[key][1] || lang === "en"));
  const missing = [...baseKeys].filter((key) => !translated.has(key));
  report.push(`${lang}: missing ${missing.length} of ${baseKeys.size} (fall back to Russian)`);
}

console.log(report.join("\n"));
