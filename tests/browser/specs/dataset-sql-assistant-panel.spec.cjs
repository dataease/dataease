const { test, expect } = require("@playwright/test");

test("the shared panel accepts ordinary questions and answers without editor data", async ({
  page,
}) => {
  await page.goto("/assistant-chat.html");
  const panel = page.getByRole("complementary", { name: "问答" });
  await expect(panel.getByRole("article")).toHaveCount(1);
  await expect(panel.getByText("第一条回答")).toBeVisible();
  await panel.getByRole("textbox").fill("第二条提问");
  await panel.getByRole("button", { name: "发送", exact: true }).click();
  await expect(panel.getByRole("article").nth(1)).toContainText("第二条提问");
  await expect(panel.getByRole("article").nth(1)).toContainText("第二条回答");
  await expect(panel.getByRole("button")).toHaveCount(1);
  await expect(panel.getByText("本轮引用")).toHaveCount(0);
});

for (const assistant of ["SQL 助手", "计算字段助手"]) {
  test(
    assistant +
      " resizes within the editor and restores its responsive default",
    async ({ page }) => {
      await page.route("**/fixture-api/ai/availability", (route) =>
        route.fulfill({ json: { available: true } })
      );
      await page.goto("/?sqlNode=1");
      if (assistant === "SQL 助手") {
        await page
          .getByRole("button", { name: "数据表关联", exact: true })
          .click();
        const row = page.locator(".node-item").filter({
          has: page.getByRole("button", { name: "items", exact: true }),
        });
        await row.locator(".hover-icon").click();
        await page
          .getByRole("menuitem", { name: "编辑SQL", exact: true })
          .click();
      } else {
        await page
          .getByRole("button", { name: "新建计算字段", exact: true })
          .click();
      }
      await page.getByRole("button", { name: assistant, exact: true }).click();
      const panel = page.getByRole("complementary", { name: assistant });
      const initial = await panel.boundingBox();
      const grip = panel.getByRole("separator", { name: "调整助手宽度" });
      await grip.press("ArrowLeft", { timeout: 4000 });
      const wider = await panel.boundingBox();
      expect(wider.width).toBeGreaterThan(initial.width);
      expect(wider.x + wider.width).toBeCloseTo(initial.x + initial.width, 0);
      await grip.press("End");
      const maximum = await panel.boundingBox();
      expect(maximum.x).toBeGreaterThanOrEqual(200);
      if (assistant === "计算字段助手") {
        const form = page.locator(".calc-field-workspace .calcu-field");
        const formula = form.locator(".cm-content");
        const searches = form.locator(".padding-lr-content input");
        expect((await formula.boundingBox()).width).toBeGreaterThanOrEqual(250);
        for (const search of await searches.all()) {
          expect((await search.boundingBox()).width).toBeGreaterThan(120);
        }
        await searches.first().fill("orders_id");
        await form.getByText("orders_id", { exact: true }).click();
        await expect(formula).toContainText("orders_id");
        await searches.last().fill("ABS");
        await form.getByText("ABS(-1)", { exact: true }).click();
        await expect(formula).toContainText("ABS");
      }
      await grip.press("ArrowLeft");
      expect((await panel.boundingBox()).width).toBe(maximum.width);
      await grip.dblclick();
      expect((await panel.boundingBox()).width).toBe(initial.width);
      const box = await grip.boundingBox();
      await page.mouse.move(box.x + box.width / 2, box.y + 50);
      await page.mouse.down();
      await page.mouse.move(box.x - 80, box.y + 50);
      await page.mouse.up();
      expect((await panel.boundingBox()).width).toBeGreaterThan(
        initial.width + 70
      );
      await grip.press("Home");
      await grip.press("ArrowRight");
      expect((await panel.boundingBox()).width).toBe(initial.width);
      await page.setViewportSize({ width: 800, height: 680 });
      await expect
        .poll(async () => (await panel.boundingBox()).width)
        .toBe(288);
      await expect(panel.getByRole("textbox")).toBeInViewport();
      if (assistant === "计算字段助手") {
        await expect(grip).toHaveCount(0);
        const form = page.locator(".calc-field-workspace .calcu-field");
        await form.getByText("ABS(-1)", { exact: true }).click();
        await expect(form.locator(".cm-content")).toContainText(
          "ABS(-1)ABS(-1)"
        );
        await expect(panel.getByRole("textbox")).toBeInViewport();
      }
    }
  );
}

test("successive questions are right aligned and separated from the following reply group", async ({
  page,
}) => {
  await page.goto("/assistant-chat.html");
  const panel = page.getByRole("complementary", { name: "问答" });
  await panel.getByRole("textbox").fill("第二条提问");
  await panel.getByRole("textbox").press("Enter");
  const previous = panel.getByRole("article").first();
  const next = panel.getByRole("article").nth(1);
  const firstAnswer = previous.getByText("第一条回答");
  const question = next.getByText("第二条提问");
  const secondAnswer = next.getByText("第二条回答");
  await expect(secondAnswer).toBeVisible();
  const questionBox = await question.boundingBox();
  const answerBox = await secondAnswer.boundingBox();
  expect(questionBox.x).toBeGreaterThan(answerBox.x + 20);
  expect(answerBox.y - (questionBox.y + questionBox.height)).toBeCloseTo(10, 0);
  const previousBox = await firstAnswer.boundingBox();
  expect(
    questionBox.y - (previousBox.y + previousBox.height)
  ).toBeGreaterThanOrEqual(27);
  await expect(next).toHaveCSS("border-top-width", "1px");
  await expect(secondAnswer).toHaveCSS("font-size", "14px");
  await expect(secondAnswer).toHaveCSS("line-height", "22px");
});

test("question text stays readable in both the light and dark host palette", async ({
  page,
}) => {
  await page.goto("/assistant-chat.html");
  const question = page.getByText("第一条提问", { exact: true });
  const contrast = () =>
    question.evaluate((el) => {
      const ctx = document.createElement("canvas").getContext("2d");
      const luminance = (color) => {
        ctx.fillStyle = color;
        ctx.fillRect(0, 0, 1, 1);
        const rgb = Array.from(ctx.getImageData(0, 0, 1, 1).data)
          .slice(0, 3)
          .map((v) => {
            const channel = v / 255;
            return channel <= 0.04045
              ? channel / 12.92
              : ((channel + 0.055) / 1.055) ** 2.4;
          });
        return rgb[0] * 0.2126 + rgb[1] * 0.7152 + rgb[2] * 0.0722;
      };
      const style = getComputedStyle(el);
      const values = [
        luminance(style.color),
        luminance(style.backgroundColor),
      ].sort((a, b) => b - a);
      return (values[0] + 0.05) / (values[1] + 0.05);
    });
  expect(await contrast()).toBeGreaterThanOrEqual(4.5);
  await page.locator("#app").evaluate((host) => {
    host.style.colorScheme = "dark";
    host.style.setProperty("--ed-bg-color", "#232323");
    host.style.setProperty("--ed-text-color-primary", "#ebebeb");
    host.style.setProperty("--ed-border-color", "#434343");
  });
  expect(await contrast()).toBeGreaterThanOrEqual(4.5);
});
