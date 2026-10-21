import { test, expect } from '@playwright/test'

test('工具自检：加载页面并执行交互断言', async ({ page }) => {
  const errors = []
  page.on('pageerror', error => errors.push(error.message))
  const response = await page.goto('/')
  expect(response.status()).toBe(200)
  await expect(page).toHaveTitle('浏览器测试工具自检')
  await page.getByLabel('测试内容').fill('Playwright 工具已就绪')
  await page.getByRole('button', { name: '显示内容' }).click()
  await expect(page.getByLabel('显示结果')).toHaveText('Playwright 工具已就绪')
  expect(errors).toEqual([])
})
