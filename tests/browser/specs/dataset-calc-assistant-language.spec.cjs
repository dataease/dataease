const { test, expect } = require("@playwright/test");

test.skip(
  !process.env.DATASET_ASSISTANT_BACKEND,
  "requires the language HTTP runner"
);

const labels = {
  "zh-CN": [
    "数据表关联",
    "编辑SQL",
    "新建计算字段",
    "新对话",
    "发送",
    "字段说明",
  ],
  tw: ["資料表關聯", "編輯SQL", "新計算欄位", "新對話", "傳送", "欄位說明"],
  en: [
    "Table relationships",
    "Edit SQL",
    "Create a new calculated field",
    "New conversation",
    "Send",
    "Field description",
  ],
};
const control = (page, part) =>
  page.getByTestId("dataset-sql-assistant-" + part);

for (const editor of ["SQL", "formula"]) {
  for (const [
    language,
    [relations, editSql, newField, newChat, send, defaultAnswer],
  ] of Object.entries(labels)) {
    test(
      editor + " editor uses the selected " + language + " UI language",
      async ({ page }) => {
        await page.route("**/fixture-api/ai/availability", (route) =>
          route.fulfill({ json: { available: true } })
        );
        await page.goto("/?sqlNode=1&language=" + language);
        if (editor === "SQL") {
          await page
            .getByRole("button", { name: relations, exact: true })
            .click();
          const row = page
            .locator(".node-item")
            .filter({
              has: page.getByRole("button", { name: "items", exact: true }),
            });
          await row.locator(".hover-icon").click();
          await page
            .locator(".menu-more_popper:visible")
            .getByRole("menuitem", { name: editSql, exact: true })
            .click();
        } else {
          await page
            .getByRole("button", { name: newField, exact: true })
            .click();
        }
        await control(page, "toggle").click();
        const panel = control(page, "panel");
        await expect(control(page, "send")).toHaveText(send);
        const ask = async (question, expected) => {
          await control(page, "input").fill(question);
          await control(page, "send").click();
          const turn = panel.locator("article").last();
          await expect(turn.locator(".question")).toHaveText(question);
          await expect(turn.locator(".assistant-answer")).toHaveText(
            expected + " · paid_at"
          );
        };
        const paste =
          editor === "SQL" ? "SELECT paid_at FROM orders" : "MONTH([paid_at])";
        await ask(paste, defaultAnswer);
        if (language === "tw") {
          await ask("接下来都用英文。解释 paid_at", "Field description");
          await ask("用简体中文回答。怎样筛选日期？", "字段说明");
          await ask("2", "字段说明");
          await ask("这次用繁体中文。请举例", "欄位說明");
          await ask("再解释一下例子", "字段说明");
          await ask("换个话题，解释客户数", "Field description");
          await panel
            .getByRole("button", { name: newChat, exact: true })
            .click();
          await expect(panel.locator("article")).toHaveCount(0);
          await ask("Explain paid_at", defaultAnswer);
        } else {
          await panel
            .getByRole("button", { name: newChat, exact: true })
            .click();
        }
      }
    );
  }
}
