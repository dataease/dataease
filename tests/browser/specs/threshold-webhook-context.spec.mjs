import { test, expect } from '@playwright/test'

for (const form of ['new', 'edit', 'batch']) {
  for (const org of ['none', 'member', 'proxy']) {
    test(`${form}: Webhook candidates respect ${org} organization context`, async ({ page }) => {
      const errors = []
      page.on('pageerror', error => errors.push(error.message))
      await page.goto(`/?form=${form}&org=${org}`)
      const webhook = page.locator('.ed-form-item').filter({
        has: page.locator('.ed-form-item__label', { hasText: /^Webhook$/ })
      })
      await expect(page.locator('.ed-form-item__label', { hasText: 'threshold.notification_email' })).toBeVisible()
      if (org === 'none') {
        await expect(webhook).toHaveCount(0)
        expect(await page.evaluate(() => window.webhookCandidateRequests || 0)).toBe(0)
      } else {
        await expect(webhook).toBeVisible()
        await expect.poll(() => page.evaluate(() => window.webhookCandidateRequests || 0)).toBe(1)
      }
      expect(errors).toEqual([])
    })
  }
}
