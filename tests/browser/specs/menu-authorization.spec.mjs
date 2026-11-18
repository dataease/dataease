import { test, expect } from "@playwright/test";
import { fileURLToPath } from "node:url";
const url = new URL("../output/menu-authorization/index.html", import.meta.url)
  .href;

test("菜单无授权权时不能编辑，有授权权时可以编辑", async ({ page }) => {
  await page.goto(url);
  await expect(page.locator("input[type=checkbox]")).toHaveCount(0);
  await page.evaluate(() => {
    window.fixture.row.disabled = false;
  });
  await page.getByRole("checkbox").check();
  expect(await page.evaluate(() => window.fixture.result().writes)).toBe(1);
});

test("按资源选择排除无授权权菜单", async ({ page }) => {
  await page.goto(url);
  const result = await page.evaluate(async () => {
    const denied = { id: "denied", leaf: true, disabled: true };
    const allowed = { id: "next", leaf: true, disabled: false };
    const first = window.fixture.first([{ children: [denied, allowed] }], true);
    await window.fixture.select(denied);
    const before = window.fixture.result();
    await window.fixture.select(allowed);
    return { first: first.id, before, after: window.fixture.result() };
  });
  expect(result.first).toBe("next");
  expect(result.before.loaded).toBe(0);
  expect(result.before.selected).toBe("allowed");
  expect(result.after.loaded).toBe(1);
  expect(result.after.selected).toBe("next");
});
