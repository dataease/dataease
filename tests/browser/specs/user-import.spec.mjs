import { test, expect } from '@playwright/test'

for (const [lang, tip] of [
  ['zh-CN', '所属组织留空时，不分配组织和角色。'],
  ['tw', '所屬組織留空時，不分配組織和角色。'],
  [
    'en',
    'When the organization is blank, no organization or roles are assigned.'
  ]
]) {
  for (const width of [1440, 640]) {
    test(`${lang} explains blank organizations at ${width}px`, async ({
      page
    }) => {
      const errors = []
      page.on('pageerror', (error) => errors.push(error.message))
      await page.setViewportSize({ width, height: 900 })
      await page.goto(`/?lang=${lang}`)
      await page.getByRole('button').click()
      const dialog = page.getByRole('dialog')
      await expect(dialog.locator('.org-empty-tip')).toHaveText(tip)
      await expect(dialog.locator('.org-empty-tip')).toBeVisible()
      const box = await dialog.locator('.org-empty-tip').boundingBox()
      expect(box.x).toBeGreaterThanOrEqual(0)
      expect(box.x + box.width).toBeLessThanOrEqual(width)
      await expect(
        dialog.locator('.dialog-footer button').last()
      ).toBeDisabled()
      await dialog.locator('input[type=file]').setInputFiles({
        name: 'synthetic-empty-org.xlsx',
        mimeType:
          'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        buffer: Buffer.from('fixture upload only')
      })
      await expect(dialog.locator('.dialog-footer button').last()).toBeEnabled()
      await dialog.locator('.dialog-footer button').last().click()
      await expect
        .poll(() => page.evaluate(() => window.importedFiles))
        .toEqual(['synthetic-empty-org.xlsx'])
      expect(errors).toEqual([])
    })
  }
}
