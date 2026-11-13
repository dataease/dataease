import { test, expect } from '@playwright/test'
test('真实参数抽屉阻止空必填日期确认，补齐后允许确认', async ({ page }) => {
  await page.goto('/')
  await page.getByRole('button', { name: '打开参数' }).click()
  await page.evaluate(() => {
    window.reportRequired.filters.defaultFilter[0].defaultValue = null
  })
  await page
    .getByRole('button', { name: 'commons.confirm', exact: true })
    .click()
  await expect(page.getByTestId('confirmed')).toHaveText('0')
  await expect(page.locator('.ed-message')).toContainText('必填日期')
  await page.evaluate(() => {
    window.reportRequired.filters.defaultFilter[0].defaultValue = '2026-11-13'
  })
  await page
    .getByRole('button', { name: 'commons.confirm', exact: true })
    .click()
  await expect(page.getByTestId('confirmed')).toHaveText('1')
})
test('最终参数校验覆盖动态日期、范围、非必填、多条件及旧必填标记', async ({
  page,
}) => {
  await page.goto('/')
  await page.waitForFunction(() => window.reportRequired)
  const result = await page.evaluate(() => {
    const { hasRequiredFilterValue: valid, missingRequiredFilters: missing } =
      window.reportRequired
    const date = {
      id: 'a',
      name: 'A',
      required: true,
      displayType: '1',
      defaultValueCheck: true,
      timeType: 'fixed',
      defaultValue: '2026-11-13',
    }
    return {
      empty: [null, '', undefined, []].map((defaultValue) =>
        valid({ ...date, defaultValue })
      ),
      valid: valid(date),
      range: valid({
        ...date,
        displayType: '7',
        defaultValue: ['2026-11-13', null],
      }),
      dynamic: valid({
        ...date,
        timeType: 'dynamic',
        relativeToCurrent: 'today',
        timeGranularity: 'date',
        defaultValue: null,
      }),
      missing: missing(
        [
          date,
          { ...date, id: 'b', name: 'B' },
          { ...date, id: 'c', required: false },
        ],
        [
          { ...date, required: false, defaultValue: null },
          { ...date, id: 'b', defaultValue: [] },
        ]
      ),
      zero: valid({
        ...date,
        displayType: '22',
        defaultNumValueStart: 0,
        defaultNumValueEnd: 0,
      }),
    }
  })
  expect(result).toEqual({
    empty: [false, false, false, false],
    valid: true,
    range: false,
    dynamic: true,
    missing: ['A', 'B'],
    zero: true,
  })
})

test('真实抽屉拒绝缺少结束日期的必填范围和历史放宽标记', async ({ page }) => {
  await page.goto('/')
  await page.getByRole('button', { name: '打开参数' }).click()
  await page.evaluate(() => {
    const filters = window.reportRequired.filters
    Object.assign(filters.sourceFilter[0], { displayType: '7' })
    Object.assign(filters.defaultFilter[0], {
      displayType: '7',
      required: false,
      defaultValue: ['2026-11-13', null],
    })
  })
  await page
    .getByRole('button', { name: 'commons.confirm', exact: true })
    .click()
  await expect(page.getByTestId('confirmed')).toHaveText('0')
  await expect(page.locator('.ed-message')).toContainText('必填日期')
})

test('真实报告向导最终保存再次校验，失败时不调用保存接口', async ({ page }) => {
  await page.goto('/')
  await page.getByRole('button', { name: '打开报告' }).click()
  await page.getByRole('button', { name: 'common.next', exact: true }).click()
  await page.getByRole('button', { name: 'common.next', exact: true }).click()
  await page.evaluate(() => {
    window.reportSaveState.valid = false
  })
  await page.getByRole('button', { name: 'chart.confirm', exact: true }).click()
  await expect(page.getByTestId('saved')).toHaveText('0')
  await expect(
    page.getByRole('button', { name: 'common.next', exact: true })
  ).toBeVisible()
  await page.evaluate(() => {
    window.reportSaveState.valid = true
  })
  await page.getByRole('button', { name: 'common.next', exact: true }).click()
  await page.getByRole('button', { name: 'common.next', exact: true }).click()
  await page.getByRole('button', { name: 'chart.confirm', exact: true }).click()
  await expect(page.getByTestId('saved')).toHaveText('1')
})
