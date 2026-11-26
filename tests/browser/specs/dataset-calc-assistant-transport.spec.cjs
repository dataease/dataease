const { test, expect } = require("@playwright/test");

test.skip(
  !process.env.DATASET_ASSISTANT_BACKEND,
  "requires the isolated conversation-socket runner"
);

const control = (page, name) =>
  page.getByTestId("dataset-sql-assistant-" + name);
const platformSocket = /\/websocket\/.*\/websocket/;

async function openEditor(page, editor) {
  if (editor.startsWith("SQL")) {
    await page.getByRole("button", { name: "数据表关联", exact: true }).click();
    const row = page.locator(".node-items .node-item").filter({
      has: page.getByRole("button", { name: "items", exact: true }),
    });
    await row.locator(".hover-icon").click();
    await page
      .locator(".menu-more_popper:visible")
      .getByRole("menuitem", { name: "编辑SQL", exact: true })
      .click();
  } else {
    await page
      .getByRole("button", { name: "新建计算字段", exact: true })
      .click();
  }
  await control(page, "toggle").click();
}
async function start(page, editor, question, intercept) {
  const frames = [],
    operations = [];
  page.on("websocket", (socket) =>
    socket.on("framereceived", (frame) => frames.push(String(frame.payload)))
  );
  if (intercept)
    await page.routeWebSocket(platformSocket, (socket) => {
      const upstream = socket.connectToServer();
      upstream.onMessage((message) =>
        intercept(socket, upstream, message, frames)
      );
    });
  page.on("request", (req) => {
    if (req.url().includes("/fixture-api/ai/conversations"))
      operations.push(req);
  });
  await page.route("**/fixture-api/ai/availability", (route) =>
    route.fulfill({ json: { available: true } })
  );
  await page.goto("/?sqlNode=1");
  await openEditor(page, editor);
  await expect
    .poll(() => frames.some((f) => f.includes("CONNECTED")))
    .toBe(true);
  await control(page, "input").fill(question);
  await control(page, "send").click();
  return { panel: control(page, "panel"), frames, operations };
}
const requests = (operations, method, suffix) =>
  operations.filter(
    (r) => r.method() === method && new URL(r.url()).pathname.endsWith(suffix)
  );
async function endConversation(page) {
  const closed = page.waitForResponse(
    (r) =>
      r.request().method() === "DELETE" &&
      r.url().includes("/ai/conversations/")
  );
  await page.getByRole("button", { name: "新对话", exact: true }).click();
  expect((await closed).ok()).toBe(true);
}
for (const editor of ["SQL", "计算字段", "SQL gap"]) {
  test(
    editor +
      " editor receives the original stream and repairs only a missing batch",
    async ({ page }) => {
      let held;
      const intercept =
        editor === "SQL gap" &&
        ((socket, _upstream, message, frames) => {
          const frame = String(message);
          frames.push(frame);
          if (!held && frame.includes("星河") && frame.includes("RUNNING")) {
            held = message;
            return;
          }
          socket.send(message);
          if (held && frame.includes("COMPLETED")) {
            socket.send(held);
            socket.send(message);
          }
        });
      const { panel, frames, operations } = await start(
        page,
        editor,
        "解释这个表达式",
        intercept
      );
      await expect(panel).toContainText("星河", { timeout: 8000 });
      await expect(panel).not.toContainText("继续");
      await expect(panel).toContainText("星河继续", { timeout: 17000 });
      await expect(panel).not.toContainText("星河星河");
      expect(requests(operations, "POST", "/executions")).toHaveLength(1);
      expect(requests(operations, "POST", "/subscriptions")).toHaveLength(1);
      expect(requests(operations, "GET", "/events")).toHaveLength(
        editor === "SQL gap" ? 1 : 0
      );
      expect(
        frames.filter(
          (f) =>
            f.includes("MESSAGE") && (f.includes("星河") || f.includes("继续"))
        ).length
      ).toBeGreaterThanOrEqual(2);
      await endConversation(page);
    }
  );
}

for (const scenario of ["reconnect", "completed offline", "grace expired"]) {
  test(
    scenario +
      " retains the original editor answer and only reads its execution",
    async ({ page }) => {
      test.setTimeout(65000);
      let dropped = false,
        recovered = false;
      await page.route("**/executions/*/events?*", (route) =>
        recovered ? route.continue() : route.abort("internetdisconnected")
      );
      const expired = scenario === "grace expired";
      const { panel, operations } = await start(
        page,
        scenario === "reconnect" ? "计算字段" : "SQL",
        expired ? "hold beyond grace" : "解释这个表达式",
        (socket, upstream, message) => {
          socket.send(message);
          if (!dropped && String(message).includes("星河")) {
            dropped = true;
            setTimeout(() => {
              socket.close();
              upstream.close();
            }, 150);
          }
        }
      );
      await expect(panel).toContainText("连接中断，正在恢复");
      await expect(panel).toContainText("星河");
      if (scenario === "completed offline") {
        await control(page, "toggle").click();
        await page.waitForTimeout(10500);
        await control(page, "toggle").click();
      }
      if (expired) {
        await expect(control(page, "resume")).toBeVisible({ timeout: 36000 });
        // Unavailable transport cannot confirm an interruption or authorize a new attempt.
        await expect(
          panel.getByRole("button", { name: "重新回答", exact: true })
        ).toHaveCount(0);
      }
      recovered = true;
      if (expired) {
        await control(page, "resume").click();
        const retry = panel.getByRole("button", {
          name: "重新回答",
          exact: true,
        });
        await expect(retry).toBeVisible();
        await expect(panel).toContainText("星河");
        expect(requests(operations, "POST", "/retry")).toHaveLength(0);
        await retry.click();
        await expect
          .poll(() => requests(operations, "POST", "/retry").length)
          .toBe(1);
        await expect(panel.locator("article > div").last()).toContainText(
          "星河"
        );
        await control(page, "stop").click();
      } else {
        await expect(panel).toContainText("星河继续", { timeout: 17000 });
        await expect(panel).not.toContainText("星河星河");
        if (scenario === "reconnect")
          expect(
            requests(operations, "POST", "/subscriptions").length
          ).toBeGreaterThanOrEqual(2);
      }
      expect(requests(operations, "POST", "/executions")).toHaveLength(1);
      expect(requests(operations, "GET", "/events").length).toBeGreaterThan(0);
      await endConversation(page);
    }
  );
}

for (const action of ["stop", "new conversation", "close drawer"]) {
  test(
    action +
      " isolates late original frames and retains only authorized content",
    async ({ page }) => {
      let replay;
      const { panel, operations } = await start(
        page,
        action === "close drawer" ? "计算字段" : "SQL",
        "断线停止",
        (socket, upstream, message) => {
          if (String(message).includes("途中")) {
            replay = () => socket.send(message);
            return;
          }
          socket.send(message);
        }
      );
      await expect(panel).toContainText("星河");
      await expect.poll(() => !!replay).toBe(true);
      if (action === "stop") {
        await control(page, "stop").click();
        await expect(panel).toContainText("星河途中");
        await expect(panel).toContainText("未完成");
        expect(requests(operations, "GET", "/events")).toHaveLength(1);
        await endConversation(page);
      } else {
        if (action === "close drawer") {
          const closed = page.waitForResponse(
            (r) =>
              r.request().method() === "DELETE" &&
              r.url().includes("/ai/conversations/")
          );
          await page
            .getByRole("button", { name: "取消", exact: true })
            .last()
            .click();
          expect((await closed).ok()).toBe(true);
          await openEditor(page, "计算字段");
        } else await endConversation(page);
        replay();
        await expect(panel.getByRole("article")).toHaveCount(0);
        await expect(panel).not.toContainText("途中");
      }
      expect(requests(operations, "POST", "/executions")).toHaveLength(1);
    }
  );
}
