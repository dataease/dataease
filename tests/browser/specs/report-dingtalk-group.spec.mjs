import { test, expect } from '@playwright/test'

test('selecting a DingTalk group does not read an undefined tag element', async ({ page }) => {
  const errors = []
  page.on('pageerror', error => errors.push(error.message))

  await page.goto('/')

  const groupField = page.locator('.ed-form-item').filter({
    has: page.locator('.ed-form-item__label', { hasText: /^report\.dingtalk_groups$/ })
  })
  await expect(groupField).toBeVisible()

  await groupField.locator('.ed-select').click()
  await page.getByRole('option', { name: '测试群消息-1', exact: true }).click()

  await expect(groupField.locator('.ed-tag')).toHaveCount(1)
  await page.waitForTimeout(200)
  expect(errors).toEqual([])
})
