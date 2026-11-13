import { expect, test } from '@playwright/test'

const datasetPath = process.env.PLAYWRIGHT_GROUPED_DATASET_PATH

test.skip(
  !process.env.PLAYWRIGHT_BASE_URL || !datasetPath || !process.env.PLAYWRIGHT_STORAGE_STATE,
  'Set the app URL, authenticated storage state, and disposable grouped dataset path'
)

test('requires reconfiguring a numeric group when its source column becomes text', async ({ page }) => {
  await page.goto(datasetPath)
  await page.locator('.node-union .hover-icon').click()
  await page.getByText('编辑SQL', { exact: true }).click()
  const sqlEditor = page.getByRole('dialog').filter({ has: page.locator('.cm-content') })
  await sqlEditor.locator('.cm-content').fill("SELECT CAST('abc' AS CHAR) AS amount")
  await sqlEditor.getByRole('button', { name: '运行', exact: true }).click()
  await expect(sqlEditor.getByText('abc', { exact: true })).toBeVisible()
  await sqlEditor.getByRole('button', { name: '保存', exact: true }).click()
  await sqlEditor.locator('.save-or-cancel .hover-icon').click()
  const fieldSelection = page.getByRole('dialog').filter({
    has: page.getByRole('button', { name: '确认', exact: true })
  })
  await fieldSelection.getByRole('button', { name: '确认', exact: true }).click()
  await expect(fieldSelection).toBeHidden()

  const saveEndpoint = '**/datasetTree/save'
  let saveRequests = 0
  await page.route(saveEndpoint, async route => {
    saveRequests++
    await route.abort()
  })
  const warning = page.getByText(/分组字段「Amount Group」的规则与源字段类型不匹配/)
  for (const name of ['保存并返回', '保存']) {
    await page.getByRole('button', { name, exact: true }).click()
    await expect(warning).toBeVisible()
    expect(saveRequests).toBe(0)
    await expect(warning).toBeHidden()
  }
  await page.unroute(saveEndpoint)

  await page.getByRole('tab', { name: '数据预览' }).click()
  const groupField = page.getByRole('treeitem', { name: 'Amount Group' })
  await groupField.hover()
  await groupField.locator('.menu-more').click()
  await page.getByText('编辑', { exact: true }).click()
  const groupEditor = page.getByRole('dialog', { name: '编辑分组字段' })
  await groupEditor.locator('.group-fields_item .ed-select').click()
  await page.getByRole('option', { name: 'abc', exact: true }).click()
  await groupEditor.getByRole('button', { name: '确认', exact: true }).click()
  await expect(groupEditor).toBeHidden()

  const saveResponse = page.waitForResponse(response => response.url().endsWith('/datasetTree/save'))
  await page.getByRole('button', { name: '保存', exact: true }).click()
  expect((await (await saveResponse).json()).code).toBe(0)
  await expect(page).toHaveURL(new URL(datasetPath, process.env.PLAYWRIGHT_BASE_URL).href)
  await page.reload()
  await page.getByRole('tab', { name: '批量管理' }).click()
  const source = page.getByRole('row').filter({ hasText: 'amount' }).first()
  await expect(source.getByText('文本', { exact: true }).first()).toBeVisible()
})
