const { test, expect } = require("@playwright/test");
const {
  button,
  openAssistant,
  ask,
  serveAnswers,
} = require("../fixtures/dataset-editor/reference.cjs");

test("switch from a suggestion to the edited SQL and keep that exact basis on the answer", async ({
  page,
}) => {
  const openings = await serveAnswers(page, [
    { type: "SQL_SUGGESTION", text: "已生成", sql: "SELECT 2" },
    { type: "EXPLANATION", text: "已解释" },
    { type: "EXPLANATION", text: "已另写" },
  ]);
  const panel = await openAssistant(page, "SQL 助手");
  await ask(panel, "生成月度汇总");
  const source = panel.getByRole("combobox", { name: "本轮引用来源" });
  await source.selectOption(
    { label: "建议 #1 · 生成月度汇总" },
    { timeout: 4000 }
  );
  const editor = page.locator(".sql-drawer-fullscreen:visible .cm-content");
  const sql =
    "SELECT\n" +
    Array.from({ length: 24 }, (_, i) => "  order_id AS col_" + i).join(",\n") +
    "\nFROM orders";
  await editor.fill(sql);
  await source.selectOption({ label: "编辑器当前 SQL" });
  await button(panel, "查看 SQL").press("Enter");
  expect(await panel.locator("footer pre").textContent()).toBe(sql);
  await ask(panel, "解释这次修改");
  const basis = panel
    .getByRole("article")
    .nth(1)
    .locator("details")
    .filter({ hasText: "生成依据" });
  const snapshot = basis.locator("pre");
  await expect(snapshot).toBeHidden();
  await basis.locator("summary").press("Enter");
  await expect(basis.getByText(/Fixture MySQL/)).toBeVisible();
  await snapshot.focus();
  await expect(snapshot).toBeFocused();
  await snapshot.press("End");
  await expect.poll(() => snapshot.evaluate((el) => el.scrollTop)).toBeGreaterThan(0);
  await editor.fill("SELECT 42");
  expect(await snapshot.textContent()).toBe(sql);
  expect(openings[1].sql).toBe(sql);
  await source.selectOption({ label: "不引用 SQL" });
  await expect(button(panel, "查看 SQL")).toBeHidden();
  await ask(panel, "请解释表结构");
  expect(openings[2]).toMatchObject({ sql: "", previousSessionId: "s2" });
});

test("a long SQL question identifies the same suggestion in the card, source menu and preview", async ({
  page,
}) => {
  const question =
    "请统计👩🏽‍💻团队各月每个渠道新增用户的订单数量     并保留最后十一个完整字符";
  const caption = "建议 #1 · 请统计👩🏽‍💻团队各月…保留最后十一个完整字符";
  await serveAnswers(page, [
    {
      type: "SQL_SUGGESTION",
      sql: "SELECT order_id\n  FROM orders",
      text: "已生成查询。",
    },
    { type: "EXPLANATION", text: "后续说明。\n".repeat(90) },
  ]);
  const panel = await openAssistant(page, "SQL 助手");
  await ask(panel, question);
  const first = panel.getByRole("article").first();
  await expect(first.getByText(caption, { exact: true })).toBeVisible({
    timeout: 3000,
  });
  const sources = panel.getByRole("combobox", { name: "本轮引用来源" });
  await sources.selectOption({ label: "不引用 SQL" });
  await ask(panel, "说明字段");
  const original = first.getByText(question, { exact: true });
  await panel.getByRole("textbox").fill("还要分组");
  await sources.selectOption({ label: caption });
  await expect(sources.getByRole("option", { name: /^建议 #2/ })).toHaveCount(
    0
  );
  await button(panel, "查看 SQL").click();
  const footer = panel.locator(".composer");
  await expect(footer.getByRole("strong")).toHaveText(caption);
  await expect(original).not.toBeInViewport();
  await footer.getByRole("button", { name: /定位到建议 #1/ }).click();
  await expect(original).toBeInViewport({ ratio: 1 });
  await expect(panel.getByRole("textbox")).toHaveValue("还要分组");
  await expect(sources).toHaveValue("1");
});

for (const [assistant, view, editorSelector] of [
  ["SQL 助手", "查看 SQL", ".sql-drawer-fullscreen:visible .cm-content"],
  ["计算字段助手", "查看公式", ".calc-field-drawer .cm-content"],
]) {
  test(
    assistant +
      " keeps the reference controls on one line and long code readable in both palettes",
    async ({ page }) => {
      await serveAnswers(page, []);
      const panel = await openAssistant(page, assistant);
      const code =
        "  " +
        Array.from(
          { length: 60 },
          (_, i) => "coalesce(field_" + i + ", 0) +"
        ).join("\n") +
        "\n  0";
      await page.locator(editorSelector).fill(code);
      const source = panel.getByRole("combobox", { name: "本轮引用来源" });
      const inspect = button(panel, view);
      const label = panel.getByText("本轮引用", { exact: true });
      const grip = panel.getByRole("separator", { name: "调整助手宽度" });
      for (const width of [1440, 800]) {
        if (width === 800) await grip.press("Home");
        await page.setViewportSize({ width, height: 780 });
        if (width === 1440) await grip.press("End");
        else await expect(panel).toHaveCSS("width", "288px");
        const [left, center, right] = await Promise.all(
          [label, source, inspect].map((el) => el.boundingBox())
        );
        for (const el of [left, right])
          expect(
            Math.abs(el.y + el.height / 2 - (center.y + center.height / 2))
          ).toBeLessThan(2);
        expect(left.x + left.width).toBeLessThanOrEqual(center.x);
        expect(center.x + center.width).toBeLessThanOrEqual(right.x);
        await expect(inspect).toBeInViewport({ ratio: 1 });
      }
      await inspect.click();
      const full = panel.locator(".composer pre");
      expect(await full.textContent()).toBe(code);
      expect(
        await full.evaluate((el) => el.scrollHeight > el.clientHeight)
      ).toBe(true);

      await full.evaluate((pre) => {
        pre.scrollTop = 75;
      });
      expect(await full.evaluate((pre) => pre.scrollTop)).toBe(75);
      const contrast = () =>
        full.evaluate((pre) => {
          const ctx = document.createElement("canvas").getContext("2d");
          const luminance = (color) => {
            ctx.fillStyle = color;
            ctx.fillRect(0, 0, 1, 1);
            const [r, g, b] = Array.from(ctx.getImageData(0, 0, 1, 1).data)
              .slice(0, 3)
              .map((n) => n / 255)
              .map((n) =>
                n <= 0.04045 ? n / 12.92 : ((n + 0.055) / 1.055) ** 2.4
              );
            return 0.2126 * r + 0.7152 * g + 0.0722 * b;
          };
          const fg = luminance(getComputedStyle(pre).color),
            bg = luminance(getComputedStyle(pre.parentElement).backgroundColor);
          return (Math.max(fg, bg) + 0.05) / (Math.min(fg, bg) + 0.05);
        });
      expect(await contrast()).toBeGreaterThanOrEqual(4.5);
      await panel.evaluate((host) => {
        host.style.colorScheme = "dark";
        host.style.setProperty("--ed-bg-color", "#232323");
        host.style.setProperty("--ed-text-color-primary", "#ebebeb");
        host.style.setProperty("--ed-border-color", "#434343");
      });
      expect(await contrast()).toBeGreaterThanOrEqual(4.5);
      await expect(panel.getByRole("textbox")).toBeInViewport({ ratio: 1 });
    }
  );
}

test("select the last manual SQL preview with its success or failure status", async ({
  page,
}) => {
  const openings = await serveAnswers(page, [
    { type: "EXPLANATION", text: "已完成" },
    { type: "EXPLANATION", text: "已完成" },
  ]);
  const sql = ["SELECT 7", "SELECT missing_column"];
  await page.route("**/fixture-api/dataset/previewSql", (route) => {
    const query = Buffer.from(
      route.request().postDataJSON().sql,
      "base64"
    ).toString();
    return query === sql[0]
      ? route.fulfill({
          json: {
            data: {
              data: [{ total: "row-secret" }],
              fields: [{ originName: "total", deType: 2 }],
            },
          },
        })
      : route.fulfill({
          status: 400,
          json: { msg: "Unknown column 'missing_column' in 'field list'" },
        });
  });
  const panel = await openAssistant(page, "SQL 助手");
  const drawer = page.locator(".sql-drawer-fullscreen:visible");
  const draft = drawer.locator(".cm-content");
  const source = panel.getByRole("combobox", { name: "本轮引用来源" });
  for (const [i, label] of [
    "上次预览的 SQL · 成功",
    "上次预览的 SQL · 失败",
  ].entries()) {
    await draft.fill(sql[i]);
    await button(drawer, "运行").click();
    await expect(
      source.getByRole("option", { name: label, exact: true })
    ).toBeAttached();
    await draft.fill("SELECT 3");
    await source.selectOption({ label });
    await expect(panel.locator(".composer")).toContainText("结果行不进入对话");
    await button(panel, "查看 SQL").click();
    expect(await panel.locator(".composer pre").textContent()).toBe(sql[i]);
    await expect(
      panel.locator(".composer").getByRole("button", { name: /定位到/ })
    ).toHaveCount(0);
    await ask(panel, "解释依据");
    expect(openings[i]).toMatchObject({
      sql: sql[i],
      preview: { outcome: i === 0 ? "SUCCESS" : "UNKNOWN_COLUMN" },
    });
    expect(JSON.stringify(openings[i])).not.toContain("row-secret");
  }
});
