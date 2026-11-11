const fs = require("fs");

const additions = {
  dev_log_title: [
    "Application log",
    "Журнал приложения",
    "Log aplikasi",
    "Log ng app",
  ],
  dev_log_desc: [
    "Always written: launch, login, connections, blocks, errors. doctor.log in app storage, rotated at 2 MB.",
    "Пишется всегда: запуск, вход, соединения, блокировки, ошибки. Файл doctor.log во внутренней памяти, ротация по 2 МБ.",
    "Selalu ditulis: peluncuran, login, koneksi, blokir, galat. doctor.log di penyimpanan aplikasi, rotasi 2 MB.",
    "Laging nakasulat: paglunsad, login, koneksyon, pagharang, errors. doctor.log sa storage ng app, rotation sa 2 MB.",
  ],
  dev_log_export: [
    "Save log",
    "Сохранить журнал",
    "Simpan log",
    "I-save ang log",
  ],
  dev_log_exported: [
    "Log saved",
    "Журнал сохранён",
    "Log disimpan",
    "Nai-save ang log",
  ],
};

const index = { en: 0, id: 2, fil: 3 };
for (const lang of ["en", "id", "fil"]) {
  const file = `app/src/main/res/values-${lang}/strings.xml`;
  let text = fs.readFileSync(file, "utf8");
  const add = [];
  for (const [key, values] of Object.entries(additions)) {
    if (text.includes(`name="${key}"`)) continue;
    add.push(`    <string name="${key}">${values[index[lang]]}</string>`);
  }
  text = text.replace("</resources>", add.join("\n") + "\n</resources>");
  fs.writeFileSync(file, text);
  console.log(`${lang}: +${add.length}`);
}
