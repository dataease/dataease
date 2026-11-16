import { test, expect } from '@playwright/test'
const types = [
  'DATASOURCE_PASSWORD',
  'DATASOURCE_SSH_PASSWORD',
  'DATASOURCE_SSH_KEY_PASSWORD',
  'DATASOURCE_REMOTE_PASSWORD',
  'ENGINE_PASSWORD',
  'SYNC_PASSWORD'
]
test('empty credentials stay empty in all six controls', async ({ page }) => {
  await page.goto('/?mode=empty')
  for (const type of types)
    await expect(page.getByLabel(type, { exact: true })).toHaveValue('')
  await expect(page.getByRole('dialog')).toHaveCount(0)
})
test('saved credentials show exactly six stars and retain verified reveal', async ({
  page
}) => {
  await page.goto('/')
  for (const type of types) {
    const input = page.getByLabel(type, { exact: true })
    await expect(input).toHaveValue('******')
    await input.locator('..').locator('[class*="-input__password"]').click()
    const dialog = page.getByRole('dialog')
    await expect(dialog).toBeVisible()
    await dialog.getByRole('button', { name: 'Close this dialog' }).click()
    await expect(dialog).toBeHidden()
    await expect(input).toHaveValue('******')
  }
})
test('copies clear masks including API and SSH configuration without changing source', async ({
  page
}) => {
  await page.goto('/?mode=copy')
  for (const type of types)
    await expect(page.getByLabel(type, { exact: true })).toHaveValue('')
  const summary = await page.evaluate(() => {
    const {
      configuration: c,
      source: s,
      clearCopiedCredentials: clear
    } = window.credentialFixture
    return {
      source: s.password,
      password: c.password,
      sshKey: c.sshKey,
      sslKey: c.sslKey,
      header: c.headers[0].value,
      host: c.host,
      optional: c.optional,
      absent: c.absent,
      array: clear([{ password: '******' }])
    }
  })
  expect(summary).toEqual({
    source: '******',
    password: '',
    sshKey: '',
    sslKey: '',
    header: '',
    host: 'fixture.invalid',
    optional: '',
    absent: null,
    array: [{ password: '' }]
  })
  const input = page.getByLabel(types[0], { exact: true })
  await input.fill('new-synthetic-password')
  await input.locator('..').locator('[class*="-input__password"]').click()
  await expect(input).toHaveAttribute('type', 'text')
  await expect(input).toHaveValue('new-synthetic-password')
  await expect(page.getByRole('dialog')).toHaveCount(0)
})
