const { test, expect } = require(process.env.PLAYWRIGHT_TEST_MODULE ||
  "@playwright/test");
async function openRelations(page, url) {
  await page.goto(url);
  if (url.includes("create")) return;
  await expect(page.locator(".sql-result .preview-data")).toContainText(
    "orders:"
  );
  await page.getByRole("button", { name: "数据表关联", exact: true }).click();
  await page.evaluate(() => {
    window.previewRequests = [];
    window.previewCompleted = [];
  });
}
test("relation details, processing layout and save preserve nodes", async ({
  page,
}, testInfo) => {
  const errors = [];
  page.on("pageerror", (e) => {
    errors.push(e.message);
    console.log("PAGE ERROR:", e.message);
  });
  await openRelations(page, "/");
  await expect(page.locator(".node-title")).toHaveText("orders");
  await expect(page.locator(".node-fields .ed-table__body tr")).toHaveCount(3);
  await page.getByRole("button", { name: "items", exact: true }).click();
  await expect(page.locator(".node-title")).toHaveText("items");
  await expect(page.locator(".node-fields")).toContainText("items_extra");
  await page
    .locator(".node-fields .ed-table__body tr")
    .nth(2)
    .locator(".ed-checkbox")
    .click();
  await expect(
    page.locator(".node-fields .ed-table__body tr").nth(2).getByRole("checkbox")
  ).toBeChecked();
  await expect(page.locator(".field-actions")).toHaveCount(0);
  await page.screenshot({
    path: testInfo.outputPath("relation.png"),
    animations: "disabled",
  });
  await page.getByRole("button", { name: "下一步，数据处理" }).click();
  await expect(page.locator(".drag-mask_dataset")).toBeHidden();
  await expect(page.locator(".sql-result .preview-data")).toContainText(
    "orders:orders_id"
  );
  await expect(
    page.locator(".sql-result .ed-loading-mask:visible")
  ).toHaveCount(0);
  await page.screenshot({
    path: testInfo.outputPath("processing.png"),
    animations: "disabled",
  });
  await expect(page.locator(".table-list")).toBeHidden();
  await expect(
    page.getByRole("tab", { name: "批量管理", exact: true })
  ).toBeVisible();
  await page.getByRole("tab", { name: "批量管理", exact: true }).click();
  await expect(page.locator(".batch-area")).toBeVisible();
  await page.getByRole("button", { name: "数据表关联", exact: true }).click();
  await expect(page.locator(".drag-mask_dataset")).toBeVisible();
  await expect(page.locator(".node-title")).toHaveText("items");
  await expect(
    page.locator(".node-fields .ed-table__body tr").nth(2).getByRole("checkbox")
  ).toBeChecked();
  await page
    .getByRole("tab", { name: "数据预览", exact: true })
    .first()
    .click();
  await expect(page.locator(".node-preview")).toContainText(
    "items:items_extra"
  );
  const request = await page.evaluate(() => window.previewRequests.at(-1));
  expect(request.union).toHaveLength(1);
  expect(request.union[0].currentDs.id).toBe("items");
  expect(request.union[0].childrenDs).toEqual([]);
  await page.getByRole("button", { name: "下一步，数据处理" }).click();
  await page.getByRole("button", { name: "保存", exact: true }).click();
  await expect
    .poll(() =>
      page.evaluate(
        () => window.saved?.union[0].childrenDs[0].currentDsFields.length
      )
    )
    .toBe(3);
  expect(errors).toEqual([]);
});

test("rapid canvas selection ignores stale fields and node preview responses", async ({
  page,
}) => {
  await openRelations(page, "/");
  await expect(page.locator(".node-fields")).toContainText("orders_extra");
  await page
    .getByRole("tab", { name: "数据预览", exact: true })
    .first()
    .click();
  await page.getByRole("button", { name: "items", exact: true }).click();
  await expect(page.locator(".node-fields")).toContainText("items_extra");
  await page
    .getByRole("tab", { name: "数据预览", exact: true })
    .first()
    .click();
  await expect
    .poll(() =>
      page.evaluate(() => window.previewCompleted?.includes("orders"))
    )
    .toBe(true);
  await expect(page.locator(".node-preview")).toContainText("items:items_id");
  await expect(page.locator(".node-preview")).not.toContainText("orders:");

  await page
    .locator(".node-union")
    .filter({ has: page.locator(".tableName", { hasText: "orders" }) })
    .click();
  await page.getByRole("button", { name: "items", exact: true }).click();
  await expect(page.locator(".node-title")).toHaveText("items");
  await expect(page.locator(".node-fields")).toContainText("items_extra");
  await expect(page.locator(".node-fields")).not.toContainText("orders_id");
});

test("field lookup failure blocks transition and can retry", async ({
  page,
}) => {
  await openRelations(page, "/");
  await expect(page.locator(".node-fields")).toContainText("orders_extra");
  await page.evaluate(() => {
    window.failFields = true;
  });
  await page.getByRole("button", { name: "items", exact: true }).click();
  await expect(
    page
      .locator(".node-fields")
      .getByRole("button", { name: "刷新数据", exact: true })
  ).toBeVisible();
  await page.getByRole("button", { name: "下一步，数据处理" }).click();
  await expect(page.locator(".drag-mask_dataset")).toBeVisible();
  await page
    .locator(".node-fields")
    .getByRole("button", { name: "刷新数据", exact: true })
    .click();
  await expect(page.locator(".node-fields")).toContainText("items_extra");
  await page.getByRole("button", { name: "下一步，数据处理" }).click();
  await expect(page.locator(".drag-mask_dataset")).toBeHidden();
});

test("removing a referenced field confirms immediately and cancellation restores selection", async ({
  page,
}) => {
  await openRelations(page, "/?dependency=1");
  const first = page.locator(".node-fields .ed-table__body tr").first();
  await expect(page.locator(".node-fields .ed-table__body tr")).toHaveCount(3);
  await first.locator(".ed-checkbox").click();
  await expect(page.locator(".ed-message-box")).toBeVisible();
  await page
    .locator(".ed-message-box")
    .getByRole("button", { name: "取消", exact: true })
    .click();
  await expect(first.getByRole("checkbox")).toBeChecked();
  await expect(page.locator(".drag-mask_dataset")).toBeVisible();
  await first.locator(".ed-checkbox").click();
  await page
    .locator(".ed-message-box")
    .getByRole("button", { name: "确认", exact: true })
    .click();
  await expect(first.getByRole("checkbox")).not.toBeChecked();
  await page.getByRole("button", { name: "下一步，数据处理" }).click();
  await expect(page.locator(".drag-mask_dataset")).toBeHidden();
  await page.getByRole("button", { name: "保存", exact: true }).click();
  await expect
    .poll(() => page.evaluate(() => window.saved?.allFields.map((f) => f.id)))
    .toEqual(["orders2", "items1", "items2"]);
});

test("adding the first table enters inline field selection without a drawer", async ({
  page,
}) => {
  await openRelations(page, "/?create=1");
  const dataTransfer = await page.evaluateHandle(() => {
    const data = new DataTransfer();
    data.setData(
      "text",
      JSON.stringify({
        tableName: "new_orders",
        type: "db",
        datasourceId: "source",
      })
    );
    return data;
  });
  await page
    .locator(".drag-mask_dataset")
    .dispatchEvent("drop", { dataTransfer });
  await expect(page.locator(".node-title")).toHaveText("new_orders");
  await expect(page.locator(".node-fields .ed-table__body tr")).toHaveCount(3);
  await expect(page.locator(".ed-drawer:visible")).toHaveCount(0);
  await expect(
    page.getByRole("button", { name: "下一步，数据处理" })
  ).toBeEnabled();
  await expect(
    page.locator(".node-fields .ed-checkbox input:checked")
  ).toHaveCount(4);
});

test("node preview reloads when the same table is removed and added again", async ({
  page,
}) => {
  await openRelations(page, "/?create=1");
  const canvas = page.locator(".drag-mask_dataset");
  const addOrders = async () => {
    const dataTransfer = await page.evaluateHandle(() => new DataTransfer());
    const source = page
      .locator(".table-list .list-item_primary")
      .filter({ hasText: /^orders$/ });
    await source.dispatchEvent("dragstart", { dataTransfer });
    await canvas.dispatchEvent("drop", { dataTransfer });
    await source.dispatchEvent("dragend", { dataTransfer });
  };
  await addOrders();
  await expect(page.locator(".node-fields .ed-table__body tr")).toHaveCount(3);
  const previewTab = page
    .locator(".node-tabs")
    .getByRole("tab", { name: "数据预览", exact: true });
  await previewTab.click();
  const preview = page.locator(".node-preview");
  await expect(preview).toContainText("orders:orders_id");
  const originalId = await page.evaluate(
    () => window.previewRequests[0].union[0].currentDs.id
  );
  await canvas.locator(".node-union .hover-icon").click();
  await page
    .locator(".menu-more_popper:visible")
    .getByRole("menuitem", { name: "删除", exact: true })
    .click();
  await expect(canvas.locator(".node-union")).toHaveCount(0);
  await expect(preview).toHaveCount(0);
  await addOrders();
  await expect(previewTab).toHaveAttribute("aria-selected", "true");
  await expect(preview).toContainText("orders:orders_id");
  const requests = await page.evaluate(() =>
    window.previewRequests.map((request) => request.union[0].currentDs.id)
  );
  expect(requests).toHaveLength(2);
  expect(requests[1]).not.toBe(originalId);
});

test("first save retains changed and unchanged field types in batch management", async ({
  page,
}) => {
  await openRelations(page, "/?create=1");
  const dataTransfer = await page.evaluateHandle(() => new DataTransfer());
  await page
    .locator(".table-list .list-item_primary")
    .filter({ hasText: /^orders$/ })
    .dispatchEvent("dragstart", { dataTransfer });
  await page.locator(".drag-mask_dataset").dispatchEvent("drop", { dataTransfer });
  await expect(page.locator(".node-fields .ed-table__body tr")).toHaveCount(3);
  await page.getByRole("button", { name: "下一步，数据处理" }).click();
  await page.getByRole("tab", { name: "批量管理", exact: true }).click();
  const typeInput = (field) =>
    page
      .locator(".batch-area .ed-table__body tr")
      .filter({ hasText: field })
      .locator(".ed-cascader input");
  await typeInput("orders_id").click();
  await page
    .locator(".ed-cascader__dropdown:visible")
    .getByText("数值", { exact: true })
    .click();
  await expect(typeInput("orders_id")).toHaveValue("数值");
  await expect(typeInput("orders_name")).toHaveValue("文本");
  await page.getByRole("button", { name: "保存", exact: true }).click();
  const dialog = page.locator(".create-dialog:visible");
  await expect(dialog).toContainText("测试文件夹");
  await dialog.getByRole("button", { name: "确认", exact: true }).click();
  await expect(dialog).toHaveCount(0);
  await expect(typeInput("orders_id")).toHaveValue("数值");
  await expect(typeInput("orders_name")).toHaveValue("文本");
  expect(await page.evaluate(() => window.saved.allFields[0].deType)).toBe(2);
});

test("returning to relations invalidates the previous processing preview", async ({
  page,
}) => {
  await openRelations(page, "/");
  await expect(page.locator(".node-fields")).toContainText("orders_extra");
  await page.evaluate(() => {
    window.previewDelays = [450, 20];
  });
  await page.getByRole("button", { name: "下一步，数据处理" }).click();
  await page.getByRole("button", { name: "数据表关联", exact: true }).click();
  await page
    .locator(".node-fields .ed-table__body tr")
    .nth(2)
    .locator(".ed-checkbox")
    .click();
  await page.getByRole("button", { name: "下一步，数据处理" }).click();
  await expect
    .poll(() => page.evaluate(() => window.previewCompleted?.length))
    .toBe(2);
  await expect(page.locator(".sql-result .preview-data")).toContainText(
    "orders:orders_extra"
  );
});

test("field selections apply immediately and reuse loaded metadata", async ({
  page,
}) => {
  await openRelations(page, "/");
  await expect(page.locator(".node-fields .ed-table__body tr")).toHaveCount(3);
  const rows = page.locator(".node-fields .ed-table__body tr");
  await rows.nth(2).locator(".ed-checkbox").click();

  await expect(rows.nth(2).getByRole("checkbox")).toBeChecked();
  expect(await page.evaluate(() => window.fieldRequests.length)).toBe(1);
  await rows.first().locator(".ed-checkbox").click();
  await expect(rows.first().getByRole("checkbox")).not.toBeChecked();
  await rows.first().locator(".ed-checkbox").click();
  await expect(rows.first().getByRole("checkbox")).toBeChecked();
  await expect(rows.nth(2).getByRole("checkbox")).toBeChecked();
  expect(await page.evaluate(() => window.fieldRequests.length)).toBe(1);
  await page
    .getByRole("tab", { name: "数据预览", exact: true })
    .first()
    .click();
  await expect(page.locator(".node-preview")).toContainText(
    "orders:orders_extra"
  );
  const added = await page.evaluate(() =>
    window.previewRequests
      .at(-1)
      .allFields.find((f) => f.originName === "orders_extra")
  );
  expect(added.id).toBeTruthy();
  expect(added.datasetTableId).toBe("orders");
  await page.getByRole("button", { name: "下一步，数据处理" }).click();
  await expect(page.locator(".drag-mask_dataset")).toBeHidden();
});

test("back navigation without edits leaves the editor without a warning", async ({
  page,
}) => {
  const errors = [];
  page.on("pageerror", (error) => errors.push(error.message));
  await page.goto("/");
  await expect(page.locator(".sql-result .preview-data")).toContainText("orders:");
  await page.locator(".top .name > .ed-icon").click();
  await expect
    .poll(() => page.evaluate(() => window.navigations?.length || 0))
    .toBe(1);
  await expect(page.locator(".ed-message-box")).toHaveCount(0);
  expect(errors).toEqual([]);
});

test("back navigation warns about unsaved field changes after saving", async ({
  page,
}) => {
  const errors = [];
  page.on("pageerror", (error) => errors.push(error.message));
  await openRelations(page, "/");
  await expect(page.locator(".node-fields .ed-table__body tr")).toHaveCount(3);
  await page.getByRole("button", { name: "下一步，数据处理" }).click();
  await page.getByRole("button", { name: "保存", exact: true }).click();
  await expect.poll(() => page.evaluate(() => !!window.saved)).toBe(true);
  await page.getByRole("button", { name: "数据表关联", exact: true }).click();
  const extra = page.locator(".node-fields .ed-table__body tr").nth(2);
  await extra.locator(".ed-checkbox").click();
  await page.locator(".top .name > .ed-icon").click();
  await expect(page.locator(".ed-message-box")).toContainText(
    "当前的更改尚未保存"
  );
  await page
    .locator(".ed-message-box")
    .getByRole("button", { name: "取消", exact: true })
    .click();
  await expect(extra.getByRole("checkbox")).toBeChecked();
  expect(await page.evaluate(() => window.navigations?.length || 0)).toBe(0);
  await page.locator(".top .name > .ed-icon").click();
  await page
    .locator(".ed-message-box")
    .getByRole("button", { name: "确认", exact: true })
    .click();
  await expect
    .poll(() => page.evaluate(() => window.navigations?.length))
    .toBe(1);
  expect(errors).toEqual([]);
});

test("dragging over the canvas without dropping does not reload node fields", async ({
  page,
}) => {
  await openRelations(page, "/");
  await expect(page.locator(".node-fields .ed-table__body tr")).toHaveCount(3);
  const canvas = page.locator(".drag-mask_dataset");
  const before = await page.evaluate(() => window.fieldRequests.length);
  for (let i = 0; i < 12; i++) {
    await canvas.dispatchEvent("dragover", {
      clientX: 300 + i * 25,
      clientY: 100 + i * 4,
    });
    await canvas.dispatchEvent("dragleave");
  }
  expect(await page.evaluate(() => window.fieldRequests.length)).toBe(before);
  await expect(page.locator(".node-title")).toHaveText("orders");
  await expect(page.locator(".node-fields .ed-table__body tr")).toHaveCount(3);
  await page
    .locator(".node-fields .ed-table__body tr")
    .nth(2)
    .locator(".ed-checkbox")
    .click();
  await canvas.dispatchEvent("dragover", { clientX: 400, clientY: 120 });
  await canvas.dispatchEvent("dragleave");
  await expect(
    page.locator(".node-fields .ed-table__body tr").nth(2).getByRole("checkbox")
  ).toBeChecked();
  expect(await page.evaluate(() => window.fieldRequests.length)).toBe(before);
});

for (const sql of [false, true]) {
  test(`node preview serializes an isolated canvas (${
    sql ? "SQL" : "table"
  })`, async ({ page }) => {
    await openRelations(page, "/?dependency=1" + (sql ? "&sqlNode=1" : ""));
    await expect(page.locator(".node-fields .ed-table__body tr")).toHaveCount(
      3
    );
    await page.getByRole("button", { name: "items", exact: true }).click();
    await expect(page.locator(".node-fields")).toContainText("items_id");
    await page
      .getByRole("tab", { name: "数据预览", exact: true })
      .first()
      .click();
    await expect(page.locator(".node-preview")).toContainText("items:items_id");
    const request = await page.evaluate(() => window.previewRequests.at(-1));
    expect(request.union).toHaveLength(1);
    const union = request.union[0];
    expect(Object.keys(union.currentDs).sort()).toEqual(
      [
        "id",
        "datasourceId",
        "tableName",
        "type",
        "info",
        ...(sql ? ["sqlVariableDetails"] : []),
      ].sort()
    );
    expect(union.currentDs.id).toBe("items");
    expect(union.currentDs.type).toBe(sql ? "sql" : "db");
    expect(union.childrenDs).toEqual([]);
    expect(union.unionToParent).toEqual({ unionType: "left", unionFields: [] });
    expect(request.allFields).toEqual(union.currentDsFields);
    expect(request.allFields.map((f) => f.datasetTableId)).toEqual([
      "items",
      "items",
    ]);
    expect(request.allFields.map((f) => f.id)).toEqual(["items1", "items2"]);
    if (sql)
      expect(JSON.parse(union.currentDs.sqlVariableDetails)).toEqual([
        { variableName: "amount", type: "LONG", defaultValue: "1" },
      ]);
    await page.getByRole("button", { name: "下一步，数据处理" }).click();
    await expect
      .poll(() =>
        page.evaluate(() => window.previewRequests.at(-1).union[0].currentDs.id)
      )
      .toBe("orders");
    const full = await page.evaluate(() => window.previewRequests.at(-1));
    expect(full.union[0].childrenDs[0].currentDs.id).toBe("items");
    expect(full.union[0].childrenDs[0].unionToParent.unionType).toBe("inner");
    expect(full.union[0].childrenDs[0].unionToParent.unionFields).toHaveLength(
      1
    );
    expect(full.allFields.some((f) => f.id === "calc")).toBe(true);
  });
}

const openNodeMenu = async (page, name) => {
  const row = page
    .locator(".node-items .node-item")
    .filter({ has: page.getByRole("button", { name, exact: true }) });
  await row.locator(".hover-icon").click();
  return page.locator(".menu-more_popper:visible");
};

test("list nodes share canvas field selection and deletion commands", async ({
  page,
}) => {
  await openRelations(page, "/");
  await expect(page.locator(".node-fields .ed-table__body tr")).toHaveCount(3);
  let menu = await openNodeMenu(page, "items");
  await expect(menu.getByRole("menuitem")).toHaveCount(2);
  await expect(page.locator(".node-title")).toHaveText("orders");
  await menu.getByRole("menuitem", { name: "字段选择", exact: true }).click();
  await expect(page.locator(".node-title")).toHaveText("items");
  await expect(page.locator(".node-union.active-node .tableName")).toHaveText(
    "items"
  );
  menu = await openNodeMenu(page, "items");
  await menu.getByRole("menuitem", { name: "删除", exact: true }).click();
  await expect(
    page
      .locator(".node-items")
      .getByRole("button", { name: "items", exact: true })
  ).toHaveCount(0);
  await expect(page.locator(".node-union")).toHaveCount(1);
  await expect(page.locator(".node-title")).toHaveText("orders");
});

test("list SQL node supports rename and edit SQL", async ({ page }) => {
  await openRelations(page, "/?sqlNode=1");
  await expect(page.locator(".node-fields .ed-table__body tr")).toHaveCount(3);
  await page
    .locator(".node-union")
    .filter({ has: page.locator(".tableName", { hasText: "items" }) })
    .locator(".hover-icon")
    .click();
  await expect(
    page.locator(".menu-more_popper:visible").getByRole("menuitem")
  ).toHaveCount(4);
  const canvasCommands = await page
    .locator(".menu-more_popper:visible")
    .getByRole("menuitem")
    .allTextContents();
  await page.locator(".node-title").click();
  await expect(page.locator(".menu-more_popper:visible")).toHaveCount(0);
  let menu = await openNodeMenu(page, "items");
  await expect(menu.getByRole("menuitem")).toHaveCount(4);
  expect(await menu.getByRole("menuitem").allTextContents()).toEqual(
    canvasCommands
  );
  await menu.getByRole("menuitem", { name: "重命名", exact: true }).click();
  const url = page.url();
  const dialog = page.locator(".ed-dialog:visible");
  const input = dialog.locator("input");
  await input.fill("items_renamed");
  await input.press("Enter");
  await expect(page).toHaveURL(url);
  await expect(input).toHaveValue("items_renamed");
  await expect(
    page.locator(".node-items").getByRole("button", { name: "items", exact: true })
  ).toBeVisible();
  await dialog.getByRole("button", { name: "确定", exact: true }).click();
  await expect(dialog).toHaveCount(0);
  await expect(page).toHaveURL(url);
  await expect(
    page
      .locator(".node-items")
      .getByRole("button", { name: "items_renamed", exact: true })
  ).toBeVisible();
  await expect(
    page.locator(".node-union .tableName").filter({ hasText: "items_renamed" })
  ).toBeVisible();
  menu = await openNodeMenu(page, "items_renamed");
  await menu.getByRole("menuitem", { name: "编辑SQL", exact: true }).click();
  await expect(page.getByTestId("sql-editor")).toContainText("items_renamed");
  await expect(page.getByTestId("sql-editor")).toContainText("c2VsZWN0IDE=");
  await expect(page.getByTestId("sql-editor")).toContainText("amount");
});

test("deleting a list node preserves dependency confirmation", async ({
  page,
}) => {
  await openRelations(page, "/?dependency=1");
  await expect(page.locator(".node-fields .ed-table__body tr")).toHaveCount(3);
  let menu = await openNodeMenu(page, "orders");
  await menu.getByRole("menuitem", { name: "删除", exact: true }).click();
  await expect(page.locator(".ed-message-box")).toBeVisible();
  await page
    .locator(".ed-message-box")
    .getByRole("button", { name: "取消", exact: true })
    .click();
  await expect(page.locator(".node-union")).toHaveCount(2);
  menu = await openNodeMenu(page, "orders");
  await menu.getByRole("menuitem", { name: "删除", exact: true }).click();
  await page
    .locator(".ed-message-box")
    .getByRole("button", { name: "确认", exact: true })
    .click();
  await expect(page.locator(".node-union")).toHaveCount(0);
  await expect(page.locator(".node-items .node-item")).toHaveCount(0);
});

test("filtered selection applies immediately without clearing search or other checked fields", async ({
  page,
}) => {
  await openRelations(page, "/");
  await expect(page.locator(".node-fields .ed-table__body tr")).toHaveCount(3);
  const search = page.locator(".node-field-search input");
  await search.fill("extra");
  await expect(page.locator(".node-fields .ed-table__body tr")).toHaveCount(1);
  await page.locator(".node-fields .ed-table__header .ed-checkbox").click();
  await expect(search).toHaveValue("extra");
  await expect(
    page.locator(".node-fields .ed-table__body tr").getByRole("checkbox")
  ).toBeChecked();
  await search.clear();
  await expect(page.locator(".node-fields .ed-table__body tr")).toHaveCount(3);
  await expect(
    page.locator(".node-fields .ed-table__body input:checked")
  ).toHaveCount(3);
  await search.fill("extra");
  await page.locator(".node-fields .ed-table__header .ed-checkbox").click();
  await expect(search).toHaveValue("extra");
  await expect(
    page.locator(".node-fields .ed-table__body tr").getByRole("checkbox")
  ).not.toBeChecked();
  await search.clear();
  await expect(
    page.locator(".node-fields .ed-table__body input:checked")
  ).toHaveCount(2);
  expect(await page.evaluate(() => window.fieldRequests.length)).toBe(1);
  await page
    .getByRole("tab", { name: "数据预览", exact: true })
    .first()
    .click();
  await expect(page.locator(".node-preview")).toContainText("orders:orders_id");
  expect(
    await page.evaluate(() =>
      window.previewRequests.at(-1).allFields.map((f) => f.id)
    )
  ).toEqual(["orders1", "orders2"]);
});

test("field count and search share the standard tabs row", async ({ page }) => {
  await openRelations(page, "/");
  await expect(
    page.getByRole("tab", { name: "字段选择(2/3)", exact: true })
  ).toBeVisible();
  const tab = page.getByRole("tab", { name: "字段选择(2/3)", exact: true });
  const search = page.locator(".node-field-search input");
  const tabBox = await tab.boundingBox();
  const searchBox = await search.boundingBox();
  expect(
    Math.abs(tabBox.y + tabBox.height / 2 - searchBox.y - searchBox.height / 2)
  ).toBeLessThan(3);
  expect(searchBox.x).toBeGreaterThan(tabBox.x + tabBox.width);
  await expect(page.locator(".node-fields .field-block-option")).toHaveCount(0);
  await search.fill("extra");
  await expect(page.locator(".node-fields .ed-table__body tr")).toHaveCount(1);
  await page.locator(".node-fields .ed-table__body tr .ed-checkbox").click();
  await expect(
    page.getByRole("tab", { name: "字段选择(3/3)", exact: true })
  ).toBeVisible();
  await expect(search).toHaveValue("extra");
  await page
    .getByRole("tab", { name: "数据预览", exact: true })
    .first()
    .click();
  await expect(search).toBeHidden();
  await expect(page.locator(".node-preview")).toContainText(
    "orders:orders_extra"
  );
});

test("canvas fullscreen hides details and restores the split and field search", async ({
  page,
}, testInfo) => {
  await openRelations(page, "/");
  await expect(page.locator(".node-fields .ed-table__body tr")).toHaveCount(3);
  await page.getByRole("button", { name: "items", exact: true }).click();
  await expect(page.locator(".node-title")).toHaveText("items");
  await page.locator(".node-field-search input").fill("extra");
  await expect(page.locator(".node-fields .ed-table__body tr")).toHaveCount(1);
  const canvas = page.locator(".drag-mask_dataset");
  const heightBefore = (await canvas.boundingBox()).height;
  const requests = await page.evaluate(() => window.fieldRequests.length);
  await page
    .locator(".canvas-fullscreen")
    .getByRole("button", { name: "画布全屏", exact: true })
    .click();
  await expect(page.locator(".node-details-region")).toBeHidden();
  await expect
    .poll(async () => (await canvas.boundingBox()).height)
    .toBeGreaterThan(heightBefore + 200);
  await expect(
    page
      .locator(".canvas-fullscreen")
      .getByRole("button", { name: "退出全屏", exact: true })
  ).toBeVisible();
  await page.screenshot({
    path: testInfo.outputPath("canvas-fullscreen.png"),
    animations: "disabled",
  });
  await page
    .locator(".canvas-fullscreen")
    .getByRole("button", { name: "退出全屏", exact: true })
    .click();
  await expect(page.locator(".node-details-region")).toBeVisible();
  expect((await canvas.boundingBox()).height).toBe(heightBefore);
  await expect(page.locator(".node-title")).toHaveText("items");
  await expect(page.locator(".node-field-search input")).toHaveValue("extra");
  expect(await page.evaluate(() => window.fieldRequests.length)).toBe(requests);
});

test("node details fullscreen preserves preview and resets on step changes", async ({
  page,
}, testInfo) => {
  await openRelations(page, "/");
  await expect(page.locator(".node-fields .ed-table__body tr")).toHaveCount(3);
  await page.getByRole("button", { name: "items", exact: true }).click();
  await expect(page.locator(".node-title")).toHaveText("items");
  await page
    .getByRole("tab", { name: "数据预览", exact: true })
    .first()
    .click();
  await expect(page.locator(".node-preview")).toContainText("items:items_id");
  const actionBox = await page.locator(".details-fullscreen").boundingBox();
  const titleBox = await page.locator(".node-title").boundingBox();
  const tabsBox = await page.locator(".node-tabs-row").boundingBox();
  expect(
    Math.abs(
      actionBox.y + actionBox.height / 2 - titleBox.y - titleBox.height / 2
    )
  ).toBeLessThan(1);
  expect(actionBox.y + actionBox.height).toBeLessThanOrEqual(tabsBox.y);
  const details = page.locator(".node-details-region");
  const heightBefore = (await details.boundingBox()).height;
  const requests = await page.evaluate(() => window.fieldRequests.length);
  await page
    .locator(".details-fullscreen")
    .getByRole("button", { name: "节点详情全屏", exact: true })
    .click();
  await expect(page.locator(".dataset-canvas-region")).toBeHidden();
  await expect
    .poll(async () => (await details.boundingBox()).height)
    .toBeGreaterThan(heightBefore + 200);
  await expect(page.locator(".node-preview")).toContainText("items:items_id");
  await page.screenshot({
    path: testInfo.outputPath("details-fullscreen.png"),
    animations: "disabled",
  });
  await page
    .locator(".details-fullscreen")
    .getByRole("button", { name: "退出全屏", exact: true })
    .click();
  await expect(page.locator(".dataset-canvas-region")).toBeVisible();
  expect((await details.boundingBox()).height).toBe(heightBefore);
  await expect(
    page.getByRole("tab", { name: "数据预览", exact: true }).first()
  ).toHaveAttribute("aria-selected", "true");
  expect(await page.evaluate(() => window.fieldRequests.length)).toBe(requests);
  await page
    .locator(".details-fullscreen")
    .getByRole("button", { name: "节点详情全屏", exact: true })
    .click();
  await page.getByRole("button", { name: "下一步，数据处理" }).click();
  await expect(page.locator(".sql-result")).toBeVisible();
  await page.getByRole("button", { name: "数据表关联", exact: true }).click();
  await expect(page.locator(".dataset-canvas-region")).toBeVisible();
  await expect(details).toBeVisible();
  await expect(
    page
      .locator(".details-fullscreen")
      .getByRole("button", { name: "节点详情全屏", exact: true })
  ).toBeVisible();
});

test("expanded canvas follows viewport resizing and restores a fitting split", async ({
  page,
}) => {
  await openRelations(page, "/");
  await expect(page.locator(".node-fields .ed-table__body tr")).toHaveCount(3);
  await page.locator(".canvas-fullscreen").getByRole("button").click();
  await page.setViewportSize({ width: 1280, height: 760 });
  await expect
    .poll(
      async () =>
        (
          await page.locator(".drag-mask_dataset").boundingBox()
        ).height
    )
    .toBe(704);
  await page
    .locator(".canvas-fullscreen")
    .getByRole("button", { name: "退出全屏", exact: true })
    .click();
  await expect(page.locator(".node-details-region")).toBeVisible();
  const canvas = await page.locator(".drag-mask_dataset").boundingBox();
  const details = await page.locator(".node-details-region").boundingBox();
  expect(canvas.height + details.height).toBe(704);
  await expect(page.locator(".node-field-search input")).toBeVisible();
});

test("SQL node dialogs remain available with the canvas hidden by details fullscreen", async ({
  page,
}) => {
  await openRelations(page, "/?sqlNode=1");
  await expect(page.locator(".node-fields .ed-table__body tr")).toHaveCount(3);
  await page
    .locator(".details-fullscreen")
    .getByRole("button", { name: "节点详情全屏", exact: true })
    .click();
  let menu = await openNodeMenu(page, "items");
  await menu.getByRole("menuitem", { name: "重命名", exact: true }).click();
  await expect(page.locator(".ed-dialog:visible input")).toBeVisible();
  await page.locator(".ed-dialog:visible input").fill("expanded_sql");
  await page
    .locator(".ed-dialog:visible .ed-dialog__footer button")
    .last()
    .click();
  menu = await openNodeMenu(page, "expanded_sql");
  await menu.getByRole("menuitem", { name: "编辑SQL", exact: true }).click();
  await expect(page.getByTestId("sql-editor")).toBeVisible();
  await expect(page.getByTestId("sql-editor")).toContainText("expanded_sql");
});

const chooseNumericType = async (page, row) => {
  await row.locator(".field-type-editor").click();
  await page
    .locator(".ed-cascader__dropdown:visible")
    .getByText("数值", { exact: true })
    .click();
};

test("prototype field table edits dataset metadata and preserves it through preview and save", async ({
  page,
}, testInfo) => {
  const errors = [];
  page.on("pageerror", (error) => errors.push(error.message));
  await openRelations(page, "/");
  await expect(page.locator(".node-fields .ed-table__body tr")).toHaveCount(3);
  await expect(page.locator(".node-fields .ed-table__header th")).toHaveText([
    "",
    "物理字段名",
    "数据集字段名",
    "数据集字段类型",
    "来源物理表",
    "备注",
  ]);
  const row = page.locator(".node-fields .ed-table__body tr").first();
  await expect(row).toContainText("orders_id");
  await expect(row).toContainText("orders");
  await expect(row).toContainText("字段说明 orders_id");
  await row
    .getByRole("textbox", { name: "数据集字段名 orders_id", exact: true })
    .fill("订单标识");
  await chooseNumericType(page, row);
  await expect(
    row.getByRole("textbox", { name: "数据集字段名 orders_id", exact: true })
  ).toHaveValue("订单标识");
  expect(await page.evaluate(() => window.fieldRequests.length)).toBe(1);
  await page.screenshot({
    path: testInfo.outputPath("prototype-fields.png"),
    animations: "disabled",
  });
  await page
    .getByRole("tab", { name: "数据预览", exact: true })
    .first()
    .click();
  await expect(page.locator(".node-preview")).toContainText("orders:订单标识");
  const isolated = await page.evaluate(() => window.previewRequests.at(-1));
  expect(isolated.allFields.find((f) => f.id === "orders1")).toMatchObject({
    name: "订单标识",
    deType: 2,
  });
  expect(
    isolated.union[0].currentDsFields.find((f) => f.id === "orders1")
  ).toMatchObject({ originName: "orders_id", deType: 0 });
  await page.getByRole("button", { name: "下一步，数据处理" }).click();
  await page.getByRole("button", { name: "保存", exact: true }).click();
  await expect
    .poll(() =>
      page.evaluate(
        () => window.saved?.allFields.find((f) => f.id === "orders1").name
      )
    )
    .toBe("订单标识");
  await page.reload();
  await page.getByRole("button", { name: "数据表关联", exact: true }).click();
  await expect(
    page.getByRole("textbox", { name: "数据集字段名 orders_id", exact: true })
  ).toHaveValue("订单标识");
  await page
    .getByRole("tab", { name: "数据预览", exact: true })
    .first()
    .click();
  await expect(page.locator(".node-preview")).toContainText("orders:订单标识");
  expect(
    await page.evaluate(
      () =>
        window.previewRequests.at(-1).allFields.find((f) => f.id === "orders1")
          .deType
    )
  ).toBe(2);
  expect(errors).toEqual([]);
});

test("unchecked field metadata survives node switching and applies when selected", async ({
  page,
}) => {
  await openRelations(page, "/");
  await expect(page.locator(".node-fields .ed-table__body tr")).toHaveCount(3);
  let row = page.locator(".node-fields .ed-table__body tr").nth(2);
  await row
    .getByRole("textbox", { name: "数据集字段名 orders_extra", exact: true })
    .fill("新增金额");
  await chooseNumericType(page, row);
  await expect(row.getByRole("checkbox")).not.toBeChecked();
  await page.getByRole("button", { name: "items", exact: true }).click();
  await expect(page.locator(".node-title")).toHaveText("items");
  await page.getByRole("button", { name: "orders", exact: true }).click();
  row = page.locator(".node-fields .ed-table__body tr").nth(2);
  await expect(
    row.getByRole("textbox", { name: "数据集字段名 orders_extra", exact: true })
  ).toHaveValue("新增金额");
  await row.locator(".ed-checkbox").click();
  await page
    .getByRole("tab", { name: "数据预览", exact: true })
    .first()
    .click();
  await expect(page.locator(".node-preview")).toContainText("orders:新增金额");
  const field = await page.evaluate(() =>
    window.previewRequests
      .at(-1)
      .allFields.find((f) => f.originName === "orders_extra")
  );
  expect(field).toMatchObject({ name: "新增金额", deType: 2 });
  expect(field.id).toBeTruthy();
});

const openSingleSqlRefresh = async (page, suffix = "") => {
  await openRelations(page, "/?singleSql=1" + suffix);
  await expect(page.locator(".node-fields")).toContainText("orders_extra");
  await page.evaluate(() => {
    window.holdFieldRequests = true;
  });
  const menu = await openNodeMenu(page, "orders");
  await menu.getByRole("menuitem", { name: "编辑SQL", exact: true }).click();
  await page.getByRole("button", { name: "保存合成 SQL", exact: true }).click();
  await expect
    .poll(() => page.evaluate(() => window.pendingFields?.length))
    .toBe(2);
};

const releaseFields = async (page, index) => {
  await page.evaluate((i) => window.pendingFields[i](), index);
};

test("SQL refresh cannot overwrite a newer inline field selection", async ({
  page,
}) => {
  const errors = [];
  page.on("pageerror", (error) => errors.push(error.message));
  await openSingleSqlRefresh(page);
  // closeSqlNode starts first; the detail watcher starts the second request.
  await releaseFields(page, 1);
  await expect(page.locator(".node-fields .ed-table__body tr")).toHaveCount(3);
  const first = page.locator(".node-fields .ed-table__body tr").first();
  await first.locator(".ed-checkbox").click();
  await expect(first.getByRole("checkbox")).not.toBeChecked();
  await releaseFields(page, 0);
  await page.getByRole("button", { name: "下一步，数据处理" }).click();
  await page.getByRole("button", { name: "保存", exact: true }).click();
  await expect
    .poll(() =>
      page.evaluate(() => window.saved?.allFields.map((f) => f.originName))
    )
    .toEqual(["orders_name"]);
  expect(
    await page.evaluate(() =>
      window.saved.union[0].currentDsFields.map((f) => f.originName)
    )
  ).toEqual(["orders_name"]);
  expect(errors).toEqual([]);
});

test("SQL refresh ignores a node deleted before the response returns", async ({
  page,
}) => {
  const errors = [];
  page.on("pageerror", (error) => errors.push(error.message));
  await openSingleSqlRefresh(page);
  const menu = await openNodeMenu(page, "orders");
  await menu.getByRole("menuitem", { name: "删除", exact: true }).click();
  await expect(page.locator(".node-title")).toHaveCount(0);
  await releaseFields(page, 0);
  await releaseFields(page, 1);
  await expect(page.locator(".node-title")).toHaveCount(0);
  await expect(
    page.getByRole("button", { name: "下一步，数据处理" })
  ).toBeDisabled();
  // Wait for the rejected/fulfilled request's continuation, not a fixed network delay.
  await page.evaluate(
    () =>
      new Promise((resolve) =>
        requestAnimationFrame(() => requestAnimationFrame(resolve))
      )
  );
  expect(errors).toEqual([]);
});

test("single-node field lookup failure retries without losing saved metadata", async ({
  page,
}) => {
  await page.addInitScript(() => {
    window.failFields = true;
  });
  await openRelations(page, "/?singleSql=1");
  const retry = page
    .locator(".node-fields")
    .getByRole("button", { name: "刷新数据", exact: true });
  await expect(retry).toBeVisible();
  await page.getByRole("button", { name: "下一步，数据处理" }).click();
  await expect(page.locator(".drag-mask_dataset")).toBeVisible();
  await retry.click();
  await expect(page.locator(".node-fields .ed-table__body tr")).toHaveCount(3);
  await page.getByRole("button", { name: "下一步，数据处理" }).click();
  await page.getByRole("button", { name: "保存", exact: true }).click();
  await expect
    .poll(() => page.evaluate(() => window.saved?.allFields.map((f) => f.id)))
    .toEqual(["orders1", "orders2"]);
  expect(
    await page.evaluate(() =>
      window.saved.union[0].currentDsFields.map((f) => f.id)
    )
  ).toEqual(["orders1", "orders2"]);
});

for (const firstResponse of [0, 1]) {
  test(`SQL refresh synchronizes selected fields and IDs, response ${firstResponse} first`, async ({
    page,
  }) => {
    const errors = [];
    page.on("pageerror", (error) => errors.push(error.message));
    await openSingleSqlRefresh(page);
    await releaseFields(page, firstResponse);
    await releaseFields(page, 1 - firstResponse);
    await expect(page.locator(".node-fields .ed-table__body tr")).toHaveCount(
      3
    );
    await expect(
      page.locator(
        '.node-fields .ed-table__body input[type="checkbox"]:checked'
      )
    ).toHaveCount(3);
    await page.getByRole("button", { name: "下一步，数据处理" }).click();
    await page.getByRole("button", { name: "保存", exact: true }).click();
    await expect
      .poll(() => page.evaluate(() => window.saved?.allFields.length))
      .toBe(3);
    const saved = await page.evaluate(() => window.saved);
    expect(saved.union[0].currentDsFields.map((f) => f.id)).toEqual(
      saved.allFields.map((f) => f.id)
    );
    expect(
      saved.allFields.every((f) => f.id && f.datasetTableId === "orders")
    ).toBe(true);
    expect(new Set(saved.allFields.map((f) => f.id)).size).toBe(3);
    expect(errors).toEqual([]);
  });
}

for (const confirmDeletion of [true, false]) {
  test(`pending dependency deletion survives SQL response, confirm=${confirmDeletion}`, async ({
    page,
  }) => {
    const errors = [];
    page.on("pageerror", (error) => errors.push(error.message));
    await openSingleSqlRefresh(page, "&dependency=1");
    await releaseFields(page, 1);
    const first = page.locator(".node-fields .ed-table__body tr").first();
    await expect(first.getByRole("checkbox")).toBeChecked();
    await first.locator(".ed-checkbox").click();
    const dialog = page.locator(".ed-message-box");
    await expect(dialog).toBeVisible();
    await releaseFields(page, 0);
    await page.evaluate(
      () =>
        new Promise((resolve) =>
          requestAnimationFrame(() => requestAnimationFrame(resolve))
        )
    );
    await expect(dialog).toBeVisible();
    await dialog
      .getByRole("button", {
        name: confirmDeletion ? "确认" : "取消",
        exact: true,
      })
      .click();
    await page.evaluate(
      () =>
        new Promise((resolve) =>
          requestAnimationFrame(() => requestAnimationFrame(resolve))
        )
    );
    expect(errors).toEqual([]);
    if (confirmDeletion)
      await expect(first.getByRole("checkbox")).not.toBeChecked();
    else await expect(first.getByRole("checkbox")).toBeChecked();
    await page.getByRole("button", { name: "下一步，数据处理" }).click();
    await page.getByRole("button", { name: "保存", exact: true }).click();
    const expectedIds = confirmDeletion
      ? ["orders2"]
      : ["orders1", "orders2", "calc"];
    await expect
      .poll(() => page.evaluate(() => window.saved?.allFields.map((f) => f.id)))
      .toEqual(expectedIds);
    expect(
      await page.evaluate(() =>
        window.saved.union[0].currentDsFields.map((f) => f.id)
      )
    ).toEqual(expectedIds.filter((id) => id !== "calc"));
    expect(errors).toEqual([]);
  });
}

test("closing unchanged SQL editor does not invalidate an in-flight refresh", async ({
  page,
}) => {
  const errors = [];
  page.on("pageerror", (error) => errors.push(error.message));
  await openSingleSqlRefresh(page);
  const menu = await openNodeMenu(page, "orders");
  await menu.getByRole("menuitem", { name: "编辑SQL", exact: true }).click();
  await page.getByRole("button", { name: "关闭合成 SQL", exact: true }).click();
  expect(await page.evaluate(() => window.pendingFields.length)).toBe(2);
  await releaseFields(page, 1);
  await expect(page.locator(".node-fields .ed-table__body tr")).toHaveCount(3);
  await releaseFields(page, 0);
  await expect(
    page.locator('.node-fields .ed-table__body input[type="checkbox"]:checked')
  ).toHaveCount(3);
  await page.getByRole("button", { name: "下一步，数据处理" }).click();
  await page.getByRole("button", { name: "保存", exact: true }).click();
  await expect
    .poll(() => page.evaluate(() => window.saved?.allFields.length))
    .toBe(3);
  const saved = await page.evaluate(() => window.saved);
  expect(saved.union[0].currentDsFields.map((f) => f.id)).toEqual(
    saved.allFields.map((f) => f.id)
  );
  expect(saved.allFields.map((f) => f.originName)).toEqual([
    "orders_id",
    "orders_name",
    "orders_extra",
  ]);
  expect(errors).toEqual([]);
});

test("editing opens processing with centered icon tools and preserves changes on round trips", async ({
  page,
}) => {
  await page.goto("/");
  await expect(page.locator(".sql-result")).toBeVisible();
  await expect(page.locator(".drag-mask_dataset")).toBeHidden();
  const tools = page.locator(".top .editor-tools");
  await expect(tools.getByRole("button")).toHaveCount(3);
  await expect(tools.locator(".ed-icon")).toHaveCount(3);
  expect(
    await tools.evaluate((el) =>
      Math.abs(
        el.getBoundingClientRect().x +
          el.getBoundingClientRect().width / 2 -
          innerWidth / 2
      )
    )
  ).toBeLessThan(2);
  await expect(
    page
      .locator(".sql-result")
      .getByRole("button", { name: "新建计算字段", exact: true })
  ).toHaveCount(0);
  await tools.getByRole("button", { name: "数据表关联", exact: true }).click();
  await expect(page.locator(".node-fields .ed-table__body tr")).toHaveCount(3);
  await page
    .getByRole("textbox", { name: "数据集字段名 orders_id", exact: true })
    .fill("保留的修改");
  await page.getByRole("button", { name: "下一步，数据处理" }).click();
  await expect(page.locator(".sql-result .preview-data")).toContainText(
    "保留的修改"
  );
  await page.getByRole("button", { name: "保存", exact: true }).click();
  await expect
    .poll(() => page.evaluate(() => window.saved?.allFields[0].name))
    .toBe("保留的修改");
  await page.reload();
  await expect(page.locator(".sql-result")).toBeVisible();
  await expect(page.locator(".sql-result .preview-data")).toContainText(
    "保留的修改"
  );
});

test("source table opens a readonly preview and searchable field dialog without adding nodes", async ({
  page,
}) => {
  await openRelations(page, "/");
  const before = await page.locator(".node-item").count();
  const source = page
    .locator(".table-checkbox-list .list-item_primary")
    .filter({ hasText: "orders" });
  await source.click();
  const dialog = page.getByRole("dialog", { name: "orders", exact: true });
  await expect(dialog).toBeVisible();
  await expect(dialog.locator(".ed-table__body")).toContainText(
    "orders:orders_id"
  );
  await dialog.getByRole("tab", { name: "字段详情", exact: true }).click();
  await expect(dialog.locator(".ed-table__body tr")).toHaveCount(3);
  await dialog.getByRole("textbox").fill("ORDERS_NAME");
  await expect(dialog.locator(".ed-table__body tr")).toHaveCount(1);
  await expect(dialog.locator(".ed-table__body")).toContainText("orders_name");
  await expect(dialog.getByRole("checkbox")).toHaveCount(0);
  expect(await page.locator(".node-item").count()).toBe(before);
  const request = await page.evaluate(() => window.previewRequests.at(-1));
  expect(request.union).toHaveLength(1);
  expect(request.union[0].currentDs.tableName).toBe("orders");
  expect(request.union[0].childrenDs).toEqual([]);
  expect(request.union[0].currentDs.id).not.toBe("orders");
  for (const field of request.allFields) {
    expect(field.id).toBeTruthy();
    expect(field.datasetTableId).toBe(request.union[0].currentDs.id);
    expect(field.checked).toBe(true);
  }
  expect(new Set(request.allFields.map((f) => f.id)).size).toBe(3);
});

test("closed source metadata cannot overwrite a reopened table and failed requests can retry", async ({
  page,
}) => {
  await openRelations(page, "/");
  await expect(page.locator(".node-fields .ed-table__body tr")).toHaveCount(3);
  await page.evaluate(() => {
    window.holdFieldRequests = true;
  });
  await page
    .locator(".table-checkbox-list .list-item_primary")
    .filter({ hasText: "orders" })
    .click();
  await expect
    .poll(() => page.evaluate(() => window.pendingFields?.length))
    .toBe(1);
  await page
    .getByRole("dialog", { name: "orders", exact: true })
    .getByRole("button", { name: "Close this dialog" })
    .click();
  await page.evaluate(() => {
    window.holdFieldRequests = false;
  });
  await page
    .locator(".table-checkbox-list .list-item_primary")
    .filter({ hasText: "items" })
    .click();
  const dialog = page.getByRole("dialog", { name: "items", exact: true });
  await expect(dialog.locator(".ed-table__body")).toContainText(
    "items:items_id"
  );
  await page.evaluate(() =>
    window.pendingFields.splice(0).forEach((resolve) => resolve())
  );
  await expect(dialog.locator(".ed-table__body")).not.toContainText("orders:");
  await page.evaluate(() => {
    window.failFields = true;
  });
  await dialog.getByRole("button", { name: "刷新数据", exact: true }).click();
  await expect(dialog.locator(".ed-table__body tr")).toHaveCount(0);
  await dialog.getByRole("button", { name: "刷新数据", exact: true }).click();
  await expect(dialog.locator(".ed-table__body")).toContainText(
    "items:items_id"
  );
});

test("late source preview stays isolated and source dragging still adds a node", async ({
  page,
}, testInfo) => {
  await openRelations(page, "/");
  await page.evaluate(() => {
    window.previewDelays = [500, 20];
  });
  const source = (name) =>
    page
      .locator(".table-checkbox-list .list-item_primary")
      .filter({ hasText: name });
  await source("orders").click();
  await expect
    .poll(() => page.evaluate(() => window.previewRequests.length))
    .toBe(1);
  await page
    .getByRole("dialog", { name: "orders", exact: true })
    .getByRole("button", { name: "Close this dialog" })
    .click();
  await source("items").click();
  const dialog = page.getByRole("dialog", { name: "items", exact: true });
  await expect(dialog.locator(".ed-table__body")).toContainText(
    "items:items_id"
  );
  await expect
    .poll(() =>
      page.evaluate(() =>
        window.previewCompleted.includes(
          window.fieldRequestDetails.find(
            (r) => r.tableName === "orders" && r.id !== "orders"
          ).id
        )
      )
    )
    .toBe(true);
  await expect(dialog.locator(".ed-table__body")).not.toContainText("orders:");
  await dialog.getByRole("tab", { name: "字段详情", exact: true }).click();
  await page.screenshot({
    path: testInfo.outputPath("source-table-details.png"),
    animations: "disabled",
  });
  await dialog.getByRole("button", { name: "Close this dialog" }).click();
  await page.goto("/?create=1");
  await expect(source("orders")).toBeVisible();
  const before = await page.evaluate(() => window.fieldRequests?.length || 0);
  const dataTransfer = await page.evaluateHandle(() => new DataTransfer());
  await source("orders").dispatchEvent("dragstart", { dataTransfer });
  expect(await page.evaluate(() => window.fieldRequests?.length || 0)).toBe(
    before
  );
  await expect(
    page.getByRole("dialog", { name: "orders", exact: true })
  ).toHaveCount(0);
  await page
    .locator(".drag-mask_dataset")
    .dispatchEvent("drop", { dataTransfer });
  await expect
    .poll(() => page.evaluate(() => window.fieldRequests.length))
    .toBeGreaterThan(before);
  await expect(page.locator(".node-list .node-item")).toHaveCount(1);
});
