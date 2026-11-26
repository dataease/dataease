const { test, expect } = require("@playwright/test");

test.skip(
  !process.env.DATASET_ASSISTANT_BACKEND,
  "requires the isolated conversation-socket runner"
);

for (const editor of ["SQL", "计算字段", "SQL gap"]) {
  test(
    editor +
      " editor receives the original model stream over its platform socket",
    async ({ page }) => {
      const loseFirstBatch = editor === "SQL gap";
      const operations = [];
      const frames = [];
      page.on("websocket", (socket) =>
        socket.on("framereceived", (frame) =>
          frames.push(String(frame.payload))
        )
      );
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
      await control("input").fill("解释这个表达式");
      await control("send").click();
      const panel = control("panel");
      await expect(panel).toContainText("星河", { timeout: 8000 });
      await expect(panel).not.toContainText("继续");
      await expect(panel).toContainText("星河继续", { timeout: 17000 });
      expect(
        operations.filter(
          (r) => r.url().endsWith("/executions") && r.method() === "POST"
        )
      ).toHaveLength(1);
      expect(
        operations.filter(
          (r) => r.url().endsWith("/subscriptions") && r.method() === "POST"
        )
      ).toHaveLength(1);
      expect(
        operations.filter((r) => r.url().includes("/events"))
      ).toHaveLength(loseFirstBatch ? 1 : 0);
      await expect(panel).not.toContainText("星河星河");
      expect(
        frames.filter(
          (f) =>
            f.includes("MESSAGE") && (f.includes("星河") || f.includes("继续"))
        ).length
      ).toBeGreaterThanOrEqual(2);
    }
  );
}
