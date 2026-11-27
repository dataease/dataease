const { test, expect } = require("../fixtures/dataset-editor/conversations.cjs");

async function openSqlEditor(page) {
  await page.getByRole("button", { name: "数据表关联", exact: true }).click();
  const row = page.locator(".node-items .node-item").filter({
    has: page.getByRole("button", { name: "items", exact: true }),
  });
  await row.locator(".hover-icon").click();
  await page
    .locator(".menu-more_popper:visible")
    .getByRole("menuitem", { name: "编辑SQL", exact: true })
    .click();
}

for (const editor of ["SQL", "计算字段"]) {
  test(
    editor + " 长对话能区分已选建议、编辑后的草稿和原步骤",
    async ({ page }) => {
      test.skip(
        !process.env.DATASET_ASSISTANT_BACKEND,
        "requires real conversation HTTP and controlled model"
      );
      await page.route("**/fixture-api/ai/availability", (route) =>
        route.fulfill({ json: { available: true } })
      );
      if (editor === "计算字段") {
        await page.addInitScript(() => {
          const field = {
            id: "102",
            datasetTableId: "1",
            datasourceId: "11",
            name: "Price",
            originName: "ID",
            checked: true,
            deType: 2,
            extField: 0,
            groupType: "q",
            deExtractType: 0,
          };
          window.fixturePhysicalTables = {
            AI_PROBE: [{ ...field, name: "ID", type: "BIGINT" }],
          };
          const table = {
            id: "1",
            datasourceId: "11",
            type: "db",
            name: "AI_PROBE",
            tableName: "AI_PROBE",
            info: JSON.stringify({ table: "AI_PROBE" }),
          };
          sessionStorage.setItem(
            "savedDataset",
            JSON.stringify({
              id: "42",
              name: "收入",
              pid: "0",
              isCross: false,
              union: [
                {
                  currentDs: table,
                  currentDsFields: [field],
                  childrenDs: [],
                  unionToParent: { unionType: "left", unionFields: [] },
                },
              ],
              allFields: [field],
            })
          );
        });
      }
      await page.goto("/?sqlNode=1");
      if (editor === "SQL") await openSqlEditor(page);
      else
        await page
          .getByRole("button", { name: "新建计算字段", exact: true })
          .click();
      const control = (part) =>
        page.getByTestId("dataset-sql-assistant-" + part);
      await control("toggle").click();
      const panel = control("panel");
      let latest;
      page.on("request", (request) => {
        if (
          request.method() === "POST" &&
          request.url().endsWith("/executions")
        )
          latest = request;
      });
      const ask = async (q, expected = "继续") => {
        await control("input").fill(q);
        await control("send").click();
        const article = panel.getByRole("article").last();
        await expect(article.locator(".question")).toHaveText(q);
        await expect(article.locator(".assistant-answer")).toContainText(
          expected
        );
        await expect(control("stop")).toHaveCount(0);
      };
      await ask("按已付款订单生成建议", "已生成建议");
      const first = panel.getByRole("article").first();
      await expect(first).toContainText("步骤 1");
      const code = page
        .locator(
          editor === "SQL"
            ? ".sql-drawer-fullscreen:visible .cm-content"
            : ".ed-drawer:visible .cm-content"
        )
        .last();
      const manual = editor === "SQL" ? "SELECT 77" : "[Price] + 77";
      await code.fill(manual);
      await first.getByRole("button", { name: "基于此建议追问" }).click();
      await ask(
        "Confirmed: exclude chargebacks. 接下来都用英文。",
        "Which convention?"
      );
      await ask("2");
      await ask("用繁體中文回答。請繼續分析支付指標");
      await ask("这次用英文回答，已确认退款订单不计入收入。");
      await ask("已确认净收入以元计，这轮请用日文回答。");
      await ask(
        "Confirmed: units are cents, please reply in French this time."
      );
      await ask("For this turn, answer in German and exclude archived orders.");
      for (let i = 4; i <= 6; i++) await ask("Continue example " + i);
      await ask(
        editor === "SQL"
          ? "第一次回答的步骤 1 里的 ID 是什么类型？"
          : "第一次回答的步骤 1 校验的是哪个公式？",
        "原步驟正確，沿用已確認的口徑"
      );
      await first.getByRole("button", { name: "基于此建议追问" }).click();
      await ask("现在呢？", "本輪依據為建議一，草稿另有修改");
      await expect(code).toHaveText(manual);
      if (editor === "计算字段") {
        await first.getByRole("button", { name: "基于此建议追问" }).click();
        await ask("重新校验当前公式", "当前草稿预览无数据");
      }
      await panel
        .getByRole("combobox", { name: "本轮引用来源" })
        .selectOption({
          label: editor === "SQL" ? "不引用 SQL" : "不引用公式",
        });
      await ask("仅沿用已确认口径", "只沿用已確認口徑");
      await ask("换个话题，解释采购", "Purchase definition");
      const denied = await page.request.post(latest.url(), {
        headers: { "X-Test-Scope": "workspace" },
        data: {
          ...latest.postDataJSON(),
          submissionKey: "other-workspace",
          text: "第一次回答的第 1 步",
        },
      });
      expect(denied.status()).toBe(403);
      expect(await denied.text()).not.toContain("chargebacks");
      await panel.getByRole("button", { name: "新对话", exact: true }).click();
      await ask("新会话的步骤 1？", "新对话无此步骤");
      const close = page.waitForResponse(
        (response) =>
          response.request().method() === "DELETE" &&
          response.url() === latest.url().replace(/\/executions$/, "")
      );
      await panel.getByRole("button", { name: "新对话", exact: true }).click();
      expect((await close).ok()).toBe(true);
    }
  );
}

test("手动重答后的澄清沿用原失败预览，即使同一草稿已另行预览成功", async ({
  page,
}) => {
  test.skip(
    !process.env.DATASET_ASSISTANT_BACKEND,
    "requires real conversation HTTP and controlled model"
  );
  await page.route("**/fixture-api/ai/availability", (route) =>
    route.fulfill({ json: { available: true } })
  );
  const failedSql = "SELECT unknown_amount FROM AI_PROBE";
  await page.route("**/fixture-api/dataset/previewSql", (route) => {
    const sql = Buffer.from(
      route.request().postDataJSON().sql,
      "base64"
    ).toString();
    if (sql === failedSql)
      return route.fulfill({
        status: 400,
        json: { msg: "Unknown column 'unknown_amount'" },
      });
    return route.fulfill({
      json: {
        data: { data: [{ n: 77 }], fields: [{ originName: "n", deType: 2 }] },
      },
    });
  });
  await page.goto("/?sqlNode=1&cross=1");
  await openSqlEditor(page);
  const editor = page.locator(".sql-drawer-fullscreen:visible");
  const draft = editor.locator(".cm-content");
  const run = editor.getByRole("button", { name: "运行", exact: true });
  await draft.fill(failedSql);
  const failure = page.waitForResponse(
    (response) =>
      response.url().endsWith("/previewSql") && response.status() === 400
  );
  await run.click();
  await failure;
  await draft.fill("SELECT 77 AS n");
  await page.getByTestId("dataset-sql-assistant-toggle").click();
  const panel = page.getByTestId("dataset-sql-assistant-panel");
  const openings = [];
  page.on("request", (request) => {
    if (request.method() === "POST" && request.url().endsWith("/conversations"))
      openings.push(request.postDataJSON());
  });
  await panel.getByRole("textbox").fill("解释刚才手动预览的问题");
  await page.getByTestId("dataset-sql-assistant-send").click();
  await panel.getByRole("button", { name: "重试" }).click();
  const choice = panel.getByRole("button", { name: "按上月的预览统计" });
  await expect(choice).toBeVisible();
  await run.click();
  await expect(editor.locator(".table-scroll")).toContainText("77");
  await choice.click();
  await expect(panel.getByRole("article").last()).toContainText(
    "按原失败预览分析"
  );
  expect(openings).toHaveLength(2);
  expect(openings[0].preview).toMatchObject({
    outcome: "UNKNOWN_COLUMN",
    resourceId: "11",
    currentResourceId: "11",
    isCross: true,
    currentIsCross: true,
  });
  expect(openings[0].preview.revision).toBeLessThan(
    openings[0].preview.currentRevision
  );
  expect(openings[1]).toMatchObject({
    isCross: true,
    resourceId: "11",
    sql: failedSql,
    preview: openings[0].preview,
  });
  await expect(draft).toHaveText("SELECT 77 AS n");
  await panel.getByRole("textbox").fill("解释当前 SQL 草稿");
  await page.getByTestId("dataset-sql-assistant-send").click();
  await expect(panel.getByRole("article").last()).toContainText(
    "按当前草稿分析"
  );
  expect(openings[2].preview).toBeUndefined();
});
