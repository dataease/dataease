import { test, expect } from '@playwright/test'

test('真实分享：刷新保留已保存配置，草稿需显式保存，列表刷新兼容', async ({ page, browser }) => {
  test.setTimeout(120_000)
  const resourceId = process.env.TICKET_TEST_RESOURCE_ID
  test.skip(!resourceId || !process.env.TICKET_TEST_PASSWORD, '需要已开启分享的专用测试资源和测试账号')
  await page.goto('/#/login')
  await page.getByRole('textbox', { name: '账号/邮箱' }).fill(process.env.TICKET_TEST_USER || 'admin')
  await page.getByRole('textbox', { name: '密码', exact: true }).fill(process.env.TICKET_TEST_PASSWORD)
  await page.getByRole('button', { name: '登录', exact: true }).click()
  await page.waitForURL(url => !url.hash.includes('/login'))
  await page.goto(`/#/panel/index?dvId=${resourceId}`)
  await page.getByRole('button', { name: '分享', exact: true }).click()
  await page.getByRole('button', { name: 'Ticket 设置', exact: true }).click()
  const settings = page.getByRole('dialog').first()
  const requiredBefore = await settings.getByRole('checkbox', { name: '必选' }).isChecked()
  await settings.getByRole('button', { name: '添加', exact: true }).click()
  const editor = page.getByRole('dialog').last()
  await editor.getByRole('spinbutton').fill('30')
  await editor.locator('.args-line input').nth(0).fill('region')
  await editor.locator('.args-line input').nth(1).fill('["saved"]')
  await expect(editor.locator('.ticket-row > span')).toHaveText(/\S+/)
  const initial = await editor.locator('.ticket-row > span').innerText()
  const save = page.waitForResponse(r => r.url().includes('/ticket/saveTicket'))
  await editor.getByRole('button', { name: '保存', exact: true }).click()
  expect((await (await save).json()).code).toBe(0)
  let row = page.locator('.ed-table__body .ed-table__row').filter({ hasText: initial })
  await expect(row).toHaveCount(1)
  await row.getByRole('button').nth(2).click()
  await editor.getByRole('spinbutton').fill('60')
  await editor.locator('.args-line input').nth(1).fill('["draft"]')
  const rotation = page.waitForResponse(r => r.url().includes('/ticket/saveTicket'))
  const reload = page.waitForResponse(r => r.url().includes('/ticket/pager/'))
  await editor.locator('.ticket-row button').nth(1).click()
  const response = await rotation
  const sent = response.request().postDataJSON()
  expect(Object.keys(sent).sort()).toEqual(['generateNew', 'ticket', 'uuid'])
  expect(sent.uuid).toBeTruthy()
  const rotated = (await response.json()).data
  expect(rotated).not.toBe(initial)
  const records = (await (await reload).json()).data.records
  expect(records.some(r => r.ticket === initial)).toBe(false)
  const persisted = records.find(r => r.ticket === rotated)
  expect(persisted.exp).toBe(30)
  expect(JSON.parse(persisted.args)).toEqual({ region: ['saved'] })
  expect(persisted.accessTime).toBeNull()
  await expect(editor.getByRole('spinbutton')).toHaveValue('60')
  await expect(editor.locator('.args-line input').nth(1)).toHaveValue('["draft"]')
  expect(await settings.getByRole('checkbox', { name: '必选' }).isChecked()).toBe(requiredBefore)
  const visitorContext = await browser.newContext({ locale: 'zh-CN' })
  try {
    const origin = new URL(page.url()).origin
    for (const [ticket, valid] of [[initial, false], [rotated, true]]) {
      const visitor = await visitorContext.newPage()
      const validation = visitor.waitForResponse(r => r.url().includes('/share/proxyInfo'))
      await visitor.goto(`${origin}/#/de-link/${sent.uuid}?ticket=${ticket}`)
      const checked = await validation
      expect(checked.request().postDataJSON().ticket).toBe(ticket)
      const data = (await checked.json()).data
      expect(data.ticketValidVO.ticketValid).toBe(valid)
      if (valid) expect(JSON.parse(data.ticketValidVO.args)).toEqual({ region: ['saved'] })
      await visitor.close()
    }
  } finally {
    await visitorContext.close()
  }
  await editor.getByRole('button', { name: '取消', exact: true }).click()
  row = page.locator('.ed-table__body .ed-table__row').filter({ hasText: rotated })
  await row.getByRole('button').nth(2).click()
  await expect(editor.getByRole('spinbutton')).toHaveValue('30')
  await expect(editor.locator('.args-line input').nth(1)).toHaveValue('["saved"]')
  await editor.getByRole('spinbutton').fill('0')
  await editor.locator('.args-line input').nth(1).fill('["committed"]')
  const savedReload = page.waitForResponse(r => r.url().includes('/ticket/pager/'))
  await editor.getByRole('button', { name: '保存', exact: true }).click()
  const savedRecords = (await (await savedReload).json()).data.records
  expect(savedRecords.find(r => r.ticket === rotated).exp).toBe(0)
  const listRotation = page.waitForResponse(r => r.url().includes('/ticket/saveTicket'))
  await row.getByRole('button').nth(1).click()
  const listTicket = (await (await listRotation).json()).data
  const listRow = page.locator('.ed-table__body .ed-table__row').filter({ hasText: listTicket })
  await listRow.getByRole('button').nth(2).click()
  await expect(editor.getByRole('spinbutton')).toHaveValue('0')
  await expect(editor.locator('.args-line input').nth(1)).toHaveValue('["committed"]')
  await editor.getByRole('button', { name: '取消', exact: true }).click()
})
