import { test, expect } from '@playwright/test'

test.beforeEach(async ({ page }) => {
  await page.route('**/fixture-api/ai/availability', (route) =>
    route.fulfill({ json: { available: true } })
  )
})

const session = {
  sessionId: 'session-a',
  contextVersion: 'v1',
  resourceId: '11',
  resourceName: 'Isolated database',
  editorId: 'editor',
  expiresAt: Date.now() + 30 * 60 * 1000
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

for (const [status, message] of [
  [403, '无权使用此 AI 功能或配置。'],
  [409, '配置或引用已变化，请刷新后重新操作。']
]) {
  test(`normalized conversation HTTP ${status} shows the specific refusal`, async ({ page }) => {
    await page.route('**/fixture-api/ai/conversations**', (route) => {
      const url = new URL(route.request().url())
      if (url.pathname.endsWith('/resources')) return route.fulfill({ json: [] })
      if (url.pathname.endsWith('/executions'))
        return route.fulfill({
          status,
          json: { msg: 'Untrusted upstream detail' }
        })
      if (route.request().method() === 'DELETE') return route.fulfill({ json: {} })
      return route.fulfill({ json: session })
    })
    await page.goto('/?view=conversation')
    await send(page, 'Explain SELECT')
    await expect(page.getByRole('alert')).toHaveText(message)
    await expect(page.getByRole('textbox', { name: '消息' })).toHaveValue('Explain SELECT')
    await expect(page.getByText('Untrusted upstream detail')).toHaveCount(0)
  })
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
      return route.fulfill({ status: 400, json: {} })
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
  await expect(page.getByText('正在生成', { exact: true })).toBeVisible()
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
    { contextVersion: 'v1', text: 'Explain this SQL', submissionKey: expect.any(String) },
    { contextVersion: 'v1', text: 'What next?', submissionKey: expect.any(String) }
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
  await expect(page.getByRole('button', { name: '核对同次提交' })).toBeVisible()
  await expect(page.getByText('· 未完成')).toHaveCount(0)
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

test('brief offline retains the execution while refresh ends the temporary session', async ({
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
  await expect(page.getByText('Visible partial', { exact: true })).toBeVisible()
  await expect(page.getByText('· 未完成')).toHaveCount(0)
  expect(closed).toHaveLength(0)
  expect(opens).toBe(1)
  await page.evaluate(() => window.dispatchEvent(new Event('pagehide')))
  await expect(page.getByText('Before disconnect', { exact: true })).toHaveCount(0)
  await expect.poll(() => closed.length).toBe(1)
  await page.reload()
  await page.getByRole('combobox', { name: '数据源' }).click()
  await page.getByRole('option', { name: 'Isolated database' }).click()
  await send(page, 'After refresh')
  await expect.poll(() => opens).toBe(2)
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

for (const [code, expected] of [
  ['timeout', 'AI 请求超时。'],
  [
    'response_truncated',
    '模型输出达到长度上限，回答已被截断且未完成。请缩短回答要求，或联系管理员调高输出上限后重试。'
  ]
]) {
  test(`safe ${code} failure is explained and partial text remains incomplete`, async ({
    page
  }) => {
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
              { sequence: 2, type: 'FAILED', error: code }
            ]
          }
        })
      return route.fulfill({ json: session })
    })
    await selectResource(page)
    await send(page, 'Will timeout')
    await expect(page.getByRole('alert')).toHaveText(expected)
    await expect(page.getByText('Interrupted partial', { exact: true })).toBeVisible()
    await expect(page.getByText('· 未完成')).toBeVisible()
  })
}


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
      {
        scene: 'sql',
        resourceId: '11',
        editorId: expect.any(String),
        sql: '',
        openKey: expect.any(String)
      }
    ])
    expect(turns).toEqual([
      { contextVersion: 'v1', text: 'New context question', submissionKey: expect.any(String) }
    ])
  })
}

test('independent background task remains active after reload and exposes cancel and retry', async ({
  page
}) => {
  let task
  let cancelled = 0
  let retried = 0
  await page.route('**/fixture-api/ai/conversations**', (route) =>
    route.fulfill({
      json: route.request().url().endsWith('/resources') ? [] : {}
    })
  )
  await page.route('**/fixture-api/ai/tasks**', (route) => {
    const url = new URL(route.request().url())
    if (url.pathname.endsWith('/notices')) return route.fulfill({ json: [] })
    if (url.pathname.endsWith('/cancel')) {
      cancelled++
      task = { ...task, status: 'CANCELLED', retryEligible: true }
    } else if (url.pathname.endsWith('/retry')) {
      retried++
      task = { ...task, attemptId: '1002', attemptNumber: 2, status: 'QUEUED' }
    } else if (route.request().method() === 'POST') {
      task = {
        taskId: '1000',
        attemptId: '1001',
        attemptNumber: 1,
        status: 'RUNNING',
        resourceName: '',
        modelIdentifier: 'fixed-model'
      }
      expect(route.request().postDataJSON().submissionKey).toBeTruthy()
    } else if (url.pathname.endsWith('/ai/tasks'))
      return route.fulfill({
        json: task && ['RUNNING', 'QUEUED'].includes(task.status) ? [task] : []
      })
    return route.fulfill({ json: task })
  })
  await page.goto('/?view=conversation')
  await page.getByRole('textbox', { name: '消息' }).fill('Explain independently')
  await page.getByRole('button', { name: '后台 SQL 辅助', exact: true }).click()
  await expect(page.getByRole('region', { name: '后台任务' })).toContainText('fixed-model')
  await page.reload()
  await expect(page.getByRole('region', { name: '后台任务' })).toContainText('fixed-model')
  expect(cancelled).toBe(0)
  await page.getByRole('button', { name: '取消后台任务' }).click()
  await expect(page.getByRole('region', { name: '后台任务' }).locator('article')).toHaveCount(0)
  await expect(page.getByRole('button', { name: '重试后台任务' })).toBeVisible()
  await page.getByRole('button', { name: '重试后台任务' }).click()
  expect(cancelled).toBe(1)
  expect(retried).toBe(1)
  await expect(page.getByRole('region', { name: '后台任务' })).toContainText('2')
})

test('online task completion is a basic toast and never becomes a terminal task list', async ({
  page
}) => {
  let task
  let completed = false
  await page.route('**/fixture-api/ai/conversations**', (route) =>
    route.fulfill({ json: route.request().url().endsWith('/resources') ? [] : {} })
  )
  await page.route('**/fixture-api/ai/tasks**', (route) => {
    const url = new URL(route.request().url())
    if (url.pathname.endsWith('/notices')) return route.fulfill({ json: [] })
    if (route.request().method() === 'POST') {
      task = {
        taskId: '2000',
        attemptId: '2001',
        attemptNumber: 1,
        status: 'RUNNING',
        modelIdentifier: 'online-model'
      }
    } else if (url.pathname.endsWith('/ai/tasks'))
      return route.fulfill({ json: task && !completed ? [task] : [] })
    return route.fulfill({
      json: completed
        ? { ...task, status: 'COMPLETED', result: { type: 'EXPLANATION', text: 'stored result' } }
        : task
    })
  })
  await page.goto('/?view=conversation')
  await page.getByRole('textbox', { name: '消息' }).fill('Explain independently')
  await page.getByRole('button', { name: '后台 SQL 辅助', exact: true }).click()
  const currentTasks = page.getByRole('region', { name: '后台任务' })
  await expect(currentTasks).toContainText('online-model')
  completed = true
  await expect(currentTasks.locator('article')).toHaveCount(0)
  await expect(
    page.getByRole('alert').filter({ hasText: '后台任务已完成，结果已保存。' })
  ).toBeVisible()
})

for (const status of ['QUEUED', 'RUNNING']) {
  test(`accepted ${status} background submission keeps its key until terminal observation`, async ({
    page
  }) => {
    const requests = []
    const tasks = new Map()
    await page.route('**/fixture-api/ai/conversations**', (route) =>
      route.fulfill({ json: route.request().url().endsWith('/resources') ? [] : {} })
    )
    await page.route('**/fixture-api/ai/tasks**', (route) => {
      const url = new URL(route.request().url())
      if (url.pathname.endsWith('/notices')) return route.fulfill({ json: [] })
      if (route.request().method() === 'POST') {
        const body = route.request().postDataJSON()
        requests.push(body)
        if (!tasks.has(body.submissionKey))
          tasks.set(body.submissionKey, {
            taskId: String(3000 + tasks.size),
            attemptId: String(4000 + tasks.size),
            attemptNumber: 1,
            status,
            modelIdentifier: 'accepted-model'
          })
        return route.fulfill({ json: tasks.get(body.submissionKey) })
      }
      if (url.pathname.endsWith('/ai/tasks'))
        return route.fulfill({
          json: [...tasks.values()].filter((task) => ['QUEUED', 'RUNNING'].includes(task.status))
        })
      return route.fulfill({
        json: [...tasks.values()].find((task) => url.pathname.endsWith('/' + task.taskId))
      })
    })
    await page.goto('/?view=conversation')
    const input = page.getByRole('textbox', { name: '消息' })
    const submit = page.getByRole('button', { name: '后台 SQL 辅助', exact: true })
    await input.fill('One accepted operation')
    await submit.click()
    await expect(submit).toBeEnabled()
    await expect(page.getByRole('region', { name: '后台任务' }).locator('article')).toHaveCount(1)
    await submit.click()
    await expect.poll(() => requests.length).toBe(2)
    expect(requests[1].submissionKey).toBe(requests[0].submissionKey)
    expect(tasks.size).toBe(1)
    await expect(submit).toBeEnabled()
    await input.fill('Explicitly changed input')
    await submit.click()
    await expect.poll(() => requests.length).toBe(3)
    expect(requests[2].submissionKey).not.toBe(requests[0].submissionKey)
    expect(tasks.size).toBe(2)
    tasks.get(requests[2].submissionKey).status = 'COMPLETED'
    await expect(
      page.getByRole('alert').filter({ hasText: '后台任务已完成，结果已保存。' })
    ).toBeVisible()
    await submit.click()
    await expect.poll(() => requests.length).toBe(4)
    expect(requests[3].submissionKey).not.toBe(requests[2].submissionKey)
    expect(tasks.size).toBe(3)
  })
}

test('unknown background admission response keeps the same operation key for retry', async ({
  page
}) => {
  const requests = []
  const tasks = new Map()
  await page.route('**/fixture-api/ai/conversations**', (route) =>
    route.fulfill({ json: route.request().url().endsWith('/resources') ? [] : {} })
  )
  await page.route('**/fixture-api/ai/tasks**', (route) => {
    if (route.request().url().endsWith('/notices')) return route.fulfill({ json: [] })
    if (route.request().method() === 'POST') {
      const body = route.request().postDataJSON()
      requests.push(body)
      if (!tasks.has(body.submissionKey))
        tasks.set(body.submissionKey, {
          taskId: '5000',
          attemptId: '5001',
          attemptNumber: 1,
          status: 'QUEUED'
        })
      if (requests.length === 1) return route.fulfill({ status: 503, json: {} })
      return route.fulfill({ json: tasks.get(body.submissionKey) })
    }
    return route.fulfill({ json: [...tasks.values()] })
  })
  await page.goto('/?view=conversation')
  await page.getByRole('textbox', { name: '消息' }).fill('Admission response was lost')
  const submit = page.getByRole('button', { name: '后台 SQL 辅助', exact: true })
  await submit.click()
  await expect(page.getByRole('alert')).toContainText('后台任务操作未确认')
  await expect(submit).toBeEnabled()
  await submit.click()
  await expect.poll(() => requests.length).toBe(2)
  expect(requests[1].submissionKey).toBe(requests[0].submissionKey)
  expect(tasks.size).toBe(1)
})

test('offline completion is toasted once after return and acknowledged without restoring chat', async ({
  page
}) => {
  let acknowledged = false
  let deliveries = 0
  await page.route('**/fixture-api/ai/conversations/resources', (route) =>
    route.fulfill({ json: [] })
  )
  await page.route('**/fixture-api/ai/tasks**', (route) => {
    const url = new URL(route.request().url())
    if (url.pathname.endsWith('/ack')) {
      acknowledged = true
      deliveries++
      return route.fulfill({ json: {} })
    }
    if (url.pathname.endsWith('/notices'))
      return route.fulfill({
        json: acknowledged
          ? []
          : [
              {
                taskId: '900',
                attemptId: '901',
                attemptNumber: 1,
                status: 'COMPLETED',
                expiresAt: Date.now() + 60000
              }
            ]
      })
    return route.fulfill({ json: [] })
  })
  await page.goto('/?view=conversation')
  await expect(page.getByRole('alert').filter({ hasText: '后台任务已完成' })).toBeVisible()
  await expect.poll(() => acknowledged).toBeTruthy()
  await page.reload()
  await expect(page.getByRole('region', { name: '后台任务' }).locator('article')).toHaveCount(0)
  await page.waitForTimeout(1100)
  expect(deliveries).toBe(1)
  await expect(page.getByText('durable answer', { exact: true })).toHaveCount(0)
})

test('background result joins only its still open original conversation after busy import and permits follow-up', async ({
  page
}) => {
  const requests = []
  let body
  let complete = false
  let consumed = 0
  const task = {
    taskId: '910',
    attemptId: '911',
    attemptNumber: 1,
    status: 'RUNNING',
    resourceId: '11',
    resourceName: 'Isolated database'
  }
  await page.route('**/fixture-api/ai/conversations**', (route) => {
    const url = new URL(route.request().url())
    if (url.pathname.endsWith('/resources'))
      return route.fulfill({ json: [{ id: '11', name: 'Isolated database' }] })
    if (route.request().method() === 'DELETE') return route.fulfill({ json: {} })
    if (url.pathname.endsWith('/executions')) {
      requests.push(route.request().postDataJSON())
      return route.fulfill({
        json: {
          ...session,
          executionId: 'chat-' + requests.length,
          status: 'RUNNING',
          deadline: Date.now() + 60000
        }
      })
    }
    if (url.pathname.endsWith('/events'))
      return route.fulfill({
        json: {
          ...session,
          executionId: 'chat-' + requests.length,
          status: 'COMPLETED',
          cursor: 1,
          events: [{ sequence: 1, type: 'TEXT', text: 'initial answer' }]
        }
      })
    return route.fulfill({ json: session })
  })
  await page.route('**/fixture-api/ai/tasks**', (route) => {
    const url = new URL(route.request().url())
    if (url.pathname.endsWith('/notices')) return route.fulfill({ json: [] })
    if (url.pathname.endsWith('/conversation')) {
      consumed++
      if (consumed <= 4)
        return route.fulfill({
          status: 409,
          json: { code: 50005, category: 'CONFLICT', messageKey: 'i18n_ai_concurrency' }
        })
      expect(route.request().postDataJSON()).toEqual({
        sessionId: session.sessionId,
        contextVersion: session.contextVersion
      })
      return route.fulfill({
        json: {
          ...task,
          status: 'COMPLETED',
          result: { type: 'EXPLANATION', text: 'original background answer' }
        }
      })
    }
    if (route.request().method() === 'POST') {
      body = route.request().postDataJSON()
      return route.fulfill({ json: task })
    }
    if (url.pathname.endsWith('/ai/tasks'))
      return route.fulfill({ json: complete ? [] : body ? [task] : [] })
    return route.fulfill({ json: { ...task, status: complete ? 'COMPLETED' : 'RUNNING' } })
  })
  await selectResource(page)
  await send(page, 'first question')
  await expect(page.getByText('initial answer', { exact: true })).toBeVisible()
  await page.getByRole('textbox', { name: '消息' }).fill('background question')
  await page.getByRole('button', { name: '后台 SQL 辅助', exact: true }).click()
  await expect.poll(() => body?.origin?.sessionId).toBe(session.sessionId)
  complete = true
  await expect(page.getByText('original background answer', { exact: true })).toBeVisible({
    timeout: 10000
  })
  expect(consumed).toBe(5)
  await expect(page.getByRole('button', { name: '重新读取后台进度' })).toHaveCount(0)
  await page.waitForTimeout(1200)
  expect(consumed).toBe(5)
  await send(page, 'follow up on that result')
  expect(requests[1].contextVersion).toBe(session.contextVersion)
})

test('closed original conversation never receives late background result', async ({ page }) => {
  const requests = []
  let body
  let complete = false
  let consumed = 0
  const task = {
    taskId: '910',
    attemptId: '911',
    attemptNumber: 1,
    status: 'RUNNING',
    resourceId: '11',
    resourceName: 'Isolated database'
  }
  await page.route('**/fixture-api/ai/conversations**', (route) => {
    const url = new URL(route.request().url())
    if (url.pathname.endsWith('/resources'))
      return route.fulfill({ json: [{ id: '11', name: 'Isolated database' }] })
    if (route.request().method() === 'DELETE') return route.fulfill({ json: {} })
    if (url.pathname.endsWith('/executions')) {
      requests.push(route.request().postDataJSON())
      return route.fulfill({
        json: {
          ...session,
          executionId: 'chat-' + requests.length,
          status: 'RUNNING',
          deadline: Date.now() + 60000
        }
      })
    }
    if (url.pathname.endsWith('/events'))
      return route.fulfill({
        json: {
          ...session,
          executionId: 'chat-' + requests.length,
          status: 'COMPLETED',
          cursor: 1,
          events: [{ sequence: 1, type: 'TEXT', text: 'initial answer' }]
        }
      })
    return route.fulfill({ json: session })
  })
  await page.route('**/fixture-api/ai/tasks**', (route) => {
    const url = new URL(route.request().url())
    if (url.pathname.endsWith('/notices')) return route.fulfill({ json: [] })
    if (url.pathname.endsWith('/conversation')) {
      consumed++
      expect(route.request().postDataJSON()).toEqual({
        sessionId: session.sessionId,
        contextVersion: session.contextVersion
      })
      return route.fulfill({
        json: {
          ...task,
          status: 'COMPLETED',
          result: { type: 'EXPLANATION', text: 'original background answer' }
        }
      })
    }
    if (route.request().method() === 'POST') {
      body = route.request().postDataJSON()
      return route.fulfill({ json: task })
    }
    if (url.pathname.endsWith('/ai/tasks'))
      return route.fulfill({ json: complete ? [] : body ? [task] : [] })
    return route.fulfill({ json: { ...task, status: complete ? 'COMPLETED' : 'RUNNING' } })
  })
  await selectResource(page)
  await send(page, 'first question')
  await expect(page.getByText('initial answer', { exact: true })).toBeVisible()
  await page.getByRole('textbox', { name: '消息' }).fill('background question')
  await page.getByRole('button', { name: '后台 SQL 辅助', exact: true }).click()
  await expect.poll(() => body?.origin?.sessionId).toBe(session.sessionId)
  await page.getByRole('button', { name: '结束会话', exact: true }).click()
  complete = true
  await page.waitForTimeout(1400)
  expect(consumed).toBe(0)
  await expect(page.getByText('original background answer', { exact: true })).toHaveCount(0)
  await expect(page.getByText('first question', { exact: true })).toHaveCount(0)
})

test('a lost notice acknowledgement is retried after refresh without repeating its toast', async ({
  page
}) => {
  let acknowledged = false
  let deliveries = 0
  await page.route('**/fixture-api/ai/conversations/resources', (route) =>
    route.fulfill({ json: [] })
  )
  await page.route('**/fixture-api/ai/tasks**', (route) => {
    const url = new URL(route.request().url())
    if (url.pathname.endsWith('/ack')) {
      deliveries++
      if (deliveries === 1) return route.fulfill({ status: 503, json: {} })
      acknowledged = true
      return route.fulfill({ json: {} })
    }
    if (url.pathname.endsWith('/notices'))
      return route.fulfill({
        json: acknowledged
          ? []
          : [
              {
                taskId: '900',
                attemptId: '901',
                attemptNumber: 1,
                status: 'COMPLETED',
                expiresAt: Date.now() + 60000
              }
            ]
      })
    return route.fulfill({ json: [] })
  })
  await page.goto('/?view=conversation')
  await expect(page.getByRole('alert').filter({ hasText: '后台任务已完成' })).toBeVisible()
  await expect.poll(() => deliveries).toBe(1)
  await page.reload()
  await expect(page.getByRole('region', { name: '后台任务' }).locator('article')).toHaveCount(0)
  await page.waitForTimeout(1100)
  await expect.poll(() => acknowledged).toBeTruthy()
  expect(deliveries).toBe(2)
  await expect(page.getByRole('alert').filter({ hasText: '后台任务已完成' })).toHaveCount(0)
  await expect(page.getByText('durable answer', { exact: true })).toHaveCount(0)
})

test('all accepted queued tasks stay visible beyond the old four-task page cap and can be cancelled', async ({
  page
}) => {
  const tasks = Array.from({ length: 10 }, (_, index) => ({
    taskId: String(2000 + index),
    attemptId: String(3000 + index),
    attemptNumber: 1,
    status: 'QUEUED',
    resourceName: `queued-${index}`,
    modelIdentifier: null
  }))
  let cancelled
  await page.route('**/fixture-api/ai/conversations**', (route) => route.fulfill({ json: [] }))
  await page.route('**/fixture-api/ai/tasks**', (route) => {
    const path = new URL(route.request().url()).pathname
    if (path.endsWith('/notices')) return route.fulfill({ json: [] })
    if (path.endsWith('/cancel')) {
      const task = tasks.find((value) => path.includes(`/${value.taskId}/`))
      task.status = 'CANCELLED'
      cancelled = task.taskId
      return route.fulfill({ json: task })
    }
    return route.fulfill({ json: tasks.filter((task) => task.status === 'QUEUED') })
  })
  await page.goto('/?view=conversation')
  const region = page.getByRole('region', { name: '后台任务' })
  await expect(region.locator('article')).toHaveCount(10)
  await region
    .locator('article')
    .filter({ hasText: 'queued-5' })
    .getByRole('button', { name: '取消后台任务' })
    .click()
  await expect(region.locator('article')).toHaveCount(9)
  expect(cancelled).toBe('2005')
})

test('full background queue promptly shows explicit busy refusal without an accepted task', async ({
  page
}) => {
  await page.route('**/fixture-api/ai/conversations**', (route) => route.fulfill({ json: [] }))
  await page.route('**/fixture-api/ai/tasks**', (route) =>
    route.fulfill(
      route.request().method() === 'POST'
        ? {
            status: 409,
            json: { messageKey: 'i18n_ai_task_queue_full', msg: 'controlled full queue' }
          }
        : { json: [] }
    )
  )
  await page.goto('/?view=conversation')
  await page.getByRole('textbox', { name: '消息' }).fill('Queue refusal')
  await page.getByRole('button', { name: '后台 SQL 辅助', exact: true }).click()
  await expect(page.getByRole('alert')).toContainText('后台任务队列已满，请稍后重试。')
  await expect(page.getByRole('region', { name: '后台任务' }).locator('article')).toHaveCount(0)
})

test('progress reconnect reuses execution and cursor without generating again', async ({
  page
}) => {
  let starts = 0
  let polls = 0
  const cursors = []
  await page.route('**/fixture-api/ai/conversations**', (route) => {
    const url = new URL(route.request().url())
    if (url.pathname.endsWith('/resources')) return route.fulfill({ json: [] })
    if (route.request().method() === 'DELETE') return route.fulfill({ json: {} })
    const run = { ...session, executionId: 'reconnect-run' }
    if (url.pathname.endsWith('/executions')) {
      starts++
      return route.fulfill({ json: { ...run, status: 'RUNNING', deadline: Date.now() + 60000 } })
    }
    if (url.pathname.endsWith('/events')) {
      polls++
      cursors.push(url.searchParams.get('after'))
      if (polls === 2 || polls === 3) return route.abort('connectionfailed')
      return route.fulfill({
        json: {
          ...run,
          status: polls === 1 ? 'RUNNING' : 'COMPLETED',
          cursor: polls === 1 ? 1 : 3,
          events: [
            { sequence: 1, type: 'TEXT', text: 'Retained ' },
            ...(polls === 1
              ? []
              : [
                  { sequence: 2, type: 'TEXT', text: 'answer' },
                  { sequence: 3, type: 'COMPLETED' }
                ])
          ]
        }
      })
    }
    return route.fulfill({ json: session })
  })
  await page.goto('/?view=conversation')
  await send(page, 'Recover the same progress')
  await expect(page.getByText('Retained answer', { exact: true })).toBeVisible({ timeout: 10000 })
  expect(starts).toBe(1)
  expect(cursors).toEqual(['0', '1', '1', '1'])
  await expect(page.getByText('· 未完成')).toHaveCount(0)
})

test('background read recovery is bounded and never submits model work', async ({ page }) => {
  let reads = 0
  let submissions = 0
  await page.route('**/fixture-api/ai/conversations/resources', (route) =>
    route.fulfill({ json: [] })
  )
  await page.route('**/fixture-api/ai/tasks**', (route) => {
    if (route.request().method() === 'POST') submissions++
    reads++
    return route.fulfill({ status: 503, json: {} })
  })
  await page.goto('/?view=conversation')
  await expect(page.getByRole('button', { name: '重新读取后台进度' })).toBeVisible({
    timeout: 12000
  })
  expect(reads).toBe(4)
  await page.waitForTimeout(1200)
  expect(reads).toBe(4)
  expect(submissions).toBe(0)
})

test('lost Open acceptance reuses its key and the same submission after explicit reconciliation', async ({
  page
}) => {
  const opens = []
  const starts = []
  await page.route('**/fixture-api/ai/conversations**', (route) => {
    const url = new URL(route.request().url())
    if (url.pathname.endsWith('/resources')) return route.fulfill({ json: [] })
    if (route.request().method() === 'DELETE') return route.fulfill({ json: {} })
    if (url.pathname.endsWith('/events'))
      return route.fulfill({
        json: {
          ...session,
          executionId: 'once',
          status: 'COMPLETED',
          cursor: 2,
          events: [
            { sequence: 1, type: 'TEXT', text: 'One accepted answer' },
            { sequence: 2, type: 'COMPLETED' }
          ]
        }
      })
    if (url.pathname.endsWith('/executions')) {
      starts.push(route.request().postDataJSON())
      return route.fulfill({
        json: { ...session, executionId: 'once', status: 'RUNNING', deadline: Date.now() + 60000 }
      })
    }
    opens.push(route.request().postDataJSON())
    return route.fulfill(opens.length === 1 ? { status: 503, json: {} } : { json: session })
  })
  await page.goto('/?view=conversation')
  await send(page, 'Uncertain acceptance')
  await expect(page.getByRole('button', { name: '核对同次提交' })).toBeVisible()
  await page.getByRole('textbox', { name: '消息' }).fill('Uncertain acceptance')
  await expect(page.getByRole('button', { name: '发送', exact: true })).toBeDisabled()
  await page.getByRole('button', { name: '核对同次提交' }).click()
  await expect(page.getByText('One accepted answer', { exact: true })).toBeVisible()
  await expect(page.getByRole('textbox', { name: '消息' })).toHaveValue('Uncertain acceptance')
  expect(opens).toHaveLength(2)
  expect(opens[0].openKey).toBeTruthy()
  expect(opens[1].openKey).toBe(opens[0].openKey)
  expect(starts).toHaveLength(1)
  expect(starts[0].submissionKey).toBeTruthy()
  await expect(page.getByText('Uncertain acceptance', { exact: true })).toHaveCount(1)
})

test('manual retry reconciles the same attempt key and keeps partial output separate', async ({
  page
}) => {
  const retries = []
  let initialStarts = 0
  await page.route('**/fixture-api/ai/conversations**', (route) => {
    const url = new URL(route.request().url())
    if (url.pathname.endsWith('/resources')) return route.fulfill({ json: [] })
    if (route.request().method() === 'DELETE') return route.fulfill({ json: {} })
    if (url.pathname.endsWith('/retry')) {
      retries.push(route.request().postDataJSON())
      return route.fulfill(
        retries.length === 1
          ? { status: 503, json: {} }
          : {
              json: {
                ...session,
                executionId: 'attempt-b',
                taskId: 'same-task',
                attemptId: 'b',
                attemptNumber: 2,
                sourceAttemptId: 'a',
                status: 'RUNNING',
                deadline: Date.now() + 60000
              }
            }
      )
    }
    if (url.pathname.endsWith('/events')) {
      const second = url.pathname.includes('attempt-b')
      return route.fulfill({
        json: {
          ...session,
          executionId: second ? 'attempt-b' : 'attempt-a',
          taskId: 'same-task',
          attemptId: second ? 'b' : 'a',
          status: second ? 'COMPLETED' : 'FAILED',
          retryEligible: !second,
          cursor: 2,
          events: [
            {
              sequence: 1,
              type: 'TEXT',
              text: second ? 'Independent new answer' : 'Incomplete original answer'
            },
            {
              sequence: 2,
              type: second ? 'COMPLETED' : 'FAILED',
              error: second ? null : 'connection_failed'
            }
          ]
        }
      })
    }
    if (url.pathname.endsWith('/executions')) {
      initialStarts++
      return route.fulfill({
        json: {
          ...session,
          executionId: 'attempt-a',
          taskId: 'same-task',
          attemptId: 'a',
          attemptNumber: 1,
          status: 'RUNNING',
          deadline: Date.now() + 60000
        }
      })
    }
    return route.fulfill({ json: session })
  })
  await page.goto('/?view=conversation')
  await send(page, 'Original frozen task')
  await page.getByRole('button', { name: '安全重试原任务' }).click()
  await expect(page.getByRole('alert')).toContainText('重试受理结果尚未确认')
  await page.getByRole('button', { name: '安全重试原任务' }).click()
  await expect(page.getByText('Independent new answer', { exact: true })).toBeVisible()
  await expect(page.getByText('Incomplete original answer', { exact: true })).toBeVisible()
  expect(retries).toHaveLength(2)
  expect(retries[0]).toEqual({ expectedAttemptId: 'a', retryKey: expect.any(String) })
  expect(retries[1]).toEqual(retries[0])
  expect(initialStarts).toBe(1)
})

test('lost execution acceptance still confirms leaving and preserves reconciliation', async ({
  page
}) => {
  const starts = []
  let closes = 0
  await page.route('**/fixture-api/ai/conversations**', (route) => {
    const url = new URL(route.request().url())
    if (url.pathname.endsWith('/resources')) return route.fulfill({ json: [] })
    if (route.request().method() === 'DELETE') {
      closes++
      return route.fulfill({ json: {} })
    }
    const run = {
      ...session,
      executionId: 'accepted-unknown',
      status: 'RUNNING',
      deadline: Date.now() + 60000
    }
    if (url.pathname.endsWith('/executions')) {
      starts.push(route.request().postDataJSON())
      return route.fulfill(starts.length === 1 ? { status: 503, json: {} } : { json: run })
    }
    if (url.pathname.endsWith('/events'))
      return route.fulfill({
        json: {
          ...run,
          status: 'COMPLETED',
          cursor: 2,
          events: [
            { sequence: 1, type: 'TEXT', text: 'Original accepted answer' },
            { sequence: 2, type: 'COMPLETED' }
          ]
        }
      })
    return route.fulfill({ json: session })
  })
  await page.goto('/?view=conversation&router=1')
  await send(page, 'Accepted but response lost')
  await expect(page.getByRole('button', { name: '核对同次提交' })).toBeVisible()
  await page.getByRole('button', { name: '结束会话', exact: true }).click()
  await expect(page.getByRole('dialog')).toBeVisible()
  await page.getByRole('button', { name: '继续生成', exact: true }).click()
  await expect(page.getByRole('dialog')).toHaveCount(0)
  await page.evaluate(() => {
    void window.aiNavigate('/after')
  })
  await expect(page.getByRole('dialog')).toBeVisible()
  await page.getByRole('button', { name: '继续生成', exact: true }).click()
  await expect(page.getByRole('dialog')).toHaveCount(0)
  expect(await page.evaluate(() => window.aiRoute())).toBe('/assistant')
  expect(
    await page.evaluate(() => {
      const event = new Event('beforeunload', { cancelable: true })
      window.dispatchEvent(event)
      return event.defaultPrevented
    })
  ).toBe(true)
  expect(closes).toBe(0)
  expect(starts).toHaveLength(1)
  await page.getByRole('button', { name: '核对同次提交' }).click()
  await expect(page.getByText('Original accepted answer', { exact: true })).toBeVisible()
  expect(starts).toHaveLength(2)
  expect(starts[1].submissionKey).toBe(starts[0].submissionKey)
  await page.evaluate(() => window.aiNavigate('/after'))
  await expect(page.getByText('After assistant', { exact: true })).toBeVisible()
})

test('exhausted foreground reads still confirm interruption and preserve cancelled navigation', async ({
  page
}) => {
  let reads = 0
  let starts = 0
  let stops = 0
  let closes = 0
  await page.route('**/fixture-api/ai/conversations**', (route) => {
    const url = new URL(route.request().url())
    const run = {
      ...session,
      executionId: 'unreachable-run',
      taskId: 'task-u',
      attemptId: 'attempt-u'
    }
    if (url.pathname.endsWith('/resources')) return route.fulfill({ json: [] })
    if (route.request().method() === 'DELETE') {
      closes++
      return route.fulfill({ json: {} })
    }
    if (url.pathname.endsWith('/stop')) {
      stops++
      return route.fulfill({
        json: {
          ...run,
          status: 'CANCELLED',
          cursor: 2,
          retryEligible: true,
          events: [{ sequence: 2, type: 'CANCELLED' }]
        }
      })
    }
    if (url.pathname.endsWith('/executions')) {
      starts++
      return route.fulfill({ json: { ...run, status: 'RUNNING', deadline: Date.now() + 60000 } })
    }
    if (url.pathname.endsWith('/events')) {
      reads++
      if (reads > 1) return route.abort('connectionfailed')
      return route.fulfill({
        json: {
          ...run,
          status: 'RUNNING',
          cursor: 1,
          events: [{ sequence: 1, type: 'TEXT', text: 'Unconfirmed partial' }]
        }
      })
    }
    return route.fulfill({ json: session })
  })
  await page.goto('/?view=conversation&router=1')
  await send(page, 'Read without replay')
  await expect(page.getByRole('button', { name: '继续读取原执行' })).toBeVisible({ timeout: 10000 })
  expect(reads).toBe(5)
  expect(starts).toBe(1)
  await expect(page.getByText('Unconfirmed partial', { exact: true })).toBeVisible()
  await expect(page.getByText('· 未完成')).toHaveCount(0)
  await page.getByRole('button', { name: '停止生成', exact: true }).click()
  await expect(page.getByRole('dialog')).toBeVisible()
  await page.getByRole('button', { name: '继续生成', exact: true }).click()
  await page.getByRole('button', { name: '结束会话', exact: true }).click()
  await page.getByRole('button', { name: '继续生成', exact: true }).click()
  await expect(page.getByRole('dialog')).toHaveCount(0)
  await page.evaluate(() => {
    void window.aiNavigate('/after')
  })
  await expect(page.getByRole('dialog')).toBeVisible()
  await page.getByRole('button', { name: '继续生成', exact: true }).click()
  await expect(page.getByRole('dialog')).toHaveCount(0)
  expect(await page.evaluate(() => window.aiRoute())).toBe('/assistant')
  expect(
    await page.evaluate(() => {
      const event = new Event('beforeunload', { cancelable: true })
      window.dispatchEvent(event)
      return event.defaultPrevented
    })
  ).toBe(true)
  expect(stops).toBe(0)
  expect(closes).toBe(0)
  expect(starts).toBe(1)
  await expect(page.getByText('Unconfirmed partial', { exact: true })).toBeVisible()
  await expect(page.getByRole('button', { name: '继续读取原执行' })).toBeVisible()
  await page.getByRole('button', { name: '停止生成', exact: true }).click()
  await page.getByRole('button', { name: '确认中断', exact: true }).click()
  await expect(page.getByRole('button', { name: '安全重试原任务' })).toBeVisible()
  expect(stops).toBe(1)
  await page.getByRole('textbox', { name: '消息' }).fill('A distinct later turn')
  await expect(page.getByRole('button', { name: '发送', exact: true })).toBeEnabled()
  await page.evaluate(() => window.aiNavigate('/after'))
  await expect(page.getByText('After assistant', { exact: true })).toBeVisible()
})

test('changed input is a distinct task while the older unknown submission keeps its key', async ({
  page
}) => {
  const posts = []
  const modelOperations = new Map()
  await page.route('**/fixture-api/ai/conversations**', (route) => {
    const url = new URL(route.request().url())
    if (url.pathname.endsWith('/resources')) return route.fulfill({ json: [] })
    if (route.request().method() === 'DELETE') return route.fulfill({ json: {} })
    if (url.pathname.endsWith('/events')) {
      const earlier = url.pathname.includes('earlier')
      return route.fulfill({
        json: {
          ...session,
          executionId: earlier ? 'earlier' : 'later',
          status: 'COMPLETED',
          cursor: 2,
          events: [
            {
              sequence: 1,
              type: 'TEXT',
              text: earlier ? 'Reconciled earlier result' : 'Distinct later result'
            },
            { sequence: 2, type: 'COMPLETED' }
          ]
        }
      })
    }
    if (url.pathname.endsWith('/executions')) {
      const body = route.request().postDataJSON()
      posts.push(body)
      const original = modelOperations.get(body.submissionKey)
      if (original) return route.fulfill({ json: original })
      const run = {
        ...session,
        executionId: posts.length === 1 ? 'earlier' : 'later',
        status: 'RUNNING',
        deadline: Date.now() + 60000
      }
      modelOperations.set(body.submissionKey, run)
      if (posts.length === 1) return route.fulfill({ status: 503, json: {} })
      return route.fulfill({ json: run })
    }
    return route.fulfill({ json: session })
  })
  await page.goto('/?view=conversation')
  await send(page, 'Earlier uncertain task')
  await expect(page.getByRole('button', { name: '核对同次提交' })).toBeVisible()
  await send(page, 'Changed input is a new task')
  await expect(page.getByText('Distinct later result', { exact: true })).toBeVisible()
  await page.getByRole('button', { name: '核对同次提交' }).click()
  await expect(page.getByText('Reconciled earlier result', { exact: true })).toBeVisible()
  expect(posts.map((post) => post.text)).toEqual([
    'Earlier uncertain task',
    'Changed input is a new task',
    'Earlier uncertain task'
  ])
  expect(posts[1].submissionKey).not.toBe(posts[0].submissionKey)
  expect(posts[2].submissionKey).toBe(posts[0].submissionKey)
  expect(modelOperations.size).toBe(2)
})

test('unknown submission capacity preserves original keys and rejects an eleventh operation', async ({
  page
}) => {
  const posts = []
  const accepted = new Map()
  await page.route('**/fixture-api/ai/conversations**', (route) => {
    const url = new URL(route.request().url())
    if (url.pathname.endsWith('/resources')) return route.fulfill({ json: [] })
    if (route.request().method() === 'DELETE') return route.fulfill({ json: {} })
    if (url.pathname.endsWith('/events'))
      return route.fulfill({
        json: {
          ...session,
          executionId: 'reconciled-first',
          status: 'COMPLETED',
          cursor: 2,
          events: [
            { sequence: 1, type: 'TEXT', text: 'Reconciled first operation' },
            { sequence: 2, type: 'COMPLETED' }
          ]
        }
      })
    if (url.pathname.endsWith('/executions')) {
      const body = route.request().postDataJSON()
      posts.push(body)
      if (accepted.has(body.submissionKey))
        return route.fulfill({
          json: {
            ...session,
            executionId: 'reconciled-first',
            status: 'RUNNING',
            deadline: Date.now() + 60000
          }
        })
      accepted.set(body.submissionKey, body.text)
      return route.fulfill({ status: 503, json: {} })
    }
    return route.fulfill({ json: session })
  })
  await page.goto('/?view=conversation')
  for (let index = 0; index < 10; index++) {
    await send(page, `unknown-${index}`)
    await expect.poll(() => posts.length).toBe(index + 1)
  }
  await send(page, 'unknown-overflow')
  await expect(page.getByRole('alert')).toContainText('未知受理操作已达上限')
  expect(posts).toHaveLength(10)
  await page.getByRole('button', { name: '核对同次提交' }).click()
  await expect(page.getByText('Reconciled first operation', { exact: true })).toBeVisible()
  expect(posts[10].submissionKey).toBe(posts[0].submissionKey)
  await send(page, 'unknown-overflow')
  await expect.poll(() => posts.length).toBe(12)
  expect(new Set(posts.map((post) => post.submissionKey)).size).toBe(11)
})

for (const entry of ['message', 'button'])
  test(`controlled resource mutex uses canonical batch names from ${entry}`, async ({ page }) => {
    let submitted
    await page.route('**/fixture-api/ai/conversations/resources', (route) =>
      route.fulfill({ json: [{ id: '11', name: 'Isolated database' }] })
    )
    await page.route('**/fixture-api/ai/tasks**', (route) => {
      const url = new URL(route.request().url())
      if (url.pathname.endsWith('/tables')) return route.fulfill({ json: ['A', 'B'] })
      if (route.request().method() === 'POST' && url.pathname.endsWith('/tasks')) {
        submitted = route.request().postDataJSON()
        return route.fulfill({
          json: {
            taskId: '71',
            attemptId: '72',
            attemptNumber: 1,
            status: 'QUEUED',
            scene: 'structure-mutex',
            resourceId: '11',
            resourceName: 'Isolated database',
            createdAt: Date.now()
          }
        })
      }
      return route.fulfill({ json: [] })
    })
    await selectResource(page)
    await page.getByText('受控只读结构 / 资源互斥验收', { exact: true }).click()
    await expect(
      page.getByText('仅解释选中表结构；不生成语义、不执行 SQL、不保存业务资源。')
    ).toBeVisible()
    await page.getByRole('combobox', { name: '互斥批次表' }).focus()
    await page.getByRole('combobox', { name: '互斥批次表' }).press('ArrowDown')
    await page.getByRole('option', { name: 'A', exact: true }).click()
    await page.getByRole('option', { name: 'B', exact: true }).click()
    await page.keyboard.press('Escape')
    if (entry === 'message')
      await page.getByRole('textbox', { name: '消息' }).fill('Explain this batch')
    await page
      .getByRole('button', {
        name: entry === 'message' ? '从消息发起受控后台' : '受控后台结构说明',
        exact: true
      })
      .click()
    await expect.poll(() => submitted?.scene).toBe('structure-mutex')
    expect(submitted.capability).toBe('structure-mutex.assist')
    expect(submitted.selectedTables).toEqual(['A', 'B'])
    expect(submitted.text).toBe(entry === 'message' ? 'Explain this batch' : '解释选中表的结构')
    expect(submitted.origin).toBeUndefined()
    if (entry === 'message') {
      await expect(page.getByRole('region', { name: '对话', exact: true })).toContainText(
        'Explain this batch'
      )
      await expect(page.getByRole('region', { name: '对话', exact: true })).toContainText(
        '受控后台任务 71 已受理'
      )
    } else await expect(page.getByRole('region', { name: '对话', exact: true })).toBeEmpty()
  })

for (const mine of [true, false])
  test(`resource busy is a clear refusal with ${
    mine ? 'own task location' : 'no other-owner link'
  }`, async ({ page }) => {
    await page.route('**/fixture-api/ai/conversations/resources', (route) =>
      route.fulfill({ json: [] })
    )
    await page.route('**/fixture-api/ai/tasks**', (route) => {
      const url = new URL(route.request().url())
      if (route.request().method() === 'POST' && url.pathname.endsWith('/tasks'))
        return route.fulfill({
          status: 409,
          json: {
            messageKey: 'i18n_ai_task_resource_busy',
            ...(mine ? { data: { taskId: '71' } } : {})
          }
        })
      if (url.pathname.endsWith('/71'))
        return route.fulfill({
          json: {
            taskId: '71',
            attemptId: '72',
            attemptNumber: 1,
            status: 'RUNNING',
            scene: 'structure-mutex',
            resourceId: '11',
            resourceName: 'My current batch',
            createdAt: Date.now()
          }
        })
      return route.fulfill({ json: [] })
    })
    await page.goto('/?view=conversation')
    await page.getByRole('textbox', { name: '消息' }).fill('Current request')
    await page.getByRole('button', { name: '后台 SQL 辅助', exact: true }).click()
    await expect(page.getByRole('alert')).toHaveText('所选资源正被同类后台任务占用，本次未受理。')
    const locate = page.getByRole('button', { name: '查看本人当前任务' })
    if (mine) {
      await locate.click()
      await expect(page.getByRole('button', { name: '取消后台任务', exact: true })).toBeVisible()
    } else await expect(locate).toHaveCount(0)
  })

test('AI off hides the assistant and re-enable retains only valid pending toast', async ({
  page
}) => {
  let available = true
  let revision = '0:0'
  let acknowledged = false
  let notificationReady = false
  await page.route('**/fixture-api/ai/availability', (route) =>
    route.fulfill({ json: { available, revision } })
  )
  await page.route('**/fixture-api/ai/conversations**', (route) => {
    if (route.request().url().endsWith('/resources')) return route.fulfill({ json: [] })
    if (route.request().method() === 'DELETE') return route.fulfill({ json: {} })
    return route.fulfill({ json: session })
  })
  await page.route('**/fixture-api/ai/tasks**', (route) => {
    if (route.request().url().endsWith('/ack')) {
      acknowledged = true
      return route.fulfill({ json: {} })
    }
    if (route.request().url().endsWith('/notices'))
      return route.fulfill({
        json:
          notificationReady && available && !acknowledged
            ? [
                {
                  taskId: '7',
                  attemptId: '8',
                  attemptNumber: 1,
                  status: 'COMPLETED',
                  errorCode: null,
                  expiresAt: Date.now() + 60000,
                  retryEligible: false
                }
              ]
            : []
      })
    return route.fulfill({ json: [] })
  })
  await page.goto('/?view=conversation')
  await page.getByRole('textbox', { name: '当前 SQL' }).fill('SELECT applied_business_draft')
  await page.getByRole('textbox', { name: '消息' }).fill('Temporary old question')
  available = false
  await expect(page.locator('main.ai-conversation')).toHaveCount(0)
  notificationReady = true
  available = true
  await expect(page.getByRole('textbox', { name: '消息' })).toHaveValue('')
  await expect(page.getByText('Temporary old question', { exact: true })).toHaveCount(0)
  await expect.poll(() => acknowledged).toBe(true)
  await expect(page.getByRole('textbox', { name: '当前 SQL' })).toHaveValue(
    'SELECT applied_business_draft'
  )
  await page.getByRole('textbox', { name: '消息' }).fill('Missed off interval')
  revision = '1:0'
  await expect(page.getByRole('textbox', { name: '消息' })).toHaveValue('')
  await expect(page.getByRole('textbox', { name: '当前 SQL' })).toHaveValue(
    'SELECT applied_business_draft'
  )
})

for (const language of [
  {
    code: 'en',
    message: 'Message',
    background: 'Background SQL assistance',
    retry: 'Retry background task',
    busy: 'Selected resources are occupied by a similar background task. This request was not accepted.',
    mutex: 'Controlled read-only structure / resource mutex acceptance'
  },
  {
    code: 'tw',
    message: '訊息',
    background: '背景 SQL 輔助',
    retry: '重試背景任務',
    busy: '選取資源正被同類背景任務占用，本次未受理。',
    mutex: '受控唯讀結構 / 資源互斥驗收'
  }
])
  test(`narrow ${language.code} shutdown new-task retry mutex and pending notice remain readable`, async ({
    page
  }, info) => {
    await page.setViewportSize({ width: 360, height: 800 })
    let available = true
    let retries = 0
    let ack = false
    await page.route('**/fixture-api/ai/availability', (route) =>
      route.fulfill({ json: { available } })
    )
    await page.route('**/fixture-api/ai/conversations/resources', (route) =>
      route.fulfill({ json: [] })
    )
    await page.route('**/fixture-api/ai/tasks**', (route) => {
      const url = new URL(route.request().url())
      if (url.pathname.endsWith('/ack')) {
        ack = true
        return route.fulfill({ json: {} })
      }
      if (url.pathname.endsWith('/retry')) {
        retries++
        return route.fulfill({
          json: {
            taskId: '80',
            attemptId: '82',
            attemptNumber: 2,
            status: 'QUEUED',
            resourceName: 'Controlled fixture',
            createdAt: Date.now()
          }
        })
      }
      if (url.pathname.endsWith('/notices'))
        return route.fulfill({
          json: ack
            ? []
            : [
                {
                  taskId: '80',
                  attemptId: '81',
                  attemptNumber: 1,
                  status: 'FAILED',
                  errorCode: 'connection_failed',
                  retryEligible: true,
                  expiresAt: Date.now() + 60000
                }
              ]
        })
      if (route.request().method() === 'POST')
        return route.fulfill({ status: 409, json: { messageKey: 'i18n_ai_task_resource_busy' } })
      return route.fulfill({ json: [] })
    })
    await page.goto('/?view=conversation&language=' + language.code)
    await expect(page.getByRole('button', { name: language.background, exact: true })).toBeVisible()
    await expect(page.getByRole('button', { name: language.retry, exact: true })).toBeVisible()
    await page.screenshot({ path: info.outputPath('narrow-notice.png'), fullPage: true })
    await page.getByRole('button', { name: language.retry, exact: true }).click()
    await expect.poll(() => retries).toBe(1)
    await page.getByRole('textbox', { name: language.message }).fill('New independent request')
    await page.getByRole('button', { name: language.background, exact: true }).click()
    await expect(page.locator('p[role="alert"]')).toHaveText(language.busy)
    await expect(page.getByText(language.mutex, { exact: true })).toBeVisible()
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true)
    await page.screenshot({ path: info.outputPath('narrow-policy.png'), fullPage: true })
    available = false
    await expect(page.locator('main.ai-conversation')).toHaveCount(0)
    available = true
    await expect(page.getByRole('textbox', { name: language.message })).toHaveValue('')
    await expect(page.getByRole('button', { name: language.retry, exact: true })).toHaveCount(0)
  })

for (const language of [
  {
    code: 'zh-CN',
    message: '消息',
    send: '发送',
    tasks: '后台任务',
    copy: '复制执行 ID',
    technical: '技术重试',
    reasons: [
      '模型服务暂不可用，正在重试当前步骤。',
      '模型服务限流，正在等待重试。',
      '连接中断，正在重试当前步骤。',
      '正在修正模型输出。',
      '正在重试当前步骤。'
    ]
  },
  {
    code: 'en',
    message: 'Message',
    send: 'Send',
    tasks: 'Background tasks',
    copy: 'Copy execution ID',
    technical: 'Technical retries',
    reasons: [
      'The model service is temporarily unavailable. Retrying this step.',
      'The model rate limit was reached. Waiting to retry.',
      'The connection was interrupted. Retrying this step.',
      'Correcting the model output.',
      'Retrying the current step.'
    ]
  },
  {
    code: 'tw',
    message: '訊息',
    send: '傳送',
    tasks: '背景任務',
    copy: '複製執行 ID',
    technical: '技術重試',
    reasons: [
      '模型服務暫不可用，正在重試目前步驟。',
      '模型服務限流，正在等待重試。',
      '連線中斷，正在重試目前步驟。',
      '正在修正模型輸出。',
      '正在重試目前步驟。'
    ]
  }
])
  test(`recovery reasons ${language.code} are safe and localized in foreground and background at 360px`, async ({
    page
  }, info) => {
    await page.setViewportSize({ width: 360, height: 800 })
    let reason = 'provider_unavailable'
    let retryCount = 1
    const recovery = () => ({
      stepId: 'step-1',
      stepKind: 'MODEL',
      technicalRetries: retryCount,
      corrections: 1,
      reason
    })
    await page.route('**/fixture-api/ai/tasks**', (route) => {
      if (new URL(route.request().url()).pathname.endsWith('/notices'))
        return route.fulfill({ json: [] })
      return route.fulfill({
        json: [
          {
            taskId: '910',
            attemptId: '911',
            attemptNumber: 2,
            status: 'RUNNING',
            resourceName: 'Controlled fixture',
            executionId: 'background-execution',
            recovery: recovery()
          }
        ]
      })
    })
    await page.route('**/fixture-api/ai/conversations**', (route) => {
      const path = new URL(route.request().url()).pathname
      if (path.endsWith('/resources')) return route.fulfill({ json: [] })
      if (path.endsWith('/events'))
        return route.fulfill({
          json: {
            ...session,
            executionId: 'foreground-execution',
            status: 'RUNNING',
            cursor: 0,
            events: [],
            recovery: recovery()
          }
        })
      if (path.endsWith('/executions'))
        return route.fulfill({
          json: {
            ...session,
            executionId: 'foreground-execution',
            status: 'RUNNING',
            deadline: Date.now() + 60000
          }
        })
      return route.fulfill({ json: session })
    })
    await page.goto('/?view=conversation&language=' + language.code)
    await page.getByRole('textbox', { name: language.message }).fill('Controlled recovery')
    await page.getByRole('button', { name: language.send, exact: true }).click()
    const foreground = page.locator('.messages')
    const background = page.getByRole('region', {
      name: language.tasks,
      exact: true
    })
    for (const [index, code] of [
      'provider_unavailable',
      'provider_rate_limit',
      'connection_failed',
      'model_correction',
      'unknown-upstream-SECRET'
    ].entries()) {
      reason = code
      retryCount = index + 1
      await expect(foreground).toContainText(language.technical + ' ' + retryCount)
      await expect(background).toContainText(language.technical + ' ' + retryCount)
      expect.soft(await foreground.innerText()).toContain(language.reasons[index])
      expect.soft(await background.innerText()).toContain(language.reasons[index])
      expect.soft(await foreground.innerText()).not.toContain(code)
      expect.soft(await background.innerText()).not.toContain(code)
      expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(
        true
      )
    }
    await expect(foreground).toContainText('foreground-execution')
    await expect(background).toContainText('background-execution')
    await expect(foreground.getByRole('button', { name: language.copy, exact: true })).toBeVisible()
    await expect(background.getByRole('button', { name: language.copy, exact: true })).toBeVisible()
    await page.screenshot({
      path: info.outputPath('recovery-' + language.code + '-360.png'),
      fullPage: true
    })
  })
