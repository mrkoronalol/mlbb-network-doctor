const fs = require("fs");

const ru = fs.readFileSync("D:/mlbb blocker/app/src/main/res/values/strings.xml", "utf8");
const en = fs.readFileSync("D:/mlbb blocker/app/src/main/res/values-en/strings.xml", "utf8");

const entries = [...ru.matchAll(/<string name="([^"]+)">([\s\S]*?)<\/string>/g)].map((m) => ({
  key: m[1],
  ru: m[2],
}));
const done = new Set([...en.matchAll(/<string name="([^"]+)"/g)].map((m) => m[1]));
const missing = entries.filter((e) => !done.has(e.key));
console.log(`missing: ${missing.length}`);
for (const e of missing) {
  console.log(`${e.key}\t${e.ru}`);
}
