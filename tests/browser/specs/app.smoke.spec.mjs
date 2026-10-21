import { test, expect } from '@playwright/test'

test.skip(!process.env.PLAYWRIGHT_BASE_URL, '设置 PLAYWRIGHT_BASE_URL 后才访问应用')

test('DataEase 页面能加载并挂载 Vue 应用', async ({ page }) => {
  const errors = []
  page.on('pageerror', error => errors.push(error.message))
  const response = await page.goto(process.env.PLAYWRIGHT_APP_PATH || '/')
  expect(response, '应用导航应返回 HTTP 响应').not.toBeNull()
  expect(response.status()).toBeLessThan(400)
  await expect(page.locator('#app')).not.toBeEmpty()
  expect(errors).toEqual([])
})
