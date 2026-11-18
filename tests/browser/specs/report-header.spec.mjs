import { test, expect } from '@playwright/test'
test('任务表头设置默认、保存重开、清空隐藏和默认恢复', async ({ page }) => {
  await page.goto('/')
  const dataset = page.getByRole('radio', {
    name: 'report.form.excel_header_dataset',
  })
  const display = page.getByRole('radio', {
    name: 'report.form.excel_header_display',
  })
  await expect(dataset).toBeChecked()
  await page
    .getByText('report.form.excel_header_display', { exact: true })
    .click()
  const saved = await page.evaluate(() => window.headerTest.save())
  expect(saved.excelHeaderMode).toBe('display')
  await page.evaluate((value) => window.headerTest.reopen(value), saved)
  await expect(display).toBeChecked()
  await page.getByRole('button', { name: 'Close this tag' }).click()
  await expect(display).toHaveCount(0)
  const cleared = await page.evaluate(() => window.headerTest.save())
  expect(cleared.excelHeaderMode).toBe('dataset')
  expect(cleared.viewIdList).toEqual([])
  await page.evaluate(
    (value) => window.headerTest.reopen({ ...value, viewIdList: ['view'] }),
    cleared
  )
  await expect(dataset).toBeChecked()
})
