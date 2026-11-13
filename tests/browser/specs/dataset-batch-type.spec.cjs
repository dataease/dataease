const { test, expect } = require(process.env.PLAYWRIGHT_TEST_MODULE ||
  "@playwright/test");
async function openRelations(page, url) {
  await page.goto(url);
  await expect(page.locator(".sql-result .preview-data")).toContainText(
    "orders:"
  );
  await page.getByRole("button", { name: "数据表关联", exact: true }).click();
}

for (const variant of ["", "calcAbsent=1&", "calcNull=1&"]) {
  test(`batch URL then restore, calc field variant: ${variant || "legacy defined"}`, async ({
    page,
  }) => {
    await openRelations(page, `/?dependency=1&${variant}`);
    await page.getByRole("button", { name: "下一步，数据处理" }).click();
    await page.getByRole("tab", { name: "批量管理", exact: true }).click();
    const rows = page.locator(".batch-area .ed-table__body tr");
    const row = (field) => rows.filter({ hasText: field }).first();
    const select = (field) =>
      row(field).locator(".ed-table-column--selection .ed-checkbox").click();
    const batchInput = page.locator(".batch-operate .ed-cascader input");
    const typeInput = (field) => row(field).locator(".ed-cascader input").first();
    const pick = async (text) => {
      await page
        .locator(".ed-cascader__dropdown:visible")
        .last()
        .getByText(text, { exact: true })
        .first()
        .click();
    };
    await select("orders_id");
    await select("计算字段");
    await expect
      .poll(() => batchInput.isEnabled(), { timeout: 10000 })
      .toBe(true);
    await batchInput.click();
    await pick("URL");
    await expect(typeInput("orders_id")).toHaveValue("URL");
    await expect(typeInput("计算字段")).toHaveValue("URL");
    await expect
      .poll(() => batchInput.isEnabled(), { timeout: 10000 })
      .toBe(true);
    await batchInput.click();
    await pick("文本");
    await expect(typeInput("orders_id")).toHaveValue("文本");
    await expect(typeInput("计算字段")).toHaveValue("文本");
  });
}
