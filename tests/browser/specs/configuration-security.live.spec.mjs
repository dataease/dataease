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

test('actual MySQL save, edit, copy, credential replacement and downstream SQL', async ({
  page
}) => {
  const fixture = JSON.parse(
    fs.readFileSync(
      process.env.CONFIGURATION_MYSQL_FIXTURE ||
        '/private/tmp/de-configuration-mysql-fixture.json',
      'utf8'
    )
  )
  const query = async (id) => {
    const values = await page.evaluate(async (id) => {
      const api = await import('/src/api/dataset.ts')
      const result = await api.getPreviewSql({
        datasourceId: id,
        sql: btoa(
          'SELECT COUNT(*) AS row_count,SUM(amount) AS amount_sum FROM sales'
        ),
        isCross: false,
        sqlVariableDetails: '[]'
      })
      return result.data.data.map((row) =>
        Object.values(row)
          .map(Number)
          .sort((a, b) => a - b)
      )
    }, id)
    expect(values).toEqual([[3, 60]])
  }
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
  const context = await enterOrganization(page)
  fs.writeFileSync(
    '/private/tmp/de-security-org-context.json',
    JSON.stringify(context),
    { mode: 0o600 }
  )
  const name = `codex-security-${Date.now()}`
  const ids = []
  const clientErrors = []
  page.on('pageerror', (error) => clientErrors.push(error.message))
  const encryptedWrites = []
  page.on('request', (request) => {
    if (
      request.method() !== 'POST' ||
      !/\/datasource\/(save|update|checkRepeat)$/.test(
        new URL(request.url()).pathname
      )
    )
      return
    const body = request.postDataJSON()
    encryptedWrites.push(
      body?.version === 1 &&
        !!body?.encryptedKey &&
        !request.postData().includes(fixture.password)
    )
  })
  const encryptedReveals = []
  page.on('request', (request) => {
    if (
      request.method() !== 'POST' ||
      !request.url().endsWith('/sensitive/reveal')
    )
      return
    const body = request.postDataJSON()
    encryptedReveals.push(
      body?.version === 1 &&
        !!body?.encryptedKey &&
        !request.postData().includes(settings.TEST_PASSWORD) &&
        !body.password
    )
  })
  try {
    const created = await page.evaluate(
      async ({ name, fixture }) => {
        const api = await import('/src/api/datasource.ts')
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
        const result = await api.save({
          name,
          pid: '0',
          nodeType: 'datasource',
          type: 'mysql',
          configuration: btoa(JSON.stringify(configuration))
        })
        let validationError = null
        try {
          await api.validate({
            ...result,
            type: 'mysql',
            configuration: btoa(JSON.stringify(configuration))
          })
        } catch (error) {
          validationError = String(error)
            .replaceAll(fixture.password, '[redacted]')
            .replaceAll(fixture.host, '[host]')
            .replaceAll(fixture.dataBase, '[database]')
        }
        return { id: result?.id, validationError }
      },
      { name, fixture }
    )
    expect(created.id, 'test datasource must be created').toBeTruthy()
    ids.push(created.id)
    expect(
      created.validationError,
      'dedicated MySQL fixture must connect'
    ).toBeNull()
    await query(created.id)
    await page.evaluate(async () => {
      const router =
        document.querySelector('#app').__vue_app__.config.globalProperties
          .$router
      await router.push('/data/datasource')
    })
    try {
      await page
        .getByText(name, { exact: true })
        .first()
        .click({ timeout: 20000 })
    } catch (error) {
      fs.writeFileSync(
        '/private/tmp/de-security-datasource-ui-diagnostic.json',
        JSON.stringify({
          url: page.url(),
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
    await page.getByRole('button', { name: '编辑', exact: true }).click()
    const drawer = page.getByRole('dialog')
    await expect(
      drawer.getByRole('textbox', { name: '密码', exact: true })
    ).toHaveValue('******')
    await drawer.locator('[class*="-input__password"]').first().click()
    const revealDialog = page.getByRole('dialog', {
      name: '查看数据源敏感信息'
    })
    await expect(revealDialog).toBeVisible()
    await expect(
      revealDialog.getByRole('button', { name: '复制', exact: true })
    ).toHaveCount(0)
    await expect(drawer.getByText('查看已保存值', { exact: true })).toHaveCount(
      0
    )
    await expect(
      drawer.getByRole('textbox', { name: '密码', exact: true })
    ).toHaveAttribute('type', 'password')
    await revealDialog
      .getByLabel('当前用户密码', { exact: true })
      .fill('wrong-synthetic-login-password')
    await revealDialog.getByRole('button', { name: '验证并查看' }).click()
    await expect(revealDialog.getByText(/无法查看/)).toBeVisible()
    await expect(
      revealDialog.getByLabel('当前用户密码', { exact: true })
    ).toHaveValue('')
    await revealDialog
      .getByLabel('当前用户密码', { exact: true })
      .fill(settings.TEST_PASSWORD)
    const revealedResponse = page.waitForResponse((response) =>
      response.url().endsWith('/sensitive/reveal')
    )
    await revealDialog.getByRole('button', { name: '验证并查看' }).click()
    const encryptedResponse = await revealedResponse
    expect(encryptedResponse.headers()['cache-control']).toContain('no-store')
    const ciphertext = await encryptedResponse.text()
    expect(ciphertext.includes(fixture.password)).toBe(false)
    expect(ciphertext.includes(settings.TEST_PASSWORD)).toBe(false)
    // Compare privately: never include credentials in assertion failure artifacts.
    expect(
      (await revealDialog.getByLabel('已保存的敏感信息').textContent()) ===
        fixture.password
    ).toBe(true)
    const resultArea = revealDialog.getByRole('region', {
      name: '已保存的敏感信息'
    })
    expect(await resultArea.evaluate((element) => element.tagName)).toBe('PRE')
    const hintGap = await resultArea.evaluate(
      (element) =>
        element.getBoundingClientRect().top -
        element.previousElementSibling.getBoundingClientRect().bottom
    )
    expect(hintGap).toBeGreaterThanOrEqual(16)
    // Exercise the existing legacy copy path without touching the host clipboard.
    await page.evaluate((expected) => {
      const original = document.execCommand
      window.__sensitiveCopyProbe = {
        matches: false,
        restore: () => {
          document.execCommand = original
        }
      }
      document.execCommand = (command) => {
        if (command !== 'copy') return original.call(document, command)
        window.__sensitiveCopyProbe.matches =
          document.activeElement instanceof HTMLTextAreaElement &&
          document.activeElement.value === expected
        return true
      }
    }, fixture.password)
    try {
      await revealDialog
        .getByRole('button', { name: '复制', exact: true })
        .click()
      await expect(page.getByText('复制成功', { exact: true })).toBeVisible()
      expect(
        await page.evaluate(() => window.__sensitiveCopyProbe.matches)
      ).toBe(true)
      await expect(resultArea.locator('textarea')).toHaveCount(0)
    } finally {
      await page.evaluate(() => {
        window.__sensitiveCopyProbe.restore()
        delete window.__sensitiveCopyProbe
      })
    }

    await revealDialog
      .getByRole('button', { name: '关闭', exact: true })
      .click()
    await expect(revealDialog).not.toBeVisible()
    await expect(
      drawer.getByRole('textbox', { name: '密码', exact: true })
    ).toHaveValue('******')
    const passwordInput = drawer.getByRole('textbox', {
      name: '密码',
      exact: true
    })
    await passwordInput.fill('unsaved-synthetic-password')
    await drawer.locator('[class*="-input__password"]').first().click()
    await expect(passwordInput).toHaveAttribute('type', 'text')
    await expect(revealDialog).not.toBeVisible()
    await drawer.locator('[class*="-input__password"]').first().click()
    await expect(passwordInput).toHaveAttribute('type', 'password')
    await passwordInput.fill('******')
    await page.clock.install()
    await page.clock.pauseAt(new Date(Date.now() + 1000))
    await drawer.locator('[class*="-input__password"]').first().click()
    await revealDialog
      .getByLabel('当前用户密码', { exact: true })
      .fill(settings.TEST_PASSWORD)
    await revealDialog.getByRole('button', { name: '验证并查看' }).click()
    await expect(revealDialog.getByLabel('已保存的敏感信息')).toBeVisible()
    const countdown = revealDialog.locator('.sensitive-reveal-hint')
    await expect(countdown).toContainText('30 秒后自动关闭')
    await page.clock.fastForward(1000)
    await expect(countdown).toContainText('29 秒后自动关闭')
    await page.clock.fastForward(4000)
    await expect(countdown).toContainText('25 秒后自动关闭')
    await page.clock.fastForward(26000)
    await expect(page.getByLabel('已保存的敏感信息')).toHaveCount(0)
    // The paused test clock also controls the dialog's closing animation frames.
    await page.clock.runFor(500)
    await page.clock.resume()
    await expect(revealDialog).not.toBeVisible()
    await expect(passwordInput).toHaveValue('******')
    await drawer
      .getByPlaceholder('请输入名称', { exact: true })
      .fill(`${name}-ui`)
    const saved = page.waitForResponse(
      (response) => response.url().endsWith('/datasource/update'),
      { timeout: 20000 }
    )
    await drawer.getByRole('button', { name: '保存', exact: true }).click()
    expect((await (await saved).json()).code).toBe(0)
    await expect(drawer).not.toBeVisible()
    await query(created.id)

    const result = await page.evaluate(
      async ({ id, name }) => {
        const api = await import('/src/api/datasource.ts')
        const response = await api.getById(id)
        const dto = response.data
        const cfg = JSON.parse(dto.configuration)
        cfg.queryTimeout = 45
        const payload = {
          ...dto,
          name: `${name}-renamed`,
          configuration: btoa(JSON.stringify(cfg))
        }
        const repeat = await api.checkRepeat(payload)
        await api.update(payload)
        const updated = (await api.getById(id)).data
        return {
          masked: cfg.password === '******',
          renamed: updated.name === `${name}-renamed`,
          stillMasked: JSON.parse(updated.configuration).password === '******',
          timeoutUpdated: JSON.parse(updated.configuration).queryTimeout === 45,
          repeatType: typeof repeat
        }
      },
      { id: created.id, name }
    )
    expect(result).toEqual({
      masked: true,
      renamed: true,
      stillMasked: true,
      timeoutUpdated: true,
      repeatType: 'boolean'
    })
    const copied = await page.evaluate(
      async ({ id, name }) => {
        const api = await import('/src/api/datasource.ts')
        const dto = (await api.getById(id)).data
        const cfg = JSON.parse(dto.configuration)
        const result = await api.save({
          ...dto,
          id: null,
          copySourceId: id,
          name: `${name}-copy`,
          configuration: btoa(JSON.stringify(cfg))
        })
        return { id: result?.id }
      },
      { id: created.id, name }
    )
    expect(copied.id).toBeTruthy()
    ids.push(copied.id)
    await query(created.id)
    await query(copied.id)
    const targetGuard = await page.evaluate(async (id) => {
      const api = await import('/src/api/datasource.ts')
      const dto = (await api.getById(id)).data
      const original = JSON.parse(dto.configuration)
      const changed = {
        ...original,
        host: original.host === '127.0.0.2' ? '127.0.0.3' : '127.0.0.2'
      }
      let rejected = false
      try {
        await api.update({
          ...dto,
          configuration: btoa(JSON.stringify(changed))
        })
      } catch {
        rejected = true
      }
      const updated = JSON.parse((await api.getById(id)).data.configuration)
      return {
        rejected,
        unchanged:
          updated.host === original.host && updated.password === '******'
      }
    }, created.id)
    expect(targetGuard).toEqual({ rejected: true, unchanged: true })
    const credentials = await page.evaluate(
      async ({ id, password, userPassword }) => {
        const api = await import('/src/api/datasource.ts')
        const dto = (await api.getById(id)).data
        const cfg = JSON.parse(dto.configuration)
        cfg.password = '***'
        await api.update({ ...dto, configuration: btoa(JSON.stringify(cfg)) })
        let rejected = false
        try {
          await api.validate({
            ...dto,
            configuration: btoa(JSON.stringify(cfg))
          })
        } catch {
          rejected = true
        }
        const { revealSensitive } = await import('/src/api/sensitive.ts')
        const literalThreeAccepted =
          (await revealSensitive(id, 'DATASOURCE_PASSWORD', userPassword)) ===
          '***'
        cfg.password = password
        await api.update({ ...dto, configuration: btoa(JSON.stringify(cfg)) })
        await api.validate({
          ...dto,
          configuration: btoa(JSON.stringify(cfg))
        })
        return { rejected, literalThreeAccepted, restored: true }
      },
      {
        id: created.id,
        password: fixture.password,
        userPassword: settings.TEST_PASSWORD
      }
    )
    expect(credentials).toEqual({
      rejected: true,
      literalThreeAccepted: true,
      restored: true
    })
    await query(created.id)
    const checks = await page.evaluate(async () => {
      const { default: request } = await import('/src/config/axios/index.ts')
      const results = []
      for (const option of [
        { method: 'get', url: '/engine/getEngine' },
        { method: 'post', url: '/sync/datasource/source/pager/1/5', data: {} },
        { method: 'post', url: '/sync/datasource/target/pager/1/5', data: {} },
        {
          method: 'post',
          url: '/datasetTree/tree',
          data: { busiFlag: 'dataset' }
        }
      ]) {
        try {
          const response = await request[option.method](option)
          results.push({
            path: option.url,
            code: response.code,
            dataIsObject:
              response.data !== null && typeof response.data === 'object'
          })
        } catch {
          results.push({ path: option.url, failed: true })
        }
      }
      return results
    })
    expect(
      checks.every((item) => item.code === 0 && item.dataIsObject),
      JSON.stringify(checks)
    ).toBe(true)

    expect(encryptedReveals.length).toBe(4)
    expect(encryptedReveals.every(Boolean)).toBe(true)
    expect(encryptedWrites.length).toBeGreaterThanOrEqual(5)
    expect(encryptedWrites.every(Boolean)).toBe(true)
  } finally {
    for (const id of ids.reverse())
      await page.evaluate(async (id) => {
        const api = await import('/src/api/datasource.ts')
        await api.deleteById({ id })
      }, id)
  }
})
