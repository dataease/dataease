const { expect } = require("@playwright/test");

const button = (scope, name) =>
  scope.getByRole("button", { name, exact: true });

async function openAssistant(page, title) {
  await page.goto("/?sqlNode=1");
  if (title === "SQL 助手") {
    await button(page, "数据表关联").click();
    const node = page
      .locator(".node-item")
      .filter({ has: button(page, "items") });
    await node.locator(".hover-icon").click();
    await page.getByRole("menuitem", { name: "编辑SQL", exact: true }).click();
  } else {
    await button(page, "新建计算字段").click();
  }
  await button(page, title).click();
  return page.getByRole("complementary", { name: title });
}

async function ask(panel, question) {
  await panel.getByRole("textbox").fill(question);
  await button(panel, "发送").click();
  await expect(button(panel, "停止生成")).toBeHidden();
}

async function serveAnswers(page, answers) {
  const openings = [];
  await page.route("**/fixture-api/ai/**", async (route) => {
    const request = route.request();
    const path = new URL(request.url()).pathname;
    const reply = (json) => route.fulfill({ json });
    if (path.endsWith("/availability")) return reply({ available: true });
    if (request.method() === "DELETE") return reply({});
    const index = path.endsWith("/conversations")
      ? openings.push(request.postDataJSON())
      : Number(path.split("/")[4].slice(1));
    const opening = openings[index - 1];
    const scope = {
      editorId: opening.editorId,
      resourceId: opening.resourceId,
      resourceName: "Fixture MySQL",
      resourceVersion: "r1",
      sessionId: "s" + index,
      contextVersion: "v" + index,
      draftVersion: "d" + index,
      expiresAt: Date.now() + 300000,
    };
    if (path.endsWith("/conversations")) return reply(scope);
    const run = { ...scope, executionId: "e" + index };
    if (path.endsWith("/executions"))
      return reply({ ...run, status: "RUNNING" });
    const result = { ...scope, ...answers[index - 1] };
    if (path.endsWith("/check-application")) return reply(result);
    if (path.endsWith("/events"))
      return reply({
        ...run,
        status: "COMPLETED",
        cursor: 1,
        events: [],
        result,
      });
    throw Error("Unexpected AI request: " + path);
  });
  return openings;
}
module.exports = { button, openAssistant, ask, serveAnswers };
