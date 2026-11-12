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

test('ordinary SQL explanations can start without selecting a datasource', async ({ page }) => {
  let opened
  const general = {
    ...session,
    resourceId: '',
    resourceName: '',
    resourceVersion: 'unbound',
    draftVersion: 'empty'
  }
  await page.route('**/fixture-api/ai/conversations**', (route) => {
    const url = new URL(route.request().url())
    if (url.pathname.endsWith('/resources')) return route.fulfill({ json: [] })
    if (route.request().method() === 'DELETE') return route.fulfill({ json: {} })
    if (url.pathname.endsWith('/executions'))
      return route.fulfill({
        json: {
          ...general,
          executionId: 'general-run',
          status: 'RUNNING',
          deadline: Date.now() + 60000
        }
      })
    if (url.pathname.endsWith('/events'))
      return route.fulfill({
        json: {
          ...general,
          executionId: 'general-run',
          status: 'COMPLETED',
          cursor: 2,
          events: [
            { sequence: 1, type: 'TEXT', text: 'SELECT is a query statement' },
            { sequence: 2, type: 'COMPLETED' }
          ],
          result: {
            ...general,
            type: 'EXPLANATION',
            text: 'SELECT is a query statement',
            sql: null
          }
        }
      })
    opened = route.request().postDataJSON()
    return route.fulfill({ json: general })
  })
  await page.goto('/?view=conversation')
  await send(page, 'Explain SELECT')
  await expect(page.getByText('SELECT is a query statement', { exact: true })).toBeVisible()
  expect(opened.resourceId).toBe('')
  await expect(page.getByRole('button', { name: '应用到当前草稿' })).toHaveCount(0)
})

test('a failed one-shot request preserves the existing independent conversation', async ({
  page
}) => {
  let opens = 0
  let closes = 0
  let starts = 0
  await page.route('**/fixture-api/ai/conversations**', (route) => {
    const url = new URL(route.request().url())
    if (url.pathname.endsWith('/resources'))
      return route.fulfill({ json: [{ id: '11', name: 'Isolated database' }] })
    if (route.request().method() === 'DELETE') {
      closes++
      return route.fulfill({ json: {} })
    }
    if (url.pathname.endsWith('/capabilities/sql.assist'))
      return route.fulfill({ status: 503, json: {} })
    if (url.pathname.endsWith('/executions')) {
      starts++
      return route.fulfill({
        json: {
          ...session,
          executionId: 'chat-' + starts,
          status: 'RUNNING',
          deadline: Date.now() + 60000
        }
      })
    }
    if (url.pathname.endsWith('/events'))
      return route.fulfill({
        json: {
          ...session,
          executionId: 'chat-' + starts,
          status: 'COMPLETED',
          cursor: 2,
          events: [
            {
              sequence: 1,
              type: 'TEXT',
              text: 'Independent chat answer ' + starts
            },
            { sequence: 2, type: 'COMPLETED' }
          ]
        }
      })
    opens++
    return route.fulfill({ json: session })
  })
  await selectResource(page)
  await send(page, 'First chat question')
  await expect(page.getByText('Independent chat answer 1', { exact: true })).toBeVisible()
  await page.getByRole('button', { name: '单次 SQL 辅助' }).click()
  await expect(page.getByRole('alert')).toBeVisible()
  expect(closes).toBe(0)
  await send(page, 'Follow up in original chat')
  await expect(page.getByText('Independent chat answer 2', { exact: true })).toBeVisible()
  expect(opens).toBe(1)
})

test('single SQL assistance uses the shared capability without opening a chat', async ({
  page
}) => {
  let chatOpens = 0
  let invocation
  const context = {
    ...session,
    resourceVersion: 'resource-v1',
    draftVersion: 'draft-v1'
  }
  const result = {
    type: 'EXPLANATION',
    text: 'Shared SQL explanation',
    ...context
  }
  await page.route('**/fixture-api/ai/conversations**', async (route) => {
    const url = new URL(route.request().url())
    if (url.pathname.endsWith('/resources'))
      return route.fulfill({ json: [{ id: '11', name: 'Isolated database' }] })
    if (route.request().method() === 'DELETE') return route.fulfill({ json: {} })
    if (url.pathname.endsWith('/capabilities/sql.assist')) {
      invocation = route.request().postDataJSON()
      return route.fulfill({
        json: {
          ...session,
          executionId: 'single-run',
          status: 'RUNNING',
          deadline: Date.now() + 60000,
          context
        }
      })
    }
    if (url.pathname.endsWith('/events'))
      return route.fulfill({
        json: {
          ...session,
          executionId: 'single-run',
          status: 'COMPLETED',
          cursor: 2,
          events: [
            { sequence: 1, type: 'TEXT', text: result.text },
            { sequence: 2, type: 'COMPLETED' }
          ],
          result
        }
      })
    chatOpens++
    return route.fulfill({ json: context })
  })
  await selectResource(page)
  await page.getByRole('textbox', { name: '当前 SQL' }).fill('SELECT ID FROM AI_PROBE')
  await page.getByRole('button', { name: '单次 SQL 辅助' }).click()
  await expect(page.getByText('Shared SQL explanation', { exact: true })).toBeVisible()
  expect(chatOpens).toBe(0)
  expect(invocation.resourceId).toBe('11')
  expect(invocation.sql).toBe('SELECT ID FROM AI_PROBE')
})

test('selection changes retain the original target and applying a suggestion cannot overwrite a newer draft', async ({
  page
}) => {
  let complete = false
  let closes = 0
  let checks = 0
  let releaseCheck
  let checkStarted
  const checking = new Promise((resolve) => {
    checkStarted = resolve
  })
  const checkDelay = new Promise((resolve) => {
    releaseCheck = resolve
  })
  const context = {
    ...session,
    resourceVersion: 'resource-v1',
    draftVersion: 'draft-v1'
  }
  const result = {
    type: 'SQL_SUGGESTION',
    text: 'Original target suggestion',
    sql: 'SELECT ID FROM AI_PROBE',
    resourceId: '11',
    resourceName: 'Isolated database',
    resourceVersion: 'resource-v1',
    draftVersion: 'draft-v1',
    editorId: 'editor'
  }
  await page.route('**/fixture-api/ai/conversations**', async (route) => {
    const url = new URL(route.request().url())
    if (url.pathname.endsWith('/resources'))
      return route.fulfill({
        json: [
          { id: '11', name: 'Isolated database' },
          { id: '22', name: 'Other database' }
        ]
      })
    if (route.request().method() === 'DELETE') {
      closes++
      return route.fulfill({ json: {} })
    }
    if (url.pathname.endsWith('/executions'))
      return route.fulfill({
        json: {
          ...context,
          executionId: 'target-run',
          status: 'RUNNING',
          deadline: Date.now() + 60000
        }
      })
    if (url.pathname.endsWith('/check-application')) {
      checks++
      checkStarted()
      await checkDelay
      return route.fulfill({ json: result })
    }
    if (url.pathname.endsWith('/events'))
      return route.fulfill({
        json: {
          ...context,
          executionId: 'target-run',
          status: complete ? 'COMPLETED' : 'RUNNING',
          cursor: complete ? 2 : 1,
          events: [
            { sequence: 1, type: 'TEXT', text: result.text },
            ...(complete ? [{ sequence: 2, type: 'COMPLETED' }] : [])
          ],
          result: complete ? result : null
        }
      })
    return route.fulfill({ json: context })
  })
  await selectResource(page)
  await page.getByRole('textbox', { name: '当前 SQL' }).fill('original draft')
  await send(page, 'Suggest SQL')
  await expect(page.getByText(result.text, { exact: true })).toBeVisible()
  await page.getByRole('combobox', { name: '数据源' }).click()
  await page.getByRole('option', { name: 'Other database' }).click()
  await expect(page.getByText(result.text, { exact: true })).toBeVisible()
  expect(closes).toBe(0)
  complete = true
  await expect(page.getByRole('button', { name: '应用到当前草稿' })).toBeVisible()
  await page.getByRole('button', { name: '应用到当前草稿' }).click()
  await expect(page.getByRole('alert')).toContainText('目标或草稿已变化')
  expect(checks).toBe(0)
  await page.getByRole('combobox', { name: '数据源' }).click()
  await page.getByRole('option', { name: 'Isolated database' }).click()
  await page.getByRole('button', { name: '应用到当前草稿' }).click()
  await checking
  await page.getByRole('textbox', { name: '当前 SQL' }).fill('newer draft while checking')
  releaseCheck()
  await expect(page.getByRole('alert')).toContainText('目标或草稿已变化')
  await expect(page.getByRole('textbox', { name: '当前 SQL' })).toHaveValue(
    'newer draft while checking'
  )
})

test('cancelling interruption confirmation preserves the execution and its context', async ({
  page
}) => {
  let stops = 0
  let closes = 0
  await page.route('**/fixture-api/ai/conversations**', async (route) => {
    const url = new URL(route.request().url())
    if (url.pathname.endsWith('/resources'))
      return route.fulfill({ json: [{ id: '11', name: 'Isolated database' }] })
    if (route.request().method() === 'DELETE') {
      closes++
      return route.fulfill({ json: {} })
    }
    if (url.pathname.endsWith('/stop')) {
      stops++
      return route.fulfill({
        json: {
          ...session,
          executionId: 'confirm-run',
          status: 'CANCELLED',
          cursor: 2,
          events: [{ sequence: 2, type: 'CANCELLED' }]
        }
      })
    }
    if (url.pathname.endsWith('/events'))
      return route.fulfill({
        json: {
          ...session,
          executionId: 'confirm-run',
          status: 'RUNNING',
          cursor: 1,
          events: [{ sequence: 1, type: 'TEXT', text: 'Keep original execution' }]
        }
      })
    if (url.pathname.endsWith('/executions'))
      return route.fulfill({
        json: {
          ...session,
          executionId: 'confirm-run',
          status: 'RUNNING',
          deadline: Date.now() + 60000
        }
      })
    return route.fulfill({ json: session })
  })
  await selectResource(page)
  await send(page, 'Original question')
  await expect(page.getByText('Keep original execution', { exact: true })).toBeVisible()
  await page.getByRole('button', { name: '停止生成', exact: true }).click()
  await expect(page.getByRole('dialog')).toBeVisible()
  await page.getByRole('button', { name: '继续生成', exact: true }).click()
  await expect(page.getByRole('status')).toContainText('正在生成')
  expect(stops).toBe(0)
  expect(closes).toBe(0)
  await expect(page.getByText('Original question', { exact: true })).toBeVisible()
  await page.getByRole('button', { name: '结束会话', exact: true }).click()
  await page.getByRole('button', { name: '继续生成', exact: true }).click()
  expect(closes).toBe(0)
  await page.getByRole('button', { name: '停止生成', exact: true }).click()
  await page.getByRole('button', { name: '确认中断', exact: true }).click()
  await expect(page.getByText('· 未完成')).toBeVisible()
  expect(stops).toBe(1)
})

test('registered Demo route passes the application permission path check', async ({ page }) => {
  await page.route('**/route-check', (route) =>
    route.fulfill({ contentType: 'text/html', body: '<div id="app"></div>' })
  )
  await page.goto('/route-check')
  const access = await page.evaluate(async () => {
    const { default: router } = await import('/src/router/index.ts')
    const { pathValid, usePermissionStoreWithOut } = await import(
      '/src/store/modules/permission.ts'
    )
    usePermissionStoreWithOut().clear()
    return {
      registered: router.resolve('/demo/ai-assistant').matched.length > 0,
      allowed: pathValid('/demo/ai-assistant'),
      unknownDemoAllowed: pathValid('/demo/unknown')
    }
  })
  expect(access.registered).toBe(true)
  expect(access.allowed).toBe(true)
  expect(access.unknownDemoAllowed).toBe(false)
  await page.route('**/fixture-api/ai/conversations/resources', (route) =>
    route.fulfill({ json: [] })
  )
  await page.goto('/?view=conversation-route#/demo/ai-assistant')
  await expect(page.getByRole('heading', { name: 'AI 对话 Demo（开发验收）' })).toBeVisible()
})

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
  await expect(page.getByRole('heading', { name: 'AI 对话 Demo（开发验收）' })).toBeVisible()
  await send(page, 'Explain this SQL')
  await expect(page.getByText('First increment', { exact: true })).toBeVisible()
  await page.getByRole('button', { name: '收起助手' }).click()
  await expect(page.getByText('First increment', { exact: true })).toBeHidden()
  await page.getByRole('button', { name: '展开助手' }).click()
  await expect(page.getByText('First increment', { exact: true })).toBeVisible()
  await page.getByRole('button', { name: '停止生成' }).click()
  await page.getByRole('button', { name: '确认中断', exact: true }).click()
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

test('SQL changes retain the original output and identity changes clear it while failed output stays incomplete', async ({
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
  await expect(page.getByText('Original question', { exact: true })).toBeVisible()
  await expect(page.getByText('Late old answer', { exact: true })).toBeVisible()
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
            {
              sequence: 1,
              type: 'TEXT',
              text: starts === 1 ? 'Partial answer' : 'New answer'
            },
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
  await page.getByRole('button', { name: '确认中断', exact: true }).click()
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
    const current = {
      ...session,
      sessionId: 'session-' + opens,
      executionId: 'run-' + opens
    }
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
    return route.fulfill({
      json: { ...session, sessionId: 'session-' + opens }
    })
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
      return route.fulfill({
        json: { ...run, status: 'RUNNING', deadline: Date.now() + 60000 }
      })
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
  await page.getByRole('button', { name: '确认中断', exact: true }).click()
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
      return route.fulfill({
        json: { ...run, status: 'RUNNING', deadline: Date.now() + 60000 }
      })
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
      await route.fulfill({
        json: [{ id: '11', name: 'Previous identity database' }]
      })
      oldFinished()
    } else
      await route.fulfill({
        json: [{ id: '22', name: 'Current identity database' }]
      })
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

test('program tool states expose safe parameters/results, deduplicate replay and stay distinct from model text', async ({
  page
}) => {
  let finished = false
  const tool = {
    callId: 'call-tables',
    name: 'get_table_fields',
    parameters: { table: 'AI_PROBE' }
  }
  const events = [
    { sequence: 1, type: 'TEXT', text: 'I will read the fields.' },
    { sequence: 2, type: 'TOOL', tool: { ...tool, status: 'STARTED' } },
    { sequence: 3, type: 'TOOL', tool: { ...tool, status: 'RUNNING' } },
    {
      sequence: 4,
      type: 'TOOL',
      tool: {
        ...tool,
        status: 'SUCCEEDED',
        result: '{"fields":[{"name":"ID","type":"BIGINT","comment":"<script>private</script>"}]}'
      }
    },
    { sequence: 5, type: 'TEXT', text: 'The actual field is ID.' },
    { sequence: 6, type: 'COMPLETED' }
  ]
  await page.route('**/fixture-api/ai/conversations**', async (route) => {
    const url = new URL(route.request().url())
    if (url.pathname.endsWith('/resources'))
      return route.fulfill({ json: [{ id: '11', name: 'Isolated database' }] })
    if (url.pathname.endsWith('/events'))
      return route.fulfill({
        json: {
          ...session,
          executionId: 'tools-run',
          status: finished ? 'COMPLETED' : 'RUNNING',
          cursor: finished ? 6 : 3,
          events: finished ? events : events.slice(0, 3)
        }
      })
    if (url.pathname.endsWith('/executions'))
      return route.fulfill({
        json: {
          ...session,
          executionId: 'tools-run',
          status: 'RUNNING',
          deadline: Date.now() + 60000
        }
      })
    return route.fulfill({ json: session })
  })
  await selectResource(page)
  await send(page, 'Read actual fields')
  const toolPanel = page.locator('[data-tool-id="call-tables"]')
  await expect(toolPanel).toHaveCount(1)
  await expect(toolPanel).toContainText('get_table_fields')
  await expect(toolPanel).toContainText('执行中')
  finished = true
  await expect(toolPanel).toContainText('成功')
  await expect(toolPanel).toContainText('AI_PROBE')
  await expect(toolPanel).toContainText('BIGINT')
  await expect(toolPanel.locator('script')).toHaveCount(0)
  await expect(
    page.getByText('I will read the fields.The actual field is ID.', {
      exact: true
    })
  ).toBeVisible()
})

for (const locale of [
  {
    language: 'zh-CN',
    resource: '数据源',
    message: '消息',
    send: '发送',
    verdict: '静态校验通过',
    reason: '已检查支持的语法和授权表/字段引用。SQL 未执行。',
    scope: '仅支持 MySQL',
    invalid: '引用无效',
    unsupported: '不支持静态校验',
    unknown: '无法识别校验结果',
    reference: '授权元数据中不存在对应的精确表名、字段名或表限定名。请修正引用后重新校验。',
    subset: '超出声明的静态校验子集。SQL 未执行。'
  },
  {
    language: 'tw',
    resource: '資料來源',
    message: '訊息',
    send: '傳送',
    verdict: '靜態校驗通過',
    reason: '已檢查支援的語法和授權資料表/欄位引用。SQL 未執行。',
    scope: '僅支援 MySQL',
    invalid: '引用無效',
    unsupported: '不支援靜態校驗',
    unknown: '無法識別校驗結果',
    reference:
      '授權中繼資料中不存在對應的精確資料表名稱、欄位名稱或資料表限定名稱。請修正引用後重新校驗。',
    subset: '超出聲明的靜態校驗子集。SQL 未執行。'
  },
  {
    language: 'en',
    resource: 'Datasource',
    message: 'Message',
    send: 'Send',
    verdict: 'Static validation passed',
    reason:
      'Supported syntax and authorized table/column references checked. SQL was not executed.',
    scope: 'MySQL only',
    invalid: 'Invalid reference',
    unsupported: 'Static validation unsupported',
    unknown: 'Unrecognized validation result',
    reference:
      'Unknown exact table, column or table qualifier in authorized metadata. Correct the reference and validate again.',
    subset: 'Outside the declared static validation subset. SQL was not executed.'
  }
]) {
  test(`SQL validation cards localize program verdict, reason and scope in ${locale.language}`, async ({
    page
  }) => {
    const run = { ...session, executionId: 'validation-run' }
    const events = ['INVALID', 'PASS', 'UNSUPPORTED'].map((status, index) => ({
      sequence: index + 1,
      type: 'TOOL',
      tool: {
        callId: 'validation-' + status,
        name: 'validate_sql',
        status: 'SUCCEEDED',
        parameters: {
          sql: status === 'INVALID' ? 'SELECT TYPO FROM AI_PROBE' : 'SELECT ID FROM AI_PROBE'
        },
        result: JSON.stringify({
          status,
          code: {
            PASS: 'verified',
            INVALID: 'reference',
            UNSUPPORTED: 'subset'
          }[status],
          message: '<script>unsafe</script>',
          scope: 'SQL was not executed; no database executability guarantee.'
        })
      }
    }))
    const unrecognized = [
      '{"status":"PASS","code":"constructor","message":"<script>bad</script>"}',
      '{"status":"INVALID","code":"verified"}',
      '{"status":"unexpected","code":"verified"}',
      '{malformed'
    ]
    for (const [index, result] of unrecognized.entries())
      events.push({
        sequence: events.length + 1,
        type: 'TOOL',
        tool: {
          callId: 'unknown-' + index,
          name: 'validate_sql',
          status: 'SUCCEEDED',
          parameters: {},
          result
        }
      })
    await page.route('**/fixture-api/ai/conversations**', async (route) => {
      const url = new URL(route.request().url())
      if (url.pathname.endsWith('/resources'))
        return route.fulfill({
          json: [{ id: '11', name: 'Isolated database' }]
        })
      if (url.pathname.endsWith('/executions'))
        return route.fulfill({
          json: { ...run, status: 'RUNNING', deadline: Date.now() + 60000 }
        })
      if (url.pathname.endsWith('/events'))
        return route.fulfill({
          json: {
            ...run,
            status: 'COMPLETED',
            cursor: events.length + 2,
            events: [
              ...events,
              {
                sequence: events.length + 1,
                type: 'TEXT',
                text: 'Corrected and revalidated; this is static advice.'
              },
              { sequence: events.length + 2, type: 'COMPLETED' }
            ]
          }
        })
      return route.fulfill({ json: session })
    })
    await page.goto('/?view=conversation&language=' + locale.language)
    await page.getByRole('combobox', { name: locale.resource }).click()
    await page.getByRole('option', { name: 'Isolated database' }).click()
    await page
      .getByRole('textbox', { name: locale.message })
      .fill('Correct invalid SQL and validate again')
    await page.getByRole('button', { name: locale.send, exact: true }).click()
    for (const status of ['INVALID', 'PASS', 'UNSUPPORTED']) {
      const card = page.locator(`[data-tool-id="validation-${status}"]`)
      await expect(card.locator('summary')).toContainText(status)
      await expect(card.locator('pre')).toContainText(['SELECT', 'SQL was not executed'])
      await expect(card.locator('script')).toHaveCount(0)
    }
    const invalid = page.locator('[data-tool-id="validation-INVALID"]')
    await expect(invalid.locator('summary')).toContainText(locale.invalid)
    await expect(invalid.getByText(locale.reference, { exact: true })).toBeVisible()
    const unsupported = page.locator('[data-tool-id="validation-UNSUPPORTED"]')
    await expect(unsupported.locator('summary')).toContainText(locale.unsupported)
    await expect(unsupported.getByText(locale.subset, { exact: true })).toBeVisible()
    for (const [index, result] of unrecognized.entries()) {
      const card = page.locator(`[data-tool-id="unknown-${index}"]`)
      await expect(card.locator('summary')).toContainText(locale.unknown)
      await expect(card.locator('summary')).not.toContainText('PASS')
      await expect(card.locator('pre').last()).toHaveText(result)
      await expect(card.locator('script')).toHaveCount(0)
    }
    const passed = page.locator('[data-tool-id="validation-PASS"]')
    await expect(passed.locator('summary')).toContainText(locale.verdict)
    await expect(passed.getByText(locale.reason, { exact: true })).toBeVisible()
    await expect(passed.locator('.validation-scope')).toContainText(locale.scope)
    // Display text comes from safe codes, while exact program JSON remains escaped evidence.
    await expect(passed.locator('pre').last()).toHaveText(events[1].tool.result)
    await expect(
      page.getByText('Corrected and revalidated; this is static advice.', {
        exact: true
      })
    ).toBeVisible()
  })
}

for (const context of ['user', 'workspace', 'proxy']) {
  test(`${context} changes exclude the old SQL and unsent draft from new requests`, async ({
    page
  }) => {
    const opens = [],
      turns = []
    await page.route('**/fixture-api/ai/conversations**', async (route) => {
      const request = route.request(),
        url = new URL(request.url())
      if (url.pathname.endsWith('/resources'))
        return route.fulfill({
          json: [{ id: '11', name: 'Isolated database' }]
        })
      if (request.method() === 'DELETE') return route.fulfill({ json: {} })
      if (url.pathname.endsWith('/executions')) {
        turns.push(request.postDataJSON())
        return route.fulfill({
          json: { ...session, executionId: 'new-run', status: 'RUNNING' }
        })
      }
      if (url.pathname.endsWith('/events'))
        return route.fulfill({
          json: {
            ...session,
            executionId: 'new-run',
            status: 'COMPLETED',
            cursor: 1,
            events: [{ sequence: 1, type: 'COMPLETED' }]
          }
        })
      opens.push(request.postDataJSON())
      return route.fulfill({ json: session })
    })
    await selectResource(page)
    await page.getByRole('textbox', { name: '当前 SQL' }).fill('SELECT PRIVATE_A FROM OLD_CONTEXT')
    await page.getByRole('textbox', { name: '消息' }).fill('Unsent private draft A')
    // Ending a conversation keeps editor drafts in the same identity.
    await page.getByRole('button', { name: '结束会话' }).click()
    await expect(page.getByRole('textbox', { name: '当前 SQL' })).toHaveValue(
      'SELECT PRIVATE_A FROM OLD_CONTEXT'
    )
    await expect(page.getByRole('textbox', { name: '消息' })).toHaveValue('Unsent private draft A')
    await page.evaluate((value) => window.aiSwitchUser(value), context)
    await page.getByRole('combobox', { name: '数据源' }).click()
    await page.getByRole('option', { name: 'Isolated database' }).click()
    const sendButton = page.getByRole('button', { name: '发送', exact: true })
    // Even a direct send attempt cannot carry the previous identity's draft.
    await sendButton.evaluate((button) => button.click())
    await expect(sendButton).toBeDisabled()
    expect(opens).toHaveLength(0)
    expect(turns).toHaveLength(0)
    await send(page, 'New context question')
    await expect.poll(() => turns.length).toBe(1)
    expect(opens).toEqual([
      { scene: 'sql', resourceId: '11', editorId: expect.any(String), sql: '' }
    ])
    expect(turns).toEqual([{ contextVersion: 'v1', text: 'New context question' }])
  })
}
