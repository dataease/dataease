import { test, expect } from '@playwright/test'
const demo = process.env.EMBEDDED_DEMO_URL || 'http://localhost:8000'
const bi = process.env.EMBEDDED_BI_URL || 'http://homeserver.local:9080'

for (const mode of ['div', 'iframe']) {
  test(`${mode}: actual demo initializes resource tree before opening windows`, async ({
    page
  }) => {
    const errors = []
    const onResponse = async (response) => {
      if (response.url().includes('/de2api/') && response.status() >= 400) {
        errors.push({ path: new URL(response.url()).pathname, status: response.status() })
      }
    }
    page.on('response', onResponse)
    const endpoint = mode === 'div' ? '/PanelTree' : '/PanelTreeIframe'
    const treeResponse = page
      .waitForResponse((r) => r.url().includes('/dataVisualization/tree'), { timeout: 18000 })
      .catch(() => null)
    await page.goto(demo + endpoint)
    const content = mode === 'div' ? page : page.frameLocator('#de-view-iframe')
    if (mode === 'div') expect(await page.locator('iframe').count()).toBe(0)
    const response = await treeResponse
    expect(response, 'resource tree API must complete').not.toBeNull()
    expect(response.status(), 'resource tree API must authorize embedded identity').toBe(200)
    await expect(content.getByRole('tree')).toBeVisible({ timeout: 15000 })
    expect(errors, 'initialization must not fail authentication').toEqual([])
  })
}

test('actual backend validates demo token with configured origin and rejects other origin', async ({
  page,
  request
}) => {
  const account = process.env.EMBEDDED_TEST_ACCOUNT
  test.skip(!account, 'Set test account used by the running demo')
  const tokenResponse = await request.get(`${demo}/token/${encodeURIComponent(account)}`)
  expect(tokenResponse.status()).toBe(200)
  const token = await tokenResponse.text()
  await page.goto(bi + '/#/login')
  const result = await page.evaluate(
    async ({ token, demo }) => {
      const encryption = await import('/assets/chunk/encryption-3.0.0-dataease.js')
      const api = await import('/assets/chunk/embedded-3.0.0-dataease.js')
      await encryption.e()
      const results = []
      for (const origin of [new URL(demo).origin, 'http://untrusted.example']) {
        try {
          const response = await api.i(token, encryption.r(btoa(origin)).toString())
          results.push({ success: response?.code === 0 })
        } catch {
          results.push({ success: false })
        }
      }
      return results
    },
    { token, demo }
  )
  expect(result).toEqual([{ success: true }, { success: false }])
})

for (const mode of ['div', 'iframe']) {
  test(`${mode}: preview window preserves embedded identity`, async ({ page }) => {
    await page.goto(demo + (mode === 'div' ? '/PanelTree' : '/PanelTreeIframe'))
    const content = mode === 'div' ? page : page.frameLocator('#de-view-iframe')
    await content
      .getByRole('treeitem', {
        name: process.env.EMBEDDED_RESOURCE_GROUP || '【官方示例】',
        exact: true
      })
      .click()
    await content
      .getByRole('treeitem', {
        name: process.env.EMBEDDED_RESOURCE_NAME || '连锁茶饮销售看板',
        exact: true
      })
      .click()
    await expect(content.getByRole('button', { name: /^(预览|Preview)$/ })).toBeEnabled({
      timeout: 15000
    })
    const validation =
      mode === 'div'
        ? page.context().waitForEvent('response', {
            predicate: (r) =>
              r.url().includes('/embedded/initIframe') && r.request().frame().page() !== page
          })
        : null
    const resource = page
      .context()
      .waitForEvent('response', {
        predicate: (r) =>
          r.url().includes('/dataVisualization/findById') && r.request().frame().page() !== page
      })
      .catch(() => null)
    const popup = page.waitForEvent('popup')
    await content.getByRole('button', { name: /^(预览|Preview)$/ }).click()
    const child = await popup
    if (validation) {
      const checked = await validation
      expect(checked.status()).toBe(200)
      expect((await checked.json()).code).toBe(0)
    }
    const response = await resource
    expect(response, 'new window must request visualization data').not.toBeNull()
    expect(response.status(), 'new window resource request must authorize embedded identity').toBe(
      200
    )
    const body = await response.json()
    expect(body.code).toBe(0)
    expect(body.data).toBeTruthy()
    await expect(child.getByRole('button', { name: /^(登录|Log in|Login)$/ })).toHaveCount(0)
  })
}
