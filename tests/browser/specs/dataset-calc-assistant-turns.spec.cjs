const { test, expect } = require("@playwright/test");

const first = "[orders_id] - [items_id]";
const second = "([orders_id] - [items_id]) / [orders_id]";
const manual = "[items_id] * 2";
const expressions = [first, second, "1", "2", "3", "4"];
const button = (scope, name) =>
  scope.getByRole("button", { name, exact: true });

async function serveTurns(page, lifetime = 300000) {
  const state = {
    opens: [],
    checks: [],
    closed: [],
    replied: [],
    beforeResult: null,
    expressions: [...expressions],
  };
  await page.route("**/fixture-api/ai/**", async (route) => {
    const request = route.request();
    const path = new URL(request.url()).pathname;
    const reply = (json) => route.fulfill({ json });
    if (path.endsWith("/availability")) return reply({ available: true });
    if (path.endsWith("/conversations")) {
      const opening = request.postDataJSON();
      const index = state.opens.push(opening);
      return reply({
        editorId: opening.editorId,
        resourceId: opening.resourceId,
        resourceName: "Fixture MySQL",
        resourceVersion: "r1",
        sessionId: "s" + index,
        contextVersion: "v" + index,
        draftVersion: "d" + index,
        expiresAt: Date.now() + lifetime,
      });
    }
    const sessionId = path.split("/")[4];
    const n = Number(sessionId.slice(1));
    const executionId = "e" + n;
    const opening = state.opens[n - 1];
    if (request.method() === "DELETE") {
      state.closed.push(sessionId);
      return reply({});
    }
    if (path.endsWith("/executions"))
      return reply({ executionId, status: "RUNNING" });
    const result = state.clarification && n === 1 ? {
      type: "EXPLANATION",
      text: state.clarification,
    } : {
      type: "CALC_FIELD_SUGGESTION",
      text: "已生成公式。",
      calcField: {
        expression: state.expressions[n - 1],
        name: "建议名" + n,
        groupType: "q",
        deType: 3,
        validation: "DETAIL_PASS",
      },
      resourceId: opening.resourceId,
      resourceVersion: "r1",
      editorId: opening.editorId,
      draftVersion: "d" + n,
    };
    if (path.endsWith("/check-application")) {
      state.checks.push(sessionId);
      return reply(result);
    }
    if (path.endsWith("/events")) {
      if (state.beforeResult) await state.beforeResult(n);
      await reply({
        sessionId,
        executionId,
        contextVersion: "v" + n,
        status: "COMPLETED",
        cursor: 1,
        events: [],
        result,
      });
      state.replied.push(sessionId);
      return;
    }
    throw Error("Unexpected AI call: " + path);
  });
  return state;
}

async function openDrawer(page) {
  await button(page, "新建计算字段").click();
  const drawer = page.locator(".calc-field-drawer");
  await button(drawer, "计算字段助手").click();
  return drawer;
}

async function ask(drawer, question) {
  await drawer.locator("textarea").fill(question);
  await button(drawer, "发送").click();
}

async function completed(drawer, index) {
  const turn = drawer.getByRole("article").nth(index - 1);
  await expect(button(turn, "应用到当前草稿")).toBeEnabled();
  return turn;
}

test("a timed-out model answer retains its original formula and retries under the same question", async ({ page, context }) => {
  const state = await serveTurns(page);
  const retryRequests = [];
  await page.route("**/executions/e1/events?*", route => route.fulfill({ json: {
    sessionId: "s1", executionId: "e1", contextVersion: "v1", attemptId: "a1",
    retryEligible: true, status: "FAILED", cursor: 1,
    recovery: { stepKind: "MODEL", technicalRetries: 1, corrections: 0 },
    events: [{ sequence: 1, type: "FAILED", error: "timeout" }]
  }}));
  await page.route("**/executions/e1/retry", route => {
    retryRequests.push(route.request().postDataJSON());
    return route.fulfill({ json: { sessionId: "s1", executionId: "e2", contextVersion: "v1", attemptId: "a2", sourceAttemptId: "a1", status: "RUNNING" } });
  });
  await page.route("**/executions/e2/events?*", route => route.fulfill({ json: {
    sessionId: "s1", executionId: "e2", contextVersion: "v1", status: "COMPLETED", cursor: 1,
    events: [], result: { type: "EXPLANATION", text: "请确认分母字段。" }
  }}));
  await page.goto("/");
  const drawer = await openDrawer(page);
  const editor = drawer.locator(".cm-content");
  await editor.fill(first);
  await ask(drawer, "用这个公式算增长率");
  const question = drawer.getByRole("article");
  const error = question.getByRole("alert");
  await expect(error).toContainText("模型调用");
  await expect(error).toContainText("超时");
  await expect(error).toContainText("尚未生成公式");
  await context.grantPermissions(["clipboard-read", "clipboard-write"]);
  await button(error, "复制诊断信息").click();
  const diagnostic = await page.evaluate(() => navigator.clipboard.readText());
  expect(diagnostic).toContain("模型调用失败");
  expect(diagnostic).toContain("尚未生成公式");
  expect(diagnostic).toContain("e1");
  expect(diagnostic).not.toContain(first);
  await editor.fill(manual);
  await button(error, "重试").click();
  await expect(question.getByText("请确认分母字段。")).toBeVisible();
  await expect(question).toHaveCount(1);
  await expect(question.getByText("用这个公式算增长率", { exact: true })).toHaveCount(1);
  await expect(error).toBeHidden();
  await question.locator("summary").filter({hasText:"上次回答"}).click();
  await expect(error).toBeVisible();
  await expect(editor).toHaveText(manual);
  expect(state.opens.map(open => open.sql)).toEqual([first]);
  expect(retryRequests).toEqual([{expectedAttemptId: "a1", retryKey: expect.any(String)}]);
});

test("clarification accepts a free-text calculation period before suggesting a formula", async ({ page }) => {
  const state = await serveTurns(page);
  state.clarification = "增长率应对比哪个期间？\n1. 上月\n2. 去年同期";
  await page.goto("/");
  const drawer = await openDrawer(page);
  await ask(drawer, "算增长率");
  const question = drawer.getByRole("article").first();
  await expect(button(question, "上月")).toBeEnabled();
  await expect(button(question, "应用到当前草稿")).toHaveCount(0);
  const input = drawer.locator("textarea");
  await input.fill("使用上周的订单量");
  await input.press("Enter");
  await completed(drawer, 2);
  await expect(button(question, "上月")).toBeDisabled();
  await expect(button(question, "上月")).toHaveAttribute("aria-pressed", "false");
  expect(state.opens[1].previousSessionId).toBe("s1");
});

test("long conversations scroll inside the calculated field drawer", async ({
  page,
}) => {
  await page.setViewportSize({ width: 1280, height: 720 });
  const state = await serveTurns(page);
  await page.goto("/");
  const drawer = await openDrawer(page);
  const history = drawer.getByRole("log");
  const editor = drawer.locator(".cm-content");
  const editorTop = (await editor.boundingBox()).y;
  for (let i = 1; i <= 5; i++) {
    await ask(drawer, "第 " + i + " 次调整计算公式");
    await completed(drawer, i);
  }
  expect(
    await history.evaluate((el) => el.scrollHeight - el.clientHeight)
  ).toBeGreaterThan(0);
  const latest = history.getByRole("article").last();
  await expect(button(latest, "应用到当前草稿")).toBeInViewport({ ratio: 1 });
  await expect(drawer.locator("textarea")).toBeInViewport({ ratio: 1 });
  await expect(button(drawer, "发送")).toBeInViewport({ ratio: 1 });
  await drawer.screenshot({
    path: test.info().outputPath("calc-field-conversation.png"),
  });

  let release;
  const hold = new Promise((resolve) => {
    release = resolve;
  });
  state.beforeResult = () => hold;
  await ask(drawer, "继续调整公式");
  const oldest = history.getByRole("article").first();
  await oldest.scrollIntoViewIfNeeded();
  release();
  await completed(drawer, 6);
  await expect(oldest).toBeInViewport({ ratio: 1 });
  await expect(drawer.locator("textarea")).toBeInViewport({ ratio: 1 });
  expect((await editor.boundingBox()).y).toBe(editorTop);
  const header = drawer.locator("header");
  for (const action of ["计算字段助手", "取消", "校验", "确认"]) {
    await expect(button(header, action)).toBeInViewport({ ratio: 1 });
  }
});

test("the current formula or any selected turn is frozen as the basis of each question", async ({
  page,
}) => {
  const state = await serveTurns(page);
  await page.goto("/");
  const drawer = await openDrawer(page);
  await ask(drawer, "逐行生成差额");
  const initial = await completed(drawer, 1);
  await button(drawer, "计算字段助手").click();
  await expect(drawer.getByRole("log")).toBeHidden();
  await button(drawer, "计算字段助手").click();
  await ask(drawer, "再改成毛利率");
  const followUp = await completed(drawer, 2);
  expect(state.opens[1]).toMatchObject({ previousSessionId: "s1", sql: first });
  await followUp.locator("summary").click();
  await expect(followUp.locator("details pre")).toHaveText(first);
  await drawer.locator(".cm-content").fill(manual);
  await ask(drawer, "保留两位小数");
  const manualTurn = await completed(drawer, 3);
  expect(state.opens[2].sql).toBe(manual);
  await button(initial, "基于此建议追问").click({ timeout: 3000 });
  await ask(drawer, "除以十");
  await completed(drawer, 4);
  expect(state.opens[3]).toMatchObject({ previousSessionId: "s3", sql: first });
  await drawer.getByRole("checkbox", { name: "本轮引用" }).uncheck();
  await ask(drawer, "每条记录计为一");
  await completed(drawer, 5);
  expect(state.opens[4].sql).toBe("");
  await ask(drawer, "根据第二轮保留三位小数");
  await completed(drawer, 6);
  expect(state.opens[5].sql).toBe(second);
  expect(state.checks).toEqual(["s1", "s1", "s2"]);
  await manualTurn.locator("summary").click();
  await expect(manualTurn.locator("details pre")).toHaveText(manual);
  await expect(drawer.locator(".cm-content")).toHaveText(manual);
});

test("asking about a previous validation preserves the edited formula and original step", async ({ page }) => {
  const state = await serveTurns(page);
  const question = "第一条回答的第 1 步校验的是哪个公式？";
  const submits = [];
  page.on("request", request => {
    if (request.method() === "POST" && new URL(request.url()).pathname.endsWith("/executions"))
      submits.push(request.postDataJSON().text);
  });
  await page.route("**/executions/e1/events?*", route => route.fulfill({ json: {
    sessionId: "s1", executionId: "e1", contextVersion: "v1", status: "COMPLETED", cursor: 2,
    events: [{ sequence: 1, type: "TOOL", tool: {
      callId: "validation-a", name: "propose_calc_field", status: "SUCCEEDED", stepNumber: 1,
      parameters: { expression: first }, result: JSON.stringify({ status: "DETAIL_PASS" })
    } }, { sequence: 2, type: "COMPLETED" }],
    result: { type: "CALC_FIELD_SUGGESTION", text: "已校验原公式", calcField: {
      expression: first, name: "差额", groupType: "q", deType: 3, validation: "DETAIL_PASS"
    } }
  } }));
  const explanation = "当时校验通过的是 " + first;
  await page.route("**/executions/e2/events?*", route => route.fulfill({ json: {
    sessionId: "s2", executionId: "e2", contextVersion: "v2", status: "COMPLETED", cursor: 1,
    events: [{ sequence: 1, type: "COMPLETED" }], result: { type: "EXPLANATION", text: explanation }
  } }));
  await page.goto("/");
  const drawer = await openDrawer(page);
  await ask(drawer, "校验原公式");
  const original = await completed(drawer, 1);
  await expect(original).toContainText("步骤 1");
  await drawer.locator(".cm-content").fill(manual);
  await ask(drawer, question);
  const answer = drawer.getByRole("article").nth(1);
  await expect(answer).toContainText(explanation);
  expect(submits.at(-1)).toContain(question);
  expect(state.opens[1]).toMatchObject({ previousSessionId: "s1", sql: manual });
  await expect(button(answer, "应用到当前草稿")).toHaveCount(0);
  await expect(drawer.locator(".cm-content")).toHaveText(manual);
});

test("explicit revalidation uses the current draft even with a pending suggestion", async ({ page }) => {
  const state = await serveTurns(page);
  await page.goto("/");
  const drawer = await openDrawer(page);
  await drawer.locator(".cm-content").fill(manual);
  await ask(drawer, "给这个公式提一个建议");
  await completed(drawer, 1);
  await ask(drawer, "重新校验当前公式");
  await completed(drawer, 2);
  expect(state.opens[1].sql).toBe(manual);
  expect(state.checks).toEqual([]);
});

test("an edit made during generation is preserved until the author confirms an older suggestion", async ({
  page,
}) => {
  const state = await serveTurns(page);
  await page.goto("/?dependency=1");
  await page.getByRole("tab", { name: "批量管理" }).click();
  const row = page.getByRole("row", { name: /计算字段/ });
  await row.locator("td").last().getByRole("button").last().click();
  const drawer = page.locator(".calc-field-drawer");
  const code = drawer.locator(".cm-content");
  await expect(code).toHaveText("[orders_id]");
  await button(drawer, "计算字段助手").click();
  let release;
  const hold = new Promise((resolve) => {
    release = resolve;
  });
  state.beforeResult = () => hold;
  await ask(drawer, "生成毛利");
  await expect.poll(() => state.opens.length).toBe(1);
  await code.fill(manual);
  const basis = drawer.getByRole("article").first().locator("details").first();
  await basis.locator("summary").click({ timeout: 3000 });
  await expect(basis.locator("pre")).toHaveText("[orders_id]");
  release();
  const original = await completed(drawer, 1);
  await expect(code).toHaveText(manual);
  await ask(drawer, "改成整数");
  await completed(drawer, 2);
  expect(state.opens[1].sql).toBe(manual);
  const applyOld = button(original, "应用到当前草稿");
  await applyOld.click();
  const confirm = page.locator(".ed-message-box:visible");
  await expect(confirm).toContainText("这会覆盖发问后对草稿的修改。");
  await button(confirm, "取消").click();
  await expect(code).toHaveText(manual);
  expect(state.checks).toEqual([]);
  await applyOld.click();
  await button(confirm, "确定").click();
  await expect(code).toHaveText(first);
  expect(state.checks).toEqual(["s1"]);
  await expect(drawer.getByPlaceholder("请输入字段名称")).toHaveValue(
    "计算字段"
  );
  await expect(button(drawer, "维度")).toHaveClass(/is-active/);
  await button(drawer, "确认").click();
  await button(page, "保存").click();
  await expect
    .poll(() =>
      page.evaluate(() => window.saved?.allFields.find((f) => f.id === "calc"))
    )
    .toMatchObject({
      name: "计算字段",
      groupType: "d",
      deType: 0,
      originName: "[orders1] - [items1]",
    });
});

test("closing a pending drawer drops its late result and starts another editing activity", async ({
  page,
}) => {
  const state = await serveTurns(page);
  let release, received;
  const hold = new Promise((resolve) => {
    release = resolve;
  });
  const pending = new Promise((resolve) => {
    received = resolve;
  });
  state.beforeResult = async (n) => {
    if (n === 1) {
      received();
      await hold;
    }
  };
  await page.goto("/");
  const drawer = await openDrawer(page);
  await ask(drawer, "稍后返回的建议");
  await pending;
  await button(drawer, "取消").click();
  await expect(drawer).toBeHidden();
  await openDrawer(page);
  await drawer.locator(".cm-content").fill(manual);
  await ask(drawer, "新字段的建议");
  await completed(drawer, 1);
  release();
  await expect.poll(() => state.replied).toContain("s1");
  await expect(drawer.getByRole("article")).toHaveCount(1);
  await expect(drawer.getByRole("log")).not.toContainText("稍后返回的建议");
  await expect(drawer.locator(".cm-content")).toHaveText(manual);
  expect(state.opens[1].previousSessionId).toBeUndefined();
  expect(state.opens[1].editorId).not.toBe(state.opens[0].editorId);
  expect(state.closed).toContain("s1");
});

for (const scope of ["user", "expiry"]) {
  test(
    scope + " clears formula suggestions while preserving the author's draft",
    async ({ page }) => {
      const state = await serveTurns(page, scope === "expiry" ? 1800 : 300000);
      await page.goto("/");
      const drawer = await openDrawer(page);
      await drawer.locator(".cm-content").fill(manual);
      await ask(drawer, "建议一个比例");
      await completed(drawer, 1);
      if (scope === "user") await page.evaluate(() => window.aiSwitchUser());
      await expect(drawer.getByRole("article")).toHaveCount(0);
      await expect(drawer.locator(".cm-content")).toHaveText(manual);
      await expect.poll(() => state.closed).toEqual(scope === "user" ? [] : ["s1"]);
    }
  );
}

test("a changed calculation parameter invalidates a proposal's field bindings", async ({
  page,
}) => {
  const state = await serveTurns(page);
  state.expressions[0] = "[orders_id] * [比例]";
  await page.goto("/");
  const drawer = await openDrawer(page);
  await drawer.locator(".hover-icon_quota").click();
  const param = page.locator(".create-dialog:visible");
  await param.getByRole("textbox").fill("比例");
  await param.getByRole("spinbutton").fill("3");
  await button(param, "确认").click();
  await ask(drawer, "按比例计算差额");
  const old = await completed(drawer, 1);
  const item = drawer.locator(".item-quota").filter({ hasText: /^比例$/ });
  await item.hover();
  await item.locator(".icon-right .hover-icon").first().click();
  await param.getByRole("textbox").fill("新比例");
  await button(param, "确认").click();
  await button(old, "应用到当前草稿").click();
  const warning = page.locator(".ed-message-box:visible");
  if (await warning.isVisible()) await button(warning, "确定").click();
  await expect(drawer.getByRole("alert")).toBeVisible();
  await expect(drawer.locator(".cm-content")).toHaveText("");
  expect(state.checks).toEqual([]);
});

test("a single blank basis keeps the discussion and recomputes the next formula reference", async ({
  page,
}) => {
  const state = await serveTurns(page);
  await page.goto("/");
  const drawer = await openDrawer(page);
  await drawer.locator(".cm-content").fill(manual);
  const checkbox = drawer.getByRole("checkbox", { name: "本轮引用" });
  const reference = drawer.getByRole("button", { name: /查看完整引用/ });
  await expect(reference).toContainText(manual);
  await ask(drawer, "按订单统计");
  const original = await completed(drawer, 1);
  await expect(reference).toContainText("建议 #1");
  await expect(reference).toContainText(first);
  await checkbox.uncheck();
  await ask(drawer, "沿用这个业务口径");
  const without = await completed(drawer, 2);
  expect(state.opens[1]).toMatchObject({ previousSessionId: "s1", sql: "" });
  await expect(without.getByText(/生成依据/)).toHaveCount(0);
  await expect(checkbox).toBeChecked();
  await expect(reference).toContainText(second);
  await button(original, "基于此建议追问").click();
  await ask(drawer, "请从头写，沿用刚才确定的口径");
  const scratch = await completed(drawer, 3);
  expect(state.opens[2]).toMatchObject({ previousSessionId: "s2", sql: "" });
  await expect(scratch.getByText(/生成依据/)).toHaveCount(0);
});

test("new conversation closes a running formula reply and starts from the current field", async ({
  page,
}) => {
  const state = await serveTurns(page);
  let release;
  const pending = new Promise((resolve) => {
    release = resolve;
  });
  let oldReply;
  state.beforeResult = async (n) => {
    if (n === 1) {
      oldReply = true;
      await pending;
    }
  };
  await page.goto("/");
  const drawer = await openDrawer(page);
  await ask(drawer, "旧对话的问题");
  await expect.poll(() => oldReply).toBe(true);
  await drawer.locator(".cm-content").fill(manual);
  try {
    await button(drawer, "新对话").click({ timeout: 3000 });
    await expect(drawer.getByRole("article")).toHaveCount(0);
    await expect.poll(() => state.closed).toEqual(["s1"]);
    await expect(drawer.locator(".cm-content")).toHaveText(manual);
    await expect(
      drawer.getByRole("checkbox", { name: "本轮引用" })
    ).toBeChecked();
    await ask(drawer, "新的问题");
    await completed(drawer, 1);
    expect(state.opens[1]).toMatchObject({
      sql: manual,
      resourceId: state.opens[0].resourceId,
    });
    expect(state.opens[1].previousSessionId).toBeUndefined();
    expect(state.opens[1].editorId).not.toBe(state.opens[0].editorId);
  } finally {
    release();
  }
  await expect.poll(() => state.replied).toContain("s1");
  await expect(drawer.getByRole("article")).toHaveCount(1);
  await expect(drawer.getByRole("log")).not.toContainText("旧对话的问题");
});

for (const [status, error, label, reason] of [
  ["CANCELLED", "cancelled", "已取消", "AI 请求已取消"],
  ["FAILED", "connection_interrupted", "失败", "连接中断超过 30 秒"],
]) {
  test(`a running tool shows its terminal reason and respects the open detail after ${error}`, async ({
    page,
  }) => {
    await serveTurns(page);
    const scope = { sessionId: "s1", contextVersion: "v1", executionId: "e1" };
    const tool = {
      callId: "formula",
      name: "propose_calc_field",
      parameters: { expression: first },
    };
    const ended = {
      ...scope,
      status,
      cursor: 3,
      events: [
        { sequence: 2, type: "TOOL", tool: { ...tool, status }, error },
        { sequence: 3, type: status, error },
      ],
    };
    let finish = false;
    await page.route("**/executions/e1/events**", (route) => {
      const after = Number(
        new URL(route.request().url()).searchParams.get("after")
      );
      return route.fulfill({
        json: finish
          ? ended
          : {
              ...scope,
              status: "RUNNING",
              cursor: 1,
              events: after
                ? []
                : [
                    {
                      sequence: 1,
                      type: "TOOL",
                      tool: { ...tool, status: "RUNNING" },
                    },
                  ],
            },
      });
    });
    await page.route("**/executions/e1/stop", (route) =>
      route.fulfill({ json: ended })
    );
    await page.goto("/");
    const drawer = await openDrawer(page);
    await ask(drawer, "检查正在执行的工具");
    const process = drawer.locator(".assistant-process");
    const detail = process.locator("details");
    const summary = detail.locator("summary");
    await expect(summary).toHaveText("步骤 1 · 校验公式 " + first + " · 执行中");
    await expect(detail).toHaveAttribute("open", "");
    if (status === "CANCELLED") {
      // Reopen the tool explicitly so the final event preserves the author's choice.
      await summary.click();
      await summary.click();
      await button(drawer, "停止生成").click();
      await expect(summary).toHaveText("步骤 1 · 校验公式 " + first + " · " + label);
      await expect(process).toHaveAttribute("open", "");
      await expect(detail).toHaveAttribute("open", "");
    } else {
      finish = true;
      await expect(summary).toHaveText("步骤 1 · 校验公式 " + first + " · " + label);
      await expect(process).not.toHaveAttribute("open", "");
      await expect(detail).not.toHaveAttribute("open", "");
      await process.locator(":scope > summary").click();
      await summary.click();
    }
    await expect(detail).toHaveCount(1);
    await expect(detail.getByText(reason, { exact: false })).toBeVisible();
  });
}

test("formula correction shows both validation outcomes and keeps the detail being read", async ({
  page,
}) => {
  await serveTurns(page);
  const tool = (sequence, callId, status, expression, verdict) => ({
    sequence,
    type: "TOOL",
    tool: {
      callId,
      name: "propose_calc_field",
      status,
      parameters: { expression },
      result: verdict && JSON.stringify(verdict),
    },
  });
  let finish, entered;
  const hold = new Promise((resolve) => {
    finish = resolve;
  });
  const waiting = new Promise((resolve) => {
    entered = resolve;
  });
  await page.route("**/executions/e1/events**", async (route) => {
    const after = Number(
      new URL(route.request().url()).searchParams.get("after")
    );
    const scope = { sessionId: "s1", contextVersion: "v1", executionId: "e1" };
    if (!after)
      return route.fulfill({
        json: {
          ...scope,
          status: "RUNNING",
          cursor: 5,
          events: [
            { sequence: 1, type: "TEXT", text: "检查原公式。" },
            tool(2, "one", "STARTED", "[missing]"),
            tool(3, "one", "SUCCEEDED", "[missing]", {
              status: "INVALID",
              message: "字段不存在",
            }),
            { sequence: 4, type: "TEXT", text: "改用订单字段。" },
            tool(5, "two", "RUNNING", "[orders_id]"),
          ],
        },
      });
    entered();
    await hold;
    return route.fulfill({
      json: {
        ...scope,
        status: "COMPLETED",
        cursor: 8,
        metering: { elapsedMillis: 2670, usageStatus: "PARTIAL", inputTokens: null, outputTokens: 12 },
        events: [
          tool(6, "two", "SUCCEEDED", "[orders_id]", { status: "DETAIL_PASS" }),
          { sequence: 7, type: "TEXT", text: "已校验修正后的公式。" },
          { sequence: 8, type: "COMPLETED" },
        ],
        result: {
          type: "CALC_FIELD_SUGGESTION",
          text: "已校验修正后的公式。",
          calcField: { expression: "[orders_id]", validation: "DETAIL_PASS" },
        },
      },
    });
  });
  await page.goto("/");
  const drawer = await openDrawer(page);
  await ask(drawer, "检查订单公式");
  const process = drawer
    .locator("details")
    .filter({ has: page.locator("summary", { hasText: "执行过程" }) })
    .first();
  const rejected = drawer
    .locator("details")
    .filter({ has: page.locator("summary", { hasText: "步骤 1 · 校验公式 [missing] · 未通过" }) })
    .last();
  const running = drawer
    .locator("details")
    .filter({ has: page.locator("summary", { hasText: "步骤 2 · 校验公式 [orders_id] · 执行中" }) })
    .last();
  try {
    await expect(rejected.locator("summary")).toBeVisible({ timeout: 3000 });
    await expect(rejected).not.toHaveAttribute("open", "");
    await expect(running).toHaveAttribute("open", "");
    await rejected.locator("summary").click();
    await expect(rejected).toContainText("字段不存在");
    await waiting;
  } finally {
    finish();
  }
  await expect(button(drawer, "应用到当前草稿")).toBeEnabled();
  await expect(process).toHaveAttribute("open", "");
  await expect(rejected).toHaveAttribute("open", "");
  const summary = process.locator(":scope > summary");
  await expect(summary).toHaveText("执行过程 · 总耗时 2.67 s");
  await summary.click();
  await expect(rejected.locator("summary")).toBeHidden();
  await expect(summary).toHaveText("执行过程 · 总耗时 2.67 s");
  await summary.click();
  await expect(rejected.getByText(/字段不存在/)).toBeVisible();
  const footer = drawer.getByRole("article").locator("footer");
  await expect(footer).toContainText("输入 Token —");
  await expect(footer).toContainText("输出 Token 12");
  await expect(footer).toContainText("用量不完整");
  await expect(process.locator("details")).toHaveCount(2);
  const passed = process.locator("details").last();
  await expect(passed.locator("summary")).toHaveText("步骤 2 · 校验公式 [orders_id] · 通过");
  await expect(passed).not.toHaveAttribute("open", "");
  await expect(process).not.toContainText("已校验修正后的公式。");
  await expect(
    drawer.getByText("已校验修正后的公式。", { exact: true })
  ).toBeVisible();
});

test("rejected HTTP admission shows the public reason and request ID in the copied diagnostic", async ({
  page,
  context,
}) => {
  await serveTurns(page);
  const requestId = "deadbeef00112233445566778899aabb";
  await page.route("**/s1/executions", (route) =>
    route.fulfill({
      status: 409,
      json: {
        code: 50005,
        category: "CONFLICT",
        messageKey: "i18n_ai_conflict",
        msg: "编辑版本已更新，请确认后重新提问。",
        requestId,
        data: { privateValue: "internal-fixture-only" },
      },
    })
  );
  await page.goto("/");
  const drawer = await openDrawer(page);
  await ask(drawer, "检查版本");
  const error = drawer.getByRole("alert");
  await expect(error).toContainText("编辑版本已更新，请确认后重新提问。", {
    timeout: 3000,
  });
  await expect(error).toContainText(requestId);
  await context.grantPermissions(["clipboard-read", "clipboard-write"]);
  await button(error, "复制诊断信息").click();
  const copied = await page.evaluate(() => navigator.clipboard.readText());
  expect(copied).toContain(requestId);
  expect(copied).toContain("编辑版本已更新");
  expect(copied).not.toContain("internal-fixture-only");
});

test("new conversation retries the exact old session close after a route error", async ({
  page,
}) => {
  const state = await serveTurns(page);
  const closes = [];
  await page.route("**/ai/conversations/s1", (route) => {
    closes.push(route.request().url());
    if (closes.length === 1)
      return route.fulfill({
        status: 502,
        json: {
          category: "UPSTREAM",
          messageKey: "i18n_ai_execution_route_unavailable",
          msg: "Origin unavailable",
        },
      });
    return route.fallback();
  });
  await page.goto("/");
  const drawer = await openDrawer(page);
  await ask(drawer, "旧会话");
  await completed(drawer, 1);
  await button(drawer, "新对话").click();
  await ask(drawer, "新会话");
  await completed(drawer, 1);
  await expect.poll(() => state.closed, { timeout: 4000 }).toEqual(["s1"]);
  expect(closes).toHaveLength(2);
  expect(state.opens).toHaveLength(2);
});

test("lost open, submission and retry responses reconcile once and a reply follows the retried question", async ({
  page,
}) => {
  const state = await serveTurns(page);
  const accepted = new Map();
  const keys = new Map();
  const seen = new Set();
  await page.route("**/fixture-api/ai/conversations**", async (route) => {
    if (route.request().method() !== "POST") return route.fallback();
    const path = new URL(route.request().url()).pathname;
    const body = route.request().postDataJSON();
    const kind =
      path.endsWith("/conversations") && !body.previousSessionId
        ? "open"
        : path.endsWith("/s1/executions")
        ? "ask"
        : path.endsWith("/e1/retry")
        ? "retry"
        : undefined;
    if (!kind) return route.fallback();
    const key = body.openKey ?? body.submissionKey ?? body.retryKey;
    keys.set(kind, [...(keys.get(kind) ?? []), key]);
    if (!accepted.has(key)) {
      if (kind === "open") {
        state.opens.push(body);
        accepted.set(key, {
          ...body,
          sessionId: "s1",
          contextVersion: "v1",
          expiresAt: Date.now() + 300000,
        });
      } else
        accepted.set(key, {
          executionId: kind === "retry" ? "er" : "e1",
          attemptId: kind === "retry" ? "ar" : "a1",
          status: "RUNNING",
        });
    }
    if (!seen.has(kind)) {
      seen.add(kind);
      return route.abort("connectionreset");
    }
    return route.fulfill({ json: accepted.get(key) });
  });
  await page.route("**/s1/executions/e1/events?*", (route) =>
    route.fulfill({
      json: {
        sessionId: "s1",
        executionId: "e1",
        contextVersion: "v1",
        attemptId: "a1",
        retryEligible: true,
        status: "FAILED",
        cursor: 1,
        events: [{ sequence: 1, type: "FAILED", error: "timeout" }],
      },
    })
  );
  await page.route("**/s1/executions/er/events?*", (route) =>
    route.fulfill({
      json: {
        sessionId: "s1",
        executionId: "er",
        contextVersion: "v1",
        attemptId: "ar",
        status: "COMPLETED",
        cursor: 1,
        events: [],
        result: {
          type: "EXPLANATION",
          text: "按哪个期间？\n1. 上月\n2. 去年同期",
        },
      },
    })
  );
  await page.goto("/");
  const drawer = await openDrawer(page);
  await drawer.locator(".cm-content").fill(manual);
  await ask(drawer, "增长率 A");
  const old = drawer.getByRole("article").first();
  await expect(button(old, "重试")).toBeEnabled({ timeout: 4000 });
  await ask(drawer, "回答 B");
  await completed(drawer, 2);
  await button(old, "重试").click();
  await button(old, "上月").click({ timeout: 4000 });
  await completed(drawer, 3);
  expect(state.opens[2]).toMatchObject({
    previousSessionId: "s1",
    previousExecutionId: "er",
    sql: manual,
  });
  for (const kind of ["open", "ask", "retry"]) {
    const [first, replay] = keys.get(kind);
    expect(first).toBeTruthy();
    expect(replay).toBe(first);
  }
  expect(accepted.size).toBe(3);
});

test("stopping a pending manual retry records that attempt and the next retry has a fresh key", async ({
  page,
}) => {
  await serveTurns(page);
  const keys = [];
  let release;
  const hold = new Promise((resolve) => {
    release = resolve;
  });
  let offline = true;
  const failed = {
    sessionId: "s1",
    executionId: "e1",
    contextVersion: "v1",
    attemptId: "a1",
    retryEligible: true,
    status: "FAILED",
    cursor: 1,
    events: [{ sequence: 1, type: "FAILED", error: "timeout" }],
  };
  await page.route("**/e1/events?*", (route) =>
    route.fulfill({ json: failed })
  );
  await page.route("**/e1/retry", async (route) => {
    keys.push(route.request().postDataJSON());
    await hold;
    if (offline) return route.abort("connectionreset");
    return route.fulfill({
      json: { executionId: "er", attemptId: "ar", status: "RUNNING" },
    });
  });
  await page.route("**/er/stop", (route) =>
    route.fulfill({
      json: {
        ...failed,
        executionId: "er",
        attemptId: "ar",
        status: "CANCELLED",
        events: [{ sequence: 1, type: "CANCELLED" }],
      },
    })
  );
  await page.route("**/er/retry", (route) => {
    keys.push(route.request().postDataJSON());
    return route.fulfill({
      json: { executionId: "er2", attemptId: "ar2", status: "RUNNING" },
    });
  });
  await page.route("**/er2/events?*", (route) =>
    route.fulfill({
      json: {
        ...failed,
        executionId: "er2",
        attemptId: "ar2",
        retryEligible: false,
        status: "COMPLETED",
        events: [],
        result: { type: "EXPLANATION", text: "本次回答已完成。" },
      },
    })
  );
  await page.goto("/");
  const drawer = await openDrawer(page);
  await ask(drawer, "按月汇总");
  await button(drawer, "重试").click();
  try {
    await expect.poll(() => keys.length).toBe(1);
    await button(drawer, "停止生成").click();
  } finally {
    release();
  }
  await expect.poll(() => keys.length, { timeout: 4000 }).toBe(3);
  await expect(button(drawer, "核对同次提交")).toBeEnabled();
  await expect(button(drawer, "发送")).toHaveCount(0);
  offline = false;
  await page.evaluate(() => window.dispatchEvent(new Event("online")));
  await expect(drawer.getByText("回答已停止", { exact: true })).toBeVisible({
    timeout: 3000,
  });
  await button(drawer, "重试").click();
  await expect(drawer.getByText("本次回答已完成。")).toBeVisible();
  expect(keys.map((k) => k.expectedAttemptId)).toEqual([
    "a1",
    "a1",
    "a1",
    "a1",
    "ar",
  ]);
  expect(new Set(keys.slice(0, 4).map((k) => k.retryKey)).size).toBe(1);
  expect(keys[0].retryKey).not.toBe(keys[4].retryKey);
});

test("a definite retry rejection has its own copyable public reason and request ID", async ({
  page,
  context,
}) => {
  await serveTurns(page);
  await page.route("**/e1/events?*", (route) =>
    route.fulfill({
      json: {
        sessionId: "s1",
        contextVersion: "v1",
        executionId: "e1",
        attemptId: "a1",
        status: "FAILED",
        cursor: 1,
        retryEligible: true,
        events: [{ sequence: 1, type: "FAILED", error: "timeout" }],
      },
    })
  );
  const requestId = "abc00112233445566778899aabbccdde";
  await page.route("**/e1/retry", (route) =>
    route.fulfill({
      status: 409,
      json: {
        category: "CONFLICT",
        messageKey: "i18n_ai_conflict",
        msg: "这次重答的上下文已更新。",
        requestId,
        data: { private: "hidden-retry-detail" },
      },
    })
  );
  await page.goto("/");
  const drawer = await openDrawer(page);
  await ask(drawer, "计算");
  const error = drawer.getByRole("alert");
  await button(error, "重试").click();
  await expect(error).toContainText("这次重答的上下文已更新。", {
    timeout: 3000,
  });
  await expect(error).toContainText(requestId);
  await context.grantPermissions(["clipboard-read", "clipboard-write"]);
  await button(error, "复制诊断信息").click();
  const info = await page.evaluate(() => navigator.clipboard.readText());
  expect(info).toContain(requestId);
  expect(info).toContain("这次重答的上下文已更新。");
  expect(info).not.toContain("hidden-retry-detail");
});

test("a clarification after retry keeps the selected suggestion as its basis", async ({
  page,
}) => {
  await serveTurns(page);
  await page.route("**/e2/events?*", (route) =>
    route.fulfill({
      json: {
        sessionId: "s2",
        executionId: "e2",
        contextVersion: "v2",
        attemptId: "a2",
        status: "FAILED",
        retryEligible: true,
        cursor: 1,
        events: [{ sequence: 1, type: "FAILED", error: "timeout" }],
      },
    })
  );
  await page.route("**/e2/retry", (route) =>
    route.fulfill({
      json: {
        sessionId: "s2",
        executionId: "er",
        contextVersion: "v2",
        attemptId: "ar",
        status: "RUNNING",
      },
    })
  );
  await page.route("**/er/events?*", (route) =>
    route.fulfill({
      json: {
        sessionId: "s2",
        executionId: "er",
        contextVersion: "v2",
        status: "COMPLETED",
        cursor: 1,
        events: [],
        result: {
          type: "EXPLANATION",
          text: "按哪个期间？\n1. 上月\n2. 去年同期",
        },
      },
    })
  );
  await page.goto("/");
  const drawer = await openDrawer(page);
  await ask(drawer, "生成公式");
  const initial = await completed(drawer, 1);
  await drawer.locator(".cm-content").fill(manual);
  await button(initial, "基于此建议追问").click();
  await ask(drawer, "计算增长率");
  await button(drawer.getByRole("article").last(), "重试").click();
  const submission = page.waitForRequest((request) =>
    request.url().endsWith("/s3/executions")
  );
  await button(drawer, "上月").click();
  expect((await submission).postDataJSON().generationBasis).toEqual({
    kind: "SUGGESTION",
    number: 1,
    matchesDraft: false,
  });
});
