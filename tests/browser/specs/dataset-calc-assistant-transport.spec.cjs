const { test, expect } = require("@playwright/test");

test.skip(
  !process.env.DATASET_ASSISTANT_BACKEND,
  "requires the isolated conversation-socket runner"
);

for (const editor of ["SQL", "计算字段", "SQL gap", "计算字段 reconnect", "SQL offline completion", "SQL stop"]) {
  test(
    editor +
      " editor receives the original model stream over its platform socket",
    async ({ page }) => {
      const loseFirstBatch = editor === "SQL gap";
      const reconnect = editor.includes("reconnect");
      const offlineCompletion = editor.includes("offline completion");
      const stop = editor === "SQL stop";
      let dropped = false, recovered = false, unread = false;
      const operations = [];
      const frames = [];
      page.on("websocket", (socket) =>
        socket.on("framereceived", (frame) =>
          frames.push(String(frame.payload))
        )
      );
      if (reconnect || offlineCompletion || stop) {
        await page.routeWebSocket(/\/websocket\/.*\/websocket/, (socket) => {
          const upstream = socket.connectToServer();
          upstream.onMessage((message) => {
            const frame = String(message);
            if (stop && frame.includes("途中")) { unread = true; return; }
            socket.send(message);
            if (!dropped && !stop && frame.includes("星河")) {
              dropped = true;
              setTimeout(() => { socket.close(); upstream.close(); }, 150);
            }
          });
        });
        if (!stop) await page.route("**/executions/*/events?*", async (route) => {
          if (!recovered) return route.abort("internetdisconnected");
          await route.continue();
        });
      }
      if (loseFirstBatch) {
        await page.routeWebSocket(/\/websocket\/.*\/websocket/, (socket) => {
          const upstream = socket.connectToServer();
          let held;
          upstream.onMessage((message) => {
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
        });
      }
      page.on("request", (req) => {
        if (req.url().includes("/fixture-api/ai/conversations"))
          operations.push(req);
      });
      await page.route("**/fixture-api/ai/availability", (route) =>
        route.fulfill({ json: { available: true } })
      );
      await page.goto("/?sqlNode=1");
      if (editor.startsWith("SQL")) {
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
      } else {
        await page
          .getByRole("button", { name: "新建计算字段", exact: true })
          .click();
      }
      const control = (name) =>
        page.getByTestId("dataset-sql-assistant-" + name);
      await control("toggle").click();
      await expect
        .poll(() => frames.some((f) => f.includes("CONNECTED")))
        .toBe(true);
      await control("input").fill(stop ? "断线停止" : "解释这个表达式");
      await control("send").click();
      const panel = control("panel");
      await expect(panel).toContainText("星河", { timeout: 8000 });
      await expect(panel).not.toContainText("继续");
      if (stop) {
        await expect.poll(() => unread).toBe(true);
        await control("stop").click();
        await expect(panel).toContainText("星河途中");
        await expect(panel).toContainText("未完成");
      } else {
        if (reconnect || offlineCompletion) {
          await expect(panel).toContainText("连接中断，正在恢复");
          if (offlineCompletion) {
            await control("toggle").click();
            await page.waitForTimeout(10500);
            await control("toggle").click();
          }
          recovered = true;
        }
        await expect(panel).toContainText("星河继续", { timeout: 17000 });
      }
      expect(
        operations.filter(
          (r) => r.url().endsWith("/executions") && r.method() === "POST"
        )
      ).toHaveLength(1);
      const closed = page.waitForResponse(r => r.request().method() === "DELETE" && r.url().includes("/ai/conversations/"));
      await page.getByRole("button", { name: "新对话", exact: true }).click();
      await closed;
      const subscriptions = operations.filter((r) => r.url().endsWith("/subscriptions") && r.method() === "POST");
      if (reconnect) expect(subscriptions.length).toBeGreaterThanOrEqual(2);
      if (reconnect || offlineCompletion || stop) {
        expect(operations.some((r) => r.url().includes("/events"))).toBe(true);
        return;
      }
      expect(
        operations.filter(
          (r) => r.url().endsWith("/subscriptions") && r.method() === "POST"
        )
      ).toHaveLength(1);
      expect(
        operations.filter((r) => r.url().includes("/events"))
      ).toHaveLength(loseFirstBatch ? 1 : 0);

      expect(
        frames.filter(
          (f) =>
            f.includes("MESSAGE") && (f.includes("星河") || f.includes("继续"))
        ).length
      ).toBeGreaterThanOrEqual(2);
    }
  );
}
