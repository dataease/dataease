import { test, expect } from '@playwright/test'

const session = {
  sessionId: 'session-a',
  contextVersion: 'v1',
  resourceId: '11',
  resourceName: 'Isolated database',
  editorId: 'editor',
  expiresAt: Date.now() + 60000
}
const selectResource = async (page) => {
  await page.goto('/?view=conversation')
  await page.getByRole('combobox', { name: '数据源' }).click()
  await page.getByRole('option', { name: 'Isolated database' }).click()
}
const send = async (page, text) => {
  await page.getByRole('textbox', { name: '消息' }).fill(text)
  await page.getByRole('button', { name: '发送', exact: true }).click()
}

test('real page renders increments, deduplicates cursors, keeps execution while hidden and stops before follow-up', async ({
  page
}) => {
  let terminal = false
  let starts = 0
  const turns = []
  await page.route('**/fixture-api/ai/conversations**', async (route) => {
    const request = route.request()
    const url = new URL(request.url())
    if (url.pathname.endsWith('/resources'))
      return route.fulfill({ json: [{ id: '11', name: 'Isolated database' }] })
    if (request.method() === 'DELETE') return route.fulfill({ json: {} })
    const run = { ...session, executionId: 'run-' + starts }
    if (url.pathname.endsWith('/stop')) {
      terminal = true
      return route.fulfill({
        json: {
          ...run,
          status: 'CANCELLED',
          cursor: 2,
          events: [{ sequence: 2, type: 'CANCELLED', error: 'cancelled' }]
        }
      })
    }
    if (url.pathname.endsWith('/events'))
      return route.fulfill({
        json: {
          ...run,
          status: starts > 1 ? 'COMPLETED' : terminal ? 'CANCELLED' : 'RUNNING',
          cursor: 2,
          events:
            starts > 1
              ? [
                  { sequence: 1, type: 'TEXT', text: 'Follow-up answer' },
                  { sequence: 2, type: 'COMPLETED' }
                ]
              : [{ sequence: 1, type: 'TEXT', text: 'First increment' }]
        }
      })
    if (url.pathname.endsWith('/executions')) {
      starts++
      turns.push(request.postDataJSON())
      return route.fulfill({
        json: {
          ...session,
          executionId: 'run-' + starts,
          status: 'RUNNING',
          deadline: Date.now() + 60000
        }
      })
    }
    return route.fulfill({ json: session })
  })
  await selectResource(page)
  await send(page, 'Explain this SQL')
  await expect(page.getByText('First increment', { exact: true })).toBeVisible()
  await page.getByRole('button', { name: '收起助手' }).click()
  await expect(page.getByText('First increment', { exact: true })).toBeHidden()
  await page.getByRole('button', { name: '展开助手' }).click()
  await expect(page.getByText('First increment', { exact: true })).toBeVisible()
  await page.getByRole('button', { name: '停止生成' }).click()
  await expect(page.getByText('· 未完成')).toBeVisible()
  await expect(page.getByText('First incrementFirst increment', { exact: true })).toHaveCount(0)
  await send(page, 'What next?')
  await expect(page.getByText('Follow-up answer', { exact: true })).toBeVisible()
  expect(turns).toEqual([
    { contextVersion: 'v1', text: 'Explain this SQL' },
    { contextVersion: 'v1', text: 'What next?' }
  ])
  await page.getByRole('button', { name: '结束会话' }).click()
  await expect(page.getByText('First increment', { exact: true })).toHaveCount(0)
})

test('SQL and identity changes discard late increments and failed output remains incomplete', async ({
  page
}) => {
  let release
  let requested
  const delayed = new Promise((resolve) => {
    release = resolve
  })
  const started = new Promise((resolve) => {
    requested = resolve
  })
  let starts = 0
  await page.route('**/fixture-api/ai/conversations**', async (route) => {
    const request = route.request(),
      url = new URL(request.url())
    if (url.pathname.endsWith('/resources'))
      return route.fulfill({ json: [{ id: '11', name: 'Isolated database' }] })
    if (request.method() === 'DELETE') return route.fulfill({ json: {} })
    if (url.pathname.endsWith('/events')) {
      requested()
      await delayed
      return route.fulfill({
        json: {
          ...session,
          executionId: 'old-run',
          status: 'COMPLETED',
          cursor: 2,
          events: [
            { sequence: 1, type: 'TEXT', text: 'Late old answer' },
            { sequence: 2, type: 'COMPLETED' }
          ]
        }
      })
    }
    if (url.pathname.endsWith('/executions')) {
      starts++
      if (starts > 1) return route.fulfill({ status: 503, json: {} })
      return route.fulfill({
        json: {
          ...session,
          executionId: 'old-run',
          status: 'RUNNING',
          deadline: Date.now() + 60000
        }
      })
    }
    return route.fulfill({ json: session })
  })
  await selectResource(page)
  await send(page, 'Original question')
  await started
  await page.getByRole('textbox', { name: '当前 SQL' }).fill('select 2')
  release()
  await expect(page.getByText('Original question', { exact: true })).toHaveCount(0)
  await expect(page.getByText('Late old answer', { exact: true })).toHaveCount(0)
  await send(page, 'Try new context')
  await expect(page.getByRole('alert')).toBeVisible()
  await expect(page.getByText('· 未完成')).toBeVisible()
  await page.evaluate(() => window.aiSwitchUser())
  await expect(page.getByText('Try new context', { exact: true })).toHaveCount(0)
})

test('stop aborts an in-flight poll without closing the context and late completion cannot replace cancellation', async ({
  page
}) => {
  let openCount = 0,
    starts = 0,
    polls = 0,
    release,
    requested
  const delayed = new Promise((resolve) => {
    release = resolve
  })
  const pending = new Promise((resolve) => {
    requested = resolve
  })
  await page.route('**/fixture-api/ai/conversations**', async (route) => {
    const request = route.request(),
      url = new URL(request.url())
    if (url.pathname.endsWith('/resources'))
      return route.fulfill({ json: [{ id: '11', name: 'Isolated database' }] })
    if (request.method() === 'DELETE') return route.fulfill({ json: {} })
    const run = { ...session, executionId: 'run-' + starts }
    if (url.pathname.endsWith('/stop'))
      return route.fulfill({
        json: {
          ...run,
          status: 'CANCELLED',
          cursor: 2,
          events: [{ sequence: 2, type: 'CANCELLED' }]
        }
      })
    if (url.pathname.endsWith('/events')) {
      polls++
      if (starts === 1 && polls > 1) {
        requested()
        await delayed
      }
      return route.fulfill({
        json: {
          ...run,
          status: starts === 1 ? 'RUNNING' : 'COMPLETED',
          cursor: 2,
          events: [
            { sequence: 1, type: 'TEXT', text: starts === 1 ? 'Partial answer' : 'New answer' },
            ...(starts > 1 ? [{ sequence: 2, type: 'COMPLETED' }] : [])
          ]
        }
      })
    }
    if (url.pathname.endsWith('/executions')) {
      starts++
      return route.fulfill({
        json: {
          ...session,
          executionId: 'run-' + starts,
          status: 'RUNNING',
          deadline: Date.now() + 60000
        }
      })
    }
    openCount++
    return route.fulfill({ json: session })
  })
  await selectResource(page)
  await send(page, 'Partial question')
  await pending
  await page.getByRole('button', { name: '停止生成' }).click()
  await expect(page.getByText('· 未完成')).toBeVisible()
  await send(page, 'Continue please')
  release()
  await expect(page.getByText('New answer', { exact: true })).toBeVisible()
  expect(openCount).toBe(1)
  await expect(page.getByRole('alert')).toHaveCount(0)
})

test('offline and refresh end the temporary session and a new activity opens a new context', async ({
  page
}) => {
  let opens = 0
  const closed = []
  await page.route('**/fixture-api/ai/conversations**', async (route) => {
    const request = route.request(),
      url = new URL(request.url())
    if (url.pathname.endsWith('/resources'))
      return route.fulfill({ json: [{ id: '11', name: 'Isolated database' }] })
    if (request.method() === 'DELETE') {
      closed.push(url.pathname)
      return route.fulfill({ json: {} })
    }
    const current = { ...session, sessionId: 'session-' + opens, executionId: 'run-' + opens }
    if (url.pathname.endsWith('/executions'))
      return route.fulfill({
        json: { ...current, status: 'RUNNING', deadline: Date.now() + 60000 }
      })
    if (url.pathname.endsWith('/events'))
      return route.fulfill({
        json: {
          ...current,
          status: 'RUNNING',
          cursor: 1,
          events: [{ sequence: 1, type: 'TEXT', text: 'Visible partial' }]
        }
      })
    opens++
    return route.fulfill({ json: { ...session, sessionId: 'session-' + opens } })
  })
  await selectResource(page)
  await send(page, 'Before disconnect')
  await expect(page.getByText('Visible partial', { exact: true })).toBeVisible()
  await page.evaluate(() => window.dispatchEvent(new Event('offline')))
  await expect(page.getByText('· 未完成')).toBeVisible()
  await expect.poll(() => closed.length).toBe(1)
  await send(page, 'New activity')
  await expect.poll(() => opens).toBe(2)
  await page.evaluate(() => window.dispatchEvent(new Event('pagehide')))
  await expect(page.getByText('New activity', { exact: true })).toHaveCount(0)
  await expect.poll(() => closed.length).toBe(2)
  await page.reload()
  await page.getByRole('combobox', { name: '数据源' }).click()
  await page.getByRole('option', { name: 'Isolated database' }).click()
  await send(page, 'After refresh')
  await expect.poll(() => opens).toBe(3)
})

test('stop racing with completed server execution drains missing text before displaying complete', async ({
  page
}) => {
  let polls = 0,
    stopped = false,
    release,
    requested
  const delayed = new Promise((resolve) => {
    release = resolve
  })
  const pending = new Promise((resolve) => {
    requested = resolve
  })
  const run = { ...session, executionId: 'race-run' }
  await page.route('**/fixture-api/ai/conversations**', async (route) => {
    const request = route.request(),
      url = new URL(request.url())
    if (url.pathname.endsWith('/resources'))
      return route.fulfill({ json: [{ id: '11', name: 'Isolated database' }] })
    if (request.method() === 'DELETE') return route.fulfill({ json: {} })
    if (url.pathname.endsWith('/executions'))
      return route.fulfill({ json: { ...run, status: 'RUNNING', deadline: Date.now() + 60000 } })
    if (url.pathname.endsWith('/stop')) {
      stopped = true
      release()
      return route.fulfill({
        json: {
          ...run,
          status: 'COMPLETED',
          cursor: 3,
          events: [{ sequence: 3, type: 'COMPLETED' }]
        }
      })
    }
    if (url.pathname.endsWith('/events')) {
      polls++
      if (polls > 1 && !stopped) {
        requested()
        await delayed
      }
      return route.fulfill({
        json: stopped
          ? {
              ...run,
              status: 'COMPLETED',
              cursor: 3,
              events: [
                { sequence: 2, type: 'TEXT', text: ' and final text' },
                { sequence: 3, type: 'COMPLETED' }
              ]
            }
          : {
              ...run,
              status: 'RUNNING',
              cursor: 1,
              events: [{ sequence: 1, type: 'TEXT', text: 'First part' }]
            }
      })
    }
    return route.fulfill({ json: session })
  })
  await selectResource(page)
  await send(page, 'Complete race')
  await pending
  await page.getByRole('button', { name: '停止生成' }).click()
  await expect(page.getByText('First part and final text', { exact: true })).toBeVisible()
  await expect(page.getByText('· 未完成')).toHaveCount(0)
})

test('safe timeout failure is explained and partial text remains incomplete', async ({ page }) => {
  const run = { ...session, executionId: 'timeout-run' }
  await page.route('**/fixture-api/ai/conversations**', async (route) => {
    const request = route.request(),
      url = new URL(request.url())
    if (url.pathname.endsWith('/resources'))
      return route.fulfill({ json: [{ id: '11', name: 'Isolated database' }] })
    if (request.method() === 'DELETE') return route.fulfill({ json: {} })
    if (url.pathname.endsWith('/executions'))
      return route.fulfill({ json: { ...run, status: 'RUNNING', deadline: Date.now() + 60000 } })
    if (url.pathname.endsWith('/events'))
      return route.fulfill({
        json: {
          ...run,
          status: 'FAILED',
          cursor: 2,
          events: [
            { sequence: 1, type: 'TEXT', text: 'Interrupted partial' },
            { sequence: 2, type: 'FAILED', error: 'timeout' }
          ]
        }
      })
    return route.fulfill({ json: session })
  })
  await selectResource(page)
  await send(page, 'Will timeout')
  await expect(page.getByRole('alert')).toHaveText('AI 请求超时。')
  await expect(page.getByText('Interrupted partial', { exact: true })).toBeVisible()
  await expect(page.getByText('· 未完成')).toBeVisible()
})

test('ending before the resource list arrives preserves that authorized list without reloading on SQL edits', async ({
  page
}) => {
  let release,
    requested,
    loads = 0
  const delayed = new Promise((resolve) => {
    release = resolve
  })
  const started = new Promise((resolve) => {
    requested = resolve
  })
  await page.route('**/fixture-api/ai/conversations/resources', async (route) => {
    loads++
    requested()
    await delayed
    await route.fulfill({ json: [{ id: '11', name: 'Isolated database' }] })
  })
  await page.goto('/?view=conversation')
  await started
  await page.getByRole('button', { name: '结束会话' }).click()
  await page.getByRole('textbox', { name: '当前 SQL' }).fill('select 1')
  release()
  await page.getByRole('combobox', { name: '数据源' }).click()
  await page.getByRole('option', { name: 'Isolated database' }).click()
  await page.getByRole('textbox', { name: '当前 SQL' }).fill('select 2')
  await page.getByRole('textbox', { name: '消息' }).fill('Ready after SQL change')
  await expect(page.getByRole('button', { name: '发送', exact: true })).toBeEnabled()
  expect(loads).toBe(1)
})

test('an old identity resource response cannot overwrite the new identity list', async ({
  page
}) => {
  let release,
    requested,
    oldFinished,
    loads = 0
  const delayed = new Promise((resolve) => {
    release = resolve
  })
  const started = new Promise((resolve) => {
    requested = resolve
  })
  const oldResponse = new Promise((resolve) => {
    oldFinished = resolve
  })
  await page.route('**/fixture-api/ai/conversations/resources', async (route) => {
    loads++
    if (loads === 1) {
      requested()
      await delayed
      await route.fulfill({ json: [{ id: '11', name: 'Previous identity database' }] })
      oldFinished()
    } else await route.fulfill({ json: [{ id: '22', name: 'Current identity database' }] })
  })
  await page.goto('/?view=conversation')
  await started
  await page.evaluate(() => window.aiSwitchUser())
  await page.getByRole('combobox', { name: '数据源' }).click()
  await expect(page.getByRole('option', { name: 'Current identity database' })).toBeVisible()
  release()
  await oldResponse
  await expect(page.getByRole('option', { name: 'Previous identity database' })).toHaveCount(0)
  await page.getByRole('option', { name: 'Current identity database' }).click()
  await page.getByRole('textbox', { name: '消息' }).fill('Ready for current identity')
  await expect(page.getByRole('button', { name: '发送', exact: true })).toBeEnabled()
  expect(loads).toBe(2)
})
