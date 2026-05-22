import { compile } from "handlebars";
import { readdir } from "node:fs/promises";

const versions = ["v1.2", "v1.3", "v1.4"];
const latest = "v1.3";

async function generate(file: string) {
  const input = Bun.file(`templates/${file}`);
  const output = Bun.file(`generated/${file}`);

  const template = compile(await input.text());

  await output.write(template({ versions, latest }));

  console.info("generated", file);
}

async function generateVersionJson() {
  const output = Bun.file("generated/versions.json");

  const json = versions.map((it) => ({
    version: it === latest ? "latest" : it,
    title: it.substring(1),
    aliases: it === latest ? ["latest"] : [],
  }));

  await output.write(JSON.stringify(json, null, 2));

  console.info("generated versions.json");
}

const templates = await readdir("templates");

await Promise.all([...templates.map(generate), generateVersionJson()]);
