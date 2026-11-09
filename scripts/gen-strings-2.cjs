const fs = require("fs");

// Дополнительные переводы (второй проход): все оставшиеся ключи базового strings.xml.
const MORE = {
  discovery_title: ["Server discovery", "Обнаружение серверов", "Penemuan server", "Pagtuklas ng server"],
  discovery_hint: [
    "Turn blocking on, open MLBB and see which IPs the game connects to. Moscow servers can be added to the blocklist.",
    "Включите блокировку, зайдите в MLBB и посмотрите, к каким IP подключается игра. Московские серверы можно добавить в блоклист.",
    "Nyalakan pemblokiran, buka MLBB dan lihat IP mana yang dihubungi game. Server Moskow bisa ditambahkan ke daftar blokir.",
    "I-on ang pagharang, buksan ang MLBB at tingnan kung aling IP ang kinokonektahan ng laro. Puwedeng idagdag sa blocklist ang Moscow servers.",
  ],
  notification_channel: ["MLBB blocking", "Блокировка MLBB", "Pemblokiran MLBB", "Pagharang sa MLBB"],
  notification_title: ["MLBB Network Doctor is active", "MLBB Network Doctor активен", "MLBB Network Doctor aktif", "Aktibo ang MLBB Network Doctor"],
  notification_text: ["Blocking %1$d rules", "Блокируется %1$d правил", "Memblokir %1$d aturan", "Hinaharangan ang %1$d patakaran"],
  notification_stop: ["Turn off", "Выключить", "Matikan", "I-off"],
  blocked_notification_title: [
    "MLBB is trying to reach a blocked server",
    "MLBB пытается подключиться к заблокированному серверу",
    "MLBB mencoba menghubungi server yang diblokir",
    "Sinusubukan ng MLBB na kumonekta sa hinarang na server",
  ],
  blocked_notification_text: [
    "%1$s:%2$d · tap Allow if this is a match server",
    "%1$s:%2$d · нажмите «Разрешить», если это матч-сервер",
    "%1$s:%2$d · ketuk Izinkan jika ini server pertandingan",
    "%1$s:%2$d · i-tap ang Payagan kung match server ito",
  ],
  blocked_notification_allow: ["Allow 5 min", "Разрешить 5 мин", "Izinkan 5 mnt", "Payagan 5 min"],
  error_mlbb_missing: [
    "Mobile Legends is not installed (com.mobile.legends / com.mobilelegends.mi).",
    "Mobile Legends не установлена (com.mobile.legends / com.mobilelegends.mi).",
    "Mobile Legends tidak terpasang (com.mobile.legends / com.mobilelegends.mi).",
    "Hindi naka-install ang Mobile Legends (com.mobile.legends / com.mobilelegends.mi).",
  ],
  error_vpn_permission: ["VPN permission was not granted.", "Разрешение VPN не выдано.", "Izin VPN tidak diberikan.", "Hindi naibigay ang pahintulot sa VPN."],
  error_empty_blocklist: [
    "Blocklist is empty. Turn on discovery and add the Moscow server IPs.",
    "Блоклист пуст. Включите обнаружение и добавьте IP московских серверов.",
    "Daftar blokir kosong. Nyalakan penemuan dan tambahkan IP server Moskow.",
    "Walang laman ang blocklist. I-on ang discovery at idagdag ang IP ng Moscow servers.",
  ],
  add_rule: ["Add rule", "Добавить правило", "Tambah aturan", "Magdagdag ng patakaran"],
  add_ip: ["Block IP", "Заблокировать IP", "Blokir IP", "Harangin ang IP"],
  add_subnet: ["Block /24", "Заблокировать /24", "Blokir /24", "Harangin ang /24"],
  delete: ["Delete", "Удалить", "Hapus", "Burahin"],
  save: ["Save", "Сохранить", "Simpan", "I-save"],
  cancel: ["Cancel", "Отмена", "Batal", "Kanselahin"],
  import_list: ["Import list", "Импорт списка", "Impor daftar", "I-import ang listahan"],
  export_list: ["Export list", "Экспорт списка", "Ekspor daftar", "I-export ang listahan"],
  clear: ["Clear", "Очистить", "Bersihkan", "Linisin"],
  no_connections: [
    "No connections yet. Turn on blocking and start the game.",
    "Пока нет подключений. Включите блокировку и запустите игру.",
    "Belum ada koneksi. Nyalakan pemblokiran dan jalankan game.",
    "Wala pang koneksyon. I-on ang pagharang at simulan ang laro.",
  ],
  no_rules: ["Blocklist is empty", "Блоклист пуст", "Daftar blokir kosong", "Walang laman ang blocklist"],
  cidr_hint: ["IP or CIDR, e.g. 1.2.3.4 or 1.2.3.0/24", "IP или CIDR, например 1.2.3.4 или 1.2.3.0/24", "IP atau CIDR, mis. 1.2.3.4 atau 1.2.3.0/24", "IP o CIDR, hal. 1.2.3.4 o 1.2.3.0/24"],
  invalid_cidr: ["Invalid IP/CIDR", "Некорректный IP/CIDR", "IP/CIDR tidak valid", "Hindi valid na IP/CIDR"],
  rule_added: ["Rule added", "Правило добавлено", "Aturan ditambahkan", "Naidagdag ang patakaran"],
  rules_added: ["Rules added: %1$d", "Добавлено правил: %1$d", "Aturan ditambahkan: %1$d", "Mga patakarang naidagdag: %1$d"],
  rules_not_added: ["No new rules found", "Новые правила не найдены", "Tidak ada aturan baru", "Walang bagong patakaran"],
  blocklist_cleared: ["Blocklist cleared", "Блоклист очищен", "Daftar blokir dibersihkan", "Nalinis ang blocklist"],
  connections_cleared: ["Connection log cleared", "Журнал подключений очищен", "Log koneksi dibersihkan", "Nalinis ang log ng koneksyon"],
  exported: ["List copied to clipboard", "Список скопирован в буфер обмена", "Daftar disalin ke clipboard", "Nakopya sa clipboard ang listahan"],
  preset_added: ["Preset «%1$s»: added %2$d", "Пресет «%1$s»: добавлено %2$d", "Preset «%1$s»: ditambah %2$d", "Preset «%1$s»: naidagdag %2$d"],
  stabilization_on: ["Stabilization on: distant servers blocked", "Стабилизация включена: дальние серверы заблокированы", "Stabilisasi aktif: server jauh diblokir", "Naka-on ang stabilization: hinarang ang malalayong server"],
  stabilization_off: ["Stabilization off", "Стабилизация выключена", "Stabilisasi mati", "Naka-off ang stabilization"],
  region_enabled: ["Profile «%1$s» enabled", "Профиль «%1$s» включён", "Profil «%1$s» aktif", "Naka-enable ang profile «%1$s»"],
  region_disabled: ["Profile «%1$s» disabled", "Профиль «%1$s» выключен", "Profil «%1$s» dimatikan", "Naka-disable ang profile «%1$s»"],
  only_mlbb: ["Mobile Legends only", "Только Mobile Legends", "Hanya Mobile Legends", "Mobile Legends lang"],
  only_mlbb_desc: [
    "All other phone traffic goes direct and is untouched.",
    "Весь остальной трафик телефона идёт напрямую и не затрагивается.",
    "Semua trafik ponsel lain langsung dan tidak tersentuh.",
    "Lahat ng ibang traffic ng phone ay direkta at walang galaw.",
  ],
  core_log: ["Core log", "Журнал ядра", "Log inti", "Log ng core"],
  discovery_mode: ["Discovery mode", "Режим обнаружения", "Mode penemuan", "Discovery mode"],
  discovery_mode_desc: [
    "Do not block rules, only record connections. Turn off after collecting IPs.",
    "Не блокировать правила, только записывать подключения. Выключите после сбора IP.",
    "Jangan blokir aturan, catat koneksi saja. Matikan setelah mengumpulkan IP.",
    "Huwag harangin ang mga patakaran, itala lang ang koneksyon. I-off pagkatapos mangolekta ng IP.",
  ],
  settings_info: [
    "The app uses Android VpnService as a local filter. There is no remote VPN server and traffic is not routed to another region. Blocking IPs does not guarantee a region change.",
    "Приложение использует Android VpnService как локальный фильтр. Удалённого VPN-сервера нет, трафик не перенаправляется в другой регион. Блокировка IP не гарантирует смену игрового региона.",
    "Aplikasi memakai Android VpnService sebagai filter lokal. Tidak ada server VPN jauh dan trafik tidak dialihkan ke wilayah lain. Memblokir IP tidak menjamin perubahan wilayah.",
    "Ginagamit ng app ang Android VpnService bilang lokal na filter. Walang malayong VPN server at hindi inililipat ang traffic sa ibang rehiyon.",
  ],
  match_channel_name: ["Match found", "Найденный матч", "Laga ditemukan", "Nakita ang laban"],
  match_found_title: ["Match found", "Матч найден", "Laga ditemukan", "Nakita ang laban"],
  match_found_text: ["%1$s · %2$s · %3$s", "%1$s · %2$s · %3$s", "%1$s · %2$s · %3$s", "%1$s · %2$s · %3$s"],
  match_found_allowed: ["allowed", "разрешён", "diizinkan", "pinayagan"],
  match_found_blocked: ["blocked", "заблокирован", "diblokir", "hinarang"],
  match_notification_open: ["Open", "Открыть", "Buka", "Buksan"],
  match_notification_block: ["Block", "Заблокировать", "Blokir", "Harangin"],
  settings_stable_alerts_title: ["Stable connections only", "Только стабильные соединения", "Hanya koneksi stabil", "Stable connections lang"],
  settings_stable_alerts_desc: [
    "Show the allowed-server card only if the connection lasts 6 seconds. Blocked servers are always shown.",
    "Показывать карточку разрешённого сервера, только если подключение держится 6 секунд. Заблокированные серверы показываются всегда.",
    "Tampilkan kartu server yang diizinkan hanya jika koneksi bertahan 6 detik. Server yang diblokir selalu ditampilkan.",
    "Ipakita ang card ng pinayagang server kung 6 segundo tumatagal ang koneksyon. Laging ipinapakita ang hinarang na server.",
  ],
  match_alert_settings_title: ["Match card", "Карточка матча", "Kartu laga", "Match card"],
  match_alert_settings_desc: [
    "Show the server and ping over the game when MLBB connects to a match server.",
    "Показывать сервер и пинг поверх игры, когда MLBB подключается к матч-серверу.",
    "Tampilkan server dan ping di atas game saat MLBB terhubung ke server pertandingan.",
    "Ipakita ang server at ping sa ibabaw ng laro kapag kumonekta ang MLBB sa match server.",
  ],
  match_overlay_allowed: ["Over the game: allowed", "Поверх игры: разрешено", "Di atas game: diizinkan", "Sa ibabaw ng laro: pinayagan"],
  match_overlay_denied: [
    "Over the game: not allowed — a regular notification will be shown.",
    "Поверх игры: не разрешено — покажем обычное уведомление.",
    "Di atas game: tidak diizinkan — notifikasi biasa akan muncul.",
    "Sa ibabaw ng laro: hindi pinayagan — regular na notification ang lalabas.",
  ],
  match_overlay_allow: ["Allow display over the game", "Разрешить показ поверх игры", "Izinkan tampil di atas game", "Payagan ang display sa ibabaw ng laro"],
  match_overlay_change: ["Change permission", "Изменить разрешение", "Ubah izin", "Palitan ang pahintulot"],
  match_overlay_test: ["Test the card", "Проверить карточку", "Uji kartu", "Subukan ang card"],
  match_overlay_test_hint: [
    "The card appears over the current screen. During a match you can close it or allow the server for 5 minutes.",
    "Карточка покажется поверх текущего экрана. Во время матча её можно закрыть или разрешить сервер на 5 минут.",
    "Kartu muncul di atas layar saat ini. Saat bertanding kamu bisa menutupnya atau mengizinkan server 5 menit.",
    "Lalabas ang card sa ibabaw ng kasalukuyang screen. Puwede itong isara o payagan ang server ng 5 minuto.",
  ],
  match_alert_off: ["Match notifications are off.", "Уведомления о матче выключены.", "Notifikasi laga dimatikan.", "Naka-off ang notifications ng laban."],
  match_alert_demo_denied: [
    "No permission for overlays and notifications.",
    "Нет разрешения на показ поверх игры и уведомления.",
    "Tidak ada izin untuk overlay dan notifikasi.",
    "Walang pahintulot para sa overlay at notifications.",
  ],
  update_channel_name: ["App updates", "Обновления приложения", "Pembaruan aplikasi", "Mga update ng app"],
  update_notification_title: ["Version %1$s is available", "Доступна версия %1$s", "Versi %1$s tersedia", "May bersyon %1$s na"],
  update_notification_text: [
    "A new MLBB Network Doctor build is on GitHub. Tap to download the APK.",
    "Новая версия MLBB Network Doctor уже на GitHub. Нажмите, чтобы скачать APK.",
    "Versi baru MLBB Network Doctor sudah di GitHub. Ketuk untuk mengunduh APK.",
    "May bagong MLBB Network Doctor sa GitHub. I-tap para i-download ang APK.",
  ],
  update_available_title: ["Version %1$s is available", "Доступна новая версия %1$s", "Versi %1$s tersedia", "May bersyon %1$s na"],
  update_available_text: [
    "Download the fresh APK from GitHub. Current version: %1$s.",
    "Скачайте свежий APK с GitHub. Текущая версия: %1$s.",
    "Unduh APK terbaru dari GitHub. Versi saat ini: %1$s.",
    "I-download ang bagong APK sa GitHub. Kasalukuyang bersyon: %1$s.",
  ],
  update_download: ["Download APK", "Скачать APK", "Unduh APK", "I-download ang APK"],
  update_release_page: ["Release on GitHub", "Релиз на GitHub", "Rilis di GitHub", "Release sa GitHub"],
  notification_channel_status: ["Protection status", "Состояние защиты", "Status perlindungan", "Status ng proteksyon"],
  notification_interrupted_title: ["Blocking stopped", "Блокировка остановлена", "Pemblokiran berhenti", "Huminto ang pagharang"],
  notification_interrupted_restart: ["Turn on", "Включить", "Nyalakan", "I-on"],
  notification_update_stopped: [
    "The update stopped blocking. Tap Turn on to restore protection.",
    "Обновление остановило блокировку. Нажмите «Включить», чтобы вернуть защиту.",
    "Pembaruan menghentikan pemblokiran. Ketuk Nyalakan untuk memulihkan proteksi.",
    "Inihinto ng update ang pagharang. I-tap ang I-on para ibalik ang proteksyon.",
  ],
  notification_update_permission_lost: [
    "The update stopped blocking: VPN permission was revoked. Open the app and turn it on again.",
    "Обновление остановило блокировку: разрешение VPN отозвано. Откройте приложение и включите её заново.",
    "Pembaruan menghentikan pemblokiran: izin VPN dicabut. Buka aplikasi dan nyalakan lagi.",
    "Inihinto ng update ang pagharang: binawi ang pahintulot sa VPN. Buksan ang app at i-on muli.",
  ],
  health_service_stopped_title: ["Protection is off", "Защита выключена", "Proteksi mati", "Naka-off ang proteksyon"],
  health_service_stopped_text: [
    "Blocking was active but the VPN service stopped. Turn it on again, otherwise match servers go direct.",
    "Блокировка работала, но VPN-сервис остановился. Включите её снова, иначе матч-серверы проходят напрямую.",
    "Pemblokiran aktif tetapi layanan VPN berhenti. Nyalakan lagi, kalau tidak server pertandingan lewat langsung.",
    "Aktibo ang pagharang pero huminto ang VPN service. I-on muli, kung hindi direkta ang match servers.",
  ],
  health_geoip_failed_title: ["GeoIP is not working", "GeoIP не работает", "GeoIP tidak berfungsi", "Hindi gumagana ang GeoIP"],
  health_geoip_failed_text: [
    "The city database cannot be opened, so servers have no names. Try restoring the database.",
    "База городов не открывается, поэтому у серверов нет названий. Попробуйте восстановить базу.",
    "Basis kota tidak bisa dibuka, jadi server tidak punya nama. Coba pulihkan basisnya.",
    "Hindi mabuksan ang city database, kaya walang pangalan ang servers. Subukang ibalik ito.",
  ],
  health_rules_failed_title: ["Rules failed to load", "Правила не загрузились", "Aturan gagal dimuat", "Hindi na-load ang rules"],
  health_rules_failed_text: [
    "The built-in rule set is used. Check the rules update in settings.",
    "Используется встроенный набор правил. Проверьте обновление правил в настройках.",
    "Memakai aturan bawaan. Periksa pembaruan aturan di pengaturan.",
    "Ginagamit ang built-in na rules. Tingnan ang update ng rules sa settings.",
  ],
  health_fix: ["Fix", "Исправить", "Perbaiki", "Ayusin"],
  unknown_port_title: ["Unknown port %1$d", "Незнакомый порт %1$d", "Port tidak dikenal %1$d", "Hindi kilalang port %1$d"],
  unknown_port_text: [
    "MLBB connected to %1$s:%2$d. Is it a match server, a service port or extra traffic?",
    "MLBB подключилась к %1$s:%2$d. Это матч-сервер, сервисный порт или лишний трафик?",
    "MLBB tersambung ke %1$s:%2$d. Ini server pertandingan, port layanan, atau trafik lain?",
    "Kumonekta ang MLBB sa %1$s:%2$d. Match server ba ito, service port, o ibang traffic?",
  ],
  unknown_port_match: ["Match port", "Матч-порт", "Port pertandingan", "Match port"],
  unknown_port_service: ["Service", "Сервисный", "Layanan", "Service"],
  unknown_port_ignore: ["Ignore", "Игнорировать", "Abaikan", "Balewalain"],
  unknown_port_added_match: ["Port %1$d is now a match port", "Порт %1$d теперь считается матч-портом", "Port %1$d sekarang dianggap port pertandingan", "Match port na ang port %1$d"],
  unknown_port_added_service: ["Port %1$d marked as service", "Порт %1$d помечен как сервисный", "Port %1$d ditandai layanan", "Minarkahan ang port %1$d bilang service"],
  settings_unknown_ports_title: ["Ask about new ports", "Спрашивать о новых портах", "Tanya tentang port baru", "Magtanong sa bagong ports"],
  settings_unknown_ports_desc: [
    "If MLBB starts using a port outside the known set, a notification asks: match port / service / ignore.",
    "Если MLBB начнёт использовать порт вне известного набора, придёт уведомление с выбором «матч-порт / сервисный / игнорировать».",
    "Kalau MLBB mulai memakai port di luar daftar, muncul notifikasi: port pertandingan / layanan / abaikan.",
    "Kung gagamit ang MLBB ng port sa labas ng kilala, may notification: match port / service / balewalain.",
  ],
  rules_settings_title: ["Server rules", "Правила с сервера", "Aturan dari server", "Mga rules mula sa server"],
  rules_settings_desc: [
    "Signed set of ports, profiles and servers. Checked once a day; on error the previous rules stay.",
    "Подписанный набор портов, профилей и серверов. Проверяется раз в сутки; при ошибке остаются прежние правила.",
    "Kumpulan port, profil, dan server bertanda tangan. Dicek sekali sehari; jika gagal, aturan lama tetap dipakai.",
    "Signadong set ng ports, profiles at servers. Sinusuri isang beses sa isang araw; kapag may error, nananatili ang dating rules.",
  ],
  rules_check_now: ["Check now", "Проверить сейчас", "Periksa sekarang", "Suriin ngayon"],
  rules_checking: ["Checking…", "Проверка…", "Memeriksa…", "Sinusuri…"],
  rules_source_builtin: ["built-in", "встроенные", "bawaan", "built-in"],
  rules_source_asset: ["from APK", "из APK", "dari APK", "mula sa APK"],
  rules_source_cached: ["saved", "сохранённые", "tersimpan", "naka-save"],
  rules_source_remote: ["from GitHub", "с GitHub", "dari GitHub", "mula sa GitHub"],
  rules_status_line: ["Source: %1$s · version %2$s", "Источник: %1$s · версия %2$s", "Sumber: %1$s · versi %2$s", "Source: %1$s · bersyon %2$s"],
  rules_updated: ["Rules updated: %1$s", "Правила обновлены: %1$s", "Aturan diperbarui: %1$s", "Na-update ang rules: %1$s"],
  rules_up_to_date: ["Rules are up to date", "Правила актуальны", "Aturan sudah terbaru", "Up to date ang rules"],
  rules_rejected: ["Rules rejected: %1$s", "Правила отклонены: %1$s", "Aturan ditolak: %1$s", "Tinanggihan ang rules: %1$s"],
  rules_network_error: ["Could not check rules: %1$s", "Не удалось проверить правила: %1$s", "Gagal memeriksa aturan: %1$s", "Hindi masuri ang rules: %1$s"],
  settings_privacy_title: ["Privacy", "Приватность", "Privasi", "Privacy"],
  settings_privacy_subtitle: ["Anonymous statistics", "Анонимная статистика", "Statistik anonim", "Anonymous na statistics"],
  settings_analytics_title: ["Send anonymous statistics", "Отправлять анонимную статистику", "Kirim statistik anonim", "Magpadala ng anonymous na statistics"],
  settings_analytics_desc: [
    "We collect only anonymous events: which profiles you pick, how often you open meta and enable the VPN. No personal data and no traffic content.",
    "Мы собираем только анонимные события: какие профили вы выбираете, сколько раз открываете мету и включаете VPN. Личные данные и содержимое трафика мы не собираем.",
    "Kami hanya mengumpulkan event anonim: profil yang dipilih, seberapa sering membuka meta dan mengaktifkan VPN. Tanpa data pribadi dan isi trafik.",
    "Anonymous na events lang: kung aling profile ang pipiliin, gaano kadalas buksan ang meta at i-on ang VPN. Walang personal data at nilalaman ng traffic.",
  ],
  region_select: ["Region", "Регион", "Wilayah", "Rehiyon"],
};

const index = { en: 0, ru: 1, id: 2, fil: 3 };
const root = "D:/mlbb blocker/app/src/main/res";

function escape(value) {
  return value.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}

for (const lang of ["en", "id", "fil"]) {
  const file = `${root}/values-${lang}/strings.xml`;
  let text = fs.readFileSync(file, "utf8");
  const known = new Set([...text.matchAll(/<string name="([^"]+)"/g)].map((m) => m[1]));
  const lines = [];
  for (const [key, values] of Object.entries(MORE)) {
    if (known.has(key)) continue;
    lines.push(`    <string name="${key}">${escape(values[index[lang]])}</string>`);
  }
  text = text.replace("</resources>", lines.join("\n") + "\n</resources>");
  fs.writeFileSync(file, text, "utf8");
  console.log(`${lang}: +${lines.length}`);
}
