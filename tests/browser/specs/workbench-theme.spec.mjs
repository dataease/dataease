import { test, expect } from '@playwright/test'

test('resource type icons keep fixed colors regardless of the primary theme color', async ({
  page
}) => {
  await page.goto('/')

  const icons = page.locator('.item-creation .item .main-color')
  await expect(icons).toHaveCount(4)
  await expect(icons.nth(0)).toHaveCSS('background-color', 'rgb(51, 112, 255)')
  await expect(icons.nth(1)).toHaveCSS('background-color', 'rgb(0, 214, 185)')
  await expect(icons.nth(2)).toHaveCSS('background-color', 'rgb(22, 192, 255)')
  await expect(icons.nth(3)).toHaveCSS('background-color', 'rgb(127, 59, 246)')

  await page.evaluate(() =>
    document.documentElement.style.setProperty('--ed-color-primary', '#12ab34')
  )

  await expect(icons.nth(0)).toHaveCSS('background-color', 'rgb(51, 112, 255)')
  await expect(icons.nth(1)).toHaveCSS('background-color', 'rgb(0, 214, 185)')
  await expect(icons.nth(2)).toHaveCSS('background-color', 'rgb(22, 192, 255)')
  await expect(icons.nth(3)).toHaveCSS('background-color', 'rgb(127, 59, 246)')
})
