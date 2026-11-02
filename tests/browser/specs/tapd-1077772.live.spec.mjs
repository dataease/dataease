import { test, expect } from '@playwright/test'
import { mkdirSync, readFileSync, writeFileSync } from 'node:fs'
import { randomBytes } from 'node:crypto'

// Use the deployed application's Axios/HMAC/authentication implementation.
async function probe(page, url, data, method = 'post') {
  return page.evaluate(
    async ({ url, data, method }) => {
      const { o: request } = await import('/assets/chunk/locale-3.0.0-dataease.js')
      try {
        const response = await request[method]({ url, data, handleErrorLocally: true })
        return {
          ok: response?.code === 0,
          data: response?.data,
          code: response?.code,
          message: response?.msg
        }
      } catch (error) {
        return {
          ok: false,
          status: error?.response?.status,
          message:
            error?.message ?? (typeof error === 'string' ? error : 'application request rejected')
        }
      }
    },
    { url, data, method }
  )
}

async function call(page, url, data, method = 'post') {
  const result = await probe(page, url, data, method)
  expect(result.ok, `${method} ${url}: ${result.code ?? result.status ?? result.message}`).toBe(
    true
  )
  return result.data
}

async function login(page, account, password, baseURL, allowPasswordChange = true) {
  await page.goto(baseURL + '/#/login')
  await page.getByRole('textbox', { name: '账号/邮箱' }).fill(account)
  await page.getByRole('textbox', { name: '密码', exact: true }).fill(password)
  await page.getByRole('button', { name: '登录', exact: true }).click()
  const changePassword = page.getByRole('dialog', { name: '修改密码' })
  await expect
    .poll(
      async () => !new URL(page.url()).hash.includes('/login') || (await changePassword.isVisible())
    )
    .toBe(true)
  if (await changePassword.isVisible()) {
    expect(allowPasswordChange, 'only newly created test users may change passwords').toBe(true)
    const newPassword = 'Dq!' + randomBytes(6).toString('hex') + '9aA'
    await changePassword.getByPlaceholder('请输入原始密码').fill(password)
    await changePassword.getByPlaceholder('请输入新密码').fill(newPassword)
    await changePassword.getByPlaceholder('请输入确认密码').fill(newPassword)
    await changePassword.getByRole('button', { name: '保存', exact: true }).click()
    await expect(changePassword).not.toBeVisible()
    await page.getByRole('textbox', { name: '账号/邮箱' }).fill(account)
    await page.getByRole('textbox', { name: '密码', exact: true }).fill(newPassword)
    await page.getByRole('button', { name: '登录', exact: true }).click()
  }
  await page.waitForURL((url) => !url.hash.includes('/login'), { timeout: 25_000 })
  await expect
    .poll(async () =>
      page.evaluate(
        () =>
          !!document
            .querySelector('#app')
            ?.__vue_app__?.config.globalProperties.$pinia?._s.get('user')
      )
    )
    .toBe(true)
}

function flatten(nodes) {
  return (nodes || []).flatMap((node) => [node, ...flatten(node.children)])
}

test('1077772: isolated users, organization, permissions, tokens and embedded cleanup', async ({
  page,
  browser,
  baseURL
}, testInfo) => {
  const prefix = 'dq1077772_' + Date.now()
  const resources = { prefix, users: [], org: null, embedded: [], folders: [] }
  const evidence = []
  const contexts = []
  let orgAdmin
  let member
  let roles = []
  let password
  let adminId
  let memberId
  const manifest = () => {
    mkdirSync('output/delay-queue-live', { recursive: true })
    writeFileSync('output/delay-queue-live/cleanup.json', JSON.stringify(resources, null, 2))
  }
  const step = async (name, body) =>
    test.step(name, async () => {
      await body()
      evidence.push(name)
      console.log('CHECK PASS: ' + name)
    })
  const form = (uid) => call(page, '/user/queryById/' + uid, undefined, 'get')
  try {
    await step('admin login state and real application mount', async () => {
      const credentials = Object.fromEntries(
        readFileSync('../../.codex/playwright_mcp/.env.local', 'utf8')
          .split(/\r?\n/)
          .filter((line) => /^TEST_(USERNAME|PASSWORD)=/.test(line))
          .map((line) => {
            const index = line.indexOf('=')
            return [line.slice(0, index), JSON.parse(line.slice(index + 1))]
          })
      )
      await login(page, credentials.TEST_USERNAME, credentials.TEST_PASSWORD, baseURL, false)
      await expect(page.getByRole('menuitem', { name: '工作台', exact: true })).toBeVisible()
      expect((await call(page, '/user/info', undefined, 'get')).id).toBe('1')
      password = await call(page, '/user/defaultPwd', undefined, 'get')
      expect(typeof password).toBe('string')
    })
    await step('create dedicated users and organization; preserve existing resources', async () => {
      for (const suffix of ['admin', 'member']) {
        const account = prefix + '_' + suffix
        await call(page, '/user/create', {
          name: account,
          account,
          email: account + '@example.invalid',
          roleIds: [],
          enable: true,
          mfaEnable: false,
          variables: [],
          defaultOid: null
        })
        const found = await call(page, '/user/pager/1/100', { keyword: account, isSystem: true })
        const user = found.records.find((user) => user.account === account)
        expect(user).toBeTruthy()
        resources.users.push(user.id)
        manifest()
      }
      ;[adminId, memberId] = resources.users
      await call(page, '/org/page/create', { name: prefix, pid: 0, adminIds: [adminId] })
      // Creation responses can contain numeric Long IDs; read the exact string ID back.
      resources.org = (await form(adminId)).defaultOid
      manifest()
      expect(typeof resources.org).toBe('string')
      const context = await browser.newContext({
        locale: 'zh-CN',
        storageState: { cookies: [], origins: [] }
      })
      contexts.push(context)
      orgAdmin = await context.newPage()
      await login(orgAdmin, prefix + '_admin', password, baseURL)
      expect(String((await call(orgAdmin, '/user/info', undefined, 'get')).oid)).toBe(
        String(resources.org)
      )
    })
    await step('create and edit isolated organization roles', async () => {
      for (const suffix of ['role_a', 'role_b']) {
        const name = prefix + '_' + suffix
        await call(orgAdmin, '/role/create', { name, typeCode: 7, desc: prefix })
        const queried = await call(orgAdmin, '/role/query', undefined, 'get')
        roles.push(queried.find((role) => role.name === name).id)
      }
      await call(orgAdmin, '/role/edit', {
        id: roles[0],
        name: prefix + '_role_a_edited',
        desc: prefix
      })
      expect((await call(orgAdmin, '/role/detail/' + roles[0], undefined, 'get')).name).toBe(
        prefix + '_role_a_edited'
      )
    })
    await step('add member, warm authenticated caches and switch member role', async () => {
      await call(page, '/sys/org/member/add', {
        orgId: resources.org,
        userIds: [memberId],
        roleIds: [roles[0]]
      })
      const context = await browser.newContext({
        locale: 'zh-CN',
        storageState: { cookies: [], origins: [] }
      })
      contexts.push(context)
      member = await context.newPage()
      await login(member, prefix + '_member', password, baseURL)
      for (let i = 0; i < 3; i++) await call(member, '/user/info', undefined, 'get')
      await call(page, '/sys/org/member/switchRole', {
        userId: memberId,
        orgId: resources.org,
        roleIds: [roles[1]]
      })
      await expect
        .poll(async () => (await form(memberId)).roleIds.map(String))
        .toEqual([String(roles[1])])
      expect(String((await call(member, '/user/info', undefined, 'get')).oid)).toBe(
        String(resources.org)
      )
    })
    await step(
      'grant and revoke role and direct user permissions on isolated organization directory',
      async () => {
        const tree = await call(orgAdmin, '/auth/busiResource', { flag: 'panel', system: false })
        const orgRoot = flatten(tree).find((node) => node.name === prefix)
        expect(orgRoot, 'isolated organization dashboard directory').toBeTruthy()
        // The organization root is virtual. Create a real folder for protected resource reads.
        const folderId = await call(orgAdmin, '/dataVisualization/saveCanvas', {
          name: prefix + '_folder',
          nodeType: 'folder',
          type: 'dashboard',
          pid: orgRoot.id
        })
        resources.folders.push(folderId)
        manifest()
        const root = { id: folderId }
        await call(orgAdmin, '/dataVisualization/updateBase', {
          id: folderId,
          pid: orgRoot.id,
          name: prefix + '_folder_renamed',
          type: 'dashboard',
          nodeType: 'folder',
          status: 1,
          mobileLayout: false
        })
        expect(
          (
            await call(orgAdmin, '/dataVisualization/findNameById', {
              id: folderId,
              busiFlag: 'dashboard'
            })
          ).name
        ).toBe(prefix + '_folder_renamed')
        for (const subject of [
          { id: roles[1], type: 1 },
          { id: memberId, type: 0 }
        ]) {
          const request = { ...subject, flag: 'panel', oid: resources.org, system: false }
          for (const weight of [1, 0]) {
            await call(orgAdmin, '/auth/saveSubjectPermission', {
              ...request,
              permissions: [{ id: root.id, weight, ext: 0, oid: resources.org }]
            })
            await expect
              .poll(async () => {
                const permissions = await call(orgAdmin, '/auth/subjectPermission', request)
                return (
                  permissions.permissions.find((item) => String(item.id) === String(root.id))
                    ?.weight ?? 0
                )
              })
              .toBe(weight)
            await call(member, '/dataVisualization/tree', { busiFlag: 'dashboard', leaf: false })
            // Exercise the permission cache through a protected resource API, not only saved rows.
            for (const delayed of [false, true]) {
              if (delayed) await member.waitForTimeout(1200)
              const access = await probe(member, '/dataVisualization/findNameById', {
                id: root.id,
                busiFlag: 'dashboard'
              })
              expect(
                access.ok,
                `type=${subject.type}, weight=${weight}, delayed=${delayed}: ${
                  access.message ?? access.code
                }`
              ).toBe(weight > 0)
              if (weight === 0) {
                expect(
                  access.status === 403 ||
                    /权限|permission|forbidden|denied/i.test(access.message || ''),
                  'denial must be permission-related rather than a network or token failure'
                ).toBe(true)
              }
            }
          }
        }
      }
    )
    await step(
      'edit test user and run bounded concurrent cache-invalidating operations',
      async () => {
        const original = await form(memberId)
        await call(page, '/user/edit', {
          ...original,
          id: memberId,
          name: prefix + '_renamed',
          enable: true,
          roleIds: [roles[1]],
          variables: []
        })
        await expect
          .poll(async () => (await call(member, '/user/info', undefined, 'get')).name)
          .toBe(prefix + '_renamed')
        // Eight operations, four concurrent requests: functional concurrency only, not a stress test.
        for (let batch = 0; batch < 2; batch++) {
          await Promise.all(Array.from({ length: 4 }, () => call(page, '/user/proxyClear', {})))
          await Promise.all(
            Array.from({ length: 4 }, () => call(member, '/user/info', undefined, 'get'))
          )
        }
        await member.waitForTimeout(1500)
        expect((await call(member, '/user/info', undefined, 'get')).name).toBe(prefix + '_renamed')
      }
    )
    await step(
      'old token survives grace period then expires; refreshed token remains usable',
      async () => {
        const oldContext = await browser.newContext({
          storageState: await member.context().storageState()
        })
        contexts.push(oldContext)
        const oldPage = await oldContext.newPage()
        await oldPage.goto(baseURL + '/#/workbranch/index')
        const result = await member.evaluate(async () => {
          const { o: request } = await import('/assets/chunk/locale-3.0.0-dataease.js')
          const store = document
            .querySelector('#app')
            .__vue_app__.config.globalProperties.$pinia._s.get('user')
          const oldToken = store.getToken
          // Avoid identical JWTs from login and refresh in the same second.
          await new Promise((resolve) => setTimeout(resolve, 1200))
          const refreshed = await request.get({ url: '/login/refresh' })
          const changed = oldToken !== refreshed.data.token
          store.setToken(refreshed.data.token)
          store.setExp(refreshed.data.exp)
          store.setTime(Date.now())
          return { changed }
        })
        expect(result).toEqual({ changed: true })
        await call(oldPage, '/user/info', undefined, 'get')
        await member.waitForTimeout(11_000)
        const resultAfter = await oldPage.evaluate(async () => {
          const { o: request } = await import('/assets/chunk/locale-3.0.0-dataease.js')
          try {
            const result = await request.get({ url: '/user/info', handleErrorLocally: true })
            return { rejected: result.code !== 0, status: null }
          } catch (error) {
            return { rejected: true, status: error?.response?.status }
          }
        })
        expect(resultAfter).toEqual({ rejected: true, status: 401 })
        await call(member, '/user/info', undefined, 'get')
      }
    )
    await step('create, edit and batch-delete only dedicated embedded applications', async () => {
      for (let i = 0; i < 2; i++) {
        await call(page, '/embedded/create', {
          name: prefix + '_embedded_' + i,
          domain: 'http://dq1077772-' + i + '.example.invalid',
          secretLength: 16
        })
        const found = await call(page, '/embedded/pager/1/100', { keyword: prefix })
        resources.embedded = found.records
          .filter((item) => item.name.startsWith(prefix))
          .map((item) => item.id)
        manifest()
      }
      expect(resources.embedded).toHaveLength(2)
      await call(page, '/embedded/edit', {
        id: resources.embedded[0],
        name: prefix + '_embedded_edited',
        domain: 'http://dq1077772-edited.example.invalid',
        secretLength: 16
      })
      await call(page, '/embedded/batchDelete', resources.embedded)
      const found = await call(page, '/embedded/pager/1/100', { keyword: prefix })
      expect(found.records.filter((item) => item.name.startsWith(prefix))).toHaveLength(0)
      resources.embedded = []
      manifest()
    })
    await step('remove member and delete assigned role without affecting other users', async () => {
      await call(orgAdmin, '/role/delete/' + roles[1])
      await expect.poll(async () => (await form(memberId)).roleIds).toEqual([])
      await call(page, '/sys/org/member/remove/' + memberId + '/' + resources.org)
      const members = await call(page, '/sys/org/member/page', {
        orgId: resources.org,
        keyword: prefix,
        currentPage: 1,
        pageSize: 100
      })
      expect(members.records.some((user) => String(user.id) === String(memberId))).toBe(false)
      await call(orgAdmin, '/role/delete/' + roles[0])
      expect(
        (await call(orgAdmin, '/role/query', undefined, 'get')).some((role) =>
          roles.includes(role.id)
        )
      ).toBe(false)
    })
  } finally {
    const cleanupErrors = []
    for (const context of contexts) await context.close()
    for (const id of [...resources.folders]) {
      try {
        await call(page, '/dataVisualization/deleteLogic', { id, busiFlag: 'dashboard' })
        resources.folders = resources.folders.filter((value) => value !== id)
      } catch {
        cleanupErrors.push('folder ' + id)
      }
    }
    for (const id of [...resources.embedded]) {
      try {
        await call(page, '/embedded/delete/' + id)
        resources.embedded = resources.embedded.filter((value) => value !== id)
      } catch {
        cleanupErrors.push('embedded ' + id)
      }
    }
    for (const id of [...resources.users]) {
      try {
        await call(page, '/user/delete/' + id)
        resources.users = resources.users.filter((value) => value !== id)
      } catch {
        cleanupErrors.push('user ' + id)
      }
    }
    if (resources.org) {
      try {
        await call(page, '/org/page/delete', { id: resources.org, delResource: true })
        resources.org = null
      } catch {
        cleanupErrors.push('org ' + resources.org)
      }
    }
    const remainingUsers = await call(page, '/user/pager/1/100', {
      keyword: prefix,
      isSystem: true
    })
    const remainingOrgs = await call(page, '/org/page/tree', undefined, 'get')
    expect(remainingUsers.records.filter((user) => user.account.startsWith(prefix))).toHaveLength(0)
    expect(flatten(remainingOrgs).filter((org) => org.name === prefix)).toHaveLength(0)
    manifest()
    await testInfo.attach('completed-business-checks', {
      body: JSON.stringify({ evidence, cleanupErrors }, null, 2),
      contentType: 'application/json'
    })
    expect(cleanupErrors, 'temporary test data must be removed').toEqual([])
  }
})
