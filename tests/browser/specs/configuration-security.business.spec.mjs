import { test, expect } from '@playwright/test'
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { enterOrganization } from '../fixtures/configuration-security/organization.mjs'
const root = fileURLToPath(new URL('../../../', import.meta.url))
const settings = Object.fromEntries(
  fs
    .readFileSync(path.join(root, '.codex/playwright_mcp/.env.local'), 'utf8')
    .split('\n')
    .filter((line) => /^TEST_(USERNAME|PASSWORD)=/.test(line))
    .map((line) => {
      const index = line.indexOf('=')
      return [line.slice(0, index), JSON.parse(line.slice(index + 1))]
    })
)

async function login(page) {
  await page.goto('/#/login')
  await expect(page.locator('input[type="password"]')).toBeVisible({
    timeout: 30000
  })
  await page
    .locator('input')
    .filter({ visible: true })
    .first()
    .fill(settings.TEST_USERNAME)
  await page.locator('input[type="password"]').fill(settings.TEST_PASSWORD)
  const loginResponse = page.waitForResponse((response) =>
    response.url().includes('/login/localLogin')
  )
  await page.getByRole('button', { name: /登\s*录|Login/i }).click()
  const login = await (await loginResponse).json()
  expect(login.code, 'test account must authenticate').toBe(0)
  await page.waitForFunction(() => !location.hash.includes('login'))
}

async function revealAndCopy(page, title, expected, passwordSelector) {
  const drawer = page.getByRole('dialog').first()
  await drawer.locator('[class*="-input__password"]').first().click()
  const dialog = page.getByRole('dialog', { name: title, exact: true })
  await expect(dialog).toBeVisible()
  await dialog
    .getByLabel('当前用户密码', { exact: true })
    .fill(settings.TEST_PASSWORD)
  await dialog.getByRole('button', { name: '验证并查看' }).click()
  const region = dialog.getByRole('region', { name: '已保存的敏感信息' })
  await expect(region).toBeVisible()
  expect((await region.textContent()) === expected).toBe(true)
  await page.evaluate((expected) => {
    const original = document.execCommand
    window.__copyBusiness = {
      ok: false,
      restore: () => {
        document.execCommand = original
      }
    }
    document.execCommand = (command) => {
      if (command !== 'copy') return original.call(document, command)
      window.__copyBusiness.ok =
        document.activeElement instanceof HTMLTextAreaElement &&
        document.activeElement.value === expected
      return true
    }
  }, expected)
  try {
    await dialog.getByRole('button', { name: '复制', exact: true }).click()
    expect(await page.evaluate(() => window.__copyBusiness.ok)).toBe(true)
    await expect(region.locator('textarea')).toHaveCount(0)
  } finally {
    await page.evaluate(() => {
      window.__copyBusiness.restore()
      delete window.__copyBusiness
    })
  }
  await dialog.getByRole('button', { name: '关闭', exact: true }).click()
  await expect(dialog).not.toBeVisible()
  await expect(
    drawer.getByRole('textbox', { name: passwordSelector, exact: true })
  ).toHaveValue('******')
}

const fixturePath = '/private/tmp/de-configuration-mysql-fixture.json'

test('engine actual API save, masked edits, reveal, copy and connection reuse', async ({
  page
}) => {
  const fixture = JSON.parse(fs.readFileSync(fixturePath, 'utf8'))
  const name = `codex-security-engine-${Date.now()}`
  const manifestPath = '/private/tmp/de-security-engine-test.json'
  const manifest = JSON.parse(fs.readFileSync(manifestPath, 'utf8'))
  expect(manifest.originalCount).toBe(0)
  fs.writeFileSync(manifestPath, JSON.stringify({ ...manifest, name }), {
    mode: 0o600
  })
  await login(page)
  const id = await page.evaluate(
    async ({ name, fixture }) => {
      const { default: request } = await import('/src/config/axios/index.ts')
      const configuration = {
        ...fixture,
        urlType: 'hostName',
        useSSH: false,
        extraParams:
          'connectTimeout=3000&socketTimeout=3000&allowPublicKeyRetrieval=true',
        initialPoolSize: 1,
        minPoolSize: 1,
        maxPoolSize: 2,
        queryTimeout: 30
      }
      const data = {
        name,
        type: 'mysql',
        configuration: btoa(JSON.stringify(configuration)),
        enableDataFill: false
      }
      await request.post({ url: '/engine/validate', data })
      await request.post({ url: '/engine/save', data })
      const engine = (await request.get({ url: '/engine/getEngine' })).data
      if (JSON.parse(engine.configuration).password !== '******')
        throw new Error('Engine response was not masked')
      return engine.id
    },
    { name, fixture }
  )
  expect(id).toBeTruthy()
  await enterOrganization(page)
  try {
    await page.evaluate(async () => {
      await document
        .querySelector('#app')
        .__vue_app__.config.globalProperties.$router.push(
          '/sys-setting/parameter'
        )
    })
  } catch (error) {
    if (!String(error).includes('Execution context was destroyed')) throw error
    await page.waitForLoadState('domcontentloaded')
  }
  await page.getByRole('tab', { name: '引擎设置', exact: true }).click()
  await page.getByRole('button', { name: '编辑', exact: true }).click()
  await revealAndCopy(page, '查看引擎连接信息', fixture.password, '密码')
  const drawer = page.getByRole('dialog').first()
  const save = page.waitForResponse((response) =>
    response.url().endsWith('/engine/save')
  )
  await drawer.getByRole('button', { name: '保存', exact: true }).click()
  expect((await (await save).json()).code).toBe(0)
  await expect(drawer).not.toBeVisible()
  const result = await page.evaluate(async (id) => {
    const { default: request } = await import('/src/config/axios/index.ts')
    const engine = (await request.get({ url: '/engine/getEngine' })).data
    const cfg = JSON.parse(engine.configuration)
    let targetRejected = false
    try {
      await request.post({
        url: '/engine/save',
        data: {
          ...engine,
          configuration: btoa(
            JSON.stringify({
              ...cfg,
              host: cfg.host === '127.0.0.2' ? '127.0.0.3' : '127.0.0.2'
            })
          )
        }
      })
    } catch {
      targetRejected = true
    }
    cfg.queryTimeout = 45
    await request.post({
      url: '/engine/save',
      data: {
        ...engine,
        description: 'synthetic regression',
        configuration: btoa(JSON.stringify(cfg))
      }
    })
    const check = await request.post({ url: `/engine/validate/${id}` })
    const updated = (await request.get({ url: '/engine/getEngine' })).data
    return {
      targetRejected,
      connected: check.code === 0,
      masked: JSON.parse(updated.configuration).password === '******',
      edited: JSON.parse(updated.configuration).queryTimeout === 45
    }
  }, id)
  expect(result).toEqual({
    targetRejected: true,
    connected: true,
    masked: true,
    edited: true
  })
})

test('sync source actual API save, masked edits, reveal, copy and table reuse', async ({
  page
}) => {
  const fixture = JSON.parse(fs.readFileSync(fixturePath, 'utf8'))
  const name = `de-sec-sync-${Date.now().toString(36)}`
  const ids = []
  await login(page)
  await enterOrganization(page)
  try {
    const id = await page.evaluate(
      async ({ name, fixture }) => {
        const api = await import('/src/api/sync/syncDatasource.ts')
        const configuration = {
          ...fixture,
          initialPoolSize: 1,
          minPoolSize: 1,
          maxPoolSize: 2,
          authMethod: 'passwd',
          queryTimeout: 30,
          extraParams:
            'connectTimeout=3000&socketTimeout=3000&allowPublicKeyRetrieval=true'
        }
        const data = {
          name,
          type: 'mysql',
          datasourceRole: 1,
          configuration: btoa(JSON.stringify(configuration))
        }
        await api.validateApi(data)
        await api.saveApi(data)
        const rows = (await api.sourceDsPageApi(1, 100, { keyword: name })).data
          .records
        const created = rows.find((row) => row.name === name)
        if (!created) throw new Error('Dedicated sync connection was not found')
        return created.id
      },
      { name, fixture }
    )
    ids.push(id)
    await page.evaluate(async () => {
      await document
        .querySelector('#app')
        .__vue_app__.config.globalProperties.$router.push('/system/sync/ds')
    })
    const row = page.getByRole('row').filter({ hasText: name })
    await row.getByRole('button').first().click()
    await revealAndCopy(page, '查看同步连接敏感信息', fixture.password, '密码')
    const drawer = page.getByRole('dialog').first()
    const nameInput = drawer.getByPlaceholder('请输入名称', { exact: true })
    await nameInput.fill(`${name}-ui`)
    const clientErrors = []
    page.on('pageerror', (error) => clientErrors.push(error.message))
    let saved
    try {
      ;[saved] = await Promise.all([
        page.waitForResponse((response) =>
          response.url().endsWith('/sync/datasource/update')
        ),
        drawer.getByRole('button', { name: '保存', exact: true }).click()
      ])
    } catch (error) {
      const validation = await drawer
        .locator('.ed-form-item__error')
        .allTextContents()
      fs.writeFileSync(
        '/private/tmp/de-security-sync-ui-diagnostic.json',
        JSON.stringify({
          validation,
          errors: clientErrors.map((message) =>
            Object.values(fixture).reduce(
              (text, value) =>
                typeof value === 'string'
                  ? text.replaceAll(value, '[redacted]')
                  : text,
              message
            )
          )
        }),
        { mode: 0o600 }
      )
      throw error
    }
    expect((await saved.json()).code).toBe(0)
    await expect(drawer).not.toBeVisible()
    const result = await page.evaluate(async (id) => {
      const api = await import('/src/api/sync/syncDatasource.ts')
      const { default: request } = await import('/src/config/axios/index.ts')
      const dto = (await api.getByIdApi(id)).data
      const cfg = JSON.parse(dto.configuration)
      let targetRejected = false
      try {
        await api.updateApi({
          ...dto,
          configuration: btoa(
            JSON.stringify({
              ...cfg,
              host: cfg.host === '127.0.0.2' ? '127.0.0.3' : '127.0.0.2'
            })
          )
        })
      } catch {
        targetRejected = true
      }
      cfg.queryTimeout = 45
      await api.updateApi({ ...dto, configuration: btoa(JSON.stringify(cfg)) })
      const checked = (await api.validateByIdApi(id)).data
      const tables = (
        await request.get({ url: `/sync/datasource/table/list/${id}` })
      ).data
      const updated = (await api.getByIdApi(id)).data
      return {
        targetRejected,
        connected: checked.status === 'Success',
        masked: JSON.parse(updated.configuration).password === '******',
        edited: JSON.parse(updated.configuration).queryTimeout === 45,
        tableFound: tables.some((table) => table.name === 'sales')
      }
    }, id)
    expect(result).toEqual({
      targetRejected: true,
      connected: true,
      masked: true,
      edited: true,
      tableFound: true
    })
  } finally {
    for (const id of ids)
      await page.evaluate(async (id) => {
        const api = await import('/src/api/sync/syncDatasource.ts')
        await api.deleteByIdApi(id)
      }, id)
  }
})

// A separate context can clean up even when a timed-out page has already closed.
test.afterAll(async ({ browser }) => {
  const manifest = '/private/tmp/de-security-sync-cleanup.json'
  if (!fs.existsSync(manifest)) return
  const ids = JSON.parse(fs.readFileSync(manifest, 'utf8'))
  if (ids.length) {
    const page = await browser.newPage()
    try {
      await login(page)
      await enterOrganization(page)
      for (const id of ids)
        await page.evaluate(async (id) => {
          const api = await import('/src/api/sync/syncDatasource.ts')
          const dto = (await api.getByIdApi(id)).data
          if (!dto.name.startsWith('codex-security-sync-'))
            throw new Error('Refusing unrelated cleanup')
          await api.deleteByIdApi(id)
        }, id)
    } finally {
      await page.close()
    }
  }
  fs.unlinkSync(manifest)
})
