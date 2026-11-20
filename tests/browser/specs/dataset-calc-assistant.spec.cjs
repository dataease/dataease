const { test, expect } = require("@playwright/test");

async function backend(page, options = {}) {
  const state = { opens: [], checks: 0 };
  let opening, result;
  await page.route("**/fixture-api/ai/**", async (route) => {
    const req = route.request();
    const path = new URL(req.url()).pathname;
    const json = (value) => route.fulfill({ json: value });
    if (path.endsWith("/availability")) return json({ available: true });
    if (path.endsWith("/conversations")) {
      opening = req.postDataJSON();
      state.opens.push(opening);
      return json({
        scene: opening.scene,
        resourceId: opening.resourceId,
        resourceName: "Fixture MySQL",
        editorId: opening.editorId,
        sessionId: "s1",
        contextVersion: "v1",
        resourceVersion: "r1",
        draftVersion: "d1",
        expiresAt: Date.now() + 60000,
      });
    }
    if (path.endsWith("/executions"))
      return json({ executionId: "e1", status: "RUNNING" });
    if (path.endsWith("/events")) {
      result = {
        type: "CALC_FIELD_SUGGESTION",
        sql: null,
        text: options.text ?? "按行计算差额。",
        calcField: {
          expression: options.expression ?? "[orders_id] - [items_id]",
          name: "差额",
          groupType: "q",
          deType: 3,
          validation: options.validation ?? "DETAIL_PASS",
          reason: options.reason,
        },
        resourceId: opening.resourceId,
        resourceVersion: "r1",
        editorId: opening.editorId,
        draftVersion: "d1",
      };
      return json({
        sessionId: "s1",
        contextVersion: "v1",
        executionId: "e1",
        status: "COMPLETED",
        cursor: 1,
        events: [],
        result,
      });
    }
    if (path.endsWith("/check-application")) {
      state.checks++;
      if (options.denyApply)
        return route.fulfill({
          status: 403,
          json: { messageKey: "i18n_ai_forbidden" },
        });
      return json(result);
    }
    return json({});
  });
  return state;
}

test("a previewed formula changes only the drawer draft and keeps explicit field properties", async ({
  page,
}) => {
  const state = await backend(page);
  await page.goto("/?dependency=1");
  await page.getByRole("button", { name: "新建计算字段", exact: true }).click();
  const drawer = page.locator(".calc-field-drawer");
  await drawer.getByPlaceholder("请输入字段名称").fill("保留我的名称");
  await drawer.getByRole("button", { name: "维度", exact: true }).click();
  await drawer
    .getByRole("button", { name: "计算字段助手", exact: true })
    .click();
  await drawer.locator("textarea").fill("逐行生成差额");
  await drawer.getByRole("button", { name: "发送", exact: true }).click();
  await expect(drawer.getByText("预览校验通过", { exact: true })).toBeVisible();
  expect(state.opens[0].scene).toBe("dataset-calc-field");
  expect(
    state.opens[0].calcField.dataset.allFields.map((f) => f.name)
  ).toContain("计算字段");
  await expect(drawer.locator(".cm-content")).toHaveText("");
  await drawer
    .getByRole("button", { name: "应用到当前草稿", exact: true })
    .click();
  await expect(drawer.locator(".cm-content")).toHaveText(
    "[orders_id] - [items_id]"
  );
  await expect(drawer.getByPlaceholder("请输入字段名称")).toHaveValue(
    "保留我的名称"
  );
  await expect(
    drawer.getByRole("button", { name: "维度", exact: true })
  ).toHaveClass(/is-active/);
  expect(state.checks).toBe(1);
  await drawer.getByRole("button", { name: "确认", exact: true }).click();
  expect(await page.evaluate(() => window.saved)).toBeUndefined();
  await page.getByRole("button", { name: "保存", exact: true }).click();
  await expect
    .poll(() => page.evaluate(() => window.saved?.allFields.at(-1)?.originName))
    .toBe("[orders1] - [items1]");
});

for (const scenario of ["preview failed", "apply permission revoked"]) {
  test(
    scenario + " keeps the formula in the drawer untouched",
    async ({ page }) => {
      const denied = scenario === "apply permission revoked";
      await backend(page, {
        denyApply: denied,
        validation: denied ? "DETAIL_PASS" : "PREVIEW_FAILED",
      });
      await page.goto("/");
      await page
        .getByRole("button", { name: "新建计算字段", exact: true })
        .click();
      const drawer = page.locator(".calc-field-drawer");
      await drawer.getByRole("button", { name: "计算字段助手" }).click();
      await drawer.locator("textarea").fill("生成毛利");
      await drawer.getByRole("button", { name: "发送", exact: true }).click();
      const apply = drawer.getByRole("button", {
        name: "应用到当前草稿",
        exact: true,
      });
      if (denied) {
        await apply.click();
        await expect(apply).toHaveCount(0);
      } else {
        await expect(
          drawer.getByText("预览校验失败", { exact: true })
        ).toBeVisible();
        await expect(apply).toBeDisabled();
      }
      await expect(drawer.locator(".cm-content")).toHaveText("");
      expect(await page.evaluate(() => window.saved)).toBeUndefined();
    }
  );
}

test("an aggregate placeholder explains chart verification and allows explicit application", async ({
  page,
}) => {
  const expression = "SUM([orders_id])";
  const state = await backend(page, {
    expression,
    text: "按订单求和。",
    validation: "AGGREGATE_PLACEHOLDER",
  });
  await page.goto("/");
  await page.getByRole("button", { name: "新建计算字段", exact: true }).click();
  const drawer = page.locator(".calc-field-drawer");
  await drawer.getByRole("button", { name: "计算字段助手" }).click();
  await drawer.locator("textarea").fill("合计订单");
  await drawer.getByRole("button", { name: "发送", exact: true }).click();
  await expect(
    drawer.getByText("聚合结果需在图表中验证", { exact: true })
  ).toBeVisible();
  await expect(drawer.getByText("预览校验通过", { exact: true })).toHaveCount(
    0
  );
  await expect(drawer.locator(".cm-content")).toHaveText("");
  await drawer
    .getByRole("button", { name: "应用到当前草稿", exact: true })
    .click();
  await expect(drawer.locator(".cm-content")).toHaveText(expression);
  expect(state.checks).toBe(1);
  expect(await page.evaluate(() => window.saved)).toBeUndefined();
});

test("failed formulas support authorized copy and follow-up while manual validation uses only the editor", async ({
  page,
  context,
}) => {
  const state = await backend(page, {
    validation: "INVALID",
    reason: "invalid_formula",
  });
  await context.grantPermissions(["clipboard-read", "clipboard-write"]);
  await page.goto("/");
  await page.getByRole("button", { name: "新建计算字段", exact: true }).click();
  const drawer = page.locator(".calc-field-drawer");
  await drawer.getByRole("button", { name: "计算字段助手" }).click();
  await drawer.locator("textarea").fill("生成公式");
  await drawer.getByRole("button", { name: "发送", exact: true }).click();
  await expect(
    drawer.getByText("请检查字段引用、函数和公式语法。")
  ).toBeVisible();
  await expect(
    drawer.getByRole("button", { name: "应用到当前草稿" })
  ).toBeDisabled();
  await drawer.getByRole("button", { name: "复制公式" }).click();
  await expect
    .poll(() => page.evaluate(() => navigator.clipboard.readText()))
    .toBe("[orders_id] - [items_id]");
  await drawer.locator("textarea").fill("请修改上一轮建议");
  await drawer.getByRole("button", { name: "发送", exact: true }).click();
  await expect.poll(() => state.opens.length).toBe(2);
  expect(state.opens[1].sql).toBe("[orders_id] - [items_id]");
  expect(state.checks).toBe(0);
  await expect(drawer.locator(".cm-content")).toHaveText("");
  await drawer.getByPlaceholder("请输入字段名称").fill("手改");
  await drawer.locator(".cm-content").fill("5 + 2");
  await drawer.getByRole("button", { name: "校验", exact: true }).click();
  await expect(page.getByText("字段表达式校验成功")).toBeVisible();
  expect(state.opens).toHaveLength(2);
  await expect(drawer.locator(".cm-content")).toHaveText("5 + 2");
});
