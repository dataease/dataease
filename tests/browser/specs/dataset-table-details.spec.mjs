import { test, expect } from '@playwright/test'

test('table details uses a 16px refresh icon, a toolbar divider and single header borders', async ({
  page
}) => {
  const errors = []
  page.on('pageerror', error => errors.push(error.message))

  await page.goto('/')
  const dialog = page.getByRole('dialog')
  await expect(dialog).toBeVisible()

  const toolbar = dialog.locator('.table-details-toolbar')
  await expect(toolbar).toHaveCSS('border-bottom-width', '1px')
  await expect(toolbar).toHaveCSS('border-bottom-color', 'rgba(31, 35, 41, 0.15)')

  const refreshIcon = dialog.getByRole('button', { name: '刷新' }).locator('.ed-icon svg')
  await expect(refreshIcon).toHaveCSS('width', '16px')
  await expect(refreshIcon).toHaveCSS('height', '16px')

  const header = dialog.locator('.ed-table__header-wrapper')
  await expect(header).toHaveCSS('border-top-width', '0px')
  await expect(dialog.locator('th.ed-table__cell').first()).toHaveCSS('border-bottom-width', '1px')

  await dialog.getByRole('tab', { name: '字段详情' }).click()
  await expect(header).toHaveCSS('border-top-width', '0px')
  await expect(dialog.locator('th.ed-table__cell').first()).toHaveCSS('border-bottom-width', '1px')
  expect(errors).toEqual([])
})
