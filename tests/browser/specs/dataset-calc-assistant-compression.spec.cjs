const { test, expect } = require("@playwright/test");

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
      if (editor === "SQL") {
        await page
          .getByRole("button", { name: "数据表关联", exact: true })
          .click();
        const row = page.locator(".node-items .node-item").filter({
          has: page.getByRole("button", { name: "items", exact: true }),
        });
        await row.locator(".hover-icon").click();
        await page
          .locator(".menu-more_popper:visible")
          .getByRole("menuitem", { name: "编辑SQL", exact: true })
          .click();
      } else
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
      await ask("这次用日文。请解释");
      for (let i = 1; i <= 6; i++) await ask("Continue example " + i);
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
      await panel.getByRole("checkbox", { name: "本轮引用" }).uncheck();
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
