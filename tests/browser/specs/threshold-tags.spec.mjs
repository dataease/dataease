import { test, expect } from '@playwright/test'

for (const width of [1440, 960]) {
  test(`recipient tags use the same font at ${width}px`, async ({ page }) => {
    await page.setViewportSize({ width, height: 1000 })
    const errors = []
    page.on('pageerror', error => errors.push(error.message))
    await page.goto('/')
    const field = label => page.locator('.ed-form-item').filter({ has: page.locator('.ed-form-item__label', { hasText: new RegExp(`^${label}$`) }) })
    await expect(page.locator('.reci-tag-item span')).toHaveText('Test recipient')
    await expect(page.locator('.reci-tag-item span')).toHaveCSS('font-size', '12px')
    for (const label of ['report.lark_groups', 'report.larksuite_groups']) {
      const control = field(label)
      const labels = control.locator('.ed-tag .custom-option > span:last-child')
      await expect(labels).toHaveCount(1)
      await expect(labels.first()).toHaveCSS('font-size', '12px')
      await control.locator('.ed-select').click()
      for (const name of ['Group 2', 'Group 3', 'Long group name ABCDEFGHIJKLMNOPQRSTUVWXYZ 长名称测试群']) {
        const option = page.getByRole('option', { name, exact: true })
        await expect(option.locator('.custom-option > span:last-child')).toHaveCSS('font-size', '14px')
        await option.click()
      }
      await page.keyboard.press('Escape')
      await expect(labels).toHaveCount(4)
      for (const item of await labels.all()) await expect(item).toHaveCSS('font-size', '12px')
      await control.locator('.ed-tag__close').first().click()
      await expect(labels).toHaveCount(3)
      await expect(labels.last()).toHaveCSS('font-size', '12px')
    }
    for (const label of ['Webhook', 'threshold.notification_email']) {
      await expect(field(label).locator('.ed-select__tags-text')).toHaveCSS('font-size', '12px')
    }
    expect(errors).toEqual([])
  })
}
