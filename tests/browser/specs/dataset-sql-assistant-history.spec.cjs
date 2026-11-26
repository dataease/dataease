const { test, expect } = require("../fixtures/dataset-editor/conversations.cjs");

test("SQL editor follows up on two fields after stopping a later read", async ({
  page,
}) => {
  test.skip(
    !process.env.DATASET_ASSISTANT_BACKEND,
    "requires the HTTP history fixture"
  );
  await page.route("**/fixture-api/ai/availability", (route) =>
    route.fulfill({ json: { available: true } })
  );
  await page.goto("/?sqlNode=1");
  await page.getByRole("button", { name: "数据表关联", exact: true }).click();
  const row = page.locator(".node-items .node-item").filter({
    has: page.getByRole("button", { name: "items", exact: true }),
  });
  await row.locator(".hover-icon").click();
  await page
    .locator(".menu-more_popper:visible")
    .getByRole("menuitem", { name: "编辑SQL", exact: true })
    .click();
  const control = (part) => page.getByTestId("dataset-sql-assistant-" + part);
  await control("toggle").click();
  await control("input").fill("先读取 orders 再读取 items");
  await control("send").click();
  const original = control("panel").getByRole("article").first();
  await expect(
    original.getByText("步骤 1 · 读取 orders 表结构 · 80 个字段", {
      exact: true,
    })
  ).toBeVisible();
  await expect(original.getByText(/步骤 2 · 读取 items 表结构/)).toBeVisible();
  await control("stop").click();
  const trace = original.locator(".assistant-process");
  const summary = trace.locator(":scope > summary");
  const processTitle = /^\s*执行过程(?: · 总耗时 \d+(?:\.\d+)? (?:ms|s))?\s*$/;
  await expect(trace).not.toHaveAttribute("open", "");
  await expect(summary).toHaveText(processTitle);
  await summary.click();
  await expect(summary).toHaveText(processTitle);
  const orders = trace.locator("details").first().locator("summary");
  await expect(orders).toHaveText("步骤 1 · 读取 orders 表结构 · 80 个字段");
  await orders.click();
  await expect(original.getByText(/column-marker-79!/)).toBeVisible();
  await control("input").fill(
    "步骤 1 里的 paid_at 和 created_at 各是什么类型？"
  );
  await control("send").click();
  await expect(control("panel").getByRole("article").nth(1)).toContainText(
    "paid_at TIMESTAMP, created_at DATETIME"
  );
  await control("input").fill("刚才 orders 的时间字段");
  await control("send").click();
  await expect(control("panel").getByRole("article").nth(2)).toContainText("当时的时间字段");
  await expect(
    page.locator(".sql-drawer-fullscreen:visible .cm-content")
  ).toHaveText("select 1");
});
