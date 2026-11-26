import { test, expect } from "@playwright/test";
test("embedded directive setup renders plugin SVG without main app registration", async ({
  page,
}) => {
  const missing = [];
  page.on("console", (message) => {
    if (message.text().includes("Failed to resolve directive"))
      missing.push(message.text());
  });
  await page.goto("/");
  await expect(page.locator("#app svg path")).toHaveAttribute("fill", "blue");
  expect(missing).toEqual([]);
});
test("plugin SVG updates retain sanitization", async ({ page }) => {
  await page.goto("/");
  await expect(page.locator("#app svg")).toHaveCount(1);
  await page.evaluate(() =>
    window.setIcon(
      '<svg onload="window.pluginExecuted=true"><script>window.pluginExecuted=true</script><path d="M0 0L8 8" onclick="window.pluginExecuted=true"/></svg>'
    )
  );
  await expect(page.locator("#app svg path")).toHaveAttribute("d", "M0 0L8 8");
  await expect(
    page.locator("#app script, #app [onload], #app [onclick]")
  ).toHaveCount(0);
  expect(await page.evaluate(() => window.pluginExecuted)).toBeUndefined();
});
