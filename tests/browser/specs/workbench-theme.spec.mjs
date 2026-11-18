import { test, expect } from '@playwright/test'

test('dashboard quick-create icon follows the primary theme color', async ({ page }) => {
  await page.goto('/')

  const icons = page.locator('.item-creation .item .main-color')
  await expect(icons).toHaveCount(4)
  await expect(icons.nth(0)).toHaveCSS('background-color', 'rgb(244, 197, 66)')
  await expect(icons.nth(1)).toHaveCSS('background-color', 'rgb(0, 214, 185)')

  await page.evaluate(() =>
    document.documentElement.style.setProperty('--ed-color-primary', '#12ab34')
  )

  await expect(icons.nth(0)).toHaveCSS('background-color', 'rgb(18, 171, 52)')
  await expect(icons.nth(1)).toHaveCSS('background-color', 'rgb(0, 214, 185)')
})
