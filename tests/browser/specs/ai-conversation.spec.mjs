import { test, expect } from '@playwright/test'

test('SQL assistant opens a temporary context and continues a conversation', async ({ page }) => {
  const turns = []
  await page.route('**/fixture-api/ai/conversations**', async (route) => {
    const request = route.request()
    if (request.url().endsWith('/resources'))
      return route.fulfill({ json: [{ id: '11', name: 'Isolated database' }] })
    if (request.method() === 'DELETE') return route.fulfill({ json: {} })
    if (request.url().endsWith('/turns')) {
      turns.push(request.postDataJSON())
      return route.fulfill({
        json: {
          sessionId: 'session-a',
          contextVersion: 'v1',
          executionId: 'run-' + turns.length,
          status: 'COMPLETED',
          text: turns.length === 1 ? 'First answer' : 'Follow-up answer'
        }
      })
    }
    return route.fulfill({
      json: {
        sessionId: 'session-a',
        contextVersion: 'v1',
        resourceId: '11',
        resourceName: 'Isolated database',
        editorId: 'editor',
        expiresAt: Date.now() + 60000
      }
    })
  })
  await page.goto('/?view=conversation')
  await page.getByRole('combobox', { name: '数据源' }).click()
  await page.getByRole('option', { name: 'Isolated database' }).click()
  await page.getByRole('textbox', { name: '当前 SQL' }).fill('select 1')
  await page.getByRole('textbox', { name: '消息' }).fill('Explain this SQL')
  await page.getByRole('button', { name: '发送', exact: true }).click()
  await expect(page.getByText('First answer', { exact: true })).toBeVisible()
  await page.getByRole('textbox', { name: '消息' }).fill('What next?')
  await page.getByRole('button', { name: '发送', exact: true }).click()
  await expect(page.getByText('Follow-up answer', { exact: true })).toBeVisible()
  expect(turns).toEqual([
    { contextVersion: 'v1', text: 'Explain this SQL' },
    { contextVersion: 'v1', text: 'What next?' }
  ])
  await page.getByRole('button', { name: '结束会话' }).click()
  await expect(page.getByText('First answer', { exact: true })).toHaveCount(0)
})

test('switching SQL context discards a late answer and failed output remains incomplete', async ({
  page
}) => {
  let release
  let requested
  const started = new Promise((resolve) => {
    requested = resolve
  })
  const delayed = new Promise((resolve) => {
    release = resolve
  })
  let turns = 0
  await page.route('**/fixture-api/ai/conversations**', async (route) => {
    const request = route.request()
    if (request.url().endsWith('/resources'))
      return route.fulfill({ json: [{ id: '11', name: 'Isolated database' }] })
    if (request.method() === 'DELETE') return route.fulfill({ json: {} })
    if (request.url().endsWith('/turns')) {
      turns++
      if (turns > 1) return route.fulfill({ status: 503, json: {} })
      requested()
      await delayed
      return route.fulfill({
        json: {
          sessionId: 'old-session',
          contextVersion: 'v1',
          executionId: 'old-run',
          status: 'COMPLETED',
          text: 'Late old answer'
        }
      })
    }
    return route.fulfill({
      json: {
        sessionId: 'old-session',
        contextVersion: 'v1',
        resourceId: '11',
        resourceName: 'Isolated database',
        editorId: 'editor',
        expiresAt: Date.now() + 60000
      }
    })
  })
  await page.goto('/?view=conversation')
  await page.getByRole('combobox', { name: '数据源' }).click()
  await page.getByRole('option', { name: 'Isolated database' }).click()
  await page.getByRole('textbox', { name: '消息' }).fill('Original question')
  await page.getByRole('button', { name: '发送', exact: true }).click()
  await started
  await page.getByRole('textbox', { name: '当前 SQL' }).fill('select 2')
  release()
  await expect(page.getByText('Original question', { exact: true })).toHaveCount(0)
  await expect(page.getByText('Late old answer', { exact: true })).toHaveCount(0)
  await page.getByRole('textbox', { name: '消息' }).fill('Try new context')
  await page.getByRole('button', { name: '发送', exact: true }).click()
  await expect(page.getByRole('alert')).toBeVisible()
  await expect(page.getByText('· 未完成')).toBeVisible()
  await page.evaluate(() => window.aiSwitchUser())
  await expect(page.getByText('Try new context', { exact: true })).toHaveCount(0)
})
