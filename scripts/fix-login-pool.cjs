const fs = require("fs");
const path = "app/src/main/assets/rules/rules.json";
const j = JSON.parse(fs.readFileSync(path, "utf8"));

const prof = j.profiles.find((p) => p.id === "far_east");
// Логин/матчмейкинг идёт в московский пул 154.85.91.0/24 на порту 10003,
// но часть служебных соединений идёт на 30021/30071 — профиль их не должен резать.
// Allow-лист профиля оставляем, блок-лист теперь НЕ содержит 154.85.91.0/24:
// его обрабатывает отдельное правило login-pool (direct) до блокировок.
const filtered = prof.rules.filter((r) => r[0] !== "154.85.91.0/24");
if (filtered.length === prof.rules.length) {
  console.error("154.85.91.0/24 not present, nothing to do");
}
prof.rules = filtered;
j.updatedAt = "2026-11-11";
fs.writeFileSync(path, JSON.stringify(j, null, 2) + "\n");
console.log("far_east rules:", JSON.stringify(prof.rules.map((r) => r[0])));
