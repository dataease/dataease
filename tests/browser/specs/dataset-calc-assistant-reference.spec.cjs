const { test, expect } = require("@playwright/test");
const {
  button,
  openAssistant,
  ask,
  serveAnswers,
} = require("../fixtures/dataset-editor/reference.cjs");

test("Escape collapses the formula preview and returns control to the drawer", async ({
  page,
}) => {
  await serveAnswers(page, []);
  const panel = await openAssistant(page, "计算字段助手");
  await page.locator(".calc-field-drawer .cm-content").fill("[orders_id]");
  const drawer = page.getByRole("dialog");
  await drawer.evaluate((el) => {
    el.addEventListener("keydown", (event) => {
      if (event.key === "Escape") el.dataset.escape = "received";
    });
  });
  const inspect = button(panel, "查看公式");
  await inspect.press("Enter");
  await panel.locator("footer pre").press("Escape");
  await expect(inspect).toBeFocused();
  await expect(inspect).toHaveAttribute("aria-expanded", "false");
  await expect(drawer).not.toHaveAttribute("data-escape", "received");
  await inspect.press("Escape");
  await expect(drawer).toHaveAttribute("data-escape", "received");
});

test("locate the original formula question from its retry suggestion while keeping the follow-up", async ({
  page,
}) => {
  await page.emulateMedia({ reducedMotion: "reduce" });
  const expression = "[orders_id] /\n  [items_id]";
  const openings = await serveAnswers(page, [
    null,
    { type: "EXPLANATION", text: "补充说明。\n".repeat(80) },
  ]);
  await page.route("**/executions/e1/events?*", (route) =>
    route.fulfill({
      json: {
        sessionId: "s1",
        executionId: "e1",
        contextVersion: "v1",
        attemptId: "a1",
        retryEligible: true,
        status: "FAILED",
        cursor: 1,
        recovery: { stepKind: "MODEL" },
        events: [{ sequence: 1, type: "FAILED", error: "timeout" }],
      },
    })
  );
  await page.route("**/executions/e1/retry", (route) =>
    route.fulfill({
      json: {
        sessionId: "s1",
        executionId: "retry1",
        contextVersion: "v1",
        attemptId: "a2",
        sourceAttemptId: "a1",
        status: "RUNNING",
      },
    })
  );
  await page.route("**/executions/retry1/events?*", (route) =>
    route.fulfill({
      json: {
        sessionId: "s1",
        executionId: "retry1",
        contextVersion: "v1",
        status: "COMPLETED",
        cursor: 1,
        events: [],
        result: {
          type: "CALC_FIELD_SUGGESTION",
          text: "公式未通过校验。",
          calcField: {
            name: "每单件数",
            expression,
            validation: "INVALID",
            groupType: "q",
            deType: 3,
          },
        },
      },
    })
  );
  const panel = await openAssistant(page, "计算字段助手");
  const editor = page.locator(".calc-field-drawer .cm-content");
  await editor.fill("[orders_id]");
  await ask(panel, "计算每个订单的件数");
  await button(panel, "重试").click();
  const first = panel.getByRole("article").first();
  await expect(
    first.getByText("建议 #2 · 每单件数", { exact: true })
  ).toBeVisible();
  await expect(button(first, "应用到当前草稿")).toBeDisabled();
  const source = panel.getByRole("combobox", { name: "本轮引用来源" });
  await expect(source.getByRole("option", { name: /^建议 #1/ })).toHaveCount(0);
  await source.selectOption({ label: "不引用公式" });
  await ask(panel, "补充说明");
  await expect(panel.getByRole("article").nth(1)).toContainText("补充说明。");
  const question = first.getByText("计算每个订单的件数", { exact: true });
  const input = panel.getByRole("textbox");
  await input.fill("保留我的半条追问");
  await source.selectOption({ label: "建议 #2 · 每单件数" });
  await button(panel, "查看公式").press("Enter");
  expect(await panel.locator("footer pre").textContent()).toBe(expression);
  await expect(question).not.toBeInViewport();
  const origin = panel
    .locator("footer")
    .getByRole("button", { name: /建议 #2/ });
  const outsideTop = await page.evaluate(() => window.scrollY);
  await origin.press("Enter", { timeout: 3000 });
  await expect(question).toBeInViewport({ ratio: 1 });
  await expect(question).toHaveCSS("outline-style", "solid");
  await expect(input).toHaveValue("保留我的半条追问");
  await expect(source).toHaveValue("2");
  expect(await page.evaluate(() => window.scrollY)).toBe(outsideTop);
  await expect(question).toHaveCSS("outline-style", "none");
  await editor.fill("[items_id] * 2");
  await source.selectOption({ label: "编辑器当前公式" });
  await button(panel, "查看公式").click();
  expect(await panel.locator("footer pre").textContent()).toBe(
    "[items_id] * 2"
  );
  await expect(origin).toHaveCount(0);
  expect(openings.map((opening) => opening.sql)).toEqual(["[orders_id]", ""]);
});

test("starting from scratch shows the effective source before sending and resets it for the next turn", async ({
  page,
}) => {
  const openings = await serveAnswers(page, [
    {
      type: "CALC_FIELD_SUGGESTION",
      calcField: {
        name: "订单数",
        expression: "[orders_id]",
        validation: "DETAIL_PASS",
      },
    },
    { type: "EXPLANATION", text: "已说明。" },
  ]);
  const panel = await openAssistant(page, "计算字段助手");
  await ask(panel, "算订单");
  const source = panel.getByRole("combobox", { name: "本轮引用来源" });
  await source.selectOption({ label: "建议 #1 · 订单数" });
  const input = panel.getByRole("textbox");
  await input.fill("请从头写");
  await expect(source.locator("option:checked")).toHaveText("不引用公式");
  const hint = panel.getByText("提问要求从头写，本轮不引用。", { exact: true });
  await expect(hint).toBeVisible();
  await expect(button(panel, "查看公式")).toBeHidden();
  await input.fill("修改这个建议");
  await expect(hint).toBeHidden();
  await expect(source.locator("option:checked")).toHaveText("建议 #1 · 订单数");
  await ask(panel, "请从头写");
  expect(openings[1]).toMatchObject({ previousSessionId: "s1", sql: "" });
  await expect(source.locator("option:checked")).toContainText("自动");
});
