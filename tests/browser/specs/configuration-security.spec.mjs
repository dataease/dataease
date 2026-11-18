import { test, expect } from '@playwright/test'
test.beforeEach(async ({ page }) => {
  await page.goto('/')
  await page.waitForFunction(() => !!window.configurationFixture)
})
const seal = (page) =>
  page.evaluate(async () => {
    const challenge = await (await fetch('/challenge')).json()
    return window.configurationFixture.sealConfiguration(
      {
        type: 'mysql',
        name: 'renamed',
        configuration: 'fixture-password长配置'.repeat(3000)
      },
      '/datasource/checkRepeat',
      challenge
    )
  })
const verify = (page, body) =>
  page.evaluate(async (body) => {
    const response = await fetch('/verify', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    })
    return { status: response.status, data: await response.json() }
  }, body)
test('actual browser encrypts long configuration without plaintext', async ({
  page
}, info) => {
  if (info.project.name === 'configuration-http')
    expect(await page.evaluate(() => window.isSecureContext)).toBe(false)
  const envelope = await seal(page)
  expect(JSON.stringify(envelope)).not.toContain('fixture-password')
  expect(envelope).not.toHaveProperty('configuration')
  const result = await verify(page, envelope)
  expect(result.status).toBe(200)
  expect(result.data).toEqual({
    accepted: true,
    type: 'mysql',
    name: 'renamed',
    length: 'fixture-password长配置'.repeat(3000).length
  })
})
test('fresh per-request encryption and replay rejection', async ({ page }) => {
  const first = await seal(page),
    second = await seal(page)
  expect(first.encryptedKey).not.toBe(second.encryptedKey)
  expect(first.iv).not.toBe(second.iv)
  expect((await verify(page, first)).status).toBe(200)
  expect((await verify(page, first)).status).toBe(400)
})
test('tampering is rejected and unrelated endpoints are not encrypted', async ({
  page
}) => {
  const envelope = await seal(page)
  envelope.ciphertext =
    (envelope.ciphertext[0] === 'A' ? 'B' : 'A') + envelope.ciphertext.slice(1)
  expect((await verify(page, envelope)).status).toBe(400)
  expect(
    await page.evaluate(() =>
      window.configurationFixture.needsConfigurationEncryption(
        '/chartData/getData',
        'post'
      )
    )
  ).toBe(false)
  expect(
    await page.evaluate(() =>
      window.configurationFixture.needsConfigurationEncryption(
        '/datasource/checkRepeat',
        'post'
      )
    )
  ).toBe(true)
})
