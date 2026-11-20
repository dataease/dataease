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
        text: "按行计算差额。",
        calcField: {
          expression: "[orders_id] - [items_id]",
          name: "差额",
          groupType: "q",
          deType: 3,
          validation: options.validation ?? "DETAIL_PASS",
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
