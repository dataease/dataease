import { test, expect } from '@playwright/test'

test('lineage map removes stale tooltips when switching nodes', async ({ page }) => {
  const errors = []
  page.on('pageerror', error => errors.push(error.message))

  await page.goto('/')

  const sqlServer = page.getByText('SQLServer', { exact: true })
  await expect(sqlServer).toHaveCount(1)
  await sqlServer.hover()
  await expect(page.getByText('SQLServer', { exact: true })).toHaveCount(2)

  await sqlServer.first().click()
  await expect(page.getByText('SQLServer', { exact: true })).toHaveCount(1)

  await page.mouse.move(10, 10)
  await expect(page.getByText('SQLServer', { exact: true })).toHaveCount(1)

  const sqlServerDataset = page.getByText('sqlserver', { exact: true })
  await sqlServerDataset.hover()
  await expect(page.getByText('sqlserver', { exact: true })).toHaveCount(2)

  await page.mouse.move(10, 10)
  await expect(page.getByText('sqlserver', { exact: true })).toHaveCount(1)

  expect(errors).toEqual([])
})
