import { test, expect } from '@playwright/test'

test('custom SQL table fields expand inline in the table tree', async ({ page }) => {
  const errors = []
  page.on('pageerror', error => errors.push(error.message))

  await page.goto('/')

  const table = page.locator('.table-tree-node').filter({ hasText: 'orders' })
  await expect(table).toBeVisible()
  await expect(table).toHaveAttribute('title', 'orders')
  await expect(table).not.toContainText('订单主表')
  await table.click()

  const fields = page.locator('.table-tree-field')
  await expect(fields).toHaveCount(3)
  await expect(fields.nth(0)).toContainText('order_id')
  await expect(fields.nth(1)).toContainText('order_date')
  await expect(fields.nth(2)).toContainText('amount')
  await expect(fields.nth(0)).toHaveAttribute('title', '订单 ID')

  expect(errors).toEqual([])
})
