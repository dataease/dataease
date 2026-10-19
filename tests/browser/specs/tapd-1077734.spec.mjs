import { test, expect } from '@playwright/test'

test.beforeEach(async ({ page }) => {
  await page.goto('/')
})

test('刷新仅发送身份字段，保留草稿，随后保存使用新 Ticket', async ({ page }) => {
  const requests = []
  await page.route('**/ticket/saveTicket', async route => {
    requests.push(route.request().postDataJSON())
    await route.fulfill({ json: { data: 'rotated-ticket' } })
  })
  await page.getByRole('button', { name: '编辑已有 Ticket', exact: true }).click()
  await page.getByRole('spinbutton').fill('60')
  await page.getByPlaceholder('link_ticket.arg_val_tips').fill('["draft"]')
  await page.locator('.ticket-row button').nth(1).click()
  await expect(page.getByText('rotated-ticket', { exact: true })).toBeVisible()
  expect(requests[0]).toEqual({ uuid: 'share-uuid', ticket: 'old-ticket', generateNew: true })
  await expect(page.getByRole('spinbutton')).toHaveValue('60')
  await expect(page.getByPlaceholder('link_ticket.arg_val_tips')).toHaveValue('["draft"]')
  await expect(page.locator('#saved')).toHaveText('1')
  await page.getByRole('button', { name: 'commons.save', exact: true }).click()
  await expect.poll(() => requests.length).toBe(2)
  expect(requests[1]).toEqual({ uuid: 'share-uuid', ticket: 'rotated-ticket', exp: 60, args: '{"region":["draft"]}' })
})

test('请求期间禁止重复刷新和保存，失败保留旧 Ticket，允许重试', async ({ page }) => {
  let release
  let count = 0
  const gate = new Promise(resolve => { release = resolve })
  const errors = []
  page.on('pageerror', error => errors.push(error.message))
  await page.route('**/ticket/saveTicket', async route => {
    count++
    if (count === 1) {
      await gate
      await route.fulfill({ status: 403, json: { msg: 'denied' } })
    } else await route.fulfill({ json: { data: 'retry-ticket' } })
  })
  await page.getByRole('button', { name: '编辑已有 Ticket', exact: true }).click()
  const refresh = page.locator('.ticket-row button').nth(1)
  await refresh.click()
  await expect.poll(() => count).toBe(1)
  await refresh.dispatchEvent('click')
  await expect(page.getByRole('button', { name: 'commons.save', exact: true })).toBeDisabled()
  expect(count).toBe(1)
  release()
  await expect(page.getByRole('button', { name: 'commons.save', exact: true })).toBeEnabled()
  await expect(page.getByText('old-ticket', { exact: true })).toBeVisible()
  await expect(page.locator('#saved')).toHaveText('0')
  await refresh.click()
  await expect(page.getByText('retry-ticket', { exact: true })).toBeVisible()
  expect(errors).toEqual([])
})

test('新建模式刷新只生成临时 Ticket', async ({ page }) => {
  let temporary = 0
  let saves = 0
  await page.route('**/ticket/tempTicket', route => route.fulfill({ json: { data: `temporary-${++temporary}` } }))
  await page.route('**/ticket/saveTicket', route => { saves++; return route.fulfill({ json: {} }) })
  await page.getByRole('button', { name: '新建 Ticket', exact: true }).click()
  await expect(page.getByText('temporary-1', { exact: true })).toBeVisible()
  await page.locator('.ticket-row button').nth(1).click()
  await expect(page.getByText('temporary-2', { exact: true })).toBeVisible()
  expect(saves).toBe(0)
  await expect(page.locator('#saved')).toHaveText('0')
})
