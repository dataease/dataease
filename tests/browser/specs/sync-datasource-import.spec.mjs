import { test, expect } from '@playwright/test'
for (const scenario of [
  'success',
  'repeat',
  'cancel',
  'check-failure',
  'save-failure',
  'empty-tree'
]) {
  test(scenario, async ({ page }) => {
    const calls = [],
      errors = []
    page.on('pageerror', (error) => errors.push(error.message))
    let fail = true
    await page.route('**/mock/**', async (route) => {
      const path = new URL(route.request().url()).pathname
      const data = route.request().postDataJSON()
      if (path === '/mock/tree') {
        return route.fulfill({
          json: scenario === 'empty-tree' ? [] : [{ id: '0', name: 'root', leaf: false, weight: 7 }]
        })
      }
      calls.push({ path, data })
      if (
        fail &&
        path.endsWith(
          scenario === 'check-failure' ? 'checkRepeat' : scenario === 'save-failure' ? 'save' : '-'
        )
      ) {
        return route.fulfill({ status: 400, json: { error: 'synthetic' } })
      }
      await route.fulfill({
        json: path.endsWith('checkRepeat')
          ? ['repeat', 'cancel'].includes(scenario)
          : { id: 'new-analysis-source' }
      })
    })
    await page.goto('/')
    await page.getByRole('button', { name: 'Open import' }).click()
    const confirm = page.getByRole('button', { name: 'common.sure', exact: true })
    if (scenario === 'empty-tree') {
      await expect(confirm).toBeDisabled()
      expect(calls).toEqual([])
      return
    }
    await expect(confirm).toBeEnabled()
    await page.getByLabel('sync_datasource.datasourcesync_datasource.name').fill('Renamed import')
    await confirm.click()
    if (['repeat', 'cancel'].includes(scenario)) {
      await expect(page.getByText('datasource.has_same_ds')).toBeVisible()
      await page
        .locator('.ed-message-box')
        .getByRole('button', { name: scenario === 'cancel' ? /Cancel|取消/ : /OK|确定/ })
        .click()
    }
    if (scenario === 'cancel') {
      await expect(confirm).toBeEnabled()
      expect(calls.map((call) => call.path)).toEqual(['/mock/checkRepeat'])
    } else if (scenario.endsWith('failure')) {
      await expect(confirm).toBeEnabled()
      await expect(page.locator('.ed-overlay-dialog')).toBeVisible()
      fail = false
      await confirm.click()
      await expect(page.locator('.ed-overlay-dialog')).toBeHidden()
    } else {
      await expect(page.locator('.ed-overlay-dialog')).toBeHidden()
      expect(calls.map((call) => call.path)).toEqual(['/mock/checkRepeat', '/mock/save'])
    }
    for (const call of calls)
      expect(call.data).toEqual({
        syncSourceId: 'synthetic-target',
        name: 'Renamed import',
        pid: '0'
      })
    expect(errors).toEqual([])
  })
}

const nameInput = (page) => page.getByLabel('sync_datasource.datasourcesync_datasource.name')
const confirmButton = (page) => page.getByRole('button', { name: 'common.sure', exact: true })
const dialog = (page) => page.locator('.ed-overlay-dialog')
const directory = [{ id: '0', name: 'root', leaf: false, weight: 7 }]
const open = async (page, url = '/') => {
  await page.goto(url)
  await page.getByRole('button', { name: 'Open import', exact: true }).click()
}

test('name is required before any import request', async ({ page }) => {
  const calls = []
  await page.route('**/mock/**', (route) => {
    if (route.request().url().endsWith('/tree')) return route.fulfill({ json: directory })
    calls.push(route.request().url())
    return route.fulfill({ json: false })
  })
  await open(page)
  await expect(confirmButton(page)).toBeEnabled()
  await nameInput(page).fill('')
  await confirmButton(page).click()
  await expect(page.getByText('commons.namecommons.cannot_be_null')).toBeVisible()
  expect(calls).toEqual([])
})

test('directory loading disables submit and directory failure can recover on reopen', async ({
  page
}) => {
  let release,
    trees = 0
  const calls = []
  await page.route('**/mock/**', async (route) => {
    if (route.request().url().endsWith('/tree')) {
      trees++
      if (trees === 1) {
        await new Promise((resolve) => {
          release = resolve
        })
        return route.fulfill({ status: 403, json: {} })
      }
      return route.fulfill({ json: directory })
    }
    calls.push(route.request().postDataJSON())
    return route.fulfill({ json: route.request().url().endsWith('/save') ? { id: 'new' } : false })
  })
  await open(page)
  await expect.poll(() => trees).toBe(1)
  await expect(confirmButton(page)).toBeDisabled()
  release()
  await expect(confirmButton(page)).toBeDisabled()
  await page.getByRole('button', { name: 'common.cancel', exact: true }).click()
  await page.getByRole('button', { name: 'Open import', exact: true }).click()
  await expect(confirmButton(page)).toBeEnabled()
  await confirmButton(page).click()
  await expect(dialog(page)).toBeHidden()
  expect(calls).toHaveLength(2)
})

test('chosen folder and reopened source are independent from the first import', async ({
  page
}) => {
  const calls = []
  await page.route('**/mock/**', (route) => {
    const path = new URL(route.request().url()).pathname
    if (path.endsWith('/tree'))
      return route.fulfill({
        json: [
          { id: '42', name: 'Folder A', leaf: false, weight: 7 },
          { id: '43', name: 'Folder B', leaf: false, weight: 7 }
        ]
      })
    calls.push(route.request().postDataJSON())
    return route.fulfill({ json: path.endsWith('/save') ? { id: 'new' } : false })
  })
  await open(page)
  await expect(confirmButton(page)).toBeEnabled()
  await page.getByRole('combobox').click()
  await page.getByRole('treeitem', { name: 'Folder B' }).click()
  await confirmButton(page).click()
  await expect(dialog(page)).toBeHidden()
  expect(calls[1]).toEqual({ syncSourceId: 'synthetic-target', name: 'Synthetic Doris', pid: '43' })
  await page.getByRole('button', { name: 'Open another import' }).click()
  await expect(nameInput(page)).toHaveValue('Second target')
  await expect(confirmButton(page)).toBeEnabled()
  await confirmButton(page).click()
  await expect(dialog(page)).toBeHidden()
  expect(calls[3]).toEqual({ syncSourceId: 'synthetic-target-2', name: 'Second target', pid: '42' })
  expect(await page.evaluate(() => window.importCacheEvents)).toEqual([
    { key: 'ds-new-success', value: true },
    { key: 'ds-new-success', value: true }
  ])
})

test('pending requests cannot be double submitted', async ({ page }) => {
  const calls = []
  let releaseCheck, releaseSave
  await page.route('**/mock/**', async (route) => {
    const path = new URL(route.request().url()).pathname
    if (path.endsWith('/tree')) return route.fulfill({ json: directory })
    calls.push(path)
    await new Promise((resolve) => {
      if (path.endsWith('checkRepeat')) releaseCheck = resolve
      else releaseSave = resolve
    })
    return route.fulfill({ json: path.endsWith('/save') ? { id: 'new' } : false })
  })
  await open(page)
  await expect(confirmButton(page)).toBeEnabled()
  await confirmButton(page).click()
  await expect.poll(() => calls.length).toBe(1)
  await confirmButton(page).dispatchEvent('click')
  expect(calls).toHaveLength(1)
  releaseCheck()
  await expect.poll(() => calls.length).toBe(2)
  await confirmButton(page).dispatchEvent('click')
  expect(calls).toHaveLength(2)
  releaseSave()
  await expect(dialog(page)).toBeHidden()
})

test('compatible plugin target uses source context without forwarding a client type', async ({
  page
}) => {
  const calls = []
  await page.route('**/mock/**', (route) => {
    if (route.request().url().endsWith('/tree')) return route.fulfill({ json: directory })
    calls.push(route.request().postDataJSON())
    return route.fulfill({ json: route.request().url().endsWith('/save') ? { id: 'new' } : false })
  })
  await open(page, '/?type=plugin-target')
  await expect(confirmButton(page)).toBeEnabled()
  await confirmButton(page).click()
  await expect(dialog(page)).toBeHidden()
  expect(calls).toEqual([
    { syncSourceId: 'synthetic-target', name: 'Synthetic Doris', pid: '0' },
    { syncSourceId: 'synthetic-target', name: 'Synthetic Doris', pid: '0' }
  ])
})

test('late directory response from a closed dialog cannot replace the new selection', async ({
  page
}) => {
  let releaseOld,
    trees = 0
  const calls = []
  await page.route('**/mock/**', async (route) => {
    if (route.request().url().endsWith('/tree')) {
      trees++
      if (trees === 1) {
        await new Promise((resolve) => {
          releaseOld = resolve
        })
        return route.fulfill({
          json: [{ id: '99', name: 'Old directory', leaf: false, weight: 7 }]
        })
      }
      return route.fulfill({
        json: [{ id: '42', name: 'Current directory', leaf: false, weight: 7 }]
      })
    }
    calls.push(route.request().postDataJSON())
    return route.fulfill({ json: route.request().url().endsWith('/save') ? { id: 'new' } : false })
  })
  await open(page)
  await expect.poll(() => trees).toBe(1)
  await page.getByRole('button', { name: 'common.cancel', exact: true }).click()
  await page.getByRole('button', { name: 'Open another import' }).click()
  await expect(confirmButton(page)).toBeEnabled()
  const oldResponse = page.waitForResponse((response) => response.url().endsWith('/mock/tree'))
  releaseOld()
  await oldResponse
  await confirmButton(page).click()
  await expect(dialog(page)).toBeHidden()
  expect(calls).toEqual([
    { syncSourceId: 'synthetic-target-2', name: 'Second target', pid: '42' },
    { syncSourceId: 'synthetic-target-2', name: 'Second target', pid: '42' }
  ])
})
